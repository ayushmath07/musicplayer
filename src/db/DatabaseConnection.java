package db;

import java.sql.*;
import java.io.*;
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/MusicPlayer";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to the database successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to connect to the database.");
            }
        }
        return connection;
    }

    // Optional: Run SQL file to set up the database
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader("sql/musicplayer.sql"))) {

            String line;
            StringBuilder sqlScript = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sqlScript.append(line).append("\n");
            }

            stmt.execute(sqlScript.toString());
            System.out.println("Database initialized successfully.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the database.");
        }
    }

    public static void main(String[] args) {
        // Initialize the database by running the SQL file
        initializeDatabase();
    }
}
