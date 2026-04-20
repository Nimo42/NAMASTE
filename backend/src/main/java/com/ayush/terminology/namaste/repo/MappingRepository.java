package com.ayush.terminology.namaste.repo;


import com.ayush.terminology.namaste.mappings.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MappingRepository extends JpaRepository<Mapping, Long> {


    List<Mapping> findByFromConceptId(Long conceptId);


    List<Mapping> findByToConceptId(Long conceptId);


    List<Mapping> findByFromConceptIdAndToConceptId(Long fromId, Long toId);
}
