package com.quizz.question.service;

import com.quizz.question.common.constants.ErrorConstants;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.exception.ForbiddenException;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Question management
 * Implements business logic with validation and authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuestionDTO createQuestion(CreateQuestionRequest request, Long userId) {
        log.info("Creating question for user: {}", userId);

        Question question = questionMapper.toEntity(request, userId);
        Question savedQuestion = questionRepository.save(question);

        log.info("Question created with ID: {}", savedQuestion.getId());
        return questionMapper.toDTO(savedQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long id) {
        log.info("Fetching question with ID: {}", id);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(String.format(ErrorConstants.QUESTION_NOT_FOUND, id)));

        return questionMapper.toDTO(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestions(List<QuestionStatus> statuses, List<String> categories) {
        log.info("Fetching questions with filters - statuses: {}, categories: {}", statuses, categories);

        // Convert empty lists to null for the query
        List<QuestionStatus> statusFilter = (statuses != null && !statuses.isEmpty()) ? statuses : null;
        List<String> categoryFilter = (categories != null && !categories.isEmpty()) ? categories : null;

        List<Question> questions = questionRepository.findByFilters(statusFilter, categoryFilter);

        log.info("Found {} questions", questions.size());
        return questionMapper.toDTOList(questions);
    }

    // NOTE: getAllCategories() removed - use CategoryService.getActiveCategories() instead

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request, Long userId, boolean isAdmin) {
        log.info("Updating question {} by user {} (admin: {})", id, userId, isAdmin);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(String.format(ErrorConstants.QUESTION_NOT_FOUND, id)));

        // Authorization check
        if (!isAdmin && !question.getCreatedBy().equals(userId)) {
            throw new ForbiddenException(ErrorConstants.FORBIDDEN_UPDATE);
        }

        // Validation: Cannot edit non-DRAFT questions unless changing status only
        if (question.getStatus() != QuestionStatus.DRAFT && !isStatusOnlyChange(question, request)) {
            throw new ValidationException(ErrorConstants.ONLY_DRAFT_EDITABLE);
        }

        // Validate status transitions
        validateStatusTransition(question.getStatus(), request.getStatus(), isAdmin);

        // Update the question
        questionMapper.updateEntityFromDTO(question, request);
        Question updatedQuestion = questionRepository.save(question);

        log.info("Question {} updated successfully", id);
        return questionMapper.toDTO(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id, Long userId, boolean isAdmin) {
        log.info("Deleting question {} by user {} (admin: {})", id, userId, isAdmin);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(String.format(ErrorConstants.QUESTION_NOT_FOUND, id)));

        // Authorization check
        if (!isAdmin && !question.getCreatedBy().equals(userId)) {
            throw new ForbiddenException(ErrorConstants.FORBIDDEN_DELETE);
        }

        // Only DRAFT questions can be deleted
        if (question.getStatus() != QuestionStatus.DRAFT) {
            throw new ValidationException(ErrorConstants.ONLY_DRAFT_DELETABLE);
        }

        questionRepository.delete(question);
        log.info("Question {} deleted successfully", id);
    }

    private boolean isStatusOnlyChange(Question existing, UpdateQuestionRequest request) {
        return existing.getText().equals(request.getText()) &&
               existing.getType() == request.getType() &&
               existing.getCategory().equals(request.getCategory()) &&
               existing.getDifficulty() == request.getDifficulty() &&
               existing.getPoints().equals(request.getPoints());
    }

    private void validateStatusTransition(QuestionStatus currentStatus, QuestionStatus newStatus, boolean isAdmin) {
        // If status hasn't changed, no validation needed
        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case DRAFT:
                // DRAFT can go to PENDING (by creator) or directly to VALIDATED/ARCHIVED (by admin)
                if (newStatus != QuestionStatus.PENDING && !isAdmin) {
                    throw new ValidationException("DRAFT questions can only be moved to PENDING by non-admin users");
                }
                break;

            case PENDING:
                // PENDING can go back to DRAFT (by creator) or to VALIDATED/REJECTED/ARCHIVED (by admin)
                if (newStatus == QuestionStatus.DRAFT) {
                    // Allowed for both creator and admin
                } else if (!isAdmin) {
                    throw new ValidationException("Only admins can validate, reject, or archive questions");
                }
                break;

            case VALIDATED:
            case REJECTED:
            case ARCHIVED:
                // Archived statuses can only be changed by admin
                if (!isAdmin) {
                    throw new ValidationException("Only admins can modify archived questions");
                }
                break;

            default:
                throw new ValidationException("Invalid status transition");
        }
    }
}
