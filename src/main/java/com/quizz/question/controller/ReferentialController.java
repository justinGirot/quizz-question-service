package com.quizz.question.controller;

import com.quizz.question.common.constants.ApiConstants;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.DifficultyLevelDTO;
import com.quizz.question.service.CategoryService;
import com.quizz.question.service.DifficultyLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Referential Data
 * Provides access to categories and difficulty levels
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/question/referentiel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Referential", description = "Referential data API (categories, difficulty levels)")
@SecurityRequirement(name = "cookieAuth")
public class ReferentialController {

    private final CategoryService categoryService;
    private final DifficultyLevelService difficultyLevelService;

    @GetMapping("/categories")
    @Operation(summary = "Get active categories", description = "Retrieves all active categories for question creation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        log.info("Fetching categories from referentiel");
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/difficulty-levels")
    @Operation(summary = "Get all difficulty levels", description = "Retrieves all difficulty levels for question creation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Difficulty levels retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = DifficultyLevelDTO.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<DifficultyLevelDTO>> getDifficultyLevels() {
        log.info("Fetching difficulty levels from referentiel");
        List<DifficultyLevelDTO> levels = difficultyLevelService.getAllDifficultyLevels();
        return ResponseEntity.ok(levels);
    }
}
