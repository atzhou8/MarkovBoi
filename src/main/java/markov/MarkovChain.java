package markov;


import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.TrustAnchor;
import java.sql.*;
import java.util.*;

public class MarkovChain {
    private String transitionTable;
    private String initialTable;
    private String addTransitionStatement;
    private String addInitialStatement;
    private String selectTransitionStatement;
    private String sumTransitionStatement;
    private String id;

    private boolean capitalizeNext;
    private Random random;

    public static final String URL = "jdbc:sqlite:data/data.db";

    public MarkovChain(String id) {
        this.id = id;
        this.transitionTable = getTableName("transitions");
        this.initialTable= getTableName("initial");
        this.addTransitionStatement = "INSERT INTO " + transitionTable + " VALUES (?, ?, ?, 1) \n" +
                                      "ON CONFLICT(prev1, prev2, next) \n" +
                                      "DO UPDATE SET count = count + 1;";
        this.addInitialStatement = "INSERT INTO " + initialTable + " VALUES (?, ?, 1) \n" +
                                   "ON CONFLICT(word1, word2) \n" +
                                   "DO UPDATE SET count = count + 1;";
        this.selectTransitionStatement = "SELECT * FROM " + transitionTable + " \n" +
                                         "WHERE prev1 = ? AND prev2 = ?;";
        this.sumTransitionStatement= "SELECT SUM(count) FROM " + transitionTable + " \n" +
                                     "WHERE prev1 = ? AND prev2 = ?;";
        this.capitalizeNext = true;
        this.random = new Random();

        try {
            Connection connection = DriverManager.getConnection(URL);
            if (connection != null) {
                createInitialTable(connection);
                createTransitionsTable(connection);
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String simulate(String s, int length) {
        try {
            Connection connection = DriverManager.getConnection(URL);
            PreparedStatement select = connection.prepareStatement(this.selectTransitionStatement);
            PreparedStatement sum = connection.prepareStatement(this.sumTransitionStatement);
            String res = "";
            String next = "";

            Deque<String> curr = getStartFromString(s, connection);
            res += MarkovUtils.stateToStringHelper(this, curr);
            while (length - 2 > 0
                    || (length - 2 <= 0 && !MarkovUtils.isEndPunct(next))) {

                curr = getNextState(curr, connection, select, sum);

                if (curr == null || length < -20 && !MarkovUtils.isEndPunct(curr.getLast())) {
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
        }

        return null;
    }


    private Deque<String> getNextState(Deque<String> current, Connection connection,
                                       PreparedStatement selectNext, PreparedStatement sumNext) {
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

    /* Helper method to find an initial state given a starting word*/
    private Deque<String> getStartFromString(String s, Connection connection) {
        try {
            /* queries database for valid initial states */
            s = s.toLowerCase();
            String q1 = "SELECT * FROM " + initialTable + " \n" +
                            "WHERE word1 = ?;";
            String q2 = "SELECT SUM(count) FROM " + initialTable + " \n" +
                            "WHERE word1 = ?";
            PreparedStatement select = connection.prepareStatement(q1);
            PreparedStatement sum = connection.prepareStatement(q2);
            select.setString(1, s);
            sum.setString(1, s);

            /* walks through queried data and picks an entry with corresponding probability */
            int total = sum.executeQuery().getInt(1);
            if (total <= 0) {
                throw new IllegalArgumentException("Not enough data for starting string. Please try something else!");
            } else {
                ResultSet rs = select.executeQuery();
                double seed = random.nextDouble();
                double cum = 0;
                Deque<String> result = new ArrayDeque<>();
                while (rs.next()) {
                    cum += (double) (rs.getInt("count")) / (double)total;
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

        throw new IllegalArgumentException("Error occured for starting string. Please try something else!");
    }

    public void readFile(String fileLocation, Connection connection) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text, connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readString(String string, Connection connection) {
        List<String> cleanedString = MarkovUtils.clean(string);
        if (cleanedString.size() <= 2
                || MarkovUtils.isPunct(Character.toString(cleanedString.get(0).charAt(0)))) {
            return;
        } else {
            boolean isInitial = true;
            Deque<String> curr = new ArrayDeque();
            curr.addAll(cleanedString.subList(0, 2));
            String next;

            try {
                PreparedStatement preparedInitial = connection.prepareStatement(addInitialStatement);
                PreparedStatement preparedTransition = connection.prepareStatement(addTransitionStatement);


                for (int pos = 0; pos < cleanedString.size() - 2; pos++) {
                    next = cleanedString.get(pos + 2);

                    if (isInitial && !MarkovUtils.isPunct(curr.getFirst())) {
                        addInitial(curr, preparedInitial);
                        isInitial = false;
                    }
//
                    if (MarkovUtils.isEndPunct(curr.getLast())) {
                        isInitial = true;
                    }

                    addTransition(curr, next, preparedTransition);

                    curr.add(next);
                    curr.removeFirst();
                }
            } catch (SQLException e) {
                try {
                    System.out.println("An error has occured. Rolling back database.");
                    connection.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error rolling back: " + ex.getMessage());
                }
                System.out.println(e.getMessage());
            }
        }
    }

    private void addInitial(Deque<String> words, PreparedStatement stmt) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addTransition(Deque<String> words, String next, PreparedStatement stmt) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.setString(3, next);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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

    private String getTableName(String name) {
        return name + "_" + id;
    }

    public void setCapitalizeNext(boolean capitalizeNext) {
        this.capitalizeNext = capitalizeNext;
    }

    public boolean getCapitalizeNext() {
        return capitalizeNext;
    }

}
