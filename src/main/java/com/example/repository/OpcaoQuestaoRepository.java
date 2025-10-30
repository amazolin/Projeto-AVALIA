package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.OpcaoQuestao;

@Repository 
public interface OpcaoQuestaoRepository extends JpaRepository<OpcaoQuestao, Long> {
    List<OpcaoQuestao> findByQuestaoId(Long questaoId);
    void deleteByQuestaoId(Long questaoId);
}