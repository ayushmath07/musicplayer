import java.sql.*;
import java.nio.file.*;
import java.util.*;
public class sqlconnection1 {
    public static void main(String[] args) {
        try {
            String URL = "jdbc:mysql://localhost:3306/MusicPlayer";
            String USER = "root";
            String PASSWORD = "1234";
            String sql = new String(Files.readAllBytes(Paths.get("C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\sql\\musicplayer.sql")));

            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = con.createStatement();
            try{
        stmt.execute("create database MusicPlayer;");}
        catch(Exception e){}
            //if database exists it wont give an error :/
            
                
            


            //Running code line by line to create the database
            String[] statements = sql.split(";");

            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    try{
                    stmt.execute(statement);}
                    catch(Exception e){}
                }
            }
            System.out.println("Database initialized successfully.");

            {// printing all songs
            List<String> songs = getallsongs(con);
            System.out.println("Songs are:");
            for(String song:songs){
                System.out.println(song);}
            }
            //TO DELETE DATABASE "FOR TESTING"
            // stmt.execute("drop database Musicplayer;");
            // System.out.println("Deleted database");

            con.close();

        } catch (Exception e) {
            e.printStackTrace(); 
            System.out.println("Failed to initialize the database.");
        }
        
    }
public static List<String> getallsongs(Connection con){
    List<String> songs= new ArrayList<>();
    try{
        String query = "Select title from Songs";
        Statement stmt = con.createStatement();
        //executing and storing the result in a result set
        ResultSet r = stmt.executeQuery(query);
        while(r.next()){
            songs.add(r.getString("title"));
        }
        
    }catch(Exception e){
        System.out.println("Not able to display all songs due to some error :<");
    }
    return songs;
}}

