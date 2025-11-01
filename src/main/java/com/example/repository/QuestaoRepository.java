package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.Questao;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    
    // Busca questões por disciplina
    List<Questao> findByDisciplinaId(Long disciplinaId);
    
    // Busca questões por criador
    List<Questao> findByCriadorId(Long criadorId);
    
    // Conta questões por disciplina
    long countByDisciplinaId(Long disciplinaId);
    
    // Busca questões por enunciado (busca parcial)
    List<Questao> findByEnunciadoContainingIgnoreCase(String enunciado);
    
    // Busca todas ordenadas por data de criação
    List<Questao> findAllByOrderByIdDesc();
}