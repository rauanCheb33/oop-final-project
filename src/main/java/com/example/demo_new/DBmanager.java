package com.example.demo_new;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class DBmanager {
    private static String URL;
    private static String USER;
    private static String PASS;

    static {
        Map<String, String> env = loadEnv(".env");
        URL = env.get("DB_URL");
        USER = env.get("DB_USER");
        PASS = env.get("DB_PASS");
    }

    private static Map<String, String> loadEnv(String filePath) {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) env.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            System.err.println("Error: .env file not found in project root!");
        }
        return env;
    }

    private Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASS == null) {
            throw new IllegalStateException("DB env vars are missing. Check .env (DB_URL, DB_USER, DB_PASS).");
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Creates required tables if missing. Call once on startup. */
    public static void setupDatabase() {
        DBmanager db = new DBmanager();
        String moviesSql = "CREATE TABLE IF NOT EXISTS movies ("+
                "id SERIAL PRIMARY KEY,"+
                "title VARCHAR(255) NOT NULL,"+
                "description VARCHAR(255),"+
                "duration INT NOT NULL,"+
                "age_restriction INT NOT NULL,"+
                "price DOUBLE PRECISION NOT NULL"+
           " );";

        String viewersSql = "CREATE TABLE IF NOT EXISTS viewers ("+
                "id SERIAL PRIMARY KEY,"+
                "full_name VARCHAR(255) NOT NULL,"+
                "age INT NOT NULL,"+
                "email VARCHAR(255)"+
            ");";

        String cinemasSql = "CREATE TABLE IF NOT EXISTS cinemas ("+
                "id SERIAL PRIMARY KEY,"+
                "name VARCHAR(255) NOT NULL,"+
                "city VARCHAR(255),"+
                "address VARCHAR(255)"+
            ");";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(moviesSql);
            stmt.execute(viewersSql);
            stmt.execute(cinemasSql);
            System.out.println("[DB] Connection established. Tables verified.");
        } catch (SQLException e) {
            throw new RuntimeException("DB setup failed", e);
        }
    }

    // -------------------- MOVIES --------------------

    public Movie createMovie(MovieCreateRequest req) {
        String sql = "INSERT INTO movies (title, description, duration, age_restriction, price) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getTitle());
            ps.setString(2, req.getDescription());
            ps.setInt(3, req.getDurationMinutes());
            ps.setInt(4, req.getAgeRestriction());
            ps.setDouble(5, req.getTicketPrice());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                return new Movie(id, req.getTitle(), req.getDescription(), req.getDurationMinutes(), req.getAgeRestriction(), req.getTicketPrice());
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB createMovie failed", e);
        }
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getInt("age_restriction"),
                        rs.getDouble("price")
                ));
            }
            return movies;
        } catch (SQLException e) {
            throw new RuntimeException("DB getAllMovies failed", e);
        }
    }

    public Optional<Movie> findMovieById(int id) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getInt("age_restriction"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB findMovieById failed", e);
        }
    }

    public boolean updateMovie(int id, MovieUpdateRequest req) {
        String sql = "UPDATE movies SET title=?, description=?, duration=?, age_restriction=?, price=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getTitle());
            ps.setString(2, req.getDescription());
            ps.setInt(3, req.getDurationMinutes());
            ps.setInt(4, req.getAgeRestriction());
            ps.setDouble(5, req.getTicketPrice());
            ps.setInt(6, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB updateMovie failed", e);
        }
    }

    public boolean deleteMovie(int id) {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB deleteMovie failed", e);
        }
    }

    // -------------------- VIEWERS --------------------

    public Viewer createViewer(ViewerCreateRequest req) {
        String sql = "INSERT INTO viewers (full_name, age, email) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getFullName());
            ps.setInt(2, req.getAge());
            ps.setString(3, req.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                return new Viewer(id, req.getFullName(), req.getAge(), req.getEmail());
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB createViewer failed", e);
        }
    }

    public List<Viewer> getAllViewers() {
        List<Viewer> viewers = new ArrayList<>();
        String sql = "SELECT * FROM viewers ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                viewers.add(new Viewer(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getInt("age"),
                        rs.getString("email")
                ));
            }
            return viewers;
        } catch (SQLException e) {
            throw new RuntimeException("DB getAllViewers failed", e);
        }
    }

    public Optional<Viewer> findViewerById(int id) {
        String sql = "SELECT * FROM viewers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Viewer(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getInt("age"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB findViewerById failed", e);
        }
    }

    public boolean updateViewer(int id, ViewerUpdateRequest req) {
        String sql = "UPDATE viewers SET full_name=?, age=?, email=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getFullName());
            ps.setInt(2, req.getAge());
            ps.setString(3, req.getEmail());
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB updateViewer failed", e);
        }
    }

    public boolean deleteViewer(int id) {
        String sql = "DELETE FROM viewers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB deleteViewer failed", e);
        }
    }

    // -------------------- CINEMAS --------------------

    public Cinema createCinema(CinemaCreateRequest req) {
        String sql = "INSERT INTO cinemas (name, city, address) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setString(2, req.getCity());
            ps.setString(3, req.getAddress());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                return new Cinema(id, req.getName(), req.getCity(), req.getAddress());
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB createCinema failed", e);
        }
    }

    public List<Cinema> getAllCinemas() {
        List<Cinema> cinemas = new ArrayList<>();
        String sql = "SELECT * FROM cinemas ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cinemas.add(new Cinema(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("address")
                ));
            }
            return cinemas;
        } catch (SQLException e) {
            throw new RuntimeException("DB getAllCinemas failed", e);
        }
    }

    public Optional<Cinema> findCinemaById(int id) {
        String sql = "SELECT * FROM cinemas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Cinema(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB findCinemaById failed", e);
        }
    }

    public boolean updateCinema(int id, CinemaUpdateRequest req) {
        String sql = "UPDATE cinemas SET name=?, city=?, address=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setString(2, req.getCity());
            ps.setString(3, req.getAddress());
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB updateCinema failed", e);
        }
    }

    public boolean deleteCinema(int id) {
        String sql = "DELETE FROM cinemas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB deleteCinema failed", e);
        }
    }
}
