# Cinema Ticket Booking

Production-style fullstack coursework project for managing cinema entities with:
- Java 21 + Spring Boot (REST API)
- PostgreSQL + JDBC (no JPA/Hibernate)
- Custom modern frontend (vanilla JS, no Bootstrap)

The app is built around three domains:
- `Movie`
- `Viewer`
- `Cinema`

---

## Highlights

- Clean CRUD API with proper HTTP status codes
- Global JSON error handling (`400/404/500`)
- PostgreSQL schema auto-setup on app startup
- Structured DTO layer for create/update operations
- Frontend admin panel with:
  - sidebar navigation
  - searchable tables
  - modal create/edit forms
  - delete confirmation modal
  - toast notifications
  - loading skeleton states
  - responsive layout and reduced-motion support

---

## Tech Stack

### Backend
- Java `21`
- Spring Boot `4.0.2`
- Spring Web
- PostgreSQL JDBC driver

### Frontend
- HTML5
- CSS3 (custom design system)
- Vanilla JavaScript (component-style state management)

---

## Project Structure

```text
.
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/demo_new/
│  │  │  ├─ *Controller.java
│  │  │  ├─ *CreateRequest.java
│  │  │  ├─ *UpdateRequest.java
│  │  │  ├─ DBmanager.java
│  │  │  ├─ GlobalExceptionHandler.java
│  │  │  └─ DemoNewApplication.java
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ static/
│  │        ├─ index.html
│  │        ├─ styles.css
│  │        └─ app.js
│  └─ test/
├─ pom.xml
└─ .env.example
```

---

## Quick Start

## 1) Clone

```bash
git clone https://github.com/rauanCheb33/oop-final-project.git
cd oop-final-project
```

## 2) Configure environment

Create `.env` in the project root:

```env
DB_URL=jdbc:postgresql://localhost:5432/cinemaDB
DB_USER=postgres
DB_PASS=your_password
```

You can copy from template:

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

## 3) Run

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## 4) Open app

- Frontend: `http://localhost:8080/`
- API example: `http://localhost:8080/movies`

---

## API Endpoints

### Movies
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

### Viewers
- `GET /viewers`
- `GET /viewers/{id}`
- `POST /viewers`
- `PUT /viewers/{id}`
- `DELETE /viewers/{id}`

Example `POST /viewers`:

```json
{
  "fullName": "Aruzhan A.",
  "age": 16,
  "email": "aru@example.com"
}
```

### Cinemas
- `GET /cinemas`
- `GET /cinemas/{id}`
- `POST /cinemas`
- `PUT /cinemas/{id}`
- `DELETE /cinemas/{id}`

Example `POST /cinemas`:

```json
{
  "name": "Kinopark",
  "city": "Almaty",
  "address": "Abylai Khan 50"
}
```

---

## Error Format

All handled API errors return JSON:

```json
{
  "timestamp": "2026-02-12T22:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Movie with id=999 not found",
  "path": "/movies/999"
}
```

Implemented in `GlobalExceptionHandler` with:
- `NotFoundException -> 404`
- `IllegalArgumentException -> 400`
- other runtime exceptions -> `500`

---

## Build & Test

Compile:

```bash
./mvnw -DskipTests compile
```

Run tests:

```bash
./mvnw test
```

---

## Known Notes

- Database credentials are loaded from root `.env` by `DBmanager`.
- Tables are created automatically during startup (`DBmanager.setupDatabase()`).
- `target/` and `.env` are git-ignored.

---

## Roadmap Ideas

- Add authentication/authorization (admin roles)
- Add pagination and server-side filtering
- Add sorting and advanced search on backend
- Add CI workflow (build + tests)
- Add Docker compose for app + postgres

---

## Author

Rauan Cheb
