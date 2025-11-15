# Question Service

Question management microservice for the Quiz application. Handles CRUD operations for questions with a complete workflow: draft → pending → validated/rejected/archived.

## Features

- ✅ **Full Question CRUD**: Create, read, update, delete questions
- ✅ **Workflow Management**: Draft → Pending → Validated/Rejected/Archived
- ✅ **JWT Authentication**: Cookie-based authentication with httpOnly cookies
- ✅ **PostgreSQL Database**: Production-ready database with Liquibase migrations
- ✅ **OpenAPI/Swagger**: Complete API documentation
- ✅ **Eureka Integration**: Service discovery and registration
- ✅ **Validation**: Comprehensive request validation with custom validators
- ✅ **Authorization**: Role-based access control (creator/admin)

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.0**
- **Spring Data JPA** - Database access
- **Spring Security** - JWT authentication
- **PostgreSQL** - Database
- **Liquibase** - Database migrations
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation
- **Spring Cloud Netflix Eureka** - Service discovery

## Prerequisites

- Java 21+
- Docker & Docker Compose (for PostgreSQL)
- Maven 3.8+

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:
- Database: `questions_db`
- Username: `postgres`
- Password: `postgres`

### 2. Build the Application

```bash
./mvnw clean install
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The service will start on **port 8082**.

## API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api-docs

## Endpoints

All endpoints require authentication via `auth_token` httpOnly cookie.

### Questions

- `POST /api/questions` - Create question (starts as DRAFT)
- `GET /api/questions/{id}` - Get question by ID
- `GET /api/questions?statuses[]=draft&categories[]=Science` - List with filters
- `GET /api/questions/categories` - Get all unique categories
- `PUT /api/questions/{id}` - Update question
- `DELETE /api/questions/{id}` - Delete question (DRAFT only)

### Health

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info

## Question Workflow

```
DRAFT → (validate) → PENDING → (admin approve) → VALIDATED
                      ↓
                  (admin reject) → REJECTED
                      ↓
                  (admin archive) → ARCHIVED
```

### Status Transitions

- **DRAFT**:
  - Created by any user
  - Can be edited, deleted, or moved to PENDING
  - Only creator or admin can modify

- **PENDING**:
  - Awaiting admin validation
  - Read-only (can be moved back to DRAFT by creator)
  - Admin can validate/reject/archive

- **VALIDATED/REJECTED/ARCHIVED**:
  - Final states
  - View-only
  - Only admin can modify

## Configuration

### Environment Variables

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/questions_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

### Database Migration

Liquibase automatically runs migrations on startup. Changelog files are in:
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/
    ├── 001-create-questions-table.yaml
    └── 002-create-answers-table.yaml
```

## Data Model

### Question

```java
{
  "id": 1,
  "text": "What is the capital of France?",
  "type": "MULTIPLE_CHOICE",
  "status": "DRAFT",
  "category": "Geography",
  "difficulty": "EASY",
  "points": 10,
  "answers": [...],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "createdBy": 123
}
```

### Answer

```java
{
  "id": 1,
  "text": "Paris",
  "isCorrect": true,
  "imageUrl": "https://example.com/image.jpg"
}
```

## Validation Rules

- **Question text**: Required, 10-1000 characters
- **Category**: Required, 3-100 characters
- **Points**: Required, 1-100
- **Answers**: At least 1 required, all must have non-empty text
- **At least one correct answer**: Required

## Development

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package -DskipTests
java -jar target/question-service-0.0.1-SNAPSHOT.jar
```

### Database Access

```bash
# Connect to PostgreSQL
docker exec -it questions-postgres psql -U postgres -d questions_db

# View tables
\dt

# Query questions
SELECT * FROM questions;
SELECT * FROM answers;
```

## Integration with Other Services

This service integrates with:

1. **Auth Service** (port 8081): JWT token validation
2. **API Gateway** (port 8080): Routes `/api/questions/**` to this service
3. **Eureka Server** (port 8761): Service registration and discovery
4. **Quiz Service** (port 8083): References question IDs for quizzes

## Architecture Notes

### Database per Service Pattern

- This service owns its database (`questions_db`)
- Other services reference questions by ID only
- No direct database access from other services
- Data consistency via API calls

### Future Enhancements

- Statistics Service can consume question data for analytics
- Event-driven updates (publish question events)
- Caching layer (Redis) for frequently accessed questions
- Full-text search (Elasticsearch) for question search

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker ps

# View logs
docker logs questions-postgres

# Restart PostgreSQL
docker-compose restart
```

### Liquibase Migration Issues

```bash
# Check migration status
./mvnw liquibase:status

# Rollback last changeset
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1
```

## License

This project is part of the Quiz Application microservices system.
