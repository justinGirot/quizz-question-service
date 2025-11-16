# Question Service API Documentation

Complete API reference for the Question Service microservice.

## Base URL

- **Local Development**: `http://localhost:8082`
- **Via API Gateway**: `http://localhost:8080` (routes to `/api/questions/**` and `/api/categories/**`)

## Authentication

All endpoints (except health/metrics) require JWT authentication via httpOnly cookie.

**Cookie Name**: `auth_token`
**Cookie Attributes**: HttpOnly, Secure (prod only), SameSite=Lax

### Getting an Auth Token

Authentication is handled by the Auth Service. Login there first to get the `auth_token` cookie.

```bash
# Login via Auth Service
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  -c cookies.txt

# Then use the cookie for subsequent requests
curl http://localhost:8082/api/questions \
  -b cookies.txt
```

---

## Questions API

### Create Question

Creates a new question in DRAFT status.

**Endpoint**: `POST /api/questions`
**Auth Required**: Yes
**Admin Required**: No

**Request Body**:
```json
{
  "text": "What is the capital of France?",
  "type": "MULTIPLE_CHOICE",
  "category": "Geography",
  "difficulty": "EASY",
  "points": 10,
  "answers": [
    {
      "text": "Paris",
      "isCorrect": true,
      "imageUrl": null
    },
    {
      "text": "London",
      "isCorrect": false,
      "imageUrl": null
    },
    {
      "text": "Berlin",
      "isCorrect": false,
      "imageUrl": null
    }
  ]
}
```

**Validation Rules**:
- `text`: 10-1000 characters, required
- `type`: MULTIPLE_CHOICE or TEXT_INPUT, required
- `category`: 3-100 characters, required
- `difficulty`: EASY, MEDIUM, or HARD, required
- `points`: 1-100, required
- `answers`: At least 1 answer, at least one must be correct

**Response**: `201 Created`
```json
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

**Error Responses**:
- `400 Bad Request` - Validation failed
- `401 Unauthorized` - No auth token or invalid token

---

### Get Question by ID

Retrieves a single question.

**Endpoint**: `GET /api/questions/{id}`
**Auth Required**: Yes
**Admin Required**: No

**Path Parameters**:
- `id` (Long) - Question ID

**Response**: `200 OK`
```json
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

**Error Responses**:
- `404 Not Found` - Question doesn't exist
- `401 Unauthorized` - No auth token

---

### List Questions with Filters

Retrieves questions with optional filtering.

**Endpoint**: `GET /api/questions`
**Auth Required**: Yes
**Admin Required**: No

**Query Parameters**:
- `statuses[]` (Array<String>) - Filter by status (DRAFT, PENDING, VALIDATED, REJECTED, ARCHIVED)
- `categories[]` (Array<String>) - Filter by category names

**Examples**:
```bash
# Get all DRAFT questions
GET /api/questions?statuses[]=DRAFT

# Get DRAFT and PENDING questions in Science category
GET /api/questions?statuses[]=DRAFT&statuses[]=PENDING&categories[]=Science

# Get all questions (no filters)
GET /api/questions
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "text": "What is the capital of France?",
    "status": "DRAFT",
    ...
  },
  {
    "id": 2,
    "text": "What is 2+2?",
    "status": "PENDING",
    ...
  }
]
```

**Error Responses**:
- `401 Unauthorized` - No auth token

---

### Update Question

Updates an existing question. DRAFT questions can be fully edited. Other statuses only allow status changes.

**Endpoint**: `PUT /api/questions/{id}`
**Auth Required**: Yes
**Admin Required**: No (for own questions), Yes (for others' questions)

**Path Parameters**:
- `id` (Long) - Question ID

**Request Body**:
```json
{
  "text": "Updated question text",
  "type": "MULTIPLE_CHOICE",
  "status": "PENDING",
  "category": "Science",
  "difficulty": "MEDIUM",
  "points": 15,
  "answers": [...]
}
```

**Authorization Rules**:
- Only creator or admin can update
- DRAFT questions: Full edit allowed
- Non-DRAFT: Only status changes allowed (unless admin)

**Status Transition Rules**:
| From | To | Who |
|------|----|----|
| DRAFT | PENDING | Creator or Admin |
| PENDING | DRAFT | Creator or Admin |
| PENDING | VALIDATED/REJECTED/ARCHIVED | Admin only |
| VALIDATED/REJECTED/ARCHIVED | Any | Admin only |

**Response**: `200 OK`
```json
{
  "id": 1,
  "text": "Updated question text",
  "status": "PENDING",
  ...
}
```

**Error Responses**:
- `400 Bad Request` - Invalid status transition or validation failed
- `403 Forbidden` - Not the creator or admin
- `404 Not Found` - Question doesn't exist
- `401 Unauthorized` - No auth token

---

### Delete Question

Deletes a question. Only DRAFT questions can be deleted.

**Endpoint**: `DELETE /api/questions/{id}`
**Auth Required**: Yes
**Admin Required**: No (for own questions), Yes (for others' questions)

**Path Parameters**:
- `id` (Long) - Question ID

**Response**: `200 OK`
```json
{
  "message": "Question deleted successfully"
}
```

**Error Responses**:
- `400 Bad Request` - Question is not in DRAFT status
- `403 Forbidden` - Not the creator or admin
- `404 Not Found` - Question doesn't exist
- `401 Unauthorized` - No auth token

---

## Categories API

### Get Active Categories

Retrieves all active categories for use in dropdowns.

**Endpoint**: `GET /api/categories/active`
**Auth Required**: Yes
**Admin Required**: No

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Science",
    "description": "Science and technology questions",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00",
    "createdBy": 1
  },
  {
    "id": 2,
    "name": "History",
    "description": "Historical events and figures",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00",
    "createdBy": 1
  }
]
```

**Error Responses**:
- `401 Unauthorized` - No auth token

---

### Get All Categories (Admin)

Retrieves all categories including inactive ones.

**Endpoint**: `GET /api/categories`
**Auth Required**: Yes
**Admin Required**: Yes (ROLE_ADMIN)

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Science",
    "status": "ACTIVE",
    ...
  },
  {
    "id": 6,
    "name": "Obsolete Category",
    "status": "INACTIVE",
    ...
  }
]
```

**Error Responses**:
- `403 Forbidden` - Not an admin
- `401 Unauthorized` - No auth token

---

### Get Category by ID (Admin)

Retrieves a single category.

**Endpoint**: `GET /api/categories/{id}`
**Auth Required**: Yes
**Admin Required**: Yes (ROLE_ADMIN)

**Path Parameters**:
- `id` (Long) - Category ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Science",
  "description": "Science and technology questions",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00",
  "createdBy": 1
}
```

**Error Responses**:
- `404 Not Found` - Category doesn't exist
- `403 Forbidden` - Not an admin
- `401 Unauthorized` - No auth token

---

### Create Category (Admin)

Creates a new category.

**Endpoint**: `POST /api/categories`
**Auth Required**: Yes
**Admin Required**: Yes (ROLE_ADMIN)

**Request Body**:
```json
{
  "name": "Mathematics",
  "description": "Math and arithmetic questions"
}
```

**Validation Rules**:
- `name`: 3-100 characters, unique (case-insensitive), alphanumeric with spaces/hyphens/underscores
- `description`: Optional, max 500 characters

**Response**: `201 Created`
```json
{
  "id": 6,
  "name": "Mathematics",
  "description": "Math and arithmetic questions",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00",
  "createdBy": 1
}
```

**Error Responses**:
- `400 Bad Request` - Validation failed or duplicate name
- `403 Forbidden` - Not an admin
- `401 Unauthorized` - No auth token

---

### Update Category (Admin)

Updates an existing category.

**Endpoint**: `PUT /api/categories/{id}`
**Auth Required**: Yes
**Admin Required**: Yes (ROLE_ADMIN)

**Path Parameters**:
- `id` (Long) - Category ID

**Request Body**:
```json
{
  "name": "Mathematics",
  "description": "Updated description",
  "status": "INACTIVE"
}
```

**Response**: `200 OK`
```json
{
  "id": 6,
  "name": "Mathematics",
  "description": "Updated description",
  "status": "INACTIVE",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:15:00",
  "createdBy": 1
}
```

**Error Responses**:
- `400 Bad Request` - Validation failed or duplicate name
- `404 Not Found` - Category doesn't exist
- `403 Forbidden` - Not an admin
- `401 Unauthorized` - No auth token

---

### Delete Category (Admin)

Deletes a category. Cannot delete if used in questions.

**Endpoint**: `DELETE /api/categories/{id}`
**Auth Required**: Yes
**Admin Required**: Yes (ROLE_ADMIN)

**Path Parameters**:
- `id` (Long) - Category ID

**Response**: `200 OK`
```json
{
  "message": "Category deleted successfully"
}
```

**Error Responses**:
- `400 Bad Request` - Category is in use by questions
- `404 Not Found` - Category doesn't exist
- `403 Forbidden` - Not an admin
- `401 Unauthorized` - No auth token

---

## Referential API

### Get Difficulty Levels

Retrieves all available difficulty levels for use in dropdowns and question creation.

**Endpoint**: `GET /api/referential/difficulty-levels`
**Auth Required**: Yes
**Admin Required**: No

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Easy",
    "description": "Easy level questions suitable for beginners",
    "displayOrder": 1,
    "pointsMultiplier": 1.0
  },
  {
    "id": 2,
    "name": "Medium",
    "description": "Medium difficulty questions for intermediate users",
    "displayOrder": 2,
    "pointsMultiplier": 1.5
  },
  {
    "id": 3,
    "name": "Hard",
    "description": "Hard questions for advanced users",
    "displayOrder": 3,
    "pointsMultiplier": 2.0
  },
  {
    "id": 4,
    "name": "Expert",
    "description": "Expert level questions for professionals",
    "displayOrder": 4,
    "pointsMultiplier": 3.0
  }
]
```

**Error Responses**:
- `401 Unauthorized` - No auth token

**Notes**:
- Difficulty levels are pre-populated via database migrations
- The `pointsMultiplier` can be used to calculate final points based on question base points
- Results are ordered by `displayOrder` ascending

---

## Health & Metrics API

### Health Check

Returns service health status.

**Endpoint**: `GET /actuator/health`
**Auth Required**: No

**Response**: `200 OK`
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 198104784896,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

---

### Application Info

Returns application information.

**Endpoint**: `GET /actuator/info`
**Auth Required**: No

**Response**: `200 OK`
```json
{
  "app": {
    "name": "question-service",
    "version": "0.0.1-SNAPSHOT",
    "description": "Question management service"
  }
}
```

---

### Metrics (Prometheus)

Returns Prometheus-formatted metrics.

**Endpoint**: `GET /actuator/metrics`
**Auth Required**: No

**Response**: `200 OK`
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.gc.memory.promoted",
    "http.server.requests",
    "system.cpu.usage",
    ...
  ]
}
```

---

## Error Response Format

All errors follow this format:

```json
{
  "message": "Detailed error message",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/questions/123"
}
```

### HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Validation error or invalid request
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

---

## Interactive API Documentation

**Swagger UI**: http://localhost:8082/swagger-ui.html

The Swagger UI provides:
- Interactive API testing
- Request/response examples
- Schema definitions
- Authentication support
- Try-it-out functionality

---

## Code Examples

### JavaScript/Fetch

```javascript
// Create question
const response = await fetch('http://localhost:8082/api/questions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include', // Include httpOnly cookie
  body: JSON.stringify({
    text: 'What is 2+2?',
    type: 'MULTIPLE_CHOICE',
    category: 'Mathematics',
    difficulty: 'EASY',
    points: 5,
    answers: [
      { text: '3', isCorrect: false },
      { text: '4', isCorrect: true },
      { text: '5', isCorrect: false }
    ]
  })
});

const question = await response.json();
console.log('Created question:', question);
```

### cURL

```bash
# Create question
curl -X POST http://localhost:8082/api/questions \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "text": "What is 2+2?",
    "type": "MULTIPLE_CHOICE",
    "category": "Mathematics",
    "difficulty": "EASY",
    "points": 5,
    "answers": [
      {"text": "3", "isCorrect": false},
      {"text": "4", "isCorrect": true},
      {"text": "5", "isCorrect": false}
    ]
  }'

# Get active categories
curl http://localhost:8082/api/categories/active \
  -b cookies.txt

# Update question status
curl -X PUT http://localhost:8082/api/questions/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "text": "What is 2+2?",
    "type": "MULTIPLE_CHOICE",
    "status": "PENDING",
    "category": "Mathematics",
    "difficulty": "EASY",
    "points": 5,
    "answers": [...]
  }'
```

---

## Rate Limiting

Currently no rate limiting is implemented at the service level. Rate limiting should be configured at the API Gateway level.

## Versioning

Current API version: `v1` (implicit in `/api/` prefix)

Future versions will use explicit versioning: `/api/v2/questions`

---

**Last Updated**: 2024-01-15
**Service Version**: 0.0.1-SNAPSHOT
**Spring Boot**: 3.4.0
