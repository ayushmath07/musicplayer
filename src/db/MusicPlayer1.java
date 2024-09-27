import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer1 {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/MusicPlayer";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // Song directory (location of mp3 files)
    private static final String SONGS_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\songs";
    private static final String STATIC_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\src\\db\\";  // Location of HTML, CSS, JS files

    public static void main(String[] args) throws Exception {
        // Automatically insert songs from the directory into the database
        insertSongsIntoDB();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new StaticFileHandler());  // Root context to serve static files
        server.createContext("/api/songs", new SongsHandler());
        server.createContext("/play", new SongStreamHandler());
        server.setExecutor(null);  // Default executor
        server.start();
        System.out.println("Server started on port 8000");
    }

    // Database connection method
    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Automatically scan songs directory and insert them into the database
public static void insertSongsIntoDB() {
    File folder = new File(SONGS_DIRECTORY);
    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

    if (files != null) {
        for (File file : files) {
            try {
                // Extract metadata using Jaudiotagger
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();
                String title = tag.getFirst(FieldKey.TITLE);
                String artist = tag.getFirst(FieldKey.ARTIST);
                String filePath = file.getName();

                if (title.isEmpty()) {
                    title = file.getName();  // Use the filename as the title if no metadata is found
                }
                if (artist.isEmpty()) {
                    artist = "Unknown Artist";
                }

                // Check if the song already exists in the database
                if (!songExistsInDB(title, artist, filePath)) {
                    // Insert the song data into the database
                    String query = "INSERT INTO Songs (title, artist, song_file_path) VALUES (?, ?, ?)";
                    try (Connection conn = getDBConnection();
                         PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setString(1, title);
                        pstmt.setString(2, artist);
                        pstmt.setString(3, filePath);
                        pstmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

// Method to check if a song already exists in the database
public static boolean songExistsInDB(String title, String artist, String filePath) {
    String query = "SELECT * FROM Songs WHERE title = ? AND artist = ? AND song_file_path = ?";
    try (Connection conn = getDBConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, title);
        pstmt.setString(2, artist);
        pstmt.setString(3, filePath);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();  // Return true if the song exists, false otherwise
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    // Static file handler to serve HTML, CSS, JS
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";  // Serve index.html by default
            }

            File file = new File(STATIC_DIRECTORY + path);
            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
        }
    }

    // Handler to fetch songs from the database
    static class SongsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<JSONObject> songs = fetchSongsFromDB();
                String response = new JSONArray(songs).toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        // Fetch songs from the database
        public static List<JSONObject> fetchSongsFromDB() {
            List<JSONObject> songs = new ArrayList<>();
            String query = "SELECT * FROM Songs";
            try (Connection conn = getDBConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    JSONObject song = new JSONObject();
                    song.put("id", rs.getInt("song_id"));
                    song.put("title", rs.getString("title"));
                    song.put("artist", rs.getString("artist"));
                    song.put("file", rs.getString("song_file_path"));
                    songs.add(song);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return songs;
        }
    }

    // Handler to stream the selected song file
    static class SongStreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("=");
            if (params.length != 2 || !params[0].equals("songFile")) {
                exchange.sendResponseHeaders(400, -1);  // Bad request if parameter is incorrect
                return;
            }

            String songFile = params[1];
            Path filePath = Paths.get(SONGS_DIRECTORY, songFile);

            if (Files.exists(filePath)) {
                exchange.getResponseHeaders().set("Content-Type", "audio/mpeg");
                exchange.sendResponseHeaders(200, Files.size(filePath));

                try (OutputStream os = exchange.getResponseBody();
                     InputStream is = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                String response = "Song not found";
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
        }
    }
}
