package com.quizz.question.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Encapsulates authenticated user context information
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private Long userId;
    private boolean isAdmin;

    /**
     * Check if the user is the owner of a resource
     */
    public boolean isOwner(Long resourceOwnerId) {
        return userId != null && userId.equals(resourceOwnerId);
    }

    /**
     * Check if the user can modify a resource (either owner or admin)
     */
    public boolean canModify(Long resourceOwnerId) {
        return isAdmin || isOwner(resourceOwnerId);
    }
}
