package com.quizz.question.controller;

import com.quizz.question.dto.CreateQuestionRequest;
import com.quizz.question.dto.QuestionDTO;
import com.quizz.question.dto.UpdateQuestionRequest;
import com.quizz.question.model.QuestionStatus;
import com.quizz.question.security.JwtAuthentication;
import com.quizz.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Questions", description = "Question management API")
@SecurityRequirement(name = "cookieAuth")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @Operation(summary = "Create a new question", description = "Creates a new question in DRAFT status")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Question created successfully",
                content = @Content(schema = @Schema(implementation = QuestionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            Authentication authentication) {

        JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
        Long userId = jwtAuth.getUserId();

        log.info("Creating question for user: {}", userId);
        QuestionDTO createdQuestion = questionService.createQuestion(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Retrieves a question by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Question found",
                content = @Content(schema = @Schema(implementation = QuestionDTO.class))),
        @ApiResponse(responseCode = "404", description = "Question not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<QuestionDTO> getQuestion(
            @Parameter(description = "Question ID") @PathVariable Long id) {

        log.info("Fetching question with ID: {}", id);
        QuestionDTO question = questionService.getQuestionById(id);

        return ResponseEntity.ok(question);
    }

    @GetMapping
    @Operation(summary = "List questions", description = "Retrieves all questions with optional filtering by status and category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Questions retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionDTO.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<QuestionDTO>> getQuestions(
            @Parameter(description = "Filter by question statuses")
            @RequestParam(required = false, name = "statuses[]") List<QuestionStatus> statuses,

            @Parameter(description = "Filter by categories")
            @RequestParam(required = false, name = "categories[]") List<String> categories) {

        log.info("Fetching questions with filters - statuses: {}, categories: {}", statuses, categories);
        List<QuestionDTO> questions = questionService.getQuestions(statuses, categories);

        return ResponseEntity.ok(questions);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieves all unique question categories")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<String>> getCategories() {
        log.info("Fetching all categories");
        List<String> categories = questionService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question", description = "Updates an existing question. Only DRAFT questions can be fully edited.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Question updated successfully",
                content = @Content(schema = @Schema(implementation = QuestionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or status transition"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not the owner or admin"),
        @ApiResponse(responseCode = "404", description = "Question not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<QuestionDTO> updateQuestion(
            @Parameter(description = "Question ID") @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequest request,
            Authentication authentication) {

        JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
        Long userId = jwtAuth.getUserId();
        boolean isAdmin = jwtAuth.isAdmin();

        log.info("Updating question {} by user {} (admin: {})", id, userId, isAdmin);
        QuestionDTO updatedQuestion = questionService.updateQuestion(id, request, userId, isAdmin);

        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question", description = "Deletes a question. Only DRAFT questions can be deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Question deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete non-DRAFT question"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not the owner or admin"),
        @ApiResponse(responseCode = "404", description = "Question not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> deleteQuestion(
            @Parameter(description = "Question ID") @PathVariable Long id,
            Authentication authentication) {

        JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
        Long userId = jwtAuth.getUserId();
        boolean isAdmin = jwtAuth.isAdmin();

        log.info("Deleting question {} by user {} (admin: {})", id, userId, isAdmin);
        questionService.deleteQuestion(id, userId, isAdmin);

        return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
    }
}
