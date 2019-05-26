package markov;

import java.sql.*;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestSQLite {

    private static final String URL = "jdbc:sqlite:data/data.db";

    @Test
    public void testInsert() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                conn.setAutoCommit(false);


                SQLiteTest sqliteTest = new SQLiteTest(conn);
                sqliteTest.readFile("data/marx.txt");
                sqliteTest.commit();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

}
