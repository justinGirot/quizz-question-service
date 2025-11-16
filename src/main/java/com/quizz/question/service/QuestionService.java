package com.quizz.question.service;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.security.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionService {

    /**
     * Create a new question
     */
    QuestionDTO createQuestion(CreateQuestionRequest request, UserContext userContext);

    /**
     * Get question by ID
     */
    QuestionDTO getQuestionById(Long id);

    /**
     * Get all questions with optional filters
     */
    List<QuestionDTO> getQuestions(List<QuestionStatus> statuses, List<Long> categoryIds);

    /**
     * Get questions with pagination and optional filters
     */
    Page<QuestionDTO> getQuestionsPageable(List<QuestionStatus> statuses, List<Long> categoryIds, Pageable pageable);

    // NOTE: Category management moved to CategoryService

    /**
     * Update a question
     */
    QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request, UserContext userContext);

    /**
     * Delete a question (only DRAFT status allowed)
     */
    void deleteQuestion(Long id, UserContext userContext);
}
