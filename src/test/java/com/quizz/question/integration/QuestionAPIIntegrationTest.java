package com.quizz.question.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizz.question.dto.AnswerDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.Category;
import com.quizz.question.model.CategoryStatus;
import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionType;
import com.quizz.question.repository.CategoryRepository;
import com.quizz.question.repository.DifficultyLevelRepository;
import com.quizz.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.quizz.question.security.WithMockJwtUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Question API Integration Tests")
public class QuestionAPIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DifficultyLevelRepository difficultyLevelRepository;

    private Category category;
    private DifficultyLevel difficultyLevel;

    @BeforeEach
    void setUp() {
        // Clean database (except difficulty levels which are reference data)
        questionRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        category = Category.builder()
                .name("Java")
                .description("Java programming questions")
                .status(CategoryStatus.ACTIVE)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        category = categoryRepository.save(category);

        // Use existing difficulty level from migrations (Easy is inserted by migration 005)
        difficultyLevel = difficultyLevelRepository.findAll().stream()
                .filter(dl -> "Easy".equals(dl.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Easy difficulty level not found"));
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should create question successfully with valid data")
    void shouldCreateQuestionSuccessfully() throws Exception {
        // Given
        AnswerDTO answer1 = AnswerDTO.builder()
                .text("A programming language")
                .isCorrect(true)
                .build();

        AnswerDTO answer2 = AnswerDTO.builder()
                .text("A coffee brand")
                .isCorrect(false)
                .build();

        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(10)
                .answers(Arrays.asList(answer1, answer2))
                .build();

        // When & Then
        String response = mockMvc.perform(post("/api/questions")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        QuestionDTO result = objectMapper.readValue(response, QuestionDTO.class);
        mockMvc.perform(get("/api/questions/" + result.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(result.getId()))
                .andExpect(jsonPath("$.text").value("What is Java?"))
                .andExpect(jsonPath("$.type").value("multiple-choice"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.points").value(10))
                .andExpect(jsonPath("$.createdBy").value(1))
                .andExpect(jsonPath("$.answers").isArray())
                .andExpect(jsonPath("$.answers", hasSize(2)));
    }

    @Test
    @DisplayName("Should return 401 when creating question without authentication")
    void shouldReturn401WhenCreatingQuestionWithoutAuthentication() throws Exception {
        // Given
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(10)
                .build();

        // When & Then
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should get question by ID")
    void shouldGetQuestionById() throws Exception {
        // Given - create a question first
        AnswerDTO answer = AnswerDTO.builder()
                .text("Answer")
                .isCorrect(true)
                .build();

        CreateQuestionRequest createRequest = CreateQuestionRequest.builder()
                .text("Test Question")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(5)
                .answers(List.of(answer))
                .build();

        String createResponse = mockMvc.perform(post("/api/questions")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        QuestionDTO createdQuestion = objectMapper.readValue(createResponse, QuestionDTO.class);

        // When & Then
        mockMvc.perform(get("/api/questions/" + createdQuestion.getId())
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdQuestion.getId()))
                .andExpect(jsonPath("$.text").value("Test Question"));
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should return 404 when question not found")
    void shouldReturn404WhenQuestionNotFound() throws Exception {
        mockMvc.perform(get("/api/questions/999999")
                        )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should update question successfully")
    void shouldUpdateQuestionSuccessfully() throws Exception {
        // Given - create a question first
        AnswerDTO answer = AnswerDTO.builder()
                .text("Original Answer")
                .isCorrect(true)
                .build();

        CreateQuestionRequest createRequest = CreateQuestionRequest.builder()
                .text("Original Question")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(5)
                .answers(List.of(answer))
                .build();

        String createResponse = mockMvc.perform(post("/api/questions")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        QuestionDTO createdQuestion = objectMapper.readValue(createResponse, QuestionDTO.class);

        // Update request
        AnswerDTO updatedAnswer = AnswerDTO.builder()
                .text("Updated Answer")
                .isCorrect(true)
                .build();

        UpdateQuestionRequest updateRequest = UpdateQuestionRequest.builder()
                .text("Updated Question")
                .type(QuestionType.MULTIPLE_CHOICE)
                .status(QuestionStatus.DRAFT)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(15)
                .answers(List.of(updatedAnswer))
                .build();

        // When & Then
        mockMvc.perform(put("/api/questions/" + createdQuestion.getId())
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated Question"))
                .andExpect(jsonPath("$.points").value(15));
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should delete DRAFT question successfully")
    void shouldDeleteDraftQuestionSuccessfully() throws Exception {
        // Given - create a DRAFT question
        AnswerDTO answer = AnswerDTO.builder()
                .text("Answer")
                .isCorrect(true)
                .build();

        CreateQuestionRequest createRequest = CreateQuestionRequest.builder()
                .text("Question to Delete")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(5)
                .answers(List.of(answer))
                .build();

        String createResponse = mockMvc.perform(post("/api/questions")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        QuestionDTO createdQuestion = objectMapper.readValue(createResponse, QuestionDTO.class);

        // When & Then - delete the question
        mockMvc.perform(delete("/api/questions/" + createdQuestion.getId())
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Question deleted successfully"));

        // Verify it's deleted
        mockMvc.perform(get("/api/questions/" + createdQuestion.getId())
                        )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should get questions with pagination")
    void shouldGetQuestionsWithPagination() throws Exception {
        // Given - create multiple questions
        for (int i = 1; i <= 3; i++) {
            AnswerDTO answer = AnswerDTO.builder()
                    .text("Answer " + i)
                    .isCorrect(true)
                    .build();

            CreateQuestionRequest request = CreateQuestionRequest.builder()
                    .text("Question " + i)
                    .type(QuestionType.MULTIPLE_CHOICE)
                    .categoryId(category.getId())
                    .difficultyLevelId(difficultyLevel.getId())
                    .points(10)
                    .answers(List.of(answer))
                    .build();

            mockMvc.perform(post("/api/questions")
                            
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then
        mockMvc.perform(get("/api/questions")
                        
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithMockJwtUser(userId = 1L, email = "user@test.com", roles = "USER")
    @DisplayName("Should filter questions by status")
    void shouldFilterQuestionsByStatus() throws Exception {
        // Given - create questions with different statuses
        AnswerDTO answer = AnswerDTO.builder()
                .text("Answer")
                .isCorrect(true)
                .build();

        // Create DRAFT question
        CreateQuestionRequest draftRequest = CreateQuestionRequest.builder()
                .text("Draft Question")
                .type(QuestionType.MULTIPLE_CHOICE)
                .categoryId(category.getId())
                .difficultyLevelId(difficultyLevel.getId())
                .points(10)
                .answers(List.of(answer))
                .build();

        mockMvc.perform(post("/api/questions")
                        
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftRequest)))
                .andExpect(status().isCreated());

        // When & Then - filter by DRAFT status
        mockMvc.perform(get("/api/questions")

                        .param("statuses", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("draft"));
    }
}
