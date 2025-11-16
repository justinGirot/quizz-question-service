package com.quizz.question.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizz.question.common.constants.ApiConstants;
import com.quizz.question.config.RequestSanitizationInterceptor;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.exception.ForbiddenException;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionType;
import com.quizz.question.security.JwtAuthentication;
import com.quizz.question.security.UserContext;
import com.quizz.question.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@DisplayName("QuestionController Unit Tests")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private RequestSanitizationInterceptor sanitizationInterceptor;

    @MockBean
    private com.quizz.question.security.JwtUtil jwtUtil;

    private QuestionDTO questionDTO;
    private CreateQuestionRequest createRequest;
    private UpdateQuestionRequest updateRequest;
    private JwtAuthentication userAuth;
    private JwtAuthentication adminAuth;

    @BeforeEach
    void setUp() {
        // Setup test data
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Java")
                .build();

        DifficultyLevelDTO difficultyDTO = DifficultyLevelDTO.builder()
                .id(1L)
                .name("Easy")
                .pointsMultiplier(1.0)
                .build();

        questionDTO = QuestionDTO.builder()
                .id(1L)
                .text("What is Java?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .points(10)
                .categoryRef(categoryDTO)
                .difficultyLevel(difficultyDTO)
                .status(QuestionStatus.DRAFT)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateQuestionRequest();
        createRequest.setText("What is Java?");
        createRequest.setType(QuestionType.MULTIPLE_CHOICE);
        createRequest.setPoints(10);
        createRequest.setCategoryId(1L);
        createRequest.setDifficultyLevelId(1L);

        updateRequest = new UpdateQuestionRequest();
        updateRequest.setText("What is Java updated?");
        updateRequest.setType(QuestionType.MULTIPLE_CHOICE);
        updateRequest.setPoints(15);
        updateRequest.setStatus(QuestionStatus.DRAFT);
        updateRequest.setCategoryId(1L);
        updateRequest.setDifficultyLevelId(1L);

        // Setup authentication
        userAuth = new JwtAuthentication(1L, "user@test.com",
                List.of("ROLE_USER"));

        adminAuth = new JwtAuthentication(2L, "admin@test.com",
                List.of("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should create question successfully")
    void shouldCreateQuestionSuccessfully() throws Exception {
        // Given
        when(questionService.createQuestion(any(CreateQuestionRequest.class), any(UserContext.class)))
                .thenReturn(questionDTO);
        doNothing().when(sanitizationInterceptor).sanitize(any(CreateQuestionRequest.class));

        // When & Then
        mockMvc.perform(post(ApiConstants.QUESTIONS_PATH)
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("What is Java?"))
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.points").value(10))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(questionService).createQuestion(any(CreateQuestionRequest.class), any(UserContext.class));
        verify(sanitizationInterceptor).sanitize(any(CreateQuestionRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when creating question without authentication")
    void shouldReturn401WhenCreatingQuestionWithoutAuthentication() throws Exception {
        mockMvc.perform(post(ApiConstants.QUESTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(questionService, never()).createQuestion(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when creating question with invalid data")
    void shouldReturn400WhenCreatingQuestionWithInvalidData() throws Exception {
        // Given
        CreateQuestionRequest invalidRequest = new CreateQuestionRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post(ApiConstants.QUESTIONS_PATH)
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService, never()).createQuestion(any(), any());
    }

    @Test
    @DisplayName("Should get question by ID successfully")
    void shouldGetQuestionByIdSuccessfully() throws Exception {
        // Given
        when(questionService.getQuestionById(1L)).thenReturn(questionDTO);

        // When & Then
        mockMvc.perform(get(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("What is Java?"))
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"));

        verify(questionService).getQuestionById(1L);
    }

    @Test
    @DisplayName("Should return 404 when question not found")
    void shouldReturn404WhenQuestionNotFound() throws Exception {
        // Given
        when(questionService.getQuestionById(999L))
                .thenThrow(new QuestionNotFoundException("Question not found with ID: 999"));

        // When & Then
        mockMvc.perform(get(ApiConstants.QUESTIONS_PATH + "/999")
                        .with(authentication(userAuth)))
                .andExpect(status().isNotFound());

        verify(questionService).getQuestionById(999L);
    }

    @Test
    @DisplayName("Should get questions with pagination")
    void shouldGetQuestionsWithPagination() throws Exception {
        // Given
        Page<QuestionDTO> questionsPage = new PageImpl<>(
                Arrays.asList(questionDTO),
                PageRequest.of(0, 10),
                1
        );
        when(questionService.getQuestionsPageable(any(), any(), any())).thenReturn(questionsPage);

        // When & Then
        mockMvc.perform(get(ApiConstants.QUESTIONS_PATH)
                        .with(authentication(userAuth))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(questionService).getQuestionsPageable(any(), any(), any());
    }

    @Test
    @DisplayName("Should get questions with status filter")
    void shouldGetQuestionsWithStatusFilter() throws Exception {
        // Given
        Page<QuestionDTO> questionsPage = new PageImpl<>(
                Arrays.asList(questionDTO),
                PageRequest.of(0, 10),
                1
        );
        when(questionService.getQuestionsPageable(any(), any(), any())).thenReturn(questionsPage);

        // When & Then
        mockMvc.perform(get(ApiConstants.QUESTIONS_PATH)
                        .with(authentication(userAuth))
                        .param(ApiConstants.STATUSES_PARAM, "DRAFT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(questionService).getQuestionsPageable(any(), any(), any());
    }

    @Test
    @DisplayName("Should get questions with category filter")
    void shouldGetQuestionsWithCategoryFilter() throws Exception {
        // Given
        Page<QuestionDTO> questionsPage = new PageImpl<>(
                Arrays.asList(questionDTO),
                PageRequest.of(0, 10),
                1
        );
        when(questionService.getQuestionsPageable(any(), any(), any())).thenReturn(questionsPage);

        // When & Then
        mockMvc.perform(get(ApiConstants.QUESTIONS_PATH)
                        .with(authentication(userAuth))
                        .param("categoryIds", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(questionService).getQuestionsPageable(any(), any(), any());
    }

    @Test
    @DisplayName("Should update question successfully")
    void shouldUpdateQuestionSuccessfully() throws Exception {
        // Given
        QuestionDTO updatedDTO = QuestionDTO.builder()
                .id(1L)
                .text("What is Java updated?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .points(15)
                .status(QuestionStatus.DRAFT)
                .build();
        when(questionService.updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class)))
                .thenReturn(updatedDTO);
        doNothing().when(sanitizationInterceptor).sanitize(any(UpdateQuestionRequest.class));

        // When & Then
        mockMvc.perform(put(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("What is Java updated?"))
                .andExpect(jsonPath("$.points").value(15));

        verify(questionService).updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class));
        verify(sanitizationInterceptor).sanitize(any(UpdateQuestionRequest.class));
    }

    @Test
    @DisplayName("Should return 403 when updating question without permission")
    void shouldReturn403WhenUpdatingQuestionWithoutPermission() throws Exception {
        // Given
        when(questionService.updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class)))
                .thenThrow(new ForbiddenException("Not authorized to update this question"));

        // When & Then
        mockMvc.perform(put(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(questionService).updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class));
    }

    @Test
    @DisplayName("Should return 400 when updating with invalid status transition")
    void shouldReturn400WhenUpdatingWithInvalidStatusTransition() throws Exception {
        // Given
        when(questionService.updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class)))
                .thenThrow(new ValidationException("Invalid status transition"));

        // When & Then
        mockMvc.perform(put(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(questionService).updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class));
    }

    @Test
    @DisplayName("Should delete question successfully")
    void shouldDeleteQuestionSuccessfully() throws Exception {
        // Given
        doNothing().when(questionService).deleteQuestion(eq(1L), any(UserContext.class));

        // When & Then
        mockMvc.perform(delete(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ApiConstants.QUESTION_DELETED_MESSAGE));

        verify(questionService).deleteQuestion(eq(1L), any(UserContext.class));
    }

    @Test
    @DisplayName("Should return 403 when deleting question without permission")
    void shouldReturn403WhenDeletingQuestionWithoutPermission() throws Exception {
        // Given
        doThrow(new ForbiddenException("Not authorized to delete this question"))
                .when(questionService).deleteQuestion(eq(1L), any(UserContext.class));

        // When & Then
        mockMvc.perform(delete(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());

        verify(questionService).deleteQuestion(eq(1L), any(UserContext.class));
    }

    @Test
    @DisplayName("Should return 400 when deleting non-DRAFT question")
    void shouldReturn400WhenDeletingNonDraftQuestion() throws Exception {
        // Given
        doThrow(new ValidationException("Only DRAFT questions can be deleted"))
                .when(questionService).deleteQuestion(eq(1L), any(UserContext.class));

        // When & Then
        mockMvc.perform(delete(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(userAuth)))
                .andExpect(status().isBadRequest());

        verify(questionService).deleteQuestion(eq(1L), any(UserContext.class));
    }

    @Test
    @DisplayName("Should admin be able to update any question")
    void shouldAdminBeAbleToUpdateAnyQuestion() throws Exception {
        // Given
        QuestionDTO updatedDTO = QuestionDTO.builder()
                .id(1L)
                .text("What is Java updated?")
                .build();
        when(questionService.updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class)))
                .thenReturn(updatedDTO);
        doNothing().when(sanitizationInterceptor).sanitize(any(UpdateQuestionRequest.class));

        // When & Then
        mockMvc.perform(put(ApiConstants.QUESTIONS_PATH + "/1")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(questionService).updateQuestion(eq(1L), any(UpdateQuestionRequest.class), any(UserContext.class));
    }
}
