import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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

public class Musicplayer {

    static String DB_URL = "jdbc:mysql://localhost:3306/MusicPlayer";
    static String DB_USER = "root";
    static String DB_PASSWORD = "1234";
    static String SONGS_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\songs";
    static String STATIC_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\src\\db\\";

    public static void main(String[] args) throws Exception {
        insertSongsIntoDB();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/songs", new SongsHandler());
        server.createContext("/play", new SongStreamHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }

    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void insertSongsIntoDB() {
        File folder = new File(SONGS_DIRECTORY);
        File[] files = folder.listFiles(File::isFile);

        if (files != null) {
            for (File file : files) {
                try {
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();
                    String title = tag.getFirst(FieldKey.TITLE);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    String filePath = file.getName();

                    if (title.isEmpty()) {
                        title = file.getName();
                    }
                    if (artist.isEmpty()) {
                        artist = "unknown artist";
                    }

                    if (!songExistsInDB(title, artist, filePath)) {
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

    public static boolean songExistsInDB(String title, String artist, String filePath) {
        String query = "SELECT * FROM Songs WHERE title = ? AND artist = ? AND song_file_path = ?";
        try (Connection conn = getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, artist);
            pstmt.setString(3, filePath);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
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

    static class SongsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder response = new StringBuilder("<ul>");

                List<String[]> songs = fetchSongsFromDB();
                for (String[] song : songs) {
                    response.append("<li class='song-item'>")
                            .append("<span>").append(song[0]).append(" - ").append(song[1]).append("</span>")
                            .append("<button onclick=\"playSong('").append(song[2]).append("')\">Play</button>")
                            .append("</li>");
                }
                response.append("</ul>");

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.toString().getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        public static List<String[]> fetchSongsFromDB() {
            List<String[]> songs = new ArrayList<>();
            String query = "SELECT title, artist, song_file_path FROM Songs";
            try (Connection conn = getDBConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    String artist = rs.getString("artist");
                    String filePath = rs.getString("song_file_path");
                    songs.add(new String[]{title, artist, filePath});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return songs;
        }
    }

    static class SongStreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("=");
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