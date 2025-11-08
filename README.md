# Catalog API

REST API for managing a bookstore catalog with automatic EUR/UAH currency conversion.

## Main Endpoints

### Books API
- POST   /api/v1/books         — create a book (returns 201 Created + Location header)
- GET    /api/v1/books         — list books (supports pagination: page, size, sort)
- GET    /api/v1/books/{id}    — get a book by id
- PUT    /api/v1/books/{id}    — partial update of a book (PATCH-like behavior)
- DELETE /api/v1/books/{id}    — soft-delete (marks the book as deleted)

### Exchange Rate API
- GET    /api/v1/rate          — get current EUR/UAH exchange rate
- POST   /api/v1/rate/fetch    — manually fetch rate from NBU API (for testing)
- POST   /api/v1/rate/update?rate={value} — manually set exchange rate (for testing)

## OpenAPI / Swagger UI
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Swagger UI:   http://localhost:8080/swagger-ui.html

## Behavior Summary
- POST /api/v1/books returns 201 Created and a Location header: /api/v1/books/{id}.
- PUT /api/v1/books/{id} behaves as a partial update: fields omitted from the request remain unchanged.
- `isbn` is validated (ISBN-10 or ISBN-13) and is required. `price.uah` is required; `price.eur` is calculated automatically using the current exchange rate.
- **Currency Conversion**: When a book is created or updated, `price.eur` is automatically calculated as `price.uah / current_rate`.
- **Automatic Rate Updates**: The exchange rate is fetched from NBU API daily at 09:00 (Europe/Kiev timezone). When the rate is updated, all book EUR prices are automatically recalculated.
- On ISBN conflict the API returns 409 Conflict.
- If a book is not found the API returns 404 Not Found using Problem Details (RFC 7807).
- DELETE is a soft delete: the book is marked deleted and excluded from list results.

## Database
- The application uses an in-memory H2 database by default (configured in application.properties).
- No additional configuration is needed to run the application.

## Run Locally

Simply run:
```cmd
.\gradlew.bat bootRun
```

The application will start on http://localhost:8080

## Run Tests

```cmd
.\gradlew.bat test
```

## API Examples (curl)

### Books API

1) Create book (POST) — returns 201 and Location

```bash
curl -i -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"0131872486","title":"Thinking in Java","author":"Method…","publicationYear":2006,"price":{"uah":400.00}}'
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
  -d '{"title":"War and Peace - Updated Edition","price":{"uah":500.00}}'
```

5) Soft delete

```bash
curl -i -X DELETE http://localhost:8080/api/v1/books/1
```

### Exchange Rate API

6) Get current exchange rate

```bash
curl -i http://localhost:8080/api/v1/rate
```

7) Fetch rate from NBU API

```bash
curl -i -X POST http://localhost:8080/api/v1/rate/fetch
```

8) Manually set exchange rate

```bash
curl -i -X POST "http://localhost:8080/api/v1/rate/update?rate=40.50"
```

## Swagger / OpenAPI Documentation

After starting the application, you can explore the API using:

- **Swagger UI** (interactive documentation): http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** (raw specification): http://localhost:8080/v3/api-docs

The Swagger UI provides a convenient interface to:
- View all available endpoints with detailed descriptions
- Test API operations directly from the browser
- See request/response schemas and examples

## Troubleshooting

- If `/v3/api-docs` returns HTTP 500, check the application log for stacktrace — the most common cause is a version mismatch between `springdoc` and Spring Framework (NoSuchMethodError referencing ControllerAdviceBean). The project currently uses `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0` with Spring Boot 3.3.1.
- If you see `Cannot mutate dependency attributes` error during build, ensure you're using Gradle 8.5 (not 9.x). The project's `gradle-wrapper.properties` is configured for Gradle 8.5.
- If port 8080 is already in use, stop any running processes or change the port in `application.properties` by adding `server.port=8081`.

## Technical Details

### Architecture
- **Three-tier architecture**: Controller -> Service -> Repository
- **DTOs/Records**: Used for request/response objects; JPA/DB entities are separate from API contracts
- **Validation**: Jakarta Bean Validation annotations (`@NotNull`, `@Pattern`, etc.)
- **Error Handling**: Global exception handler with RFC 7807 Problem Details format
- **Transaction Management**: `@Transactional` annotations on service methods

### Currency Conversion
- **Formula**: `price.eur = price.uah / exchange_rate`
- **Rate Source**: NBU (National Bank of Ukraine) API - https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode=EUR&json
- **Initialization**: On application startup, the exchange rate is automatically fetched from NBU API and saved to the database. If NBU is unavailable, a default rate of 40.00 is used.
- **Update Schedule**: Daily at 09:00 (Europe/Kiev timezone) via Quartz Scheduler
- **Rate Storage**: Only the current exchange rate is stored in the database (previous rates are deleted on update)
- **Automatic Recalculation**: When the exchange rate is updated, EUR prices for all books are automatically recalculated

### Technologies
- **Java**: 21
- **Spring Boot**: 3.3.1
- **Database**: H2 (in-memory)
- **Build Tool**: Gradle 8.5
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Logging**: Logback with Logstash JSON encoder
- **Scheduler**: Quartz
