package com.quizz.question.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum QuestionType {
    @JsonProperty("multiple-choice")
    MULTIPLE_CHOICE,

    @JsonProperty("text-input")
    TEXT_INPUT
}
