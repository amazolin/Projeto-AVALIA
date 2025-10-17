package com.example.repository;

import com.example.model.OpcaoQuestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 

@Repository 
public interface OpcaoQuestaoRepository extends JpaRepository<OpcaoQuestao, Integer> {
    
  
}