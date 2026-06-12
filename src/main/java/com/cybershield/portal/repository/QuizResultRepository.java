package com.cybershield.portal.repository;

import com.cybershield.portal.model.QuizResult;
import com.cybershield.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserOrderByDateCompletedDesc(User user);
    Optional<QuizResult> findByCertificateUuid(String certificateUuid);
}
