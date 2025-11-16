# Quiz Play System - Comprehensive Architecture

**Target Scale:** 100,000 users, 100,000 questions

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Microservices Breakdown](#microservices-breakdown)
4. [Data Models](#data-models)
5. [API Specifications](#api-specifications)
6. [Scalability & Performance](#scalability--performance)
7. [Security & Permissions](#security--permissions)
8. [Implementation Roadmap](#implementation-roadmap)

---

## Overview

### System Capabilities

**Quiz Playing Modes:**
1. **Random Mode**: Answer random validated questions (never answered before)
2. **Quiz Mode**: Take structured quizzes created by users/groups

**Group System:**
- Application admins create groups
- Groups have designated group admins (users)
- Groups can be **public** or **private**
- Private group questions → Only usable in private quizzes
- Public group questions → Usable anywhere

**User Features:**
- Personal dashboard with statistics
- KPIs: correct/wrong answers, accuracy, points, streaks
- Leaderboards (global, group, category)
- Achievements and badges
- Answer history and progress tracking

### Architecture Principles

- **Microservices**: Independently deployable services
- **Database per Service**: No shared databases
- **Event-Driven**: Async communication for non-critical operations
- **Scalable**: Support 100k users and 100k questions
- **Secure**: Role-based access control (RBAC)

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                          FRONTEND (React)                            │
│  Dashboard │ Quiz Play │ Quiz Builder │ Groups │ Leaderboard │ Admin│
└────────────┬─────────────────────────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (8080)                             │
│  - Routing  - Load Balancing  - Rate Limiting  - CORS  - Auth      │
└───┬────┬────┬────┬────┬────┬────────────────────────────────────────┘
    │    │    │    │    │    │
    ▼    ▼    ▼    ▼    ▼    ▼
┌────────┐ ┌──────────┐ ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌──────────┐
│  Auth  │ │ Question │ │  Group  │ │   Quiz   │ │  Play   │ │ Profile  │
│ Service│ │ Service  │ │ Service │ │ Service  │ │ Service │ │ Service  │
│ (8081) │ │ (8082)   │ │ (8083)  │ │ (8084)   │ │ (8085)  │ │ (8086)   │
└───┬────┘ └────┬─────┘ └────┬────┘ └────┬─────┘ └────┬────┘ └────┬─────┘
    │           │             │           │            │           │
    │           │             │           └────────────┴───────────┤
    │           │             │                                    │
    ▼           ▼             ▼             ▼          ▼           ▼
┌────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│   H2   │ │PostgreSQL│ │PostgreSQL│ │PostgreSQL│ │PostgreSQL│ │PostgreSQL│
│ auth_db│ │questions │ │ groups_db│ │ quizzes  │ │play_db   │ │profiles  │
└────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
                              │             │            │           │
                              └─────────────┴────────────┴───────────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │   RabbitMQ     │
                                    │  Event Bus     │
                                    │  (Optional)    │
                                    └────────────────┘
                                             │
                              ┌──────────────┼──────────────┐
                              ▼              ▼              ▼
                    QuestionAnswered  SessionCompleted  AchievementUnlocked
                         Events           Events            Events

┌────────────────────────────────────────────────────────────────────┐
│                         CACHING LAYER (Redis)                       │
│  - Leaderboards  - Hot Questions  - User Sessions  - Statistics    │
└────────────────────────────────────────────────────────────────────┘
```

---

## Microservices Breakdown

### 1. Auth Service (Existing - Port 8081)

**No Changes Required**

Responsibilities:
- User authentication and registration
- JWT token generation (httpOnly cookies)
- User roles: USER, ADMIN, GROUP_ADMIN

### 2. Question Service (Extended - Port 8082)

**Current + New Features**

**Additions:**
- Group association for questions
- Public/Private visibility
- Group-based permissions

**New Fields:**
```java
Question {
  // ... existing fields
  Long groupId;              // NULL = public question
  QuestionVisibility visibility; // PUBLIC, PRIVATE
}

enum QuestionVisibility {
  PUBLIC,   // Anyone can use in quizzes
  PRIVATE   // Only group members can use
}
```

**New Endpoints:**
```
GET    /api/questions?groupId={id}&visibility=PUBLIC
POST   /api/questions (with optional groupId)
```

**Business Rules:**
- If groupId is null → question is public (anyone can use)
- If groupId is set → question belongs to group
  - If visibility=PUBLIC → anyone can use
  - If visibility=PRIVATE → only group members can use in quizzes

### 3. Group Service (NEW - Port 8083)

**Responsibilities:**
- Group CRUD operations
- Group membership management
- Group admin assignment
- Public/Private group management

**Database: `groups_db`**

**Data Model:**
```java
Group {
  Long id;
  String name;                    // unique
  String description;
  GroupType type;                 // PUBLIC, PRIVATE
  Long createdBy;                 // Application admin who created it
  Long groupAdminId;              // Designated user who is group admin
  String avatarUrl;
  Integer memberCount;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

GroupMember {
  Long id;
  Long groupId;
  Long userId;
  MemberRole role;                // MEMBER, ADMIN
  MemberStatus status;            // ACTIVE, PENDING, REMOVED
  LocalDateTime joinedAt;
  LocalDateTime removedAt;
}

GroupInvitation {
  Long id;
  Long groupId;
  Long invitedBy;
  String inviteeEmail;            // Can invite by email
  Long inviteeUserId;             // Or by userId
  InvitationStatus status;        // PENDING, ACCEPTED, DECLINED, EXPIRED
  String token;                   // Unique invitation token
  LocalDateTime expiresAt;
  LocalDateTime respondedAt;
}

enum GroupType {
  PUBLIC,    // Anyone can join
  PRIVATE    // Invitation only
}

enum MemberRole {
  MEMBER,    // Regular member
  ADMIN      // Can manage group (not app admin)
}
```

**Key Endpoints:**
```
# Group Management (App Admins Only)
POST   /api/groups                           - Create group
PUT    /api/groups/{id}                      - Update group
DELETE /api/groups/{id}                      - Delete group
PUT    /api/groups/{id}/admin                - Assign group admin

# Group Admin Operations (Group Admin Only)
POST   /api/groups/{id}/invite               - Invite users
DELETE /api/groups/{id}/members/{userId}     - Remove member
PUT    /api/groups/{id}/members/{userId}/role - Change member role

# Public Operations
GET    /api/groups                           - List public groups
GET    /api/groups/{id}                      - Get group details
POST   /api/groups/{id}/join                 - Join public group
POST   /api/groups/invitations/{token}/accept - Accept invitation
GET    /api/groups/my-groups                 - Get user's groups
GET    /api/groups/{id}/members              - Get group members
```

**Business Rules:**
- Only app admins (ROLE_ADMIN) can create groups
- App admin designates one user as group admin
- Group admin can invite/remove members, manage group
- Public groups: Anyone can join
- Private groups: Invitation-only
- User can be in multiple groups

**Scalability Considerations:**
- Index on userId for member lookups
- Cache group membership for permission checks
- Paginate member lists for large groups

### 4. Quiz Service (NEW - Port 8084)

**Responsibilities:**
- Quiz creation and management
- Quiz question association
- Quiz templates and categories
- Quiz visibility (public/private/group)

**Database: `quizzes_db`**

**Data Model:**
```java
Quiz {
  Long id;
  String title;
  String description;
  Long createdBy;                 // User who created it
  Long groupId;                   // NULL = public, not null = group quiz
  QuizVisibility visibility;      // PUBLIC, PRIVATE
  QuizDifficulty difficulty;      // MIXED, EASY, MEDIUM, HARD
  Integer questionCount;
  Integer timeLimit;              // seconds, NULL = no limit
  Integer passingScore;           // NULL = no passing requirement
  Boolean randomizeQuestions;     // Randomize question order
  Boolean randomizeAnswers;       // Randomize answer order
  QuizStatus status;              // DRAFT, PUBLISHED, ARCHIVED
  Integer timesPlayed;
  Double averageScore;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  LocalDateTime publishedAt;
}

QuizQuestion {
  Long id;
  Long quizId;
  Long questionId;                // Reference to Question Service
  Integer displayOrder;           // Order in quiz
  Integer points;                 // Override question points (optional)
}

enum QuizVisibility {
  PUBLIC,                         // Anyone can play
  PRIVATE                         // Only group members (if group quiz)
}

enum QuizStatus {
  DRAFT,                          // Being created
  PUBLISHED,                      // Available to play
  ARCHIVED                        // No longer available
}
```

**Key Endpoints:**
```
# Quiz Management
POST   /api/quizzes                          - Create quiz
PUT    /api/quizzes/{id}                     - Update quiz
DELETE /api/quizzes/{id}                     - Delete quiz
POST   /api/quizzes/{id}/questions           - Add question to quiz
DELETE /api/quizzes/{id}/questions/{qId}     - Remove question
PUT    /api/quizzes/{id}/publish             - Publish quiz
PUT    /api/quizzes/{id}/archive             - Archive quiz

# Quiz Discovery
GET    /api/quizzes                          - List public quizzes
GET    /api/quizzes/{id}                     - Get quiz details
GET    /api/quizzes/group/{groupId}          - List group quizzes
GET    /api/quizzes/my-quizzes               - User's created quizzes
GET    /api/quizzes/popular                  - Most played quizzes
GET    /api/quizzes/categories/{categoryId}  - Quizzes by category
```

**Business Rules:**
- Users can create quizzes from:
  - Public questions (always allowed)
  - Group questions (if member and question visibility allows)
- Quiz visibility rules:
  - PUBLIC quiz (groupId=null): Anyone can play
  - Group quiz + PUBLIC visibility: Anyone can play
  - Group quiz + PRIVATE visibility: Only group members
- Creator can edit DRAFT quizzes
- Published quizzes are immutable (must create new version)
- Questions are validated at quiz creation:
  - All must be VALIDATED status
  - Respect group/visibility permissions

**Scalability Considerations:**
- Index quizzes by groupId, createdBy, status
- Cache popular quizzes
- Pagination for all list endpoints
- Denormalize questionCount for performance

### 5. Quiz Play Service (NEW - Port 8085)

**Responsibilities:**
- Play quizzes (structured)
- Play random questions (practice mode)
- Record user answers
- Calculate scores
- Track history (which questions answered)
- Session management

**Database: `play_db`**

**Data Model:**
```java
PlaySession {
  Long id;
  Long userId;
  PlayMode mode;                  // RANDOM, QUIZ
  Long quizId;                    // NULL if RANDOM mode
  SessionStatus status;           // IN_PROGRESS, COMPLETED, ABANDONED

  Integer totalQuestions;
  Integer questionsAnswered;
  Integer correctAnswers;
  Integer wrongAnswers;
  Integer totalPoints;
  Integer timeSpentSeconds;
  Double scorePercentage;

  LocalDateTime startedAt;
  LocalDateTime completedAt;
  LocalDateTime lastActivityAt;
}

UserAnswer {
  Long id;
  Long userId;
  Long sessionId;                 // Can be NULL for standalone answers
  Long questionId;
  Long selectedAnswerId;          // User's choice
  Boolean isCorrect;
  Integer pointsEarned;
  Integer timeSpentSeconds;
  LocalDateTime answeredAt;

  // Metadata
  Long quizId;                    // NULL if random mode
  String categoryName;
  String difficultyLevel;

  UNIQUE(user_id, question_id, session_id)
}

UserQuestionHistory {
  Long id;
  Long userId;
  Long questionId;
  Integer timesAnswered;
  Integer timesCorrect;
  Integer timesWrong;
  Integer totalPointsEarned;
  LocalDateTime firstAnsweredAt;
  LocalDateTime lastAnsweredAt;

  UNIQUE(user_id, question_id)
}

enum PlayMode {
  RANDOM,                         // Practice with random questions
  QUIZ                            // Structured quiz
}

enum SessionStatus {
  IN_PROGRESS,
  COMPLETED,
  ABANDONED                       // Left incomplete
}
```

**Key Endpoints:**
```
# Random Play Mode
POST   /api/play/random/start                - Start random practice
GET    /api/play/random/next-question        - Get next unanswered question
POST   /api/play/random/answer               - Submit answer
GET    /api/play/random/available-count      - Count unanswered questions

# Quiz Play Mode
POST   /api/play/quiz/{quizId}/start         - Start quiz session
GET    /api/play/sessions/{sessionId}/question/{order} - Get question
POST   /api/play/sessions/{sessionId}/answer - Submit answer
POST   /api/play/sessions/{sessionId}/complete - Complete session
GET    /api/play/sessions/{sessionId}        - Get session details
GET    /api/play/sessions/{sessionId}/results - Get results

# History
GET    /api/play/history                     - User answer history
GET    /api/play/sessions                    - User sessions
GET    /api/play/statistics                  - Quick stats
```

**Business Logic:**

**Random Mode:**
1. User starts random practice
2. System fetches random VALIDATED question that:
   - User has never answered, OR
   - User answered incorrectly (retry option)
   - Respects user's group memberships (can see group questions)
3. User submits answer
4. System validates answer, calculates points
5. Updates UserQuestionHistory
6. Returns next question

**Quiz Mode:**
1. User starts quiz (validates permissions)
2. Creates PlaySession with quiz questions
3. User answers questions in order (or random if configured)
4. Each answer validated and scored
5. Session completed when all answered or time expires
6. Final score calculated and stored

**Permission Validation:**
- Check quiz visibility
- Check group membership (if private quiz)
- Validate question access (public or user's groups)

**Scoring Algorithm:**
```java
int calculatePoints(Question question, boolean isCorrect, int timeSpent) {
  if (!isCorrect) return 0;

  int basePoints = question.getPoints();
  double difficultyMultiplier = question.getDifficultyLevel().getPointsMultiplier();

  // Time bonus (optional): faster = more points
  double timeBonus = 1.0;
  if (timeLimit > 0) {
    double timeRatio = (double) timeSpent / timeLimit;
    if (timeRatio < 0.5) timeBonus = 1.2;      // Very fast
    else if (timeRatio < 0.75) timeBonus = 1.1; // Fast
  }

  return (int) (basePoints * difficultyMultiplier * timeBonus);
}
```

**Scalability Considerations:**
- Index on (userId, questionId) for history lookup
- Index on (userId, answeredAt) for recent activity
- Cache unanswered question IDs per user (Redis)
- Batch answer validation for quiz completion
- Archive old sessions periodically

### 6. User Profile Service (NEW - Port 8086)

**Responsibilities:**
- User profile management
- Statistics aggregation
- Leaderboards
- Achievements
- KPI calculations

**Database: `profiles_db`**

**Data Model:**
```java
UserProfile {
  Long id;
  Long userId;
  String displayName;
  String bio;
  String avatarUrl;
  String location;

  // Overall Statistics
  Integer totalQuestionsAnswered;
  Integer totalCorrectAnswers;
  Integer totalWrongAnswers;
  Double accuracyPercentage;
  Integer totalPoints;

  // Streaks
  Integer currentStreak;
  Integer longestStreak;
  LocalDateTime lastPlayedAt;

  // Rankings
  Integer globalRank;

  // Metadata
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
}

UserCategoryStats {
  Long id;
  Long userId;
  Long categoryId;
  String categoryName;            // Denormalized
  Integer questionsAnswered;
  Integer correctAnswers;
  Integer wrongAnswers;
  Double accuracyPercentage;
  Integer totalPoints;

  UNIQUE(user_id, category_id)
}

UserDifficultyStats {
  Long id;
  Long userId;
  Long difficultyLevelId;
  String difficultyName;          // Denormalized
  Integer questionsAnswered;
  Integer correctAnswers;
  Integer wrongAnswers;
  Double accuracyPercentage;
  Integer totalPoints;

  UNIQUE(user_id, difficulty_level_id)
}

UserGroupStats {
  Long id;
  Long userId;
  Long groupId;
  String groupName;               // Denormalized
  Integer questionsAnswered;
  Integer quizzesCompleted;
  Integer totalPoints;
  Integer groupRank;

  UNIQUE(user_id, group_id)
}

Achievement {
  Long id;
  String code;                    // FIRST_ANSWER, STREAK_10, PERFECT_SESSION
  String name;
  String description;
  String iconUrl;
  String category;                // MILESTONE, STREAK, ACCURACY, SPEED
  AchievementTier tier;           // BRONZE, SILVER, GOLD, PLATINUM

  // Unlock criteria (JSON or separate table)
  String criteriaType;            // TOTAL_QUESTIONS, STREAK, ACCURACY, POINTS
  Integer criteriaValue;
}

UserAchievement {
  Long id;
  Long userId;
  Long achievementId;
  LocalDateTime unlockedAt;

  UNIQUE(user_id, achievement_id)
}

Leaderboard {
  Long id;
  LeaderboardType type;           // GLOBAL, GROUP, CATEGORY
  LeaderboardPeriod period;       // ALL_TIME, MONTHLY, WEEKLY
  Long referenceId;               // groupId or categoryId (NULL for global)
  LocalDateTime periodStart;
  LocalDateTime periodEnd;
}

LeaderboardEntry {
  Long id;
  Long leaderboardId;
  Long userId;
  String displayName;             // Denormalized
  String avatarUrl;               // Denormalized
  Integer totalPoints;
  Integer questionsAnswered;
  Integer correctAnswers;
  Double accuracyPercentage;
  Integer rank;

  UNIQUE(leaderboard_id, user_id)
}

enum AchievementTier {
  BRONZE, SILVER, GOLD, PLATINUM
}

enum LeaderboardType {
  GLOBAL, GROUP, CATEGORY
}

enum LeaderboardPeriod {
  ALL_TIME, MONTHLY, WEEKLY, DAILY
}
```

**Key Endpoints:**
```
# Profile Management
GET    /api/users/profile                    - Get user profile
PUT    /api/users/profile                    - Update profile
GET    /api/users/{userId}/profile           - Get other user's profile

# Statistics
GET    /api/users/statistics                 - Overall stats
GET    /api/users/statistics/categories      - Stats by category
GET    /api/users/statistics/difficulties    - Stats by difficulty
GET    /api/users/statistics/groups          - Stats by group
GET    /api/users/statistics/trends          - Performance trends

# Achievements
GET    /api/users/achievements               - User's achievements
GET    /api/users/achievements/progress      - Progress toward locked achievements
GET    /api/achievements                     - All available achievements

# Leaderboards
GET    /api/leaderboards/global              - Global leaderboard
GET    /api/leaderboards/group/{groupId}     - Group leaderboard
GET    /api/leaderboards/category/{categoryId} - Category leaderboard
GET    /api/leaderboards/{type}?period=weekly - Time-based leaderboard
```

**Business Logic:**

**Statistics Aggregation:**
- Listen to QuestionAnsweredEvent from Play Service
- Update UserProfile statistics in real-time
- Update category/difficulty/group stats
- Recalculate accuracy percentages
- Update streaks

**Achievement Checking:**
```java
// Check achievements after each answer
public void checkAchievements(Long userId, UserAnswer answer) {
  UserProfile profile = getProfile(userId);

  // First answer achievement
  if (profile.getTotalQuestionsAnswered() == 1) {
    unlockAchievement(userId, "FIRST_ANSWER");
  }

  // Streak achievements
  if (profile.getCurrentStreak() == 10) {
    unlockAchievement(userId, "STREAK_10");
  }

  // Accuracy achievements
  if (profile.getAccuracyPercentage() >= 90.0
      && profile.getTotalQuestionsAnswered() >= 100) {
    unlockAchievement(userId, "ACCURACY_MASTER");
  }

  // Points milestones
  if (profile.getTotalPoints() >= 10000) {
    unlockAchievement(userId, "POINTS_10K");
  }
}
```

**Leaderboard Generation:**
```java
// Scheduled job (every hour for weekly, daily at midnight for all-time)
@Scheduled(cron = "0 0 * * * *") // Every hour
public void updateWeeklyLeaderboards() {
  List<LeaderboardType> types = Arrays.asList(GLOBAL, GROUP, CATEGORY);

  for (LeaderboardType type : types) {
    updateLeaderboard(type, WEEKLY);
  }
}

private void updateLeaderboard(LeaderboardType type, LeaderboardPeriod period) {
  // Calculate top 100 users
  // Use Redis for caching
  // Update database
}
```

**Streak Calculation:**
```java
public int calculateCurrentStreak(Long userId) {
  List<UserAnswer> recentAnswers = getRecentAnswers(userId, 100);

  int streak = 0;
  for (UserAnswer answer : recentAnswers) {
    if (answer.isCorrect()) {
      streak++;
    } else {
      break; // Streak broken
    }
  }

  return streak;
}
```

**Scalability Considerations:**
- Cache leaderboards in Redis (TTL: 1 hour)
- Denormalize frequently accessed data
- Use materialized views for complex aggregations
- Batch process statistics updates
- Index heavily queried fields (userId, globalRank)

---

## Data Models Summary

### Database Schema Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    Questions    │───────│  QuizQuestions  │───────│     Quizzes     │
│                 │       │                 │       │                 │
│  + groupId      │       │  quizId         │       │  + groupId      │
│  + visibility   │       │  questionId     │       │  + visibility   │
└────────┬────────┘       └─────────────────┘       └────────┬────────┘
         │                                                    │
         │                                                    │
         ▼                                                    ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     Groups      │───────│  GroupMembers   │       │  PlaySessions   │
│                 │       │                 │       │                 │
│  + type         │       │  groupId        │       │  + quizId       │
│  + groupAdminId │       │  userId         │       │  + mode         │
└─────────────────┘       │  + role         │       └────────┬────────┘
                          └─────────────────┘                │
                                                              │
                                                              ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│  UserProfiles   │───────│  UserAnswers    │───────│UserQuestionHist │
│                 │       │                 │       │                 │
│  + globalRank   │       │  + sessionId    │       │  + timesAnswered│
│  + totalPoints  │       │  + questionId   │       │  + timesCorrect │
└────────┬────────┘       │  + isCorrect    │       └─────────────────┘
         │                └─────────────────┘
         │
         ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ UserAchievements│───────│  Achievements   │       │  Leaderboards   │
│                 │       │                 │       │                 │
│  + achievementId│       │  + code         │       │  + type         │
│  + unlockedAt   │       │  + tier         │       │  + period       │
└─────────────────┘       └─────────────────┘       └────────┬────────┘
                                                              │
                                                              ▼
                                                     ┌─────────────────┐
                                                     │LeaderboardEntry │
                                                     │                 │
                                                     │  + rank         │
                                                     │  + totalPoints  │
                                                     └─────────────────┘
```

---

## API Specifications

### Service Communication

**Synchronous REST Calls:**

```
Play Service → Question Service:
  GET /api/questions/{id}                     - Validate question
  GET /api/questions/random?excludeIds=...    - Get random question

Play Service → Quiz Service:
  GET /api/quizzes/{id}/questions             - Get quiz questions

Play Service → Group Service:
  GET /api/groups/{id}/members/{userId}       - Check membership

Profile Service → Play Service:
  GET /api/play/user/{userId}/statistics      - Get play stats

Quiz Service → Question Service:
  GET /api/questions/{id}                     - Validate question exists

Quiz Service → Group Service:
  GET /api/groups/{id}/members/{userId}       - Check membership
```

**Asynchronous Events (RabbitMQ):**

```java
// Event: QuestionAnsweredEvent
{
  "eventType": "QUESTION_ANSWERED",
  "userId": 123,
  "questionId": 456,
  "answerId": 789,
  "isCorrect": true,
  "pointsEarned": 15,
  "categoryId": 1,
  "difficultyLevelId": 2,
  "groupId": 5,
  "quizId": 10,
  "timestamp": "2024-01-20T10:30:00Z"
}

// Event: SessionCompletedEvent
{
  "eventType": "SESSION_COMPLETED",
  "userId": 123,
  "sessionId": 999,
  "mode": "QUIZ",
  "quizId": 10,
  "totalQuestions": 20,
  "correctAnswers": 18,
  "totalPoints": 250,
  "scorePercentage": 90.0,
  "timeSpentSeconds": 600,
  "timestamp": "2024-01-20T10:45:00Z"
}

// Event: AchievementUnlockedEvent
{
  "eventType": "ACHIEVEMENT_UNLOCKED",
  "userId": 123,
  "achievementId": 5,
  "achievementCode": "STREAK_10",
  "timestamp": "2024-01-20T10:30:00Z"
}
```

**Subscribers:**
- Profile Service subscribes to all events
- Could add Notification Service for real-time alerts
- Analytics Service for business intelligence

---

## Scalability & Performance

### Target: 100k Users, 100k Questions

#### Database Optimizations

**Indexing Strategy:**

```sql
-- Question Service
CREATE INDEX idx_questions_group_visibility ON questions(group_id, visibility, status);
CREATE INDEX idx_questions_category ON questions(category_id, status);
CREATE INDEX idx_questions_difficulty ON questions(difficulty_level_id, status);

-- Group Service
CREATE INDEX idx_group_members_user ON group_members(user_id, status);
CREATE INDEX idx_group_members_group ON group_members(group_id, status);

-- Quiz Service
CREATE INDEX idx_quizzes_group ON quizzes(group_id, visibility, status);
CREATE INDEX idx_quizzes_created_by ON quizzes(created_by, status);
CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id, display_order);

-- Play Service
CREATE INDEX idx_user_answers_user ON user_answers(user_id, answered_at DESC);
CREATE INDEX idx_user_answers_question ON user_answers(user_id, question_id);
CREATE INDEX idx_play_sessions_user ON play_sessions(user_id, started_at DESC);
CREATE INDEX idx_user_question_history ON user_question_history(user_id, question_id);

-- Profile Service
CREATE INDEX idx_user_profiles_rank ON user_profiles(global_rank);
CREATE INDEX idx_user_profiles_points ON user_profiles(total_points DESC);
CREATE INDEX idx_leaderboard_entries_board ON leaderboard_entries(leaderboard_id, rank);
```

#### Caching Strategy (Redis)

```
Cache Key                           TTL         Usage
--------------------------------------------------------------------------------
leaderboard:global:weekly          1 hour      Global weekly leaderboard
leaderboard:group:{groupId}:all    1 hour      Group leaderboard
user:{userId}:answered_questions   1 day       Set of answered question IDs
user:{userId}:profile              15 min      User profile data
quiz:{quizId}:questions            1 hour      Quiz questions list
group:{groupId}:members            30 min      Group member IDs
question:random:pool               5 min       Pool of random question IDs
```

**Cache Patterns:**
```java
// Cache-Aside Pattern
public Question getQuestion(Long id) {
  String key = "question:" + id;
  Question cached = redisTemplate.opsForValue().get(key);

  if (cached != null) {
    return cached;
  }

  Question question = questionRepository.findById(id).orElseThrow();
  redisTemplate.opsForValue().set(key, question, Duration.ofMinutes(15));
  return question;
}

// Write-Through Pattern
public void updateUserProfile(UserProfile profile) {
  userProfileRepository.save(profile);
  String key = "user:" + profile.getUserId() + ":profile";
  redisTemplate.opsForValue().set(key, profile, Duration.ofMinutes(15));
}
```

#### Pagination

**All list endpoints must paginate:**
```java
@GetMapping("/api/quizzes")
public Page<QuizDTO> getQuizzes(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "20") int size,
  @RequestParam(defaultValue = "createdAt,desc") String sort
) {
  Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
  return quizService.getPublicQuizzes(pageable);
}
```

**Max page size: 100 items**

#### Database Connection Pooling

```yaml
# application.yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Adjust based on load
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### Load Testing Targets

```
Concurrent Users:     1,000
Requests/Second:      500
Average Response:     < 200ms
P95 Response:         < 500ms
P99 Response:         < 1000ms
```

#### Monitoring

**Metrics to Track:**
- Request rate per endpoint
- Response times (P50, P95, P99)
- Database query times
- Cache hit/miss rates
- Error rates
- Active user sessions
- Queue depths (RabbitMQ)

**Tools:**
- Prometheus + Grafana
- Spring Boot Actuator
- Database slow query logs
- APM (Application Performance Monitoring)

---

## Security & Permissions

### Role-Based Access Control (RBAC)

**Roles:**
```java
enum UserRole {
  USER,         // Regular user
  GROUP_ADMIN,  // Admin of one or more groups
  ADMIN         // Application admin
}
```

**Permission Matrix:**

| Action | USER | GROUP_ADMIN | ADMIN |
|--------|------|-------------|-------|
| Create Question (public) | ✅ | ✅ | ✅ |
| Create Question (in group) | ✅ (if member) | ✅ (if admin) | ✅ |
| Edit Question | ✅ (own) | ✅ (group) | ✅ (any) |
| Create Quiz (public) | ✅ | ✅ | ✅ |
| Create Quiz (group) | ✅ (if member) | ✅ (if admin) | ✅ |
| Create Group | ❌ | ❌ | ✅ |
| Manage Group | ❌ | ✅ (own groups) | ✅ |
| Invite to Group | ❌ | ✅ (own groups) | ✅ |
| Join Public Group | ✅ | ✅ | ✅ |
| Play Public Quiz | ✅ | ✅ | ✅ |
| Play Private Quiz | ✅ (if member) | ✅ | ✅ |
| View Leaderboards | ✅ | ✅ | ✅ |

### Authorization Checks

**Question Access:**
```java
public boolean canAccessQuestion(Long userId, Question question) {
  // Public questions: everyone
  if (question.getGroupId() == null) {
    return true;
  }

  // Private group questions: only members
  if (question.getVisibility() == PRIVATE) {
    return groupService.isMember(question.getGroupId(), userId);
  }

  // Public group questions: everyone
  return true;
}
```

**Quiz Access:**
```java
public boolean canPlayQuiz(Long userId, Quiz quiz) {
  // Public quiz: everyone
  if (quiz.getGroupId() == null) {
    return true;
  }

  // Private group quiz: only members
  if (quiz.getVisibility() == PRIVATE) {
    return groupService.isMember(quiz.getGroupId(), userId);
  }

  // Public group quiz: everyone
  return true;
}
```

**Group Management:**
```java
public boolean canManageGroup(Long userId, Long groupId) {
  // Check if user is app admin
  if (hasRole(userId, ADMIN)) {
    return true;
  }

  // Check if user is group admin
  GroupMember member = groupMemberRepository
    .findByGroupIdAndUserId(groupId, userId);

  return member != null && member.getRole() == MemberRole.ADMIN;
}
```

### Input Validation & Sanitization

**All services inherit from Question Service:**
- OWASP HTML Sanitizer
- XSS prevention
- SQL injection prevention (JPA)
- Input validation with Bean Validation

**Additional validations:**
```java
// Quiz Service
@NotNull
@Size(min = 3, max = 200)
@Sanitized(type = SanitizationType.TEXT)
private String title;

@Min(1)
@Max(100)
private Integer questionCount;

// Group Service
@NotNull
@Size(min = 3, max = 100)
@Pattern(regexp = "^[a-zA-Z0-9 _-]+$")
private String groupName;
```

### Rate Limiting

**API Gateway Level:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: play-service
          uri: lb://play-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10    # requests per second
                redis-rate-limiter.burstCapacity: 20
```

**Per-User Limits:**
- 10 requests/second per user
- 100 quiz sessions per day
- 1000 questions answered per day

---

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-2)

**Week 1:**
- [ ] Set up Group Service project structure
- [ ] Database schema and Liquibase migrations
- [ ] Group CRUD operations
- [ ] Group membership management
- [ ] Unit tests for Group Service

**Week 2:**
- [ ] Extend Question Service with group support
- [ ] Add visibility field to questions
- [ ] Update question creation/editing logic
- [ ] Integration tests for group-based questions
- [ ] Update Question Service documentation

**Deliverables:**
- Group Service fully functional
- Questions can be associated with groups
- Public/Private visibility working

### Phase 2: Quiz Management (Weeks 3-4)

**Week 3:**
- [ ] Set up Quiz Service project structure
- [ ] Database schema and Liquibase migrations
- [ ] Quiz CRUD operations
- [ ] Quiz-Question association
- [ ] Quiz visibility and permissions

**Week 4:**
- [ ] Quiz publishing workflow
- [ ] Quiz discovery endpoints
- [ ] Permission checks (group membership)
- [ ] Unit and integration tests
- [ ] Frontend: Quiz builder UI

**Deliverables:**
- Users can create quizzes
- Quizzes respect group/visibility rules
- Quiz management UI complete

### Phase 3: Quiz Playing (Weeks 5-6)

**Week 5:**
- [ ] Set up Play Service project structure
- [ ] Database schema and Liquibase migrations
- [ ] Random play mode implementation
- [ ] Question selection algorithm (unanswered)
- [ ] Answer validation and scoring

**Week 6:**
- [ ] Quiz play mode implementation
- [ ] Session management
- [ ] Answer history tracking
- [ ] Unit and integration tests
- [ ] Frontend: Quiz play UI

**Deliverables:**
- Random practice mode working
- Quiz play mode working
- Scoring and validation complete

### Phase 4: User Profiles & Statistics (Weeks 7-8)

**Week 7:**
- [ ] Set up Profile Service project structure
- [ ] Database schema and Liquibase migrations
- [ ] User profile CRUD
- [ ] Statistics aggregation logic
- [ ] Category/Difficulty stats

**Week 8:**
- [ ] Achievement system
- [ ] Achievement checking logic
- [ ] Leaderboard generation
- [ ] KPI calculations
- [ ] Frontend: Dashboard UI

**Deliverables:**
- User profiles with complete statistics
- Achievements unlocking automatically
- Leaderboards displaying correctly
- Dashboard with charts and KPIs

### Phase 5: Event-Driven Architecture (Week 9)

- [ ] RabbitMQ setup and configuration
- [ ] Event publishers in Play Service
- [ ] Event subscribers in Profile Service
- [ ] Event schemas and versioning
- [ ] Testing event flows
- [ ] Monitoring event queues

**Deliverables:**
- Async communication working
- Real-time statistics updates
- Resilient to service failures

### Phase 6: Performance & Scalability (Week 10)

- [ ] Redis setup and configuration
- [ ] Implement caching layer
- [ ] Database query optimization
- [ ] Add database indexes
- [ ] Load testing with JMeter/Gatling
- [ ] Performance tuning

**Deliverables:**
- System handles 100k users
- Response times < 200ms P95
- Cache hit rate > 80%

### Phase 7: Polish & Production Ready (Weeks 11-12)

**Week 11:**
- [ ] Frontend polish (animations, UX)
- [ ] Error handling improvements
- [ ] Logging and monitoring setup
- [ ] Documentation updates
- [ ] Security audit

**Week 12:**
- [ ] End-to-end testing
- [ ] User acceptance testing
- [ ] Performance testing
- [ ] Production deployment
- [ ] Monitoring dashboards

**Deliverables:**
- Production-ready system
- Complete documentation
- Monitoring and alerts configured

---

## Technology Stack Summary

### Backend Services
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.0
- **Database**: PostgreSQL 16
- **Migrations**: Liquibase
- **Cache**: Redis 7
- **Message Queue**: RabbitMQ 3.12
- **Service Discovery**: Eureka (optional)
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5, Mockito, TestContainers
- **Documentation**: SpringDoc OpenAPI

### Frontend
- **Framework**: React 18.3
- **Build Tool**: Vite 6
- **Charts**: Recharts or Chart.js
- **State Management**: React Context / Redux
- **HTTP Client**: Axios
- **Routing**: React Router

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose (dev), Kubernetes (prod)
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **APM**: Sentry or Datadog

---

## Conclusion

This architecture provides:

✅ **Scalability**: Supports 100k users and 100k questions
✅ **Flexibility**: Two play modes (random, quiz)
✅ **Group System**: Public/Private groups with admin management
✅ **Rich Statistics**: Detailed KPIs, leaderboards, achievements
✅ **Performance**: Caching, indexing, pagination
✅ **Security**: RBAC, input validation, permission checks
✅ **Maintainability**: Microservices, clean architecture, comprehensive tests
✅ **Extensibility**: Event-driven, easy to add features

Ready to start implementation with Group Service and extended Question Service!

---

**Document Version**: 1.0
**Last Updated**: 2024-01-20
**Author**: Quiz Play System Architecture Team
