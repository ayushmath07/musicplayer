import java.sql.*;
import java.nio.file.*;
public class sqlconnection {
    public static void main(String[] args) {
        try {
            String URL = "jdbc:mysql://localhost:3306/MusicPlayer";
            String USER = "root";
            String PASSWORD = "1234";
            String sql = new String(Files.readAllBytes(Paths.get("C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\sql\\musicplayer.sql")));

            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = con.createStatement();

            //if database exists it wont give an error :/
            try{
                stmt.execute("create database MusicPlayer;");
            }
            catch(Exception e){}


            //Running code line by line
            String[] statements = sql.split(";");

            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    stmt.execute(statement);
                }
            }
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace(); 
            System.out.println("Failed to initialize the database.");
        }
    }
}