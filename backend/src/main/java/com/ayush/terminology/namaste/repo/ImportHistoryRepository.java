package com.ayush.terminology.namaste.repo;

import com.ayush.terminology.namaste.model.ImportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    Page<ImportHistory> findByIsDeletedFalseOrderByImportTimeDesc(Pageable pageable);
    long countByIsDeletedFalse();
}
