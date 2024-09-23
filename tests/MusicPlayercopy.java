import java.sql.*;
import java.nio.file.*;
import java.io.File;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;


public class  MusicPlayercopy {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    private static final String DB_NAME = "MusicPlayer";

    public static void main(String[] args) {
        try {
            // Connect to MySQL without selecting a specific database
            try (Connection con = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                 Statement stmt = con.createStatement()) {

                // Create the database if it doesn't exist
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("Database created or already exists.");
            }

            // Now connect to the specific database
            String dbUrl = BASE_URL + DB_NAME;
            try (Connection con = DriverManager.getConnection(dbUrl, USER, PASSWORD);
                 Statement stmt = con.createStatement()) {

                // Execute the SQL file to create tables
                executeSqlFile(con);

                // Extract metadata and insert it into the database
                insertMetadata(con);

                // Display all songs from the database
                displaySongs(con);
                
                //TO DELETE DATABASE "FOR TESTING"
            stmt.execute("drop database Musicplayer;");
            System.out.println("Deleted database");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the database.");
        }
    }


    // Method to execute the SQL file and create tables
    private static void executeSqlFile(Connection con) throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get("C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\sql\\musicplayer.sql")));
        try (Statement stmt = con.createStatement()) {
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    try {
                        stmt.execute(statement.trim());
                    } catch (Exception e) {
                        // Catch errors while running SQL
                        System.out.println("Error in SQL execution: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("SQL file executed successfully.");
    }

    // Method to extract metadata using Jaudiotagger and insert into the Songs table
    private static void insertMetadata(Connection con) throws SQLException {
        File musicFolder = new File("C:\\Users\\AYUSHMAN MATH\\Desktop\\songs"); // Update path to your songs folder
        File[] songFiles = musicFolder.listFiles();

        if (songFiles != null) {
            for (File song : songFiles) {
                if (song.isFile()) {
                    try {
                        // Extract metadata using Jaudiotagger
                        AudioFile audioFile = AudioFileIO.read(song);
                        Tag tag = audioFile.getTag();
                        String title = tag.getFirst(FieldKey.TITLE);
                        String artist = tag.getFirst(FieldKey.ARTIST);
                        String genre = tag.getFirst(FieldKey.GENRE);
                        String duration = audioFile.getAudioHeader().getTrackLength() / 60 + ":"
                                + audioFile.getAudioHeader().getTrackLength() % 60;
                        String releaseyear = tag.getFirst(FieldKey.YEAR);

                        // Insert metadata into the Songs table
                        String insertSQL = "INSERT INTO Songs (title, artist, genre, release_year, duration, song_file_path) "
                                + "VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement ps = con.prepareStatement(insertSQL);
                        ps.setString(1, title.isEmpty() ? "Unknown Title" : title);
                        ps.setString(2, artist.isEmpty() ? "Unknown Artist" : artist);
                        ps.setString(3, genre.isEmpty() ? "Unknown Genre" : genre);
                        ps.setString(4, releaseyear);
                        ps.setString(5, duration);
                        ps.setString(6, song.getAbsolutePath());
                        ps.executeUpdate();
                    } catch (Exception e) {
                        System.out.println("Error processing song: " + song.getName() + " -> " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Metadata inserted into the database.");
    }

    // Method to display all songs from the database
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
