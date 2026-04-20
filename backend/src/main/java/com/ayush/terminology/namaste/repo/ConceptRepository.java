package com.ayush.terminology.namaste.repo;




import com.ayush.terminology.namaste.model.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, Long> {

    Optional<Concept> findByCode(String code);


    List<Concept> findByDisplayNameContainingIgnoreCase(String displayName);


    @Query("SELECT c FROM Concept c WHERE c.code = :code AND c.codeSystem.name = :systemName")
    Optional<Concept> findByCodeAndSystem(@Param("code") String code, @Param("systemName") String systemName);

    List<Concept> findByCodeSystemId(Long codeSystemId);
}
