package com.quizz.question.service;

import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.exception.ForbiddenException;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.model.*;
import com.quizz.question.repository.CategoryRepository;
import com.quizz.question.repository.DifficultyLevelRepository;
import com.quizz.question.repository.QuestionRepository;
import com.quizz.question.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService Unit Tests")
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private UserContext userContext;
    private UserContext adminContext;
    private Question question;
    private QuestionDTO questionDTO;
    private CreateQuestionRequest createRequest;
    private UpdateQuestionRequest updateRequest;
    private Category category;
    private DifficultyLevel difficultyLevel;

    @BeforeEach
    void setUp() {
        // Setup user contexts
        userContext = UserContext.builder()
                .userId(1L)
                .isAdmin(false)
                .build();

        adminContext = UserContext.builder()
                .userId(999L)
                .isAdmin(true)
                .build();

        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Java");
        category.setDescription("Java questions");
        category.setStatus(CategoryStatus.ACTIVE);
        category.setCreatedBy(1L);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Setup difficulty level
        difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(1L);
        difficultyLevel.setName("EASY");
        difficultyLevel.setDescription("Easy questions");
        difficultyLevel.setDisplayOrder(1);
        difficultyLevel.setPointsMultiplier(1.0);

        // Setup question
        question = Question.builder()
                .id(1L)
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .status(QuestionStatus.DRAFT)
                .category(category)
                .difficultyLevel(difficultyLevel)
                .points(10)
                .createdBy(1L)
                .answers(new ArrayList<>())
                .build();

        // Setup DTO
        questionDTO = QuestionDTO.builder()
                .id(1L)
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .status(QuestionStatus.DRAFT)
                .points(10)
                .build();

        // Setup create request
        AnswerDTO answer1 = AnswerDTO.builder()
                .text("A programming language")
                .isCorrect(true)
                .build();

        AnswerDTO answer2 = AnswerDTO.builder()
                .text("A coffee brand")
                .isCorrect(false)
                .build();

        createRequest = CreateQuestionRequest.builder()
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(1L)
                .difficultyLevelId(1L)
                .points(10)
                .answers(Arrays.asList(answer1, answer2))
                .build();

        // Setup update request
        updateRequest = UpdateQuestionRequest.builder()
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .status(QuestionStatus.DRAFT)
                .categoryId(1L)
                .difficultyLevelId(1L)
                .points(10)
                .answers(Arrays.asList(answer1, answer2))
                .build();
    }

    @Test
    @DisplayName("Should create question successfully")
    void shouldCreateQuestionSuccessfully() {
        // Given
        when(questionMapper.toEntity(any(CreateQuestionRequest.class), anyLong()))
                .thenReturn(question);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.createQuestion(createRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("What is Java?");
        verify(questionRepository).save(any(Question.class));
        verify(categoryRepository).findById(1L);
        verify(difficultyLevelRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(questionMapper.toEntity(any(CreateQuestionRequest.class), anyLong()))
                .thenReturn(question);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> questionService.createQuestion(createRequest, userContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("Should throw exception when difficulty level not found")
    void shouldThrowExceptionWhenDifficultyLevelNotFound() {
        // Given
        when(questionMapper.toEntity(any(CreateQuestionRequest.class), anyLong()))
                .thenReturn(question);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> questionService.createQuestion(createRequest, userContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Difficulty level not found");
    }

    @Test
    @DisplayName("Should get question by ID successfully")
    void shouldGetQuestionByIdSuccessfully() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionMapper.toDTO(question)).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.getQuestionById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(questionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when question not found")
    void shouldThrowExceptionWhenQuestionNotFound() {
        // Given
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> questionService.getQuestionById(999L))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should get questions with filters")
    void shouldGetQuestionsWithFilters() {
        // Given
        List<Question> questions = Arrays.asList(question);
        List<QuestionDTO> questionDTOs = Arrays.asList(questionDTO);

        when(questionRepository.findByFilters(any(), any())).thenReturn(questions);
        when(questionMapper.toDTOList(questions)).thenReturn(questionDTOs);

        // When
        List<QuestionDTO> result = questionService.getQuestions(
                Arrays.asList(QuestionStatus.DRAFT),
                Arrays.asList(1L)
        );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(questionRepository).findByFilters(any(), any());
    }

    @Test
    @DisplayName("Should get questions with pagination")
    void shouldGetQuestionsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> questionPage = new PageImpl<>(Arrays.asList(question));

        when(questionRepository.findByFiltersPageable(any(), any(), any())).thenReturn(questionPage);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        Page<QuestionDTO> result = questionService.getQuestionsPageable(
                Arrays.asList(QuestionStatus.DRAFT),
                Arrays.asList(1L),
                pageable
        );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(questionRepository).findByFiltersPageable(any(), any(), eq(pageable));
    }

    @Test
    @DisplayName("Should update question by owner")
    void shouldUpdateQuestionByOwner() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.updateQuestion(1L, updateRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should update question by admin")
    void shouldUpdateQuestionByAdmin() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.updateQuestion(1L, updateRequest, adminContext);

        // Then
        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to update")
    void shouldThrowExceptionWhenNonOwnerTriesToUpdate() {
        // Given
        UserContext otherUser = UserContext.builder()
                .userId(2L)
                .isAdmin(false)
                .build();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When/Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, updateRequest, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should throw exception when updating non-DRAFT without admin rights")
    void shouldThrowExceptionWhenUpdatingNonDraftWithoutAdminRights() {
        // Given
        question.setStatus(QuestionStatus.VALIDATED);
        updateRequest.setStatus(QuestionStatus.VALIDATED);
        updateRequest.setText("Different text");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When/Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, updateRequest, userContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("Should allow status-only change on non-DRAFT question")
    void shouldAllowStatusOnlyChangeOnNonDraftQuestion() {
        // Given
        question.setStatus(QuestionStatus.PENDING);
        updateRequest.setStatus(QuestionStatus.DRAFT);
        updateRequest.setText(question.getText());
        updateRequest.setPoints(question.getPoints());

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.updateQuestion(1L, updateRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should delete DRAFT question by owner")
    void shouldDeleteDraftQuestionByOwner() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When
        questionService.deleteQuestion(1L, userContext);

        // Then
        verify(questionRepository).delete(question);
    }

    @Test
    @DisplayName("Should delete question by admin")
    void shouldDeleteQuestionByAdmin() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When
        questionService.deleteQuestion(1L, adminContext);

        // Then
        verify(questionRepository).delete(question);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete")
    void shouldThrowExceptionWhenNonOwnerTriesToDelete() {
        // Given
        UserContext otherUser = UserContext.builder()
                .userId(2L)
                .isAdmin(false)
                .build();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When/Then
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-DRAFT question")
    void shouldThrowExceptionWhenDeletingNonDraftQuestion() {
        // Given
        question.setStatus(QuestionStatus.VALIDATED);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When/Then
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, userContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("Should create category when using categoryName")
    void shouldCreateCategoryWhenUsingCategoryName() {
        // Given
        createRequest.setCategoryId(null);
        createRequest.setCategoryName("New Category");

        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("New Category");
        newCategory.setStatus(CategoryStatus.ACTIVE);

        when(questionMapper.toEntity(any(CreateQuestionRequest.class), anyLong()))
                .thenReturn(question);
        when(categoryRepository.findByNameIgnoreCase("New Category"))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.createQuestion(createRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should reuse existing category when using categoryName")
    void shouldReuseExistingCategoryWhenUsingCategoryName() {
        // Given
        createRequest.setCategoryId(null);
        createRequest.setCategoryName("Java");

        when(questionMapper.toEntity(any(CreateQuestionRequest.class), anyLong()))
                .thenReturn(question);
        when(categoryRepository.findByNameIgnoreCase("Java"))
                .thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

        // When
        QuestionDTO result = questionService.createQuestion(createRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryRepository).findByNameIgnoreCase("Java");
    }

    @Test
    @DisplayName("Should validate status transition from DRAFT to PENDING")
    void shouldValidateStatusTransitionFromDraftToPending() {
        // Given
        question.setStatus(QuestionStatus.DRAFT);
        updateRequest.setStatus(QuestionStatus.PENDING);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));

        // When
        QuestionDTO result = questionService.updateQuestion(1L, updateRequest, userContext);

        // Then
        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should prevent non-admin from transitioning DRAFT to VALIDATED")
    void shouldPreventNonAdminFromTransitioningDraftToValidated() {
        // Given
        question.setStatus(QuestionStatus.DRAFT);
        updateRequest.setStatus(QuestionStatus.VALIDATED);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When/Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, updateRequest, userContext))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Should allow admin to transition DRAFT to VALIDATED")
    void shouldAllowAdminToTransitionDraftToValidated() {
        // Given
        question.setStatus(QuestionStatus.DRAFT);
        updateRequest.setStatus(QuestionStatus.VALIDATED);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(difficultyLevel));

        // When
        QuestionDTO result = questionService.updateQuestion(1L, updateRequest, adminContext);

        // Then
        assertThat(result).isNotNull();
        verify(questionRepository).save(any(Question.class));
    }
}
