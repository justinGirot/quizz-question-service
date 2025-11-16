package com.quizz.question.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Client for communicating with Group Service
 */
@Component
@Slf4j
public class GroupServiceClient {

    private final RestTemplate restTemplate;
    private final String groupServiceUrl;

    public GroupServiceClient(
            RestTemplate restTemplate,
            @Value("${group.service.url:http://localhost:8083}") String groupServiceUrl) {
        this.restTemplate = restTemplate;
        this.groupServiceUrl = groupServiceUrl;
    }

    /**
     * Check if user is member of a group
     */
    public boolean isMemberOfGroup(Long groupId, Long userId) {
        try {
            String url = groupServiceUrl + "/api/groups/" + groupId + "/members/" + userId;
            log.debug("Checking group membership: groupId={}, userId={}", groupId, userId);

            MembershipResponse response = restTemplate.getForObject(url, MembershipResponse.class);
            boolean isMember = response != null && response.isMember();

            log.debug("Group membership check result: {}", isMember);
            return isMember;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Group or member not found: groupId={}, userId={}", groupId, userId);
            return false;
        } catch (Exception e) {
            log.error("Failed to check group membership: groupId={}, userId={}", groupId, userId, e);
            throw new RuntimeException("Failed to check group membership", e);
        }
    }

    /**
     * Get group name
     */
    public String getGroupName(Long groupId) {
        try {
            String url = groupServiceUrl + "/api/groups/" + groupId;
            log.debug("Fetching group name for groupId={}", groupId);

            GroupResponse response = restTemplate.getForObject(url, GroupResponse.class);
            String groupName = response != null ? response.getName() : null;

            log.debug("Group name: {}", groupName);
            return groupName;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Group not found: groupId={}", groupId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch group name: groupId={}", groupId, e);
            return null;
        }
    }

    /**
     * Get all groups user is member of
     */
    public List<Long> getUserGroupIds(Long userId) {
        try {
            String url = groupServiceUrl + "/api/groups/user/" + userId + "/groups";
            log.debug("Fetching user groups for userId={}", userId);

            // This endpoint would need to be added to Group Service
            // For now, return empty list as fallback
            UserGroupsResponse response = restTemplate.getForObject(url, UserGroupsResponse.class);
            List<Long> groupIds = response != null ? response.getGroupIds() : Collections.emptyList();

            log.debug("User belongs to {} groups", groupIds.size());
            return groupIds;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User groups not found: userId={}", userId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch user groups, returning empty list: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    // Inner classes for responses
    public static class MembershipResponse {
        private boolean isMember;
        private String role;

        public MembershipResponse() {}

        public boolean isMember() {
            return isMember;
        }

        public void setMember(boolean member) {
            isMember = member;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class GroupResponse {
        private Long id;
        private String name;

        public GroupResponse() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class UserGroupsResponse {
        private List<Long> groupIds;

        public UserGroupsResponse() {}

        public List<Long> getGroupIds() {
            return groupIds != null ? groupIds : Collections.emptyList();
        }

        public void setGroupIds(List<Long> groupIds) {
            this.groupIds = groupIds;
        }
    }
}
