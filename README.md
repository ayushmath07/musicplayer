Project Title: Smart Music Player with Autocomplete and Personalized Recommendations

*Installation Steps for Smart Music Player*
1. Clone/Download the Project:
Clone the repository or download the source code to your local machine.
2. Setup MySQL Database:
Open MySQL Workbench or any MySQL client.
Navigate to the SQL folder in the project directory and find the SQL file that contains the schema for your music player.
Run the SQL file to create the necessary tables in your database. This includes tables such as Songs, Users, and Playlists.
3. Configure Database Connection:
In the Musicplayer.java file, you need to update the following variables:

static String DB_URL = "jdbc:mysql://localhost:3306/MusicPlayer";  // Database URL
static String DB_USER = "root";  // Replace with your MySQL username
static String DB_PASSWORD = "1234";  // Replace with your MySQL password

4. Configure File Paths:
You’ll also need to update the file paths for your songs and static files:

static String SONGS_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\songs";  // Path where your music files are stored
static String STATIC_DIRECTORY = "C:\\Users\\AYUSHMAN MATH\\Desktop\\musicplayer\\src\\db\\";  // Path for static files like index.html


Make sure these directories exist on your machine, or update the paths according to where your files are located.

5. Add JAR Dependencies:
MySQL JDBC Connector: Download from https://dev.mysql.com/downloads/connector/j/
Jaudiotagger: Download from https://bitbucket.org/ijabz/jaudiotagger/downloads/
Ensure both JAR files are added to your project’s classpath. IMP

6. Run the Application:
Open the project in your IDE (IntelliJ, Eclipse, etc.).
Make sure the MySQL service is running and the database is set up.
Run the Musicplayer class. This will:
Insert songs from the SONGS_DIRECTORY into the MySQL database.
Build the Trie for autocomplete.
Start the server on localhost:8000.
7. Access the Application:
Open a web browser and go to http://localhost:8000/ to interact with the music player.
Use the autocomplete search to find songs by title or artist, and stream them directly from the server.
8. Modify Static Files:
If you want to customize the frontend (like index.html), you can modify the files in the STATIC_DIRECTORY. These files are served through the StaticFileHandler in your code.

Overview: This project focuses on building a fully functional music player application using Java, JavaFX, and MySQL. The application will feature a powerful search engine with autocomplete functionality, efficient song management using non-linear data structures (NLDS), and a personalized recommendation system. It merges three key concepts: Object-Oriented Programming (OOP), Non-Linear Data Structures (NLDS), and Database Systems (DBS), making it a comprehensive software project that demonstrates both technical skill and practical application.

Project Features:

Autocomplete Search:
The search functionality is optimized using a Trie data structure to provide fast and dynamic autocomplete suggestions as users type song names, artists, or albums. The system can suggest results from a large music library stored in a database.

Playlist Management:
Users can create and manage playlists. Heap data structures are used to efficiently sort and track the most played or recently added songs, ensuring seamless organization of user preferences.

Personalized Song Recommendations:
The system leverages advanced algorithms to recommend songs based on user listening patterns, previously played tracks, and user-generated playlists. This recommendation feature is built using SQL queries that interact with the User_Preferences and Song_Recommendations tables in the database.

Database Integration:
MySQL is used to store and manage data such as users, songs, playlists, albums, and artist information. Efficient SQL queries are employed to fetch and update data based on user interactions. This ensures the music player is both scalable and reliable.

Seamless UI/UX with JavaFX:
The frontend of the music player is developed using JavaFX, providing a user-friendly and intuitive interface. Users can search, play, and organize songs easily with smooth transitions and animations.

OOP Principles:
The entire system is built using object-oriented principles such as encapsulation, inheritance, and polymorphism. Classes like Song, Playlist, and User represent key entities, each with specific attributes and methods that handle functionality like adding songs to playlists, tracking playback history, and managing user preferences.

Advanced Data Structures for Efficiency:
The Trie data structure is used to optimize the search feature, making the autocomplete fast and accurate, even with a large music library.
A Min-Heap or Max-Heap is used for keeping track of the most played songs, ensuring that user preferences are always up-to-date.

Music Playback:
The actual playback of songs is handled by integrating media libraries that support various formats. The music files themselves may be stored either locally or streamed from a server, with metadata such as song name, artist, and album stored in the MySQL database.

Technical Components:
Frontend: JavaFX will be used for building the user interface with features like dynamic search bars, playlist displays, and media controls (play, pause, skip, etc.).
Backend: Java is used to connect the application with the MySQL database, handle business logic, and implement core functionalities like search, song recommendations, and playlist management.
Database: MySQL stores user data, songs, playlists, and playback history. Queries are optimized for fast data retrieval and updates.
Data Structures: Tries for autocomplete and heaps for managing most-played songs ensure efficient data handling.

Application Use Cases:
Music Enthusiasts: A user-friendly platform for discovering, organizing, and playing music with smart recommendations.
Data-Driven Playlists: Automatically create playlists based on the user’s listening habits and preferences.
Advanced Search: The autocomplete feature saves users’ time by quickly suggesting matching songs, artists, or albums.
Interactive UI: An intuitive JavaFX-based interface enhances the overall user experience by providing seamless navigation and control.
Conclusion: The Smart Music Player is a robust application combining OOP principles, advanced data structures, and database management to provide a powerful and efficient platform for music lovers. The integration of autocomplete, personalized recommendations, and efficient song management demonstrates a deep understanding of software engineering principles while offering practical, user-centered functionality.
