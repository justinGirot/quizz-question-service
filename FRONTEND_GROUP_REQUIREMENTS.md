# Frontend Requirements - Group Integration

**Version**: 1.0
**Last Updated**: 2024-01-20

This document describes all frontend changes needed to support the Quiz Play System with group-based questions, quizzes, profiles, and leaderboards.

---

## Table of Contents

1. [Overview](#overview)
2. [New Pages](#new-pages)
3. [Updated Pages](#updated-pages)
4. [New Components](#new-components)
5. [API Integration](#api-integration)
6. [User Flows](#user-flows)
7. [UI/UX Specifications](#uiux-specifications)
8. [Implementation Checklist](#implementation-checklist)

---

## Overview

### Technology Stack

**Recommended**:
- React 18.3+ with TypeScript
- Vite 6 for build tooling
- React Router v6 for routing
- Axios for HTTP requests
- Context API or Redux for state management
- Tailwind CSS or Material-UI for styling
- React Query for server state management
- Recharts or Chart.js for visualizations

### Architecture Principles

- **Component-Based**: Reusable, composable components
- **Type-Safe**: Full TypeScript coverage
- **Responsive**: Mobile-first design
- **Accessible**: WCAG 2.1 AA compliance
- **Performance**: Code splitting, lazy loading
- **Real-time**: WebSocket support for live features (future)

---

## New Pages

### 1. Groups Page (`/groups`)

**Purpose**: Browse and join groups

**Layout**:
```
┌─────────────────────────────────────────┐
│  Header: My Groups                      │
│  [+ Create Group] (Admin only)          │
├─────────────────────────────────────────┤
│  Tabs: [All Groups] [My Groups]         │
├─────────────────────────────────────────┤
│  Search: [_____________] [Filter ▼]     │
├─────────────────────────────────────────┤
│  ┌──────────────────────────────┐       │
│  │ 🎯 Science Enthusiasts       │       │
│  │ Public • 245 members          │       │
│  │ "For science lovers..."       │       │
│  │            [Join Group]       │       │
│  └──────────────────────────────┘       │
│  ┌──────────────────────────────┐       │
│  │ 🔒 Math Masters              │       │
│  │ Private • 42 members          │       │
│  │ "Advanced mathematics..."     │       │
│  │            [Request Invite]   │       │
│  └──────────────────────────────┘       │
│  ... more groups ...                    │
└─────────────────────────────────────────┘
```

**Features**:
- List all public groups
- Show user's joined groups
- Search and filter groups
- Join public groups instantly
- Request invite for private groups
- See member count and description
- Group cards with avatars

**API Calls**:
- `GET /api/groups` - List public groups
- `GET /api/groups/my-groups` - Get user's groups
- `POST /api/groups/{id}/join` - Join group

---

### 2. Group Detail Page (`/groups/:id`)

**Purpose**: View group details and manage membership

**Layout**:
```
┌─────────────────────────────────────────┐
│  🎯 Science Enthusiasts                 │
│  Public Group • Created Jan 2024        │
│  [Leave Group] [Share] [⋮]              │
├─────────────────────────────────────────┤
│  Tabs: [About] [Questions] [Quizzes]   │
│        [Members] [Leaderboard]          │
├─────────────────────────────────────────┤
│  📊 Group Stats                         │
│  • 245 members                          │
│  • 1,234 questions                      │
│  • 56 quizzes                           │
│  • Created by: admin@example.com        │
├─────────────────────────────────────────┤
│  📝 Description                         │
│  A community for science enthusiasts... │
├─────────────────────────────────────────┤
│  Recent Activity                        │
│  • John created a new question (2h ago) │
│  • Sarah completed a quiz (3h ago)      │
└─────────────────────────────────────────┘
```

**Features**:
- Group information display
- Member list with roles (Member/Admin badges)
- Group questions feed
- Group quizzes list
- Group leaderboard
- Activity feed
- Leave group option
- Admin panel (if user is group admin)

**API Calls**:
- `GET /api/groups/{id}` - Get group details
- `GET /api/groups/{id}/members` - Get members
- `GET /api/questions?groupId={id}` - Group questions
- `GET /api/quizzes/group/{id}` - Group quizzes
- `GET /api/leaderboards/group/{id}` - Group leaderboard
- `POST /api/groups/{id}/leave` - Leave group

---

### 3. Group Admin Panel (`/groups/:id/admin`)

**Purpose**: Manage group (for group admins only)

**Layout**:
```
┌─────────────────────────────────────────┐
│  Group Management                       │
├─────────────────────────────────────────┤
│  Tabs: [Settings] [Members] [Invites]  │
├─────────────────────────────────────────┤
│  ⚙️ Group Settings                      │
│  Name: [Science Enthusiasts]            │
│  Description: [____________]            │
│  Type: ( ) Public (•) Private           │
│  Avatar: [Choose File] [Upload]         │
│               [Save Changes]            │
├─────────────────────────────────────────┤
│  👥 Member Management                   │
│  Search: [____________]                 │
│  ┌──────────────────────────────┐       │
│  │ 👤 John Doe                  │       │
│  │ john@example.com             │       │
│  │ Role: Member                 │       │
│  │ [Promote] [Remove]           │       │
│  └──────────────────────────────┘       │
├─────────────────────────────────────────┤
│  ✉️ Send Invitations                   │
│  Email: [____________]                  │
│  Message: [____________]                │
│                [Send Invite]            │
│                                         │
│  Pending Invitations (3)                │
│  • sarah@example.com (expires in 5d)   │
│  • mike@example.com (expires in 6d)    │
└─────────────────────────────────────────┘
```

**Features**:
- Edit group settings
- Invite users by email
- Manage members (promote/demote/remove)
- View pending invitations
- Group analytics

**API Calls**:
- `PUT /api/groups/{id}` - Update group
- `POST /api/groups/{id}/invite` - Invite user
- `DELETE /api/groups/{id}/members/{userId}` - Remove member
- `PUT /api/groups/{id}/members/{userId}/role` - Change role

---

### 4. Quiz Play Page (`/play/quiz/:quizId`)

**Purpose**: Take a quiz with real-time scoring

**Layout**:
```
┌─────────────────────────────────────────┐
│  Science Quiz 101                       │
│  Question 3 of 10                       │
│  ⏱️ 02:35 remaining                     │
│  Score: 25 points                       │
├─────────────────────────────────────────┤
│  What is the speed of light?            │
│                                         │
│  ( ) 299,792 km/s                       │
│  (•) 300,000 km/s ✓                     │
│  ( ) 250,000 km/s                       │
│  ( ) 150,000 km/s                       │
│                                         │
│              [Submit Answer]            │
│              [Skip Question]            │
├─────────────────────────────────────────┤
│  Progress: [■■■□□□□□□□] 30%            │
└─────────────────────────────────────────┘
```

**Features**:
- Question display with answers
- Radio buttons for single choice
- Checkboxes for multiple choice
- Text input for open questions
- Timer display
- Progress bar
- Current score display
- Submit button with validation
- Skip option (if allowed)
- Immediate feedback on submission
- Transition to next question

**API Calls**:
- `POST /api/play/quiz/{quizId}/start` - Start quiz session
- `GET /api/play/sessions/{sessionId}/question/{order}` - Get question
- `POST /api/play/sessions/{sessionId}/answer` - Submit answer
- `POST /api/play/sessions/{sessionId}/complete` - Complete quiz

---

### 5. Random Practice Page (`/play/random`)

**Purpose**: Practice with random questions

**Layout**:
```
┌─────────────────────────────────────────┐
│  Random Practice Mode                   │
│  [⚙️ Settings]                          │
├─────────────────────────────────────────┤
│  Filters:                               │
│  Category: [All Categories ▼]           │
│  Difficulty: [All Levels ▼]             │
│  [Apply Filters]                        │
├─────────────────────────────────────────┤
│  📊 Available Questions: 1,234          │
│  ✅ Answered: 56                        │
│  ⭐ Correct Rate: 85%                   │
├─────────────────────────────────────────┤
│  What is photosynthesis?                │
│  Category: Science • Difficulty: Easy   │
│                                         │
│  ( ) Process of eating                  │
│  ( ) Process of sleeping                │
│  (•) Process of making food from light  │
│  ( ) Process of breathing               │
│                                         │
│              [Submit Answer]            │
│              [Get New Question]         │
├─────────────────────────────────────────┤
│  Session Stats:                         │
│  Questions: 12  Correct: 10  Points: 85 │
└─────────────────────────────────────────┘
```

**Features**:
- Category and difficulty filters
- Display available questions count
- Current session statistics
- Get random question
- Submit answer with immediate feedback
- Get next question automatically
- Session summary

**API Calls**:
- `POST /api/play/random/start` - Start random session
- `GET /api/play/random/next-question` - Get random question
- `POST /api/play/random/answer` - Submit answer
- `GET /api/play/random/available-count` - Get count

---

### 6. User Dashboard (`/dashboard`)

**Purpose**: Personal statistics and overview

**Layout**:
```
┌─────────────────────────────────────────┐
│  Welcome back, John! 👋                 │
│  Current Streak: 🔥 12 days             │
├─────────────────────────────────────────┤
│  📊 Overview                            │
│  ┌─────────┬─────────┬─────────┐       │
│  │ 1,234   │ 85.6%   │ 12,500  │       │
│  │ Answered│ Accuracy│ Points  │       │
│  └─────────┴─────────┴─────────┘       │
├─────────────────────────────────────────┤
│  📈 Performance Trends                  │
│  [Chart showing last 30 days activity]  │
├─────────────────────────────────────────┤
│  🏆 Recent Achievements                 │
│  [🥇 First Answer] [🔥 Streak 10]      │
├─────────────────────────────────────────┤
│  📚 By Category                         │
│  Science:    ■■■■■□□□□□ 85% (234)      │
│  Math:       ■■■■□□□□□□ 72% (156)      │
│  History:    ■■■■■■□□□□ 90% (98)       │
├─────────────────────────────────────────┤
│  🎯 By Difficulty                       │
│  Easy:       ■■■■■■■■□□ 92% (456)      │
│  Medium:     ■■■■■■□□□□ 81% (234)      │
│  Hard:       ■■■■□□□□□□ 68% (112)      │
├─────────────────────────────────────────┤
│  🎮 Quick Actions                       │
│  [Random Practice] [Browse Quizzes]    │
│  [View Leaderboard] [My Groups]        │
└─────────────────────────────────────────┘
```

**Features**:
- Overall statistics cards
- Performance chart (line/bar chart)
- Recent achievements showcase
- Category breakdown with progress bars
- Difficulty breakdown
- Quick action buttons
- Streak display
- Rank display

**API Calls**:
- `GET /api/users/profile` - Get profile
- `GET /api/users/statistics` - Get stats
- `GET /api/users/statistics/categories` - Category stats
- `GET /api/users/statistics/difficulties` - Difficulty stats
- `GET /api/users/achievements` - Recent achievements
- `GET /api/users/statistics/trends` - Trend data

---

### 7. Leaderboard Page (`/leaderboard`)

**Purpose**: View rankings and compete

**Layout**:
```
┌─────────────────────────────────────────┐
│  🏆 Leaderboards                        │
├─────────────────────────────────────────┤
│  Tabs: [Global] [Groups] [Categories]  │
│  Period: [All Time ▼] [Weekly] [Monthly]│
├─────────────────────────────────────────┤
│  🥇 Top Players - Global                │
│  ┌─────────────────────────────────┐    │
│  │ 1. 👑 Sarah Johnson            │    │
│  │    15,234 pts • 95.2% accuracy │    │
│  │    [View Profile]              │    │
│  ├─────────────────────────────────┤    │
│  │ 2. 🥈 Mike Chen                │    │
│  │    14,567 pts • 92.8% accuracy │    │
│  ├─────────────────────────────────┤    │
│  │ 3. 🥉 Emma Wilson              │    │
│  │    13,890 pts • 94.1% accuracy │    │
│  ├─────────────────────────────────┤    │
│  │ ...                            │    │
│  ├─────────────────────────────────┤    │
│  │ 24. You (John Doe) ⭐          │    │
│  │    8,234 pts • 85.6% accuracy  │    │
│  └─────────────────────────────────┘    │
├─────────────────────────────────────────┤
│  [Load More]                            │
└─────────────────────────────────────────┘
```

**Features**:
- Global, group, and category leaderboards
- Time period selection (all-time, monthly, weekly)
- Top 100 display
- User's current rank highlighted
- Profile links
- Medal icons for top 3
- Load more pagination

**API Calls**:
- `GET /api/leaderboards/global` - Global leaderboard
- `GET /api/leaderboards/group/{groupId}` - Group leaderboard
- `GET /api/leaderboards/category/{categoryId}` - Category leaderboard
- `GET /api/leaderboards/me` - User's ranks

---

### 8. Achievements Page (`/achievements`)

**Purpose**: View all achievements and progress

**Layout**:
```
┌─────────────────────────────────────────┐
│  🏆 Achievements                        │
│  12 / 48 Unlocked                       │
├─────────────────────────────────────────┤
│  Filters: [All] [Unlocked] [Locked]    │
│  Category: [All ▼]                      │
├─────────────────────────────────────────┤
│  ✅ Unlocked Achievements (12)          │
│  ┌──────────────────────────────┐       │
│  │ 🥇 First Answer               │       │
│  │ BRONZE                        │       │
│  │ Answer your first question    │       │
│  │ Unlocked: Jan 15, 2024        │       │
│  └──────────────────────────────┘       │
│  ┌──────────────────────────────┐       │
│  │ 🔥 Streak 10                  │       │
│  │ SILVER                        │       │
│  │ Get 10 correct in a row       │       │
│  │ Unlocked: Jan 18, 2024        │       │
│  └──────────────────────────────┘       │
├─────────────────────────────────────────┤
│  🔒 Locked Achievements                 │
│  ┌──────────────────────────────┐       │
│  │ 🎯 Accuracy Master            │       │
│  │ GOLD                          │       │
│  │ 90%+ accuracy (100 questions) │       │
│  │ Progress: 56/100 (85.6%)      │       │
│  │ ■■■■■■□□□□ 56%              │       │
│  └──────────────────────────────┘       │
└─────────────────────────────────────────┘
```

**Features**:
- Grid/list view of achievements
- Filter by status (unlocked/locked)
- Filter by category
- Progress bars for locked achievements
- Unlock date display
- Tier badges (Bronze/Silver/Gold/Platinum)
- Achievement descriptions

**API Calls**:
- `GET /api/users/achievements` - Unlocked achievements
- `GET /api/users/achievements/progress` - Progress on locked
- `GET /api/achievements` - All achievements

---

## Updated Pages

### 1. Question Creation Page (`/questions/new`)

**Updates Needed**:

**Add Group Selection**:
```jsx
<FormField>
  <Label>Group (Optional)</Label>
  <Select
    value={groupId}
    onChange={(e) => setGroupId(e.target.value)}
  >
    <option value="">Public Question (No Group)</option>
    {myGroups.map(group => (
      <option key={group.id} value={group.id}>
        {group.name}
      </option>
    ))}
  </Select>
  <HelpText>
    Select a group to create a group-specific question
  </HelpText>
</FormField>
```

**Add Visibility Selection**:
```jsx
<FormField>
  <Label>Visibility</Label>
  <RadioGroup>
    <Radio
      value="PUBLIC"
      checked={visibility === 'PUBLIC'}
      disabled={!groupId}
    >
      Public - Anyone can use this question
    </Radio>
    <Radio
      value="PRIVATE"
      checked={visibility === 'PRIVATE'}
      disabled={!groupId}
    >
      Private - Only group members can use
    </Radio>
  </RadioGroup>
  {!groupId && (
    <HelpText>
      Public questions must have PUBLIC visibility
    </HelpText>
  )}
</FormField>
```

**API Updates**:
```typescript
// Add to create request
interface CreateQuestionRequest {
  text: string;
  type: QuestionType;
  category: string;
  difficulty: string;
  points: number;
  groupId?: number;  // NEW
  visibility: 'PUBLIC' | 'PRIVATE';  // NEW
  answers: Answer[];
}
```

---

### 2. Questions List Page (`/questions`)

**Updates Needed**:

**Add Group Filter**:
```jsx
<Filters>
  <Select
    label="Group"
    value={groupFilter}
    onChange={(e) => setGroupFilter(e.target.value)}
  >
    <option value="">All Questions</option>
    <option value="public">Public Questions Only</option>
    <optgroup label="My Groups">
      {myGroups.map(group => (
        <option key={group.id} value={group.id}>
          {group.name}
        </option>
      ))}
    </optgroup>
  </Select>
</Filters>
```

**Update Question Card**:
```jsx
<QuestionCard>
  <QuestionText>{question.text}</QuestionText>
  <QuestionMeta>
    <Badge>{question.category}</Badge>
    <Badge>{question.difficulty}</Badge>
    {question.groupId && (
      <GroupBadge>
        {question.visibility === 'PRIVATE' ? '🔒' : '🌐'}
        {question.groupName}
      </GroupBadge>
    )}
  </QuestionMeta>
  <QuestionStats>
    {question.points} pts • Created {formatDate(question.createdAt)}
  </QuestionStats>
</QuestionCard>
```

---

### 3. Quiz Creation Page (`/quizzes/new`)

**Updates Needed**:

**Add Group Selection**:
```jsx
<FormField>
  <Label>Quiz Type</Label>
  <RadioGroup>
    <Radio value="public">
      Public Quiz - Anyone can play
    </Radio>
    <Radio value="group">
      Group Quiz - Select a group
    </Radio>
  </RadioGroup>
</FormField>

{quizType === 'group' && (
  <FormField>
    <Label>Select Group</Label>
    <Select value={groupId}>
      {myGroups.map(group => (
        <option value={group.id}>{group.name}</option>
      ))}
    </Select>
  </FormField>
)}

{groupId && (
  <FormField>
    <Label>Visibility</Label>
    <RadioGroup>
      <Radio value="PUBLIC">
        Public - Anyone can play
      </Radio>
      <Radio value="PRIVATE">
        Private - Only group members can play
      </Radio>
    </RadioGroup>
  </FormField>
)}
```

**Update Question Selection**:
- Filter questions by user's group access
- Show group badge on questions
- Indicate which questions user can add

---

### 4. Navigation Bar

**Updates Needed**:

Add new menu items:
```jsx
<Navigation>
  <NavItem to="/" icon={Home}>Home</NavItem>
  <NavItem to="/dashboard" icon={Dashboard}>Dashboard</NavItem>
  <NavItem to="/play/random" icon={PlayCircle}>Practice</NavItem>
  <NavItem to="/quizzes" icon={QuizIcon}>Quizzes</NavItem>
  <NavItem to="/questions" icon={Question}>Questions</NavItem>
  <NavItem to="/groups" icon={Group}>Groups</NavItem> {/* NEW */}
  <NavItem to="/leaderboard" icon={Trophy}>Leaderboard</NavItem> {/* NEW */}
  <NavItem to="/achievements" icon={Award}>Achievements</NavItem> {/* NEW */}
  <NavItem to="/profile" icon={User}>Profile</NavItem>
</Navigation>
```

---

## New Components

### 1. GroupCard Component

**Purpose**: Display group summary

```tsx
interface GroupCardProps {
  group: Group;
  onJoin?: () => void;
  onLeave?: () => void;
}

const GroupCard: React.FC<GroupCardProps> = ({ group, onJoin, onLeave }) => {
  return (
    <Card>
      <CardHeader>
        <Avatar src={group.avatarUrl} />
        <Title>{group.name}</Title>
        <Badge>{group.type}</Badge>
      </CardHeader>
      <CardBody>
        <Description>{group.description}</Description>
        <Stats>
          <Stat icon={Users}>{group.memberCount} members</Stat>
          <Stat icon={Question}>{group.questionCount} questions</Stat>
        </Stats>
      </CardBody>
      <CardFooter>
        {group.isMember ? (
          <Button onClick={onLeave} variant="secondary">
            Leave Group
          </Button>
        ) : group.type === 'PUBLIC' ? (
          <Button onClick={onJoin} variant="primary">
            Join Group
          </Button>
        ) : (
          <Button variant="secondary" disabled>
            Private - Invite Only
          </Button>
        )}
      </CardFooter>
    </Card>
  );
};
```

---

### 2. LeaderboardTable Component

**Purpose**: Display rankings

```tsx
interface LeaderboardTableProps {
  entries: LeaderboardEntry[];
  currentUserId: number;
}

const LeaderboardTable: React.FC<LeaderboardTableProps> = ({
  entries,
  currentUserId
}) => {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Rank</TableHead>
          <TableHead>Player</TableHead>
          <TableHead>Points</TableHead>
          <TableHead>Accuracy</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {entries.map((entry) => (
          <TableRow
            key={entry.userId}
            highlighted={entry.userId === currentUserId}
          >
            <TableCell>
              {entry.rank <= 3 ? (
                <Medal rank={entry.rank} />
              ) : (
                entry.rank
              )}
            </TableCell>
            <TableCell>
              <UserAvatar src={entry.avatarUrl} />
              {entry.displayName}
              {entry.userId === currentUserId && <Badge>You</Badge>}
            </TableCell>
            <TableCell>{entry.totalPoints.toLocaleString()}</TableCell>
            <TableCell>{entry.accuracyPercentage}%</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};
```

---

### 3. AchievementCard Component

**Purpose**: Display achievement with progress

```tsx
interface AchievementCardProps {
  achievement: Achievement;
  progress?: AchievementProgress;
  unlocked: boolean;
}

const AchievementCard: React.FC<AchievementCardProps> = ({
  achievement,
  progress,
  unlocked
}) => {
  return (
    <Card className={unlocked ? 'unlocked' : 'locked'}>
      <IconContainer>
        <Icon src={achievement.iconUrl} grayscale={!unlocked} />
        <TierBadge tier={achievement.tier} />
      </IconContainer>
      <CardBody>
        <Title>{achievement.name}</Title>
        <Description>{achievement.description}</Description>
        {!unlocked && progress && (
          <Progress>
            <ProgressBar
              value={progress.currentValue}
              max={progress.targetValue}
            />
            <ProgressText>
              {progress.currentValue} / {progress.targetValue}
            </ProgressText>
          </Progress>
        )}
        {unlocked && (
          <UnlockedDate>
            Unlocked: {formatDate(progress.unlockedAt)}
          </UnlockedDate>
        )}
      </CardBody>
    </Card>
  );
};
```

---

### 4. QuizPlayer Component

**Purpose**: Handle quiz gameplay

```tsx
interface QuizPlayerProps {
  quizId: number;
  onComplete: (results: SessionResults) => void;
}

const QuizPlayer: React.FC<QuizPlayerProps> = ({ quizId, onComplete }) => {
  const [session, setSession] = useState<PlaySession | null>(null);
  const [currentQuestion, setCurrentQuestion] = useState<Question | null>(null);
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [timeRemaining, setTimeRemaining] = useState<number>(0);

  // Start session on mount
  useEffect(() => {
    startQuizSession(quizId).then(setSession);
  }, [quizId]);

  // Timer countdown
  useEffect(() => {
    if (!session || timeRemaining <= 0) return;
    const timer = setInterval(() => {
      setTimeRemaining((t) => t - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [session, timeRemaining]);

  const handleSubmit = async () => {
    if (!selectedAnswer) return;

    const result = await submitAnswer(
      session.id,
      currentQuestion.id,
      selectedAnswer
    );

    // Show feedback
    showFeedback(result);

    // Move to next question or complete
    if (result.nextQuestion) {
      setCurrentQuestion(result.nextQuestion);
      setSelectedAnswer(null);
    } else {
      const results = await completeSession(session.id);
      onComplete(results);
    }
  };

  return (
    <QuizPlayerContainer>
      <QuizHeader>
        <QuizTitle>{session?.quizTitle}</QuizTitle>
        <QuizProgress>
          Question {session?.questionsAnswered + 1} of {session?.totalQuestions}
        </QuizProgress>
        {timeRemaining > 0 && (
          <Timer>⏱️ {formatTime(timeRemaining)}</Timer>
        )}
        <Score>Score: {session?.totalPoints} pts</Score>
      </QuizHeader>

      <QuestionDisplay>
        <QuestionText>{currentQuestion?.text}</QuestionText>
        <AnswersList>
          {currentQuestion?.answers.map((answer) => (
            <AnswerOption
              key={answer.id}
              selected={selectedAnswer === answer.id}
              onClick={() => setSelectedAnswer(answer.id)}
            >
              <Radio checked={selectedAnswer === answer.id} />
              {answer.text}
            </AnswerOption>
          ))}
        </AnswersList>
      </QuestionDisplay>

      <QuizActions>
        <Button
          onClick={handleSubmit}
          disabled={!selectedAnswer}
          variant="primary"
        >
          Submit Answer
        </Button>
      </QuizActions>

      <ProgressBar
        value={session?.questionsAnswered}
        max={session?.totalQuestions}
      />
    </QuizPlayerContainer>
  );
};
```

---

### 5. StatsCard Component

**Purpose**: Display statistics summary

```tsx
interface StatsCardProps {
  title: string;
  value: number | string;
  subtitle?: string;
  icon?: React.ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
  };
}

const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  trend
}) => {
  return (
    <Card>
      <CardHeader>
        {icon && <Icon>{icon}</Icon>}
        <Title>{title}</Title>
      </CardHeader>
      <CardBody>
        <Value>{value}</Value>
        {subtitle && <Subtitle>{subtitle}</Subtitle>}
        {trend && (
          <Trend positive={trend.isPositive}>
            <TrendIcon>
              {trend.isPositive ? '↑' : '↓'}
            </TrendIcon>
            {Math.abs(trend.value)}%
          </Trend>
        )}
      </CardBody>
    </Card>
  );
};
```

---

## API Integration

### API Client Setup

**File**: `src/api/client.ts`

```typescript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Important for httpOnly cookies
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use((config) => {
  // Add any auth headers if needed
  return config;
});

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

### API Service Functions

**File**: `src/api/services/groupService.ts`

```typescript
import { apiClient } from '../client';
import { Group, GroupMember, Invitation } from '../types';

export const groupService = {
  // Get all public groups
  getPublicGroups: async (): Promise<Group[]> => {
    const { data } = await apiClient.get('/api/groups');
    return data;
  },

  // Get user's groups
  getMyGroups: async (): Promise<Group[]> => {
    const { data } = await apiClient.get('/api/groups/my-groups');
    return data;
  },

  // Get group by ID
  getGroup: async (id: number): Promise<Group> => {
    const { data } = await apiClient.get(`/api/groups/${id}`);
    return data;
  },

  // Join public group
  joinGroup: async (id: number): Promise<GroupMember> => {
    const { data } = await apiClient.post(`/api/groups/${id}/join`);
    return data;
  },

  // Leave group
  leaveGroup: async (id: number): Promise<void> => {
    await apiClient.post(`/api/groups/${id}/leave`);
  },

  // Get group members
  getMembers: async (id: number): Promise<GroupMember[]> => {
    const { data } = await apiClient.get(`/api/groups/${id}/members`);
    return data;
  },

  // Admin: Invite user
  inviteUser: async (
    groupId: number,
    email: string,
    message?: string
  ): Promise<Invitation> => {
    const { data } = await apiClient.post(`/api/groups/${groupId}/invite`, {
      email,
      message,
    });
    return data;
  },
};
```

**File**: `src/api/services/playService.ts`

```typescript
import { apiClient } from '../client';
import {
  PlaySession,
  Question,
  AnswerResult,
  SessionResults,
} from '../types';

export const playService = {
  // Start quiz session
  startQuizSession: async (quizId: number): Promise<PlaySession> => {
    const { data } = await apiClient.post(`/api/play/quiz/${quizId}/start`);
    return data;
  },

  // Get question
  getQuestion: async (
    sessionId: number,
    order: number
  ): Promise<Question> => {
    const { data } = await apiClient.get(
      `/api/play/sessions/${sessionId}/question/${order}`
    );
    return data;
  },

  // Submit answer
  submitAnswer: async (
    sessionId: number,
    questionId: number,
    answerId: number,
    timeSpent: number
  ): Promise<AnswerResult> => {
    const { data } = await apiClient.post(
      `/api/play/sessions/${sessionId}/answer`,
      {
        questionId,
        answerId,
        timeSpentSeconds: timeSpent,
      }
    );
    return data;
  },

  // Complete session
  completeSession: async (sessionId: number): Promise<SessionResults> => {
    const { data } = await apiClient.post(
      `/api/play/sessions/${sessionId}/complete`
    );
    return data;
  },

  // Random mode
  startRandomSession: async (): Promise<PlaySession> => {
    const { data } = await apiClient.post('/api/play/random/start');
    return data;
  },

  getRandomQuestion: async (
    categoryId?: number,
    difficultyLevelId?: number
  ): Promise<Question> => {
    const { data } = await apiClient.get('/api/play/random/next-question', {
      params: { categoryId, difficultyLevelId },
    });
    return data;
  },

  submitRandomAnswer: async (
    questionId: number,
    answerId: number,
    timeSpent: number
  ): Promise<AnswerResult> => {
    const { data } = await apiClient.post('/api/play/random/answer', {
      questionId,
      answerId,
      timeSpentSeconds: timeSpent,
    });
    return data;
  },
};
```

**File**: `src/api/services/profileService.ts`

```typescript
import { apiClient } from '../client';
import {
  UserProfile,
  UserStats,
  Achievement,
  Leaderboard,
} from '../types';

export const profileService = {
  // Get profile
  getProfile: async (): Promise<UserProfile> => {
    const { data } = await apiClient.get('/api/users/profile');
    return data;
  },

  // Update profile
  updateProfile: async (
    profile: Partial<UserProfile>
  ): Promise<UserProfile> => {
    const { data } = await apiClient.put('/api/users/profile', profile);
    return data;
  },

  // Get statistics
  getStats: async (): Promise<UserStats> => {
    const { data } = await apiClient.get('/api/users/statistics');
    return data;
  },

  // Get achievements
  getAchievements: async (): Promise<Achievement[]> => {
    const { data } = await apiClient.get('/api/users/achievements');
    return data;
  },

  // Get achievement progress
  getAchievementProgress: async (): Promise<AchievementProgress[]> => {
    const { data } = await apiClient.get('/api/users/achievements/progress');
    return data;
  },

  // Get leaderboard
  getLeaderboard: async (
    type: 'global' | 'group' | 'category',
    referenceId?: number,
    period: string = 'all-time'
  ): Promise<Leaderboard> => {
    let url = `/api/leaderboards/${type}`;
    if (type === 'group') url += `/${referenceId}`;
    if (type === 'category') url += `/${referenceId}`;

    const { data } = await apiClient.get(url, {
      params: { period },
    });
    return data;
  },
};
```

---

## TypeScript Types

**File**: `src/types/index.ts`

```typescript
// Groups
export interface Group {
  id: number;
  name: string;
  description: string;
  type: 'PUBLIC' | 'PRIVATE';
  createdBy: number;
  groupAdminId: number;
  groupAdminName: string;
  avatarUrl?: string;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface GroupMember {
  id: number;
  groupId: number;
  userId: number;
  userDisplayName: string;
  userAvatarUrl?: string;
  role: 'MEMBER' | 'ADMIN';
  status: 'ACTIVE' | 'PENDING' | 'REMOVED';
  joinedAt: string;
}

// Questions
export interface Question {
  id: number;
  text: string;
  type: 'MULTIPLE_CHOICE' | 'TEXT_INPUT';
  status: 'DRAFT' | 'PENDING' | 'VALIDATED' | 'REJECTED' | 'ARCHIVED';
  category: string;
  difficulty: string;
  points: number;
  groupId?: number;
  groupName?: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  answers: Answer[];
  createdAt: string;
  updatedAt: string;
  createdBy: number;
}

// Quiz
export interface Quiz {
  id: number;
  title: string;
  description: string;
  createdBy: number;
  creatorName: string;
  groupId?: number;
  groupName?: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  difficulty: 'MIXED' | 'EASY' | 'MEDIUM' | 'HARD' | 'EXPERT';
  questionCount: number;
  timeLimit?: number;
  passingScore?: number;
  randomizeQuestions: boolean;
  randomizeAnswers: boolean;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  timesPlayed: number;
  averageScore?: number;
  createdAt: string;
  publishedAt?: string;
}

// Play
export interface PlaySession {
  id: number;
  userId: number;
  mode: 'RANDOM' | 'QUIZ';
  quizId?: number;
  quizTitle?: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';
  totalQuestions: number;
  questionsAnswered: number;
  correctAnswers: number;
  wrongAnswers: number;
  totalPoints: number;
  timeSpentSeconds: number;
  scorePercentage?: number;
  startedAt: string;
  completedAt?: string;
}

export interface AnswerResult {
  isCorrect: boolean;
  pointsEarned: number;
  correctAnswerText: string;
  explanation?: string;
  totalPoints: number;
  questionsAnswered: number;
  nextQuestion?: Question;
}

// Profile
export interface UserProfile {
  id: number;
  userId: number;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  location?: string;
  isPublic: boolean;
  totalQuestionsAnswered: number;
  totalCorrectAnswers: number;
  totalWrongAnswers: number;
  accuracyPercentage: number;
  totalPoints: number;
  currentStreak: number;
  longestStreak: number;
  globalRank?: number;
  lastPlayedAt?: string;
  createdAt: string;
}

export interface Achievement {
  id: number;
  code: string;
  name: string;
  description: string;
  iconUrl: string;
  category: string;
  tier: 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';
}

export interface AchievementProgress {
  achievement: Achievement;
  currentValue: number;
  targetValue: number;
  progressPercentage: number;
  unlockedAt?: string;
}

export interface Leaderboard {
  type: 'GLOBAL' | 'GROUP' | 'CATEGORY';
  period: 'ALL_TIME' | 'MONTHLY' | 'WEEKLY' | 'DAILY';
  name: string;
  periodStart?: string;
  periodEnd?: string;
  entries: LeaderboardEntry[];
  totalPlayers: number;
  myRank?: LeaderboardEntry;
}

export interface LeaderboardEntry {
  rank: number;
  userId: number;
  displayName: string;
  avatarUrl?: string;
  totalPoints: number;
  questionsAnswered: number;
  correctAnswers: number;
  accuracyPercentage: number;
}
```

---

## User Flows

### Flow 1: Join a Group and Create a Question

```
1. User navigates to /groups
2. Browses public groups
3. Clicks "Join Group" on "Science Enthusiasts"
4. Confirmation: "You are now a member!"
5. Navigates to /questions/new
6. Fills question form
7. Selects "Science Enthusiasts" from group dropdown
8. Selects "PRIVATE" visibility
9. Adds answers
10. Clicks "Create Question"
11. Success: Question created in group
12. Question visible only to group members
```

### Flow 2: Take a Quiz

```
1. User navigates to /quizzes
2. Browses available quizzes
3. Clicks "Start Quiz" on "Science 101"
4. Quiz session starts
5. Timer begins (if time limit set)
6. User reads question
7. Selects answer
8. Clicks "Submit Answer"
9. Immediate feedback shown (correct/incorrect)
10. Automatic transition to next question
11. Repeat steps 6-10 for all questions
12. Quiz completes automatically
13. Results screen shows:
    - Final score
    - Percentage
    - Correct/wrong breakdown
    - Time spent
    - "Pass" or "Fail" badge
14. Options: "Retake Quiz", "View Leaderboard", "Back to Quizzes"
```

### Flow 3: Unlock an Achievement

```
1. User answers 10 questions correctly in a row
2. Play Service publishes QuestionAnsweredEvent
3. Profile Service receives event
4. Checks achievement criteria
5. Detects "Streak 10" achievement unlocked
6. Saves to database
7. Frontend polls for new achievements (or WebSocket notification)
8. Toast notification: "Achievement Unlocked! 🔥 Streak 10"
9. User clicks notification
10. Navigates to /achievements
11. Achievement highlighted with animation
12. User sees unlock date and description
```

---

## Implementation Checklist

### Phase 1: Setup & Infrastructure
- [ ] Set up React + TypeScript + Vite project
- [ ] Configure Tailwind CSS or Material-UI
- [ ] Set up React Router
- [ ] Create API client with Axios
- [ ] Set up state management (Context/Redux)
- [ ] Configure environment variables
- [ ] Set up React Query for server state

### Phase 2: Authentication & Layout
- [ ] Create authentication context
- [ ] Implement login/logout
- [ ] Create main layout component
- [ ] Create navigation bar
- [ ] Create sidebar (if needed)
- [ ] Create footer
- [ ] Implement protected routes

### Phase 3: Groups Feature
- [ ] Create Groups page
- [ ] Create GroupCard component
- [ ] Create Group Detail page
- [ ] Implement join/leave group
- [ ] Create Group Admin panel
- [ ] Implement invite system
- [ ] Add group tests

### Phase 4: Questions Feature (Update)
- [ ] Update Question Creation form
- [ ] Add group selection dropdown
- [ ] Add visibility selection
- [ ] Update Questions List page
- [ ] Add group filter
- [ ] Update QuestionCard component
- [ ] Add validation for group questions

### Phase 5: Quiz Feature
- [ ] Create Quiz List page
- [ ] Create Quiz Detail page
- [ ] Update Quiz Creation form
- [ ] Add group/visibility for quizzes
- [ ] Create QuizPlayer component
- [ ] Implement timer logic
- [ ] Implement scoring display
- [ ] Create quiz results page

### Phase 6: Play Feature
- [ ] Create Random Practice page
- [ ] Implement category/difficulty filters
- [ ] Create question display component
- [ ] Implement answer submission
- [ ] Add feedback animations
- [ ] Create session statistics display
- [ ] Add "Get Next Question" logic

### Phase 7: Profile & Dashboard
- [ ] Create Dashboard page
- [ ] Create stats cards
- [ ] Implement performance chart
- [ ] Create category breakdown
- [ ] Create difficulty breakdown
- [ ] Add quick action buttons
- [ ] Create profile edit page

### Phase 8: Achievements
- [ ] Create Achievements page
- [ ] Create AchievementCard component
- [ ] Implement filter by status
- [ ] Show progress bars
- [ ] Add achievement notifications
- [ ] Implement unlock animations

### Phase 9: Leaderboards
- [ ] Create Leaderboard page
- [ ] Create LeaderboardTable component
- [ ] Implement period selection
- [ ] Implement type tabs (global/group/category)
- [ ] Highlight current user
- [ ] Add medals for top 3
- [ ] Implement pagination

### Phase 10: Polish & Testing
- [ ] Add loading states
- [ ] Add error handling
- [ ] Add success notifications
- [ ] Implement responsive design
- [ ] Add accessibility features
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Performance optimization
- [ ] Cross-browser testing

---

## Estimated Timeline

- **Phase 1-2**: 1 week
- **Phase 3**: 1 week
- **Phase 4**: 3 days
- **Phase 5**: 1 week
- **Phase 6**: 4 days
- **Phase 7**: 1 week
- **Phase 8**: 3 days
- **Phase 9**: 4 days
- **Phase 10**: 1 week

**Total**: ~6-7 weeks for full implementation

---

## Priority Order

1. **High Priority** (Must have):
   - Authentication
   - Navigation
   - Groups (basic CRUD)
   - Question updates (group support)
   - Quiz playing
   - Random practice
   - Dashboard

2. **Medium Priority** (Should have):
   - Leaderboards
   - Achievements
   - Group admin panel
   - Profile editing

3. **Low Priority** (Nice to have):
   - Advanced filters
   - Performance charts
   - Achievement notifications
   - Activity feeds

---

**Document Owner**: Frontend Team
**Estimated Effort**: 6-7 weeks (1 developer)
**Dependencies**: All 4 microservices must be running
