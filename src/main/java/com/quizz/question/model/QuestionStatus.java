package com.quizz.question.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestionStatus {
    @JsonProperty("draft")
    DRAFT,

    @JsonProperty("pending")
    PENDING,

    @JsonProperty("validated")
    VALIDATED,

    @JsonProperty("rejected")
    REJECTED,

    @JsonProperty("archived")
    ARCHIVED
}
