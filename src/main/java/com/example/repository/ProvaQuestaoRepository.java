package com.example.repository;

import com.example.model.ProvaQuestao;
import com.example.model.ProvaQuestaoId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProvaQuestaoRepository extends JpaRepository<ProvaQuestao, ProvaQuestaoId> {
    
}