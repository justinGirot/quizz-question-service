package com.quizz.question.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionStatus {
    @JsonProperty("draft")
    DRAFT("draft"),

    @JsonProperty("pending")
    PENDING("pending"),

    @JsonProperty("validated")
    VALIDATED("validated"),

    @JsonProperty("rejected")
    REJECTED("rejected"),

    @JsonProperty("archived")
    ARCHIVED("archived");

    private final String value;

    QuestionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuestionStatus fromValue(String value) {
        for (QuestionStatus status : QuestionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid QuestionStatus value: " + value);
    }
}
