package markov;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class SQLiteTest {

    private Connection connection;
    private String transitionTable;
    private String initialTable;
    private String addTransitionStatement;
    private String addInitialStatement;
    private static final String ID =  "1";


    public SQLiteTest(Connection connection) {
        this.connection = connection;
        this.transitionTable = getTableName("transitions");
        this.initialTable= getTableName("initial");
        this.addTransitionStatement = "INSERT INTO " + transitionTable + " VALUES (?, ?, ?, 1) \n" +
                                      "ON CONFLICT(prev1, prev2, next) \n" +
                                      "DO UPDATE SET count = count + 1;";

        this.addInitialStatement = "INSERT INTO " + initialTable + " VALUES (?, ?, 1) \n" +
                                   "ON CONFLICT(word1, word2) \n" +
                                   "DO UPDATE SET count = count + 1;";

        createInitialTable(connection);
        createTransitionsTable(connection);
    }

    private String simulate(int length, Deque<String> initial) {
        boolean capitalizeNext = true;
        if (length > 2) {
            StringBuilder sb = new StringBuilder(MarkovUtils.keyToStringHelper(this, initial, length));
            String next = "";

            while (length - curr.getOrder() > 0
                    || (length - curr.getOrder() <= 0 && !MarkovUtils.isEndPunct(next))) {
                MarkovMatrixRow<String> nextRow = getTransitionMatrix().get(curr);
                /* ends string construction upon reaching an end state or loop */
                if (nextRow == null) {
                    break;
                }
                if (length < -20 && !MarkovUtils.isEndPunct(next)) {
                    sb.append(".");
                    break;
                }

                next = nextRow.nextKey();
                sb.append(MarkovUtils.grammarHelper(this, next));
                curr = MarkovUtils.nextKeyHelper(curr, next);
                length--;
            }

            return sb.toString();
        } else {
            throw new IllegalArgumentException("Length too small!");
        }
    }

    public void readFile(String fileLocation) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(fileLocation)), StandardCharsets.UTF_8);
            readString(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readString(String string) {
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
                int i = 0;
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
                    i++;
                }
                System.out.println(i);
                connection.commit();
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

    public void addInitial(Deque<String> words, PreparedStatement stmt) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addTransition(Deque<String> words, String next, PreparedStatement stmt) {
        try {
            stmt.setString(1, words.getFirst());
            stmt.setString(2, words.getLast());
            stmt.setString(3, next);
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void commit() {
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
        return name + "_" + ID;
    }


}
