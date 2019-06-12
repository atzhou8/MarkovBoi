package markov;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class MarkovChain {
    private String transitionTable;
    private String initialTable;
    private String addTransitionStatement;
    private String addInitialStatement;
    private String selectTransitionStatement1;
    private String selectTransitionStatement2;
    private String sumTransitionStatement1;
    private String sumTransitionStatement2;
    private String id;

    private Deque<String> lastReadState = null;

    private PreparedStatement addInitial = null;
    private PreparedStatement addTransition = null;

    private boolean capitalizeNext;
    private Random random;

    private static final int GENERATION_LENGTH_MIN = 10;
    private static final int GENERATION_LENGTH_VARIANCE = 5;

    public MarkovChain(String id, Connection connection) {
        this.id = id;
        this.transitionTable = getTableName("transitions");
        this.initialTable= getTableName("initial");
        this.addTransitionStatement = "INSERT INTO " + transitionTable + " VALUES (?, ?, ?, ?) \n" +
                                      "ON CONFLICT(prev1, prev2, next) \n" +
                                      "DO UPDATE SET count = count + 1;";
        this.addInitialStatement = "INSERT INTO " + initialTable + " VALUES (?, ?, 6) \n" +
                                   "ON CONFLICT(word1, word2) \n" +
                                   "DO UPDATE SET count = count + 1;";

        this.selectTransitionStatement1 = "SELECT * FROM " + transitionTable + " \n" +
                                          "WHERE prev2 = ?;";
        this.sumTransitionStatement1 = "SELECT SUM(count) FROM " + transitionTable + " \n" +
                                       "WHERE prev2 = ?;";

        this.selectTransitionStatement2 = "SELECT * FROM " + transitionTable + " \n" +
                                          "WHERE prev1 = ? AND prev2 = ?;";
        this.sumTransitionStatement2 = "SELECT SUM(count) FROM " + transitionTable + " \n" +
                                       "WHERE prev1 = ? AND prev2 = ?;";

        this.capitalizeNext = true;
        this.random = new Random();

        if (connection != null) {
            createInitialTable(connection);
            createTransitionsTable(connection);
        }
    }

    /* Sets the connection that this MC uses to read. Call before readString() and readFile()*/
    public void setConnection(Connection connection) {
        try {
            this.addInitial = connection.prepareStatement(this.addInitialStatement);
            this.addTransition = connection.prepareStatement(this.addTransitionStatement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Reads text file and loads data into db using connection set in setConnection()*/
    public void readFile(String fileLocation) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Reads string and loads data into db using connection set in setConnection()*/
    public boolean readString(String string) {
        List<String> cleanedString = MarkovUtils.clean(string);
        if (cleanedString.size() <= 2
                || MarkovUtils.isPunct(Character.toString(cleanedString.get(0).charAt(0)))) {
            return false;
        } else {
            boolean isInitial = true;
            this.lastReadState = new ArrayDeque();
            this.lastReadState.addAll(cleanedString.subList(0, 2));
            String next;
            for (int pos = 0; pos < cleanedString.size() - 2; pos++) {
                /* Get's the next word */
                next = cleanedString.get(pos + 2);

                /* Add to initial dist if relevant */
                if (isInitial && !MarkovUtils.isPunct(this.lastReadState.getFirst())) {
                    addToInitialTable(this.lastReadState, this.addInitial);
                    addToTransitionTable(this.lastReadState, next, this.addTransition, 1);
                    isInitial = false;
                } else {
                    addToTransitionTable(this.lastReadState, next, this.addTransition, 1);
                }

                if (MarkovUtils.isEndPunct(this.lastReadState.getLast())) {
                    isInitial = true;
                }

                /* Updates state */
                this.lastReadState.add(next);
                this.lastReadState.removeFirst();
            }
            return true;
        }
    }

    /* Simulates a sentence by walking through the MC. Considers previous state and current state by default,
     * but also considers just the current state if there is not enough data. */
    public String simulate() {
        int length = randLength();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            PreparedStatement sum1 = connection.prepareStatement(this.sumTransitionStatement1);
            PreparedStatement select1 = connection.prepareStatement(this.selectTransitionStatement1);
            PreparedStatement sum2 = connection.prepareStatement(this.sumTransitionStatement2);
            PreparedStatement select2 = connection.prepareStatement(this.selectTransitionStatement2);
            String res = "";
            String next = "";

            Deque<String> curr = getStartState(connection);
            res += MarkovUtils.stateToStringHelper(this, curr);
            while (length - 2 > 0
                    || (length - 2 <= 0 && !MarkovUtils.isEndPunct(next))) {

                Deque<String> nextState = getNextState2(curr, select2, sum2);
                curr = (nextState == null) ? getNextState1(curr, select1, sum1) : nextState;

                if (curr == null & length > 0) {
                    curr = getStartState(connection);
                } else if (curr == null || length < -20 && !MarkovUtils.isEndPunct(curr.getLast())) {
                    res+= ".";
                    break;
                }
                next = curr.getLast();
                res += MarkovUtils.grammarHelper(this, next);
                length--;
            }
            connection.close();
            setCapitalizeNext(true);
            return res;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try{
                connection.close();
            } catch (SQLException e) {}
        }

        return null;
    }

    /* Simulates a sentence by walking through the MC, but chooses a starting state based on a given string */
    public String simulate(String s) {
        int length = randLength();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(MarkovUtils.URL);
            PreparedStatement sum1 = connection.prepareStatement(this.sumTransitionStatement1);
            PreparedStatement select1 = connection.prepareStatement(this.selectTransitionStatement1);
            PreparedStatement sum2 = connection.prepareStatement(this.sumTransitionStatement2);
            PreparedStatement select2 = connection.prepareStatement(this.selectTransitionStatement2);
            String res = "";
            String next = "";

            Deque<String> curr = getStartState(connection, s);
            res += MarkovUtils.stateToStringHelper(this, curr);
            while (length - 2 > 0
                    || (length - 2 <= 0 && !MarkovUtils.isEndPunct(next))) {
                Deque<String> nextState = getNextState2(curr, select2, sum2);
                curr = (nextState == null) ? getNextState1(curr, select1, sum1) : nextState;

                if (curr == null & length > 0) {
                    curr = getStartState(connection);
                } else if (curr == null || length < -20 && !MarkovUtils.isEndPunct(curr.getLast())) {
                    res+= ".";
                    break;
                }
                next = curr.getLast();
                res += MarkovUtils.grammarHelper(this, next);
                length--;
            }
            connection.close();
            setCapitalizeNext(true);
            return res;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try{
                connection.close();
            } catch (SQLException e) {}
        }

        return null;
    }

    /* Helper method to pick a random initial state */
    private Deque<String> getStartState(Connection connection) {
        try {
            /* queries database for valid initial states */
            String q1 = "SELECT * FROM " + initialTable + ";";
            String q2 = "SELECT SUM(count) FROM " + initialTable + ";";
            PreparedStatement select = connection.prepareStatement(q1);
            PreparedStatement sum = connection.prepareStatement(q2);

            /* walks through queried data and picks an entry with corresponding probability */
            int total = sum.executeQuery().getInt(1);
            if (total <= 0) {
                connection.close();
                throw new IllegalArgumentException("Not enough data for simulation. Please try something else!");
            } else {
                ResultSet rs = select.executeQuery();
                double seed = random.nextDouble();
                double cum = 0;
                Deque<String> result = new ArrayDeque<>();
                while (rs.next()) {
                    cum += (double) (rs.getInt("count")) / (double) total;
                    if (seed <= cum) {
                        result.addFirst(rs.getString("word1"));
                        result.addLast(rs.getString("word2"));
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    /* Helper method to pick a random initial state that matches a specified word*/
    private Deque<String> getStartState(Connection connection, String s) {
        try {
            /* queries database for valid initial states */
            String q1 = "SELECT * FROM " + transitionTable + " \n" +
                    "WHERE prev1 = ?;";
            String q2 = "SELECT SUM(count) FROM " + transitionTable + " \n" +
                    "WHERE prev1 = ?";
            PreparedStatement select = connection.prepareStatement(q1);
            PreparedStatement sum = connection.prepareStatement(q2);
            select.setString(1, s);
            sum.setString(1, s);

            /* walks through queried data and picks an entry with corresponding probability */
            int total = sum.executeQuery().getInt(1);
            if (total <= 0) {
                connection.close();
                throw new IllegalArgumentException("Not enough data for starting string. Please try something else!");
            } else {
                ResultSet rs = select.executeQuery();
                double seed = random.nextDouble();
                double cum = 0;
                Deque<String> result = new ArrayDeque<>();
                while (rs.next()) {
                    cum += (double) (rs.getInt("count")) / (double)total;
                    if (seed <= cum) {
                        result.addFirst(rs.getString("prev1"));
                        result.addLast(rs.getString("prev2"));
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        throw new IllegalArgumentException("Error occured for starting string. Please try something else!");
    }

    /* Stochastically determines the next state given the current and previous state*/
    private Deque<String> getNextState2(Deque<String> current, PreparedStatement selectNext, PreparedStatement sumNext) {
        try {
            /* queries database for valid initial states */
            selectNext.setString(1, current.getFirst());
            selectNext.setString(2, current.getLast());
            sumNext.setString(1, current.getFirst());
            sumNext.setString(2, current.getLast());

            /* walks through queried data and picks an entry with corresponding probability */
            int total = sumNext.executeQuery().getInt(1);
            if (total <= 0) {
                return null;
            } else {
                ResultSet rs = selectNext.executeQuery();
                double seed = random.nextDouble();
                double cum = 0;
                Deque<String> result = new ArrayDeque<>();
                while (rs.next()) {
                    cum += (double) (rs.getInt("count")) / (double) total;
                    if (seed <= cum) {
                        result.addFirst(current.getLast());
                        result.addLast(rs.getString("next"));
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        throw new IllegalArgumentException("Error occured for starting string. Please try something else!");
    }

    /* Stochastically determines the next state given just the current state*/
    private Deque<String> getNextState1(Deque<String> current, PreparedStatement selectNext, PreparedStatement sumNext) {
        try {
            /* queries database for valid initial states */
            selectNext.setString(1, current.getLast());
            sumNext.setString(1, current.getLast());

            /* walks through queried data and picks an entry with corresponding probability */
            int total = sumNext.executeQuery().getInt(1);
            if (total <= 0) {
                return null;
            } else {
                ResultSet rs = selectNext.executeQuery();
                double seed = random.nextDouble();
                double cum = 0;
                Deque<String> result = new ArrayDeque<>();
                while (rs.next()) {
                    cum += (double) (rs.getInt("count")) / (double)total;
                    if (seed <= cum) {
                        result.addFirst(current.getLast());
                        result.addLast(rs.getString("next"));
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        throw new IllegalArgumentException("Error occured for starting string. Please try something else!");
    }

    /* Helper method to add a state to initial table */
    private void addToInitialTable(Deque<String> words, PreparedStatement stmt) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Helper method to add a state to transition table */
    private void addToTransitionTable(Deque<String> words, String next, PreparedStatement stmt, int count) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.setString(3, next);
            stmt.setInt(4, count);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Helper method to add a state to create initial table */
    private void createInitialTable(Connection connection) {
        String statement = "CREATE TABLE IF NOT EXISTS " + initialTable + " (\n " +
                "word1 TEXT NOT NULL, \n " +
                "word2 TEXT NOT NULL, \n " +
                "count INTEGER, \n" +
                "PRIMARY KEY (word1, word2) \n" +
                ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Helper method to add a state to create transition table */
    private void createTransitionsTable(Connection connection) {
        String statement = "CREATE TABLE IF NOT EXISTS " + transitionTable + " (\n " +
                "prev1 TEXT NOT NULL, \n " +
                "prev2 TEXT NOT NULL, \n " +
                "next TEXT NOT NULL, \n " +
                "count INTEGER DEFAULT 1, \n" +
                "PRIMARY KEY (prev1, prev2, next) \n" +
                ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(statement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Helper method to get table names from ID. Appends id to name */
    private String getTableName(String name) {
        return name + "_" + id;
    }

    /* Setter for a boolean that keeps track of whether or not next word is capitalized in a walk */
    public void setCapitalizeNext(boolean capitalizeNext) {
        this.capitalizeNext = capitalizeNext;
    }

    /* Getter for a boolean that keeps track of whether or not next word is capitalized in a walk */
    public boolean getCapitalizeNext() {
        return capitalizeNext;
    }

    /* Randomly gets a minimum length for a walk (may continue until we get to a ending punctuation mark) */
    private int randLength() {
        return random.nextInt(GENERATION_LENGTH_VARIANCE) + GENERATION_LENGTH_MIN;
    }
}
