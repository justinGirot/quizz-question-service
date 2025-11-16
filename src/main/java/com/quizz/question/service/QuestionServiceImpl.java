package com.quizz.question.service;

import com.quizz.question.client.GroupServiceClient;
import com.quizz.question.common.constants.ErrorConstants;
import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.exception.ForbiddenException;
import com.quizz.question.exception.QuestionNotFoundException;
import com.quizz.question.exception.ValidationException;
import com.quizz.question.mapper.QuestionMapper;
import com.quizz.question.model.Category;
import com.quizz.question.model.DifficultyLevel;
import com.quizz.question.model.Question;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.model.QuestionVisibility;
import com.quizz.question.repository.CategoryRepository;
import com.quizz.question.repository.DifficultyLevelRepository;
import com.quizz.question.repository.QuestionRepository;
import com.quizz.question.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CategoryRepository categoryRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final QuestionMapper questionMapper;
    private final GroupServiceClient groupServiceClient;

    @Override
    @Transactional
    public QuestionDTO createQuestion(CreateQuestionRequest request, UserContext userContext) {
        log.info("Creating question for user: {} (groupId: {})", userContext.getUserId(), request.getGroupId());

        // Validate group access if groupId is provided
        if (request.getGroupId() != null) {
            validateGroupAccess(request.getGroupId(), userContext.getUserId());
        }

        // Validate visibility rules
        validateVisibility(request.getGroupId(), request.getVisibility());

        Question question = questionMapper.toEntity(request, userContext.getUserId());

        // Resolve and set category FK reference (only categoryId, no auto-creation)
        Category category = resolveCategoryReference(request);
        question.setCategory(category);

        // Resolve and set difficulty level FK reference
        DifficultyLevel difficultyLevel = resolveDifficultyLevelReference(request);
        question.setDifficultyLevel(difficultyLevel);

        Question savedQuestion = questionRepository.save(question);

        log.info("Question created with ID: {} (group: {}, visibility: {})",
                 savedQuestion.getId(), savedQuestion.getGroupId(), savedQuestion.getVisibility());
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
    public List<QuestionDTO> getQuestions(List<QuestionStatus> statuses, List<Long> categoryIds) {
        log.info("Fetching questions with filters - statuses: {}, categoryIds: {}", statuses, categoryIds);

        // Convert empty lists to null for the query
        List<QuestionStatus> statusFilter = (statuses != null && !statuses.isEmpty()) ? statuses : null;
        List<Long> categoryFilter = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds : null;

        List<Question> questions = questionRepository.findByFilters(statusFilter, categoryFilter);

        log.info("Found {} questions", questions.size());
        return questionMapper.toDTOList(questions);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDTO> getQuestionsPageable(List<QuestionStatus> statuses, List<Long> categoryIds, Pageable pageable) {
        log.info("Fetching questions with pagination - statuses: {}, categoryIds: {}, page: {}",
                 statuses, categoryIds, pageable.getPageNumber());

        // Convert empty lists to null for the query
        List<QuestionStatus> statusFilter = (statuses != null && !statuses.isEmpty()) ? statuses : null;
        List<Long> categoryFilter = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds : null;

        Page<Question> questionsPage = questionRepository.findByFiltersPageable(statusFilter, categoryFilter, pageable);

        log.info("Found {} questions on page {} of {}",
                 questionsPage.getNumberOfElements(),
                 questionsPage.getNumber(),
                 questionsPage.getTotalPages());

        return questionsPage.map(questionMapper::toDTO);
    }

    // NOTE: getAllCategories() removed - use CategoryService.getActiveCategories() instead

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request, UserContext userContext) {
        log.info("Updating question {} by user {} (admin: {})", id, userContext.getUserId(), userContext.isAdmin());

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(String.format(ErrorConstants.QUESTION_NOT_FOUND, id)));

        // Authorization check
        if (!userContext.canModify(question.getCreatedBy())) {
            throw new ForbiddenException(ErrorConstants.FORBIDDEN_UPDATE);
        }

        // If changing group, validate access to new group
        if (request.getGroupId() != null &&
            !java.util.Objects.equals(request.getGroupId(), question.getGroupId())) {
            validateGroupAccess(request.getGroupId(), userContext.getUserId());
        }

        // Validate visibility rules
        validateVisibility(request.getGroupId(), request.getVisibility());

        // Validation: Cannot edit non-DRAFT questions unless changing status only
        if (question.getStatus() != QuestionStatus.DRAFT && !isStatusOnlyChange(question, request)) {
            throw new ValidationException(ErrorConstants.ONLY_DRAFT_EDITABLE);
        }

        // Validate status transitions
        validateStatusTransition(question.getStatus(), request.getStatus(), userContext.isAdmin());

        // Update the question
        questionMapper.updateEntityFromDTO(question, request);

        // Resolve and update category FK reference if provided
        if (request.getCategoryId() != null) {
            Category category = resolveCategoryReference(request);
            question.setCategory(category);
        }

        // Resolve and update difficulty level FK reference if provided
        if (request.getDifficultyLevelId() != null) {
            DifficultyLevel difficultyLevel = resolveDifficultyLevelReference(request);
            question.setDifficultyLevel(difficultyLevel);
        }

        Question updatedQuestion = questionRepository.save(question);

        log.info("Question {} updated successfully", id);
        return questionMapper.toDTO(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id, UserContext userContext) {
        log.info("Deleting question {} by user {} (admin: {})", id, userContext.getUserId(), userContext.isAdmin());

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(String.format(ErrorConstants.QUESTION_NOT_FOUND, id)));

        // Authorization check
        if (!userContext.canModify(question.getCreatedBy())) {
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
        // Check if only status changed (all other fields remain the same)
        boolean textUnchanged = java.util.Objects.equals(existing.getText(), request.getText());
        boolean typeUnchanged = existing.getType() == request.getType();
        boolean pointsUnchanged = java.util.Objects.equals(existing.getPoints(), request.getPoints());

        // Check category unchanged (either no category provided in request, or same ID)
        boolean categoryUnchanged = (request.getCategoryId() == null && request.getCategoryName() == null) ||
                                    (request.getCategoryId() != null &&
                                     existing.getCategory() != null &&
                                     existing.getCategory().getId().equals(request.getCategoryId()));

        // Check difficulty unchanged (either no difficulty provided, or same ID)
        boolean difficultyUnchanged = request.getDifficultyLevelId() == null ||
                                      (existing.getDifficultyLevel() != null &&
                                       existing.getDifficultyLevel().getId().equals(request.getDifficultyLevelId()));

        return textUnchanged && typeUnchanged && pointsUnchanged && categoryUnchanged && difficultyUnchanged;
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

    /**
     * Validate user has access to group
     */
    private void validateGroupAccess(Long groupId, Long userId) {
        if (groupId == null) {
            return; // Public question, no validation needed
        }

        log.debug("Validating group access: groupId={}, userId={}", groupId, userId);
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
     * Resolve category reference from request
     * Only handles categoryId (existing category) - no auto-creation
     * Note: categoryName field is deprecated, only admins can create categories
     *
     * @param request CreateQuestionRequest or UpdateQuestionRequest
     * @return Category entity
     */
    private Category resolveCategoryReference(Object request) {
        final Long categoryId;

        // Extract categoryId from request
        if (request instanceof CreateQuestionRequest) {
            categoryId = ((CreateQuestionRequest) request).getCategoryId();
        } else if (request instanceof UpdateQuestionRequest) {
            categoryId = ((UpdateQuestionRequest) request).getCategoryId();
        } else {
            categoryId = null;
        }

        // Category ID is required
        if (categoryId == null) {
            throw new ValidationException("Category ID is required");
        }

        log.debug("Looking up category by ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ValidationException("Category not found with ID: " + categoryId));
    }

    /**
     * Resolve difficulty level reference from request
     *
     * @param request CreateQuestionRequest or UpdateQuestionRequest
     * @return DifficultyLevel
     */
    private DifficultyLevel resolveDifficultyLevelReference(Object request) {
        final Long difficultyLevelId;

        // Extract difficultyLevelId from request
        if (request instanceof CreateQuestionRequest) {
            difficultyLevelId = ((CreateQuestionRequest) request).getDifficultyLevelId();
        } else if (request instanceof UpdateQuestionRequest) {
            difficultyLevelId = ((UpdateQuestionRequest) request).getDifficultyLevelId();
        } else {
            difficultyLevelId = null;
        }

        // Difficulty level ID is required
        if (difficultyLevelId == null) {
            throw new ValidationException("Difficulty level ID is required");
        }

        log.debug("Looking up difficulty level by ID: {}", difficultyLevelId);
        final Long finalDifficultyLevelId = difficultyLevelId;
        return difficultyLevelRepository.findById(difficultyLevelId)
                .orElseThrow(() -> new ValidationException("Difficulty level not found with ID: " + finalDifficultyLevelId));
    }
}
