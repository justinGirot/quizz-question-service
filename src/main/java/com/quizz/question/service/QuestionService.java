package com.quizz.question.service;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.QuestionStatus;

import java.util.List;

public interface QuestionService {

    /**
     * Create a new question
     */
    QuestionDTO createQuestion(CreateQuestionRequest request, Long userId);

    /**
     * Get question by ID
     */
    QuestionDTO getQuestionById(Long id);

    /**
     * Get all questions with optional filters
     */
    List<QuestionDTO> getQuestions(List<QuestionStatus> statuses, List<String> categories);

    // NOTE: Category management moved to CategoryService

    /**
     * Update a question
     */
    QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request, Long userId, boolean isAdmin);

    /**
     * Delete a question (only DRAFT status allowed)
     */
    void deleteQuestion(Long id, Long userId, boolean isAdmin);
}
