package com.quizz.question.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Question visibility enum
 * - PUBLIC: Anyone can use this question
 * - PRIVATE: Only group members can use this question
 */
public enum QuestionVisibility {
    @JsonProperty("public")
    PUBLIC("public"),

    @JsonProperty("private")
    PRIVATE("private");

    private final String value;

    QuestionVisibility(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuestionVisibility fromValue(String value) {
        for (QuestionVisibility visibility : QuestionVisibility.values()) {
            if (visibility.value.equalsIgnoreCase(value)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Invalid QuestionVisibility value: " + value);
    }
}
