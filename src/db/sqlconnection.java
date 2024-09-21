import java.sql.*;
import java.nio.file.*;

public class sqlconnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    //storing db name seperaterly
    private static final String DB_NAME = "MusicPlayer";

    public static void main(String[] args) {
        try {
            try (Connection con = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                 Statement stmt = con.createStatement()) {

                // Create the database if it doesn't exist
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("Database created or already exists.");
            }

            // Now, connect to the specific database
            String dbUrl = BASE_URL + DB_NAME;
            try (Connection con = DriverManager.getConnection(dbUrl, USER, PASSWORD);
                 Statement stmt = con.createStatement()) {

                // Execute the sql to create tables
                executeSqlFile(con);

                // display all songs
                displaySongs(con);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the database.");
        }
    }

    private static void executeSqlFile(Connection con) throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get("C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\sql\\musicplayer.sql")));
        try (Statement stmt = con.createStatement()) {
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    try{
                    stmt.execute(statement.trim());}
                    catch(Exception e){}
                }
            }
        }
        System.out.println("SQL file executed successfully.");
    }

    private static void displaySongs(Connection con) throws SQLException {
        String query = "SELECT title FROM Songs";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Songs in the database:");
            while (rs.next()) {
                System.out.println(rs.getString("title"));
            }
        }
    }
}