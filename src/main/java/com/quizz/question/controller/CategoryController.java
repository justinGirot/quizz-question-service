package com.quizz.question.controller;

import com.quizz.question.common.constants.ApiConstants;
import com.quizz.question.dto.CategoryDTO;
import com.quizz.question.dto.CreateCategoryRequest;
import com.quizz.question.dto.UpdateCategoryRequest;
import com.quizz.question.security.JwtAuthentication;
import com.quizz.question.service.CategoryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Category management (Admin only)
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management API (Admin only)")
@SecurityRequirement(name = "cookieAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/active")
    @Operation(summary = "Get active categories",
               description = "Retrieves active categories. If groupId provided, returns public + group-specific categories. Otherwise, returns only public categories.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryDTO>> getActiveCategories(
            @Parameter(description = "Optional group ID to get group-specific categories")
            @RequestParam(required = false) Long groupId) {

        log.info("Fetching active categories (groupId: {})", groupId);

        List<CategoryDTO> categories;
        if (groupId != null) {
            categories = categoryService.getAccessibleCategories(groupId);
        } else {
            categories = categoryService.getPublicCategories();
        }

        return ResponseEntity.ok(categories);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all categories", description = "Retrieves all categories (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.info("Fetching all categories (admin)");
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping(ApiConstants.ID_PATH_PARAM)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by ID (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found",
                content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<CategoryDTO> getCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Fetching category with ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create category", description = "Creates a new category (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully",
                content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or category already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            Authentication authentication) {

        JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
        Long adminId = jwtAuth.getUserId();

        log.info("Creating category by admin: {}", adminId);
        CategoryDTO createdCategory = categoryService.createCategory(request, adminId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping(ApiConstants.ID_PATH_PARAM)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Updates a category (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
                content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        log.info("Updating category: {}", id);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping(ApiConstants.ID_PATH_PARAM)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Deletes a category (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<Map<String, String>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {

        log.info("Deleting category: {}", id);
        categoryService.deleteCategory(id);

        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }
}
