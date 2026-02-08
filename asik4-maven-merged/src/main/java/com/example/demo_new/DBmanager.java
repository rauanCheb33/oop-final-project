package com.example.demo_new;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Simple JDBC layer (no Spring Data) for the endterm defence requirements:
 * - request/response JSON (controllers)
 * - data from DB via JDBC
 */
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

        String moviesSql = """
            CREATE TABLE IF NOT EXISTS movies (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description VARCHAR(255),
                duration INT NOT NULL,
                age_restriction INT NOT NULL,
                price DOUBLE PRECISION NOT NULL
            );
        """;

        String viewersSql = """
            CREATE TABLE IF NOT EXISTS viewers (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                age INT NOT NULL,
                balance DOUBLE PRECISION NOT NULL
            );
        """;

        String cinemasSql = """
            CREATE TABLE IF NOT EXISTS cinemas (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL
            );
        """;

        String cinemaMoviesSql = """
            CREATE TABLE IF NOT EXISTS cinema_movies (
                cinema_id INT NOT NULL REFERENCES cinemas(id) ON DELETE CASCADE,
                movie_id INT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
                seats INT NOT NULL,
                PRIMARY KEY (cinema_id, movie_id)
            );
        """;

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(moviesSql);
            stmt.execute(viewersSql);
            stmt.execute(cinemasSql);
            stmt.execute(cinemaMoviesSql);
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
                movies.add(mapRowToMovie(rs));
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
                return Optional.of(mapRowToMovie(rs));
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
        String sql = "INSERT INTO viewers (name, age, balance) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setInt(2, req.getAge());
            ps.setDouble(3, req.getBalance());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                return new Viewer(id, req.getName(), req.getAge(), req.getBalance());
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
                viewers.add(mapRowToViewer(rs));
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
                return Optional.of(mapRowToViewer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB findViewerById failed", e);
        }
    }

    public boolean updateViewer(int id, ViewerUpdateRequest req) {
        String sql = "UPDATE viewers SET name=?, age=?, balance=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setInt(2, req.getAge());
            ps.setDouble(3, req.getBalance());
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
        String sql = "INSERT INTO cinemas (name) VALUES (?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                return new Cinema(id, req.getName());
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
                        rs.getString("name")
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
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB findCinemaById failed", e);
        }
    }

    public boolean updateCinema(int id, CinemaUpdateRequest req) {
        String sql = "UPDATE cinemas SET name=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setInt(2, id);
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

    // -------------------- SCHEDULE / BOOKING (extra) --------------------

    /** Add or replace seat count for a movie in a cinema schedule. */
    public void upsertScheduleItem(int cinemaId, int movieId, int seats) {
        // Ensure referenced rows exist (for nicer 404s in controllers)
        if (findCinemaById(cinemaId).isEmpty()) throw new NotFoundException("Cinema", cinemaId);
        if (findMovieById(movieId).isEmpty()) throw new NotFoundException("Movie", movieId);

        String sql = "INSERT INTO cinema_movies (cinema_id, movie_id, seats) VALUES (?, ?, ?) " +
                "ON CONFLICT (cinema_id, movie_id) DO UPDATE SET seats = EXCLUDED.seats";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cinemaId);
            ps.setInt(2, movieId);
            ps.setInt(3, seats);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB upsertScheduleItem failed", e);
        }
    }

    public boolean deleteScheduleItem(int cinemaId, int movieId) {
        String sql = "DELETE FROM cinema_movies WHERE cinema_id = ? AND movie_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cinemaId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB deleteScheduleItem failed", e);
        }
    }

    public List<ScheduleItem> getCinemaSchedule(int cinemaId) {
        if (findCinemaById(cinemaId).isEmpty()) throw new NotFoundException("Cinema", cinemaId);

        String sql = "SELECT m.*, cm.seats FROM cinema_movies cm " +
                "JOIN movies m ON m.id = cm.movie_id " +
                "WHERE cm.cinema_id = ? ORDER BY m.id";

        List<ScheduleItem> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movie movie = mapRowToMovie(rs);
                    int seats = rs.getInt("seats");
                    items.add(new ScheduleItem(movie, seats));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("DB getCinemaSchedule failed", e);
        }
    }

    /**
     * Books tickets using your OOP logic (Cinema.bookTickets + Viewer.pay), then persists changes.
     * Returns a result that controllers can serialize into JSON.
     */
    public BookingResult bookTickets(int cinemaId, int viewerId, int movieId, int quantity) {
        if (quantity <= 0) return BookingResult.failure("quantity must be positive");

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Lock rows to avoid two bookings at the same time overwriting each other (basic).
            Viewer viewer = selectViewerForUpdate(conn, viewerId)
                    .orElseThrow(() -> new NotFoundException("Viewer", viewerId));

            Movie movie = selectMovie(conn, movieId)
                    .orElseThrow(() -> new NotFoundException("Movie", movieId));

            Cinema cinema = selectCinema(conn, cinemaId)
                    .orElseThrow(() -> new NotFoundException("Cinema", cinemaId));

            Integer seats = selectSeatsForUpdate(conn, cinemaId, movieId);
            if (seats == null) {
                conn.rollback();
                return BookingResult.failure("Booking failed: movie is not available in this cinema");
            }

            cinema.addMovie(movie, seats);

            String message = cinema.bookTickets(viewer, movie, quantity);
            boolean success = message.startsWith("Booking success");

            if (!success) {
                conn.rollback();
                return BookingResult.failure(message);
            }

            int remainingSeats = cinema.getAvailableSeats(movie);
            double newBalance = viewer.getBalance();

            // persist changes
            updateViewerBalance(conn, viewerId, newBalance);
            updateSeats(conn, cinemaId, movieId, remainingSeats);

            conn.commit();
            return BookingResult.success(message, remainingSeats, newBalance);

        } catch (SQLException e) {
            throw new RuntimeException("DB bookTickets failed", e);
        }
    }

    // -------------------- helpers --------------------

    private static Movie mapRowToMovie(ResultSet rs) throws SQLException {
        return new Movie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("duration"),
                rs.getInt("age_restriction"),
                rs.getDouble("price")
        );
    }

    private static Viewer mapRowToViewer(ResultSet rs) throws SQLException {
        return new Viewer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getDouble("balance")
        );
    }

    private Optional<Viewer> selectViewerForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM viewers WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRowToViewer(rs));
            }
        }
    }

    private Optional<Movie> selectMovie(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRowToMovie(rs));
            }
        }
    }

    private Optional<Cinema> selectCinema(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM cinemas WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Cinema(rs.getInt("id"), rs.getString("name")));
            }
        }
    }

    private Integer selectSeatsForUpdate(Connection conn, int cinemaId, int movieId) throws SQLException {
        String sql = "SELECT seats FROM cinema_movies WHERE cinema_id = ? AND movie_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cinemaId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getInt("seats");
            }
        }
    }

    private void updateViewerBalance(Connection conn, int viewerId, double newBalance) throws SQLException {
        String sql = "UPDATE viewers SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, viewerId);
            ps.executeUpdate();
        }
    }

    private void updateSeats(Connection conn, int cinemaId, int movieId, int seats) throws SQLException {
        String sql = "UPDATE cinema_movies SET seats = ? WHERE cinema_id = ? AND movie_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seats);
            ps.setInt(2, cinemaId);
            ps.setInt(3, movieId);
            ps.executeUpdate();
        }
    }

    // small DTOs used by controllers
    public static class ScheduleItem {
        private Movie movie;
        private int seats;

        public ScheduleItem() {}

        public ScheduleItem(Movie movie, int seats) {
            this.movie = movie;
            this.seats = seats;
        }

        public Movie getMovie() { return movie; }
        public void setMovie(Movie movie) { this.movie = movie; }

        public int getSeats() { return seats; }
        public void setSeats(int seats) { this.seats = seats; }
    }

    public static class BookingResult {
        private boolean success;
        private String message;
        private Integer remainingSeats;
        private Double viewerBalance;

        public BookingResult() {}

        public static BookingResult success(String message, int remainingSeats, double viewerBalance) {
            BookingResult r = new BookingResult();
            r.success = true;
            r.message = message;
            r.remainingSeats = remainingSeats;
            r.viewerBalance = viewerBalance;
            return r;
        }

        public static BookingResult failure(String message) {
            BookingResult r = new BookingResult();
            r.success = false;
            r.message = message;
            return r;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Integer getRemainingSeats() { return remainingSeats; }
        public void setRemainingSeats(Integer remainingSeats) { this.remainingSeats = remainingSeats; }

        public Double getViewerBalance() { return viewerBalance; }
        public void setViewerBalance(Double viewerBalance) { this.viewerBalance = viewerBalance; }
    }
}
