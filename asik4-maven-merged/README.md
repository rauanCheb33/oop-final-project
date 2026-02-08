# Cinema Ticket Booking REST API (Endterm Defence)

REST API web-service for the subject area **Cinema Ticket Booking**:
- `Movie`
- `Viewer`
- `Cinema`

Data is stored in **PostgreSQL** and accessed via **JDBC** (no Spring Data / Hibernate).

## Quick start
1) Create a PostgreSQL database (example: `cinemaDB`)
2) Copy `.env.example` → `.env` and put your real values:
```
DB_URL=jdbc:postgresql://localhost:5432/cinemaDB
DB_USER=postgres
DB_PASS=your_password
```
3) Run:
```bash
./mvnw spring-boot:run
```

Tables are created automatically on startup (`DBmanager.setupDatabase()` is called from `DemoNewApplication`).

## Endpoints (JSON)

### Movies (CRUD)
- `GET /movies`
- `GET /movies/{id}`
- `POST /movies`
- `PUT /movies/{id}`
- `DELETE /movies/{id}`

Example `POST /movies`:
```json
{
  "title": "Interstellar",
  "description": "Sci-fi drama about space",
  "durationMinutes": 169,
  "ageRestriction": 12,
  "ticketPrice": 2500
}
```

### Viewers (CRUD)
- `GET /viewers`
- `GET /viewers/{id}`
- `POST /viewers`
- `PUT /viewers/{id}`
- `DELETE /viewers/{id}`

Example `POST /viewers`:
```json
{
  "name": "Aruzhan",
  "age": 16,
  "balance": 10000
}
```

### Cinemas (CRUD)
- `GET /cinemas`
- `GET /cinemas/{id}`
- `POST /cinemas`
- `PUT /cinemas/{id}`
- `DELETE /cinemas/{id}`

Example `POST /cinemas`:
```json
{
  "name": "Sary Arka"
}
```

## Schedule + booking (extra endpoints)
These endpoints reuse your OOP booking logic (`Cinema.bookTickets` + `Viewer.pay`) and persist changes via JDBC.

### Schedule
- `GET /cinemas/{cinemaId}/schedule` → list of `{ movie, seats }`
- `POST /cinemas/{cinemaId}/schedule` → add/update one movie in schedule
- `DELETE /cinemas/{cinemaId}/schedule/{movieId}` → remove one movie from schedule

Example `POST /cinemas/1/schedule`:
```json
{ "movieId": 5, "seats": 50 }
```

### Book tickets
- `POST /cinemas/{cinemaId}/book`

Example `POST /cinemas/1/book`:
```json
{ "viewerId": 2, "movieId": 5, "quantity": 2 }
```

Response example:
```json
{
  "success": true,
  "message": "Booking success: ...",
  "remainingSeats": 48,
  "viewerBalance": 5000.0
}
```

## Error handling
Errors are returned as JSON with correct HTTP status (400/404/500) via `GlobalExceptionHandler`.
