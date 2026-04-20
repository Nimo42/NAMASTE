package com.ayush.terminology.namaste.repo;

import com.ayush.terminology.namaste.model.MlFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MlFeedbackRepository extends JpaRepository<MlFeedback, Long> {
    List<MlFeedback> findAllByOrderByCreatedAtDesc();
    List<MlFeedback> findByReviewedFalseOrderByCreatedAtDesc();
    long countByReviewed(Boolean reviewed);
    long countByAdminDecision(String adminDecision);
    long countByFeedbackType(String feedbackType);
}
