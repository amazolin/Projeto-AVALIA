package com.example.repository;

import com.example.model.SimuladoQuestao;
import com.example.model.SimuladoQuestaoId; 
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimuladoQuestaoRepository extends JpaRepository<SimuladoQuestao, SimuladoQuestaoId> {
    
}