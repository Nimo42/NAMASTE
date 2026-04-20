package com.ayush.terminology.namaste.repo;

import com.ayush.terminology.namaste.model.SystemVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SystemVersionRepository extends JpaRepository<SystemVersion, Long> {
    List<SystemVersion> findBySystemIdOrderByImportedAtDesc(Long systemId);
}
