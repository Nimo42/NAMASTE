package com.ayush.terminology.namaste.repo;





import com.ayush.terminology.namaste.model.CodeSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CodeSystemRepository extends JpaRepository<CodeSystem, Long> {



    Optional<CodeSystem> findByName(String name);
    Optional<CodeSystem> findByNameAndIsActiveTrue(String name);
}
