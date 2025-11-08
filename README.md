# Catalog API

Simple REST API to manage a bookstore catalog.

Main endpoints
- POST   /api/v1/books         — create a book (returns 201 Created + Location header)
- GET    /api/v1/books         — list books (supports pagination: page, size, sort)
- GET    /api/v1/books/{id}    — get a book by id
- PUT    /api/v1/books/{id}    — partial update of a book (PATCH-like behavior)
- DELETE /api/v1/books/{id}    — soft-delete (marks the book as deleted)

OpenAPI / Swagger UI
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Swagger UI:   http://localhost:8080/swagger-ui/index.html

Behavior summary
- POST /api/v1/books returns 201 Created and a Location header: /api/v1/books/{id}.
- PUT /api/v1/books/{id} behaves as a partial update: fields omitted from the request remain unchanged.
- `isbn` is validated (ISBN-10 or ISBN-13) and is required. `price.uah` is required; `price.eur` is calculated automatically using the current exchange rate.
- On ISBN conflict the API returns 409 Conflict.
- If a book is not found the API returns 404 Not Found using Problem Details (RFC 7807).
- DELETE is a soft delete: the book is marked deleted and excluded from list results.

Database
- The application uses an in-memory H2 database by default (recommended for tests and local runs).
- A file-based H2 configuration exists under the `dev` profile, but that profile is not active by default to avoid file-lock problems on Windows.

Run locally

PowerShell (recommended on Windows)

Option A — set environment variable (avoids escaping `;`):

```powershell
cd C:\Users\Alex\IdeaProjects\catalog
$env:SPRING_DATASOURCE_URL='jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
.\gradlew.bat bootRun --no-daemon --console=plain --info --stacktrace
```

Option B — pass application args via `--args`:

```powershell
cd C:\Users\Alex\IdeaProjects\catalog
.\gradlew.bat bootRun --no-daemon --console=plain --info --stacktrace --args='--spring.datasource.url=jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
```

CMD (cmd.exe):

```cmd
cd C:\Users\Alex\IdeaProjects\catalog
.\gradlew.bat -Dspring.datasource.url="jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" bootRun --no-daemon --console=plain --info --stacktrace
```

Run tests

```cmd
.\gradlew.bat clean test --no-daemon --stacktrace
```

Examples (curl)

1) Create book (POST) — returns 201 and Location

```bash
curl -i -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"1234567890123","title":"Integration Title","author":"Author","publicationYear":2021,"price":{"uah":200.00}}'
```

2) List books (pagination + sort)

```bash
curl -i "http://localhost:8080/api/v1/books?page=0&size=10&sort=title,asc"
```

3) Get book by id

```bash
curl -i http://localhost:8080/api/v1/books/1
```

4) Partial update (PUT used as partial update)

```bash
curl -i -X PUT http://localhost:8080/api/v1/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"New Title","price":{"uah":300.00}}'
```

5) Soft delete

```bash
curl -i -X DELETE http://localhost:8080/api/v1/books/1
```

Swagger / OpenAPI

- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Swagger UI:   http://localhost:8080/swagger-ui/index.html

Troubleshooting

- If `/v3/api-docs` returns HTTP 500, check the application log for stacktrace — the most common cause is a version mismatch between `springdoc` and Spring Framework (NoSuchMethodError referencing ControllerAdviceBean). The project currently uses `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0`.
- Avoid redirecting Gradle output into files inside the `build/` folder (for example `> build/log.txt`) on Windows because `clean` may fail due to file locks; prefer writing logs to the project root.

Notes for developers

- Code structure: Controller -> Service -> Repository
- DTOs/Records are used for request/response objects; JPA/DB entities are separate from API contracts.
- Validation uses Jakarta Bean Validation annotations (e.g. `@NotNull`, `@Pattern`).
