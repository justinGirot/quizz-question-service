# Question Service - Group Integration Implementation Guide

**Version**: 1.0
**Last Updated**: 2024-01-20

This document describes all changes needed in the Question Service to support group-based questions and visibility management.

---

## Overview

### Goals
- Associate questions with groups (optional)
- Support public/private visibility for questions
- Enforce group-based permissions
- Maintain backward compatibility (existing questions remain public)

### Key Concepts
- **Public Question** (`groupId = NULL`): Available to everyone
- **Group Question** (`groupId != NULL`): Associated with a group
  - **PUBLIC visibility**: Anyone can use in quizzes
  - **PRIVATE visibility**: Only group members can use in quizzes

---

## 1. Database Changes

### 1.1 Migration File: Add Group Support to Questions

**File**: `src/main/resources/db/changelog/changes/010-add-group-support-to-questions.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 010-add-group-support-to-questions
      author: question-service
      changes:
        # Add groupId column (nullable - null means public question)
        - addColumn:
            tableName: questions
            columns:
              - column:
                  name: group_id
                  type: BIGINT
                  constraints:
                    nullable: true
                  remarks: "Reference to group (null = public question)"

        # Add visibility column
        - addColumn:
            tableName: questions
            columns:
              - column:
                  name: visibility
                  type: VARCHAR(20)
                  defaultValue: 'PUBLIC'
                  constraints:
                    nullable: false
                  remarks: "Question visibility: PUBLIC or PRIVATE"

        # Add check constraint for visibility
        - sql:
            sql: >
              ALTER TABLE questions
              ADD CONSTRAINT chk_question_visibility
              CHECK (visibility IN ('PUBLIC', 'PRIVATE'));

        # Add index for group queries
        - createIndex:
            indexName: idx_questions_group_visibility
            tableName: questions
            columns:
              - column:
                  name: group_id
              - column:
                  name: visibility
              - column:
                  name: status

        # Add index for group-based filtering
        - createIndex:
            indexName: idx_questions_group_status
            tableName: questions
            columns:
              - column:
                  name: group_id
              - column:
                  name: status

      rollback:
        - dropIndex:
            indexName: idx_questions_group_status
            tableName: questions
        - dropIndex:
            indexName: idx_questions_group_visibility
            tableName: questions
        - sql:
            sql: ALTER TABLE questions DROP CONSTRAINT IF EXISTS chk_question_visibility;
        - dropColumn:
            tableName: questions
            columnName: visibility
        - dropColumn:
            tableName: questions
            columnName: group_id
```

### 1.2 Update Master Changelog

**File**: `src/main/resources/db/changelog/db.changelog-master.yaml`

Add the new changeset:
```yaml
  - include:
      file: db/changelog/changes/010-add-group-support-to-questions.yaml
```

---

## 2. Entity Changes

### 2.1 Create QuestionVisibility Enum

**File**: `src/main/java/com/quizz/question/model/QuestionVisibility.java`

```java
package com.quizz.question.model;

public enum QuestionVisibility {
    PUBLIC,     // Anyone can use this question
    PRIVATE     // Only group members can use this question
}
```

### 2.2 Update Question Entity

**File**: `src/main/java/com/quizz/question/model/Question.java`

```java
// Add new fields
@Column(name = "group_id")
private Long groupId;

@Enumerated(EnumType.STRING)
@Column(name = "visibility", nullable = false)
private QuestionVisibility visibility = QuestionVisibility.PUBLIC;

// Add getters and setters
public Long getGroupId() {
    return groupId;
}

public void setGroupId(Long groupId) {
    this.groupId = groupId;
}

public QuestionVisibility getVisibility() {
    return visibility;
}

public void setVisibility(QuestionVisibility visibility) {
    this.visibility = visibility;
}

// Add helper methods
public boolean isPublicQuestion() {
    return groupId == null;
}

public boolean isGroupQuestion() {
    return groupId != null;
}

public boolean isPublicVisibility() {
    return visibility == QuestionVisibility.PUBLIC;
}

public boolean isPrivateVisibility() {
    return visibility == QuestionVisibility.PRIVATE;
}
```

---

## 3. DTO Changes

### 3.1 Update CreateQuestionRequest

**File**: `src/main/java/com/quizz/question/dto/CreateQuestionRequest.java`

```java
// Add new fields
private Long groupId;

@NotNull(message = "Visibility is required")
private QuestionVisibility visibility;

// Add getters and setters
public Long getGroupId() {
    return groupId;
}

public void setGroupId(Long groupId) {
    this.groupId = groupId;
}

public QuestionVisibility getVisibility() {
    return visibility;
}

public void setVisibility(QuestionVisibility visibility) {
    this.visibility = visibility;
}
```

### 3.2 Update UpdateQuestionRequest

**File**: `src/main/java/com/quizz/question/dto/UpdateQuestionRequest.java`

```java
// Add new fields
private Long groupId;

@NotNull(message = "Visibility is required")
private QuestionVisibility visibility;

// Add getters and setters (same as CreateQuestionRequest)
```

### 3.3 Update QuestionDTO

**File**: `src/main/java/com/quizz/question/dto/QuestionDTO.java`

```java
// Add new fields
private Long groupId;
private String groupName;  // Denormalized for display
private QuestionVisibility visibility;

// Add getters and setters
public Long getGroupId() {
    return groupId;
}

public void setGroupId(Long groupId) {
    this.groupId = groupId;
}

public String getGroupName() {
    return groupName;
}

public void setGroupName(String groupName) {
    this.groupName = groupName;
}

public QuestionVisibility getVisibility() {
    return visibility;
}

public void setVisibility(QuestionVisibility visibility) {
    this.visibility = visibility;
}
```

---

## 4. Repository Changes

### 4.1 Update QuestionRepository

**File**: `src/main/java/com/quizz/question/repository/QuestionRepository.java`

```java
// Add new query methods

/**
 * Find all questions by group ID and status
 */
List<Question> findByGroupIdAndStatus(Long groupId, QuestionStatus status);

/**
 * Find all public questions (groupId is null) with given status
 */
List<Question> findByGroupIdIsNullAndStatus(QuestionStatus status);

/**
 * Find questions by group ID, visibility, and status
 */
List<Question> findByGroupIdAndVisibilityAndStatus(
    Long groupId,
    QuestionVisibility visibility,
    QuestionStatus status
);

/**
 * Find questions accessible by user (public questions + user's group questions)
 * This will be used with custom query
 */
@Query("""
    SELECT q FROM Question q
    WHERE q.status = :status
    AND (
        q.groupId IS NULL
        OR q.groupId IN :groupIds
        OR q.visibility = 'PUBLIC'
    )
    ORDER BY q.createdAt DESC
    """)
List<Question> findAccessibleQuestions(
    @Param("status") QuestionStatus status,
    @Param("groupIds") List<Long> groupIds
);

/**
 * Count questions by group
 */
Long countByGroupId(Long groupId);

/**
 * Check if question belongs to group
 */
boolean existsByIdAndGroupId(Long questionId, Long groupId);
```

---

## 5. Service Layer Changes

### 5.1 Create GroupServiceClient

**File**: `src/main/java/com/quizz/question/client/GroupServiceClient.java`

```java
package com.quizz.question.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class GroupServiceClient {

    private final RestTemplate restTemplate;
    private final String groupServiceUrl;

    public GroupServiceClient(
            RestTemplate restTemplate,
            @Value("${group.service.url}") String groupServiceUrl) {
        this.restTemplate = restTemplate;
        this.groupServiceUrl = groupServiceUrl;
    }

    /**
     * Check if user is member of a group
     */
    public boolean isMemberOfGroup(Long groupId, Long userId) {
        try {
            String url = groupServiceUrl + "/api/groups/" + groupId + "/members/" + userId;
            MembershipResponse response = restTemplate.getForObject(url, MembershipResponse.class);
            return response != null && response.isMember();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check group membership", e);
        }
    }

    /**
     * Get group name
     */
    public String getGroupName(Long groupId) {
        try {
            String url = groupServiceUrl + "/api/groups/" + groupId;
            GroupResponse response = restTemplate.getForObject(url, GroupResponse.class);
            return response != null ? response.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all groups user is member of
     */
    public List<Long> getUserGroupIds(Long userId) {
        try {
            String url = groupServiceUrl + "/api/groups/my-groups";
            // This endpoint would need to be added to Group Service
            // For now, return empty list as fallback
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    // Inner classes for responses
    public static class MembershipResponse {
        private boolean isMember;
        private String role;

        public boolean isMember() { return isMember; }
        public void setMember(boolean member) { isMember = member; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class GroupResponse {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
```

### 5.2 Update QuestionServiceImpl

**File**: `src/main/java/com/quizz/question/service/QuestionServiceImpl.java`

Add new validation and business logic:

```java
// Inject GroupServiceClient
private final GroupServiceClient groupServiceClient;

public QuestionServiceImpl(
        QuestionRepository questionRepository,
        CategoryRepository categoryRepository,
        DifficultyLevelRepository difficultyLevelRepository,
        QuestionMapper questionMapper,
        SanitizationUtil sanitizationUtil,
        GroupServiceClient groupServiceClient) {
    this.questionRepository = questionRepository;
    this.categoryRepository = categoryRepository;
    this.difficultyLevelRepository = difficultyLevelRepository;
    this.questionMapper = questionMapper;
    this.sanitizationUtil = sanitizationUtil;
    this.groupServiceClient = groupServiceClient;
}

// Update createQuestion method
@Override
@Transactional
public QuestionDTO createQuestion(CreateQuestionRequest request, Long userId) {
    // Validate group access if groupId is provided
    if (request.getGroupId() != null) {
        validateGroupAccess(request.getGroupId(), userId);
    }

    // Validate visibility rules
    validateVisibility(request.getGroupId(), request.getVisibility());

    // Rest of existing creation logic...
    Question question = new Question();
    // ... existing field mapping ...
    question.setGroupId(request.getGroupId());
    question.setVisibility(request.getVisibility());
    // ... save and return ...
}

// Update updateQuestion method
@Override
@Transactional
public QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request, Long userId, boolean isAdmin) {
    Question existingQuestion = questionRepository.findById(id)
            .orElseThrow(() -> new QuestionNotFoundException("Question not found with ID: " + id));

    // Authorization check
    if (!isAdmin && !existingQuestion.getCreatedBy().equals(userId)) {
        throw new ForbiddenException("You don't have permission to update this question");
    }

    // If changing group, validate access
    if (request.getGroupId() != null &&
        !request.getGroupId().equals(existingQuestion.getGroupId())) {
        validateGroupAccess(request.getGroupId(), userId);
    }

    // Validate visibility rules
    validateVisibility(request.getGroupId(), request.getVisibility());

    // Update fields
    if (existingQuestion.getStatus() == QuestionStatus.DRAFT) {
        // Full update allowed for DRAFT
        // ... existing update logic ...
        existingQuestion.setGroupId(request.getGroupId());
        existingQuestion.setVisibility(request.getVisibility());
    } else {
        // Only status changes for non-DRAFT
        validateStatusTransition(existingQuestion.getStatus(), request.getStatus(), isAdmin);
        existingQuestion.setStatus(request.getStatus());
    }

    Question updated = questionRepository.save(existingQuestion);
    return questionMapper.toDTO(updated);
}

// Add new methods

/**
 * Validate user has access to group
 */
private void validateGroupAccess(Long groupId, Long userId) {
    if (groupId == null) {
        return; // Public question, no validation needed
    }

    boolean isMember = groupServiceClient.isMemberOfGroup(groupId, userId);
    if (!isMember) {
        throw new ForbiddenException("You must be a member of the group to create questions in it");
    }
}

/**
 * Validate visibility rules
 */
private void validateVisibility(Long groupId, QuestionVisibility visibility) {
    if (groupId == null && visibility == QuestionVisibility.PRIVATE) {
        throw new ValidationException("Public questions (no group) cannot be PRIVATE");
    }
}

/**
 * Get questions accessible by user
 */
public List<QuestionDTO> getAccessibleQuestions(Long userId, QuestionStatus status) {
    // Get user's group IDs
    List<Long> userGroupIds = groupServiceClient.getUserGroupIds(userId);

    // Get accessible questions
    List<Question> questions = questionRepository.findAccessibleQuestions(status, userGroupIds);

    return questions.stream()
            .map(this::toDTOWithGroupName)
            .collect(Collectors.toList());
}

/**
 * Get questions by group
 */
public List<QuestionDTO> getQuestionsByGroup(Long groupId, Long userId) {
    // Validate user has access to group
    boolean isMember = groupServiceClient.isMemberOfGroup(groupId, userId);
    if (!isMember) {
        throw new ForbiddenException("You don't have access to this group's questions");
    }

    List<Question> questions = questionRepository.findByGroupIdAndStatus(
        groupId,
        QuestionStatus.VALIDATED
    );

    return questions.stream()
            .map(this::toDTOWithGroupName)
            .collect(Collectors.toList());
}

/**
 * Convert to DTO with group name
 */
private QuestionDTO toDTOWithGroupName(Question question) {
    QuestionDTO dto = questionMapper.toDTO(question);

    if (question.getGroupId() != null) {
        String groupName = groupServiceClient.getGroupName(question.getGroupId());
        dto.setGroupName(groupName);
    }

    return dto;
}

/**
 * Validate question access for quiz/play services
 */
public boolean canUserAccessQuestion(Long questionId, Long userId) {
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new QuestionNotFoundException("Question not found"));

    // Public question - everyone can access
    if (question.isPublicQuestion()) {
        return true;
    }

    // Public visibility - everyone can access
    if (question.isPublicVisibility()) {
        return true;
    }

    // Private group question - check membership
    return groupServiceClient.isMemberOfGroup(question.getGroupId(), userId);
}
```

---

## 6. Controller Changes

### 6.1 Update QuestionController

**File**: `src/main/java/com/quizz/question/controller/QuestionController.java`

```java
// Update list questions endpoint to support group filtering
@GetMapping
public ResponseEntity<List<QuestionDTO>> listQuestions(
        @RequestParam(required = false) List<QuestionStatus> statuses,
        @RequestParam(required = false) List<String> categories,
        @RequestParam(required = false) Long groupId,
        @RequestParam(required = false) QuestionVisibility visibility,
        @AuthenticationPrincipal JwtAuthentication authentication) {

    Long userId = authentication.getUserId();

    if (groupId != null) {
        // Get questions for specific group
        return ResponseEntity.ok(questionService.getQuestionsByGroup(groupId, userId));
    } else {
        // Get all accessible questions
        // This needs to be updated to use new filtering logic
        List<QuestionDTO> questions = questionService.getAccessibleQuestions(
            userId,
            statuses != null && !statuses.isEmpty() ? statuses.get(0) : null
        );
        return ResponseEntity.ok(questions);
    }
}

// Add new endpoint for validating question access
@GetMapping("/{id}/validate-access")
public ResponseEntity<QuestionAccessResponse> validateAccess(
        @PathVariable Long id,
        @AuthenticationPrincipal JwtAuthentication authentication) {

    Long userId = authentication.getUserId();
    boolean hasAccess = questionService.canUserAccessQuestion(id, userId);

    return ResponseEntity.ok(new QuestionAccessResponse(hasAccess));
}

// Response DTO
public static class QuestionAccessResponse {
    private boolean hasAccess;

    public QuestionAccessResponse(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public boolean isHasAccess() { return hasAccess; }
    public void setHasAccess(boolean hasAccess) { this.hasAccess = hasAccess; }
}
```

---

## 7. Configuration Changes

### 7.1 Add RestTemplate Bean

**File**: `src/main/java/com/quizz/question/config/RestTemplateConfig.java`

```java
package com.quizz.question.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 7.2 Update application.properties

**File**: `src/main/resources/application.properties`

```properties
# Group Service URL
group.service.url=${GROUP_SERVICE_URL:http://localhost:8083}
```

---

## 8. Mapper Changes

### 8.1 Update QuestionMapper

**File**: `src/main/java/com/quizz/question/mapper/QuestionMapper.java`

```java
// Update toEntity method
public Question toEntity(CreateQuestionRequest request) {
    Question question = new Question();
    // ... existing mappings ...
    question.setGroupId(request.getGroupId());
    question.setVisibility(request.getVisibility() != null ?
        request.getVisibility() : QuestionVisibility.PUBLIC);
    return question;
}

// Update toDTO method
public QuestionDTO toDTO(Question question) {
    QuestionDTO dto = new QuestionDTO();
    // ... existing mappings ...
    dto.setGroupId(question.getGroupId());
    dto.setVisibility(question.getVisibility());
    return dto;
}

// Update fromUpdateRequest method
public void updateEntity(Question question, UpdateQuestionRequest request) {
    // ... existing mappings ...
    if (question.getStatus() == QuestionStatus.DRAFT) {
        question.setGroupId(request.getGroupId());
        question.setVisibility(request.getVisibility());
    }
}
```

---

## 9. Testing Changes

### 9.1 Update Unit Tests

**File**: `src/test/java/com/quizz/question/service/QuestionServiceImplTest.java`

```java
// Add GroupServiceClient mock
@Mock
private GroupServiceClient groupServiceClient;

// Add tests for group functionality

@Test
@DisplayName("Should create question in group when user is member")
void shouldCreateQuestionInGroupWhenUserIsMember() {
    // Arrange
    CreateQuestionRequest request = createValidRequest();
    request.setGroupId(1L);
    request.setVisibility(QuestionVisibility.PRIVATE);

    when(groupServiceClient.isMemberOfGroup(1L, 1L)).thenReturn(true);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
    when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    QuestionDTO result = questionService.createQuestion(request, 1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getGroupId()).isEqualTo(1L);
    assertThat(result.getVisibility()).isEqualTo(QuestionVisibility.PRIVATE);
}

@Test
@DisplayName("Should throw exception when creating question in group user is not member of")
void shouldThrowExceptionWhenCreatingQuestionInGroupUserNotMember() {
    // Arrange
    CreateQuestionRequest request = createValidRequest();
    request.setGroupId(1L);

    when(groupServiceClient.isMemberOfGroup(1L, 1L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> questionService.createQuestion(request, 1L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("must be a member of the group");
}

@Test
@DisplayName("Should throw exception when making public question private")
void shouldThrowExceptionWhenMakingPublicQuestionPrivate() {
    // Arrange
    CreateQuestionRequest request = createValidRequest();
    request.setGroupId(null);  // Public question
    request.setVisibility(QuestionVisibility.PRIVATE);  // But private visibility

    // Act & Assert
    assertThatThrownBy(() -> questionService.createQuestion(request, 1L))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Public questions (no group) cannot be PRIVATE");
}

@Test
@DisplayName("Should return only accessible questions for user")
void shouldReturnOnlyAccessibleQuestionsForUser() {
    // Arrange
    List<Long> userGroupIds = List.of(1L, 2L);
    when(groupServiceClient.getUserGroupIds(1L)).thenReturn(userGroupIds);

    List<Question> accessibleQuestions = List.of(question);
    when(questionRepository.findAccessibleQuestions(QuestionStatus.VALIDATED, userGroupIds))
            .thenReturn(accessibleQuestions);

    // Act
    List<QuestionDTO> result = questionService.getAccessibleQuestions(1L, QuestionStatus.VALIDATED);

    // Assert
    assertThat(result).hasSize(1);
}
```

### 9.2 Update Integration Tests

**File**: `src/test/java/com/quizz/question/integration/QuestionAPIIntegrationTest.java`

```java
// Add tests for group-based question creation and filtering

@Test
@DisplayName("Should create question with group")
@WithMockJwtUser(userId = 1L, roles = {"USER"})
void shouldCreateQuestionWithGroup() throws Exception {
    // Mock group service response
    // (You'll need WireMock or similar to mock external service)

    String requestBody = """
        {
            "text": "What is a group question?",
            "type": "MULTIPLE_CHOICE",
            "category": "Science",
            "difficulty": "Easy",
            "points": 10,
            "groupId": 1,
            "visibility": "PRIVATE",
            "answers": [
                {"text": "Answer 1", "isCorrect": true},
                {"text": "Answer 2", "isCorrect": false}
            ]
        }
        """;

    mockMvc.perform(post("/api/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.groupId").value(1))
            .andExpect(jsonPath("$.visibility").value("private"));
}

@Test
@DisplayName("Should filter questions by group")
@WithMockJwtUser(userId = 1L, roles = {"USER"})
void shouldFilterQuestionsByGroup() throws Exception {
    // Create group question
    // ...

    mockMvc.perform(get("/api/questions")
            .param("groupId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].groupId").value(everyItem(is(1))));
}
```

---

## 10. API Documentation Updates

### 10.1 Update Swagger Annotations

Add to controllers:

```java
@Operation(summary = "Create a new question",
           description = "Creates a question. If groupId is provided, user must be a member of that group.")
@ApiResponse(responseCode = "201", description = "Question created successfully")
@ApiResponse(responseCode = "403", description = "User is not a member of the specified group")
```

---

## 11. Error Handling

### 11.1 Add New Exception

**File**: `src/main/java/com/quizz/question/exception/GroupAccessException.java`

```java
package com.quizz.question.exception;

public class GroupAccessException extends RuntimeException {
    public GroupAccessException(String message) {
        super(message);
    }
}
```

### 11.2 Update GlobalExceptionHandler

```java
@ExceptionHandler(GroupAccessException.class)
public ResponseEntity<ErrorResponse> handleGroupAccessException(
        GroupAccessException ex,
        HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            request.getRequestURI()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
}
```

---

## 12. Implementation Checklist

### Database
- [ ] Create migration file 010-add-group-support-to-questions.yaml
- [ ] Update db.changelog-master.yaml
- [ ] Test migration on local database
- [ ] Verify rollback works

### Models
- [ ] Create QuestionVisibility enum
- [ ] Update Question entity
- [ ] Update CreateQuestionRequest DTO
- [ ] Update UpdateQuestionRequest DTO
- [ ] Update QuestionDTO

### Repository
- [ ] Add new query methods to QuestionRepository
- [ ] Test custom queries

### Service Layer
- [ ] Create GroupServiceClient
- [ ] Update QuestionServiceImpl with validation
- [ ] Add group access validation
- [ ] Add visibility validation
- [ ] Implement getAccessibleQuestions
- [ ] Implement getQuestionsByGroup
- [ ] Implement canUserAccessQuestion

### Controllers
- [ ] Update QuestionController list endpoint
- [ ] Add validate-access endpoint
- [ ] Update Swagger documentation

### Configuration
- [ ] Create RestTemplateConfig
- [ ] Update application.properties
- [ ] Add group.service.url property

### Mappers
- [ ] Update QuestionMapper toEntity
- [ ] Update QuestionMapper toDTO
- [ ] Update QuestionMapper fromUpdateRequest

### Testing
- [ ] Update unit tests for QuestionServiceImpl
- [ ] Add group-specific test cases
- [ ] Update integration tests
- [ ] Add WireMock for Group Service mocking
- [ ] Verify test coverage remains above 85%

### Documentation
- [ ] Update API_DOCUMENTATION.md
- [ ] Update ARCHITECTURE.md
- [ ] Update README.md

---

## 13. Testing Strategy

### Unit Tests
```bash
mvn test -Dtest=QuestionServiceImplTest
```

### Integration Tests
```bash
mvn test -Dtest=QuestionAPIIntegrationTest
```

### Manual Testing Scenarios

1. **Create public question**:
   - groupId = null
   - visibility = PUBLIC
   - Should succeed for any user

2. **Create group question (user is member)**:
   - groupId = 1
   - visibility = PRIVATE
   - Should succeed if user is member

3. **Create group question (user not member)**:
   - groupId = 1
   - visibility = PRIVATE
   - Should fail with 403

4. **Invalid visibility**:
   - groupId = null
   - visibility = PRIVATE
   - Should fail with 400

5. **Filter by group**:
   - GET /api/questions?groupId=1
   - Should return only group 1 questions user can access

---

## 14. Rollout Plan

1. **Phase 1**: Database migration
   - Deploy migration
   - Verify all existing questions have visibility=PUBLIC and groupId=null

2. **Phase 2**: Backend code
   - Deploy updated service
   - Monitor logs for errors
   - Verify backward compatibility

3. **Phase 3**: API testing
   - Test all endpoints
   - Verify group integration
   - Load testing

4. **Phase 4**: Documentation
   - Update API docs
   - Notify frontend team

---

## 15. Backward Compatibility

- All existing questions will have `groupId = NULL` and `visibility = PUBLIC`
- Existing API calls without group parameters will continue to work
- Default visibility is PUBLIC
- No breaking changes to existing endpoints

---

## 16. Performance Considerations

- **Indexes**: Added indexes on (group_id, visibility, status) for fast filtering
- **Caching**: Consider caching group membership checks in Redis
- **N+1 Queries**: Batch group name lookups when listing many questions
- **Connection Pooling**: Ensure RestTemplate uses connection pooling for Group Service calls

---

## 17. Security Considerations

- Always validate group membership before allowing operations
- Never trust client-provided groupId without verification
- Log all group-based access attempts for audit
- Rate limit calls to Group Service to prevent abuse

---

**Estimated Implementation Time**: 2-3 days
**Complexity**: Medium
**Priority**: High (Required for Phase 1)
