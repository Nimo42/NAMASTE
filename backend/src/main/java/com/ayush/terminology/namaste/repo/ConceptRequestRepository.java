package com.ayush.terminology.namaste.repo;

import com.ayush.terminology.namaste.model.ConceptRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConceptRequestRepository extends JpaRepository<ConceptRequest, Long> {
    List<ConceptRequest> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
    List<ConceptRequest> findAllByOrderByCreatedAtDesc();
    List<ConceptRequest> findByStatusOrderByCreatedAtDesc(String status);
    long countByStatus(String status);
}
