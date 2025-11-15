# Quiz Application - Microservices Architecture

This document describes the overall architecture of the Quiz application microservices system.

## Architecture Overview

The Quiz application follows a microservices architecture pattern with service discovery:

```
┌─────────────┐
│   Frontend  │ (React + Vite)
│  (Port 5174)│
└──────┬──────┘
       │
       │ HTTP/REST
       │
┌──────▼──────────────┐
│   API Gateway       │ (Spring Cloud Gateway)
│   (Port 8080)       │ ─────┐
└──────┬──────────────┘      │
       │                     │ Service Discovery
       │                     │
       ├─────────┬───────────┼─────────┬──────────┐
       │         │           │         │          │
┌──────▼──────┐  │   ┌───────▼────────┐│          │
│Auth Service │  │   │ Eureka Server  ││          │
│ (Port 8081) │──┘   │  (Port 8761)   ││          │
└─────────────┘      └────────────────┘│          │
       │                     ▲          │          │
┌──────▼──────┐              │  ┌───────▼──────┐  │  ┌────────────────┐
│   H2 DB     │      Register│  │Question Svc  │──┘  │  Quiz Svc      │
│  (File)     │              │  │ (Port 8082)  │     │  (Port 8083)   │
└─────────────┘              │  └──────┬───────┘     └────────┬───────┘
                             │         │                      │
                             └─────────┘              ┌───────▼──────┐
                                     ┌────────────────┤   H2 DB      │
                             ┌───────▼──────┐         │  (File)      │
                             │   H2 DB      │         └──────────────┘
                             │  (File)      │
                             └──────────────┘
```

**Key Components**:
- **Eureka Server** (8761): Service registry and discovery
- **API Gateway** (8080): Routes requests and discovers services via Eureka
- **Microservices** (8081-8083): Register with Eureka at startup
- **Frontend** (5174): Communicates only with API Gateway

## Services

### 1. Frontend (quizz_frontend)
- **Repository**: https://github.com/justinGirot/Quizz_frontend
- **Technology**: React 18.3, Vite 6.x, React Router
- **Port**: 5173 (development)
- **Responsibilities**:
  - User interface for quiz application
  - **Authentication UI** (login/signup with httpOnly cookie support)
  - **Question Management** (full CRUD with workflow: draft → pending → validated/rejected/archived)
  - Quiz taking interface (future)
  - User dashboard and scoring (future)

**Features Implemented**:
- Secure authentication with httpOnly cookies
- Question lifecycle management (create, edit, delete, validate, archive)
- Dynamic question types (text-input, multiple-choice)
- Image support for answers
- Advanced filtering (status, category)
- Pagination (10 items per page)
- Dual view modes (active/archive)

### 2. API Gateway (quizz-api-gateway)
- **Repository**: https://github.com/justinGirot/quizz-api-gateway
- **Technology**: Spring Boot 3.4, Spring Cloud Gateway, Java 21
- **Port**: 8080
- **Responsibilities**:
  - Single entry point for all client requests
  - Request routing to appropriate microservices
  - Load balancing across service instances
  - CORS configuration
  - Rate limiting and throttling
  - Request/response logging
  - API versioning

**Routing Configuration**:
```yaml
/api/auth/**        → auth-service (8081)
/api/questions/**   → question-service (8082)
/api/quizzes/**     → quiz-service (8083)
```

### 3. Eureka Server (quizz-eureka-server)
- **Repository**: https://github.com/justinGirot/quizz-eureka-server
- **Technology**: Spring Boot 3.4, Spring Cloud Netflix Eureka Server, Java 21
- **Port**: 8761
- **Responsibilities**:
  - Service registry and discovery
  - Service health monitoring
  - Dynamic service instance tracking
  - Load balancing support
  - Service metadata management

**Key Features**:
- Services register themselves at startup
- API Gateway discovers services dynamically
- Heartbeat-based health checking
- Web dashboard for service monitoring

**Configuration**:
```yaml
eureka:
  client:
    register-with-eureka: false  # Eureka doesn't register with itself
    fetch-registry: false
  server:
    enable-self-preservation: false  # Disabled in development
```

**Dashboard Access**: `http://localhost:8761`

**Registered Services**:
- api-gateway
- auth-service
- question-service
- quiz-service

### 4. Auth Service (quizz-auth-service)
- **Repository**: https://github.com/justinGirot/quizz-auth-service
- **Technology**: Spring Boot 3.4, Spring Security, Spring Data JPA, Java 21
- **Port**: 8081
- **Database**: H2 (file-based: `./data/auth.db`)
- **Responsibilities**:
  - User registration and authentication
  - **JWT token generation and validation with httpOnly cookies**
  - User profile management
  - Password hashing and security (BCrypt)
  - Session management

**Key Endpoints**:
- `POST /api/auth/register` - User registration (sets httpOnly cookie)
- `POST /api/auth/login` - User authentication (sets httpOnly cookie)
- `POST /api/auth/logout` - User logout (clears httpOnly cookie)
- `GET /api/auth/me` - Get current user profile (requires cookie)

**Security Features**:
- **httpOnly Cookies**: JWT stored in httpOnly cookie (XSS protection)
- **SameSite Attribute**: CSRF protection
- **Secure Flag**: HTTPS-only in production
- **BCrypt Hashing**: Strong password encryption
- **Auto-login**: Sets cookie and returns user data on register/login

**Data Model**:
```java
User {
  Long id;
  String email;
  String password; // BCrypt hashed
  String firstName;
  String lastName;
  LocalDateTime createdAt;
  LocalDateTime lastLogin;
  Set<Role> roles;
}
```

### 5. Question Service (quizz-question-service)
- **Repository**: https://github.com/justinGirot/quizz-question-service
- **Technology**: Spring Boot 3.4, Spring Data JPA, Java 21, PostgreSQL, Liquibase
- **Port**: 8082
- **Database**: PostgreSQL (questions_db)
- **Responsibilities**:
  - Question creation, retrieval, update, deletion (CRUD)
  - **Question workflow management** (draft → pending → validated/rejected/archived)
  - **Category referential system** (admin-managed categories with active/inactive status)
  - Question categorization with foreign key constraints
  - Question difficulty levels and points
  - User ownership and permissions
  - Input sanitization and XSS prevention
  - OpenAPI/Swagger documentation

**Question Endpoints**:
- `POST /api/questions` - Create question (starts as draft)
- `GET /api/questions/{id}` - Get question by ID
- `GET /api/questions?statuses[]=DRAFT&categories[]=Science` - List questions with filters
- `PUT /api/questions/{id}` - Update question (including status changes)
- `DELETE /api/questions/{id}` - Delete question (only drafts)

**Category Endpoints**:
- `GET /api/categories/active` - Get active categories (all authenticated users)
- `GET /api/categories` - Get all categories including inactive (admin only)
- `GET /api/categories/{id}` - Get category by ID (admin only)
- `POST /api/categories` - Create new category (admin only)
- `PUT /api/categories/{id}` - Update category (admin only)
- `DELETE /api/categories/{id}` - Delete category if not in use (admin only)

**Question Workflow**:
1. **Draft** (editable):
   - Created by users
   - Can be edited, deleted, or validated
   - Only creator or admin can modify
   - Validate action → moves to Pending

2. **Pending** (read-only):
   - Awaiting admin validation
   - Cannot be edited (must move back to draft first)
   - Can be moved back to draft by creator
   - Admin can validate/reject

3. **Validated** (archived):
   - Approved for use in quizzes
   - View-only, cannot be modified

4. **Rejected** (archived):
   - Not approved for use
   - View-only, cannot be modified

5. **Archived** (archived):
   - Historical record
   - View-only, cannot be modified

**Data Model**:
```java
Category {
  Long id;
  String name; // unique, 3-100 characters
  String description; // optional, max 500 characters
  CategoryStatus status; // ACTIVE or INACTIVE
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  Long createdBy; // User ID (admin who created)
}

Question {
  Long id;
  String text; // min 10 characters, HTML sanitized
  QuestionType type; // MULTIPLE_CHOICE, TEXT_INPUT
  QuestionStatus status; // DRAFT, PENDING, VALIDATED, REJECTED, ARCHIVED
  String category; // deprecated field for backward compatibility
  Long categoryId; // foreign key to categories table
  DifficultyLevel difficulty; // EASY, MEDIUM, HARD
  Integer points; // 1-100
  List<Answer> answers; // min 1 answer required
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  Long createdBy; // User ID (owner)
}

Answer {
  Long id;
  String text; // required, non-empty, HTML sanitized
  boolean isCorrect; // at least one must be true
  String imageUrl; // optional, URL to answer image, validated
  Long questionId; // foreign key with CASCADE delete
}
```

**Validation Rules**:
- **Question text**: required, 10-1000 characters, HTML sanitized, XSS prevention
- **Category**: must reference existing active category (foreign key constraint)
- **Points**: required, 1-100
- **Answers**: at least 1 required, all must have non-empty text, HTML sanitized
- **At least one answer must be marked as correct**
- **Image URLs**: optional, validated for malicious URLs (javascript:, data:, vbscript:)
- **Status transitions**: draft ↔ pending, pending → validated/rejected/archived
- **Category name**: unique (case-insensitive), 3-100 characters, alphanumeric with spaces/hyphens/underscores
- **Category description**: optional, max 500 characters

**Input Sanitization & Security**:
- **OWASP HTML Sanitizer**: All text inputs stripped of HTML tags
- **Apache Commons Text**: HTML entity encoding
- **@Sanitized annotation**: Custom validation for dangerous content
- **URL validation**: Blocks javascript:, data:, vbscript: protocols
- **Control characters**: Removed from all inputs
- **SQL injection prevention**: Parameterized queries via JPA

**Authorization**:
- All endpoints require authentication (JWT httpOnly cookie)
- **Question endpoints**:
  - Create: any authenticated user
  - Edit/Delete draft: only creator or admin
  - Validate/Reject/Archive: only admin
  - Move to draft: creator or admin
- **Category endpoints**:
  - GET /api/categories/active: all authenticated users
  - All other category endpoints: admin only (@PreAuthorize("hasRole('ADMIN')"))

### 5. Quiz Service (quizz-quiz-service)
- **Repository**: https://github.com/justinGirot/quizz-quiz-service
- **Technology**: Spring Boot 3.4, Spring Data JPA, Java 21
- **Port**: 8083
- **Database**: H2 (file-based: `./data/quizzes.db`)
- **Responsibilities**:
  - Quiz creation and configuration
  - Quiz session management
  - Score calculation and tracking
  - Leaderboard management
  - Quiz participation tracking
  - Quiz results and analytics

**Key Endpoints**:
- `POST /api/quizzes` - Create quiz
- `GET /api/quizzes/{id}` - Get quiz details
- `GET /api/quizzes` - List available quizzes
- `POST /api/quizzes/{id}/start` - Start quiz session
- `POST /api/quizzes/sessions/{sessionId}/answer` - Submit answer
- `POST /api/quizzes/sessions/{sessionId}/complete` - Complete quiz
- `GET /api/quizzes/{id}/leaderboard` - Get quiz leaderboard
- `GET /api/quizzes/sessions/{sessionId}/results` - Get quiz results

**Data Model**:
```java
Quiz {
  Long id;
  String title;
  String description;
  List<Long> questionIds; // References to Question Service
  Integer timeLimit; // in minutes
  Integer maxAttempts;
  LocalDateTime startDate;
  LocalDateTime endDate;
  Long createdBy; // User ID
}

QuizSession {
  Long id;
  Long quizId;
  Long userId;
  SessionStatus status; // IN_PROGRESS, COMPLETED, ABANDONED
  LocalDateTime startedAt;
  LocalDateTime completedAt;
  Integer score;
  List<QuizAnswer> answers;
}

QuizAnswer {
  Long questionId;
  String answerId;
  boolean isCorrect;
  Integer pointsAwarded;
  LocalDateTime answeredAt;
}
```

## Communication Patterns

### 1. Synchronous Communication (REST)
- Frontend → API Gateway → Services
- Used for: CRUD operations, authentication, real-time data

### 2. Service-to-Service Communication
- **Quiz Service → Question Service**: Fetch questions for quiz
- **Quiz Service → Auth Service**: Validate user tokens (optional, can use JWT validation)

### 3. Future: Asynchronous Communication (Message Queue)
- Event-driven updates (e.g., quiz completion events)
- Consider: RabbitMQ, Apache Kafka for future scalability

## Authentication & Authorization

### Authentication Flow (httpOnly Cookies):
1. User submits credentials to `POST /api/auth/login` via API Gateway
2. Auth Service validates credentials
3. Auth Service generates JWT token
4. **Auth Service sets httpOnly cookie** in response header:
   ```
   Set-Cookie: auth_token=<jwt>; HttpOnly; Secure; SameSite=Lax; Path=/; MaxAge=604800
   ```
5. **Frontend receives user data** (non-sensitive): `{ user: { id, email, firstName, lastName } }`
6. Frontend stores user data in localStorage (not the token)
7. **Subsequent requests**: Browser automatically includes cookie
8. API Gateway/Services validate JWT from cookie
9. Request processed if valid

### Logout Flow:
1. User triggers logout
2. Frontend calls `POST /api/auth/logout`
3. Backend clears cookie: `Set-Cookie: auth_token=; HttpOnly; MaxAge=0`
4. Frontend clears localStorage user data
5. User redirected to login page

### JWT Token Structure (stored in httpOnly cookie):
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "email": "user@example.com",
  "roles": ["USER"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

### Security:
- **httpOnly Cookies**: JavaScript cannot access tokens (XSS protection)
- **Secure Flag**: HTTPS-only transmission in production
- **SameSite Attribute**: CSRF protection (Lax or Strict)
- **BCrypt Hashing**: All passwords hashed with BCrypt
- **JWT Expiration**: Tokens expire after configurable time (e.g., 7 days)
- **CORS Configuration**: At API Gateway only, with credentials: true
- **Rate Limiting**: Per user/IP (future enhancement)

## Data Storage

### Development:
- **Auth Service**: H2 file-based database (`./data/auth.db`)
- **Question Service**: PostgreSQL with Docker Compose (`questions_db` on port 5432)
- **Quiz Service**: H2 file-based database (`./data/quizzes.db`)
- Data persisted locally for easy development and testing
- Liquibase migrations for database version control (Question Service)

### Production Considerations:
- Migrate all services to PostgreSQL/MySQL
- Each service has its own database (database per service pattern)
- No direct database access between services
- Data consistency via API calls or eventual consistency patterns
- Database backups and disaster recovery
- Connection pooling and performance tuning

## Configuration

### Application Ports:
```properties
frontend:         5173 (dev), 3000 (prod)
api-gateway:      8080
eureka-server:    8761
auth-service:     8081
question-service: 8082
quiz-service:     8083
```

### Environment Variables:

**Auth Service / Quiz Service (H2)**:
```properties
# Server
SERVER_PORT=8081 or 8083
SPRING_PROFILES_ACTIVE=dev|prod

# Database (H2)
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/service.db
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# JWT (Auth Service only)
JWT_SECRET=your-super-secret-key-change-this-in-production
JWT_EXPIRATION=604800000

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

**Question Service (PostgreSQL)**:
```properties
# Server
SERVER_PORT=8082
SPRING_PROFILES_ACTIVE=dev|prod

# Database (PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/questions_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT (for validation)
JWT_SECRET=your-super-secret-key-change-this-in-production

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

## API Documentation

### OpenAPI/Swagger:
- Each service exposes OpenAPI documentation
- Accessible at: `http://localhost:808X/swagger-ui.html`
- API Gateway aggregates all service APIs

### Adding OpenAPI to Services:
Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Development Workflow

### Running Locally:
1. Start Eureka Server (optional): `cd quizz-eureka-server && ./mvnw spring-boot:run`
2. Start API Gateway: `cd quizz-api-gateway && ./mvnw spring-boot:run`
3. Start Auth Service: `cd quizz-auth-service && ./mvnw spring-boot:run`
4. Start Question Service (when implemented): `cd quizz-question-service && ./mvnw spring-boot:run`
5. Start Quiz Service (when implemented): `cd quizz-quiz-service && ./mvnw spring-boot:run`
6. Start Frontend: `cd quizz_frontend && npm run dev` (runs on port 5173)

**Note**: The frontend currently uses mock data for questions. Once the Question Service backend is implemented, switch `USE_MOCK = false` in `questionService.js`.

### Testing Services:
```bash
# Health check
curl http://localhost:8081/actuator/health

# Register user (sets httpOnly cookie)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123","firstName":"John","lastName":"Doe"}' \
  -c cookies.txt

# Login (sets httpOnly cookie)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123"}' \
  -c cookies.txt

# Create question (uses cookie from cookies.txt)
curl -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "text":"What is the capital of France?",
    "category":"Geography",
    "difficulty":"easy",
    "points":10,
    "type":"multiple-choice",
    "answers":[
      {"text":"London","isCorrect":false,"imageUrl":""},
      {"text":"Paris","isCorrect":true,"imageUrl":""},
      {"text":"Berlin","isCorrect":false,"imageUrl":""}
    ]
  }'

# List questions with filters (uses cookie)
curl http://localhost:8080/api/questions?statuses[]=draft&categories[]=Geography \
  -b cookies.txt

# Update question status to pending (uses cookie)
curl -X PUT http://localhost:8080/api/questions/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"status":"pending"}'

# Delete question (uses cookie)
curl -X DELETE http://localhost:8080/api/questions/1 \
  -b cookies.txt
```

## Future Enhancements

### 1. Service Discovery
- Add Eureka Server for dynamic service discovery
- Services register themselves at startup
- API Gateway discovers services dynamically

### 2. Centralized Configuration
- Spring Cloud Config Server
- Externalized configuration for all services
- Environment-specific configurations

### 3. Distributed Tracing
- Spring Cloud Sleuth + Zipkin
- Track requests across services
- Performance monitoring

### 4. Circuit Breaker
- Resilience4j for fault tolerance
- Graceful degradation when services fail
- Fallback mechanisms

### 5. Message Queue
- RabbitMQ or Apache Kafka
- Asynchronous event processing
- Decoupled service communication

### 6. Caching
- Redis for distributed caching
- Cache frequently accessed data
- Reduce database load

### 7. Containerization
- Docker containers for each service
- Docker Compose for local orchestration
- Kubernetes for production deployment

## Best Practices

1. **Database per Service**: Each service owns its database
2. **API Versioning**: Use `/api/v1/` for future-proof APIs
3. **Error Handling**: Consistent error response format
4. **Logging**: Structured logging with correlation IDs
5. **Testing**: Unit tests, integration tests, contract tests
6. **Documentation**: Keep OpenAPI specs up to date
7. **Security**: Validate all inputs, use parameterized queries
8. **Monitoring**: Health checks, metrics, alerting

## Technology Stack Summary

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.0
- **Build Tool**: Maven
- **Database**: H2 (dev), PostgreSQL (prod)
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security, JWT
- **Documentation**: SpringDoc OpenAPI
- **Monitoring**: Spring Boot Actuator
- **Frontend**: React 18.3, Vite 6.x

## Repository Links

- Frontend: https://github.com/justinGirot/Quizz_frontend
- Auth Service: https://github.com/justinGirot/quizz-auth-service
- Question Service: https://github.com/justinGirot/quizz-question-service
- Quiz Service: https://github.com/justinGirot/quizz-quiz-service
- API Gateway: https://github.com/justinGirot/quizz-api-gateway

---

**Note**: This architecture is designed for scalability and maintainability. Start simple and add complexity as needed.
