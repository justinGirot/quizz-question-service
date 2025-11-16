package com.quizz.question.repository;

import com.quizz.question.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // Basic CRUD operations are inherited from JpaRepository
}
