package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.model.Questao;
import com.example.repository.QuestaoRepository;

@Service 
public class QuestaoService {
    
    private final QuestaoRepository questaoRepository;
    
    public QuestaoService(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }
    
    /**
     * Busca todas as questões
     */
    public List<Questao> buscarTodas() {
        return questaoRepository.findAll();
    }
    
    /**
     * Salva uma questão
     */
    public Questao salvarQuestao(Questao novaQuestao) {
        return questaoRepository.save(novaQuestao);
    }
    
    /**
     * Busca questão por ID
     */
    public Questao buscarPorId(Long id) {
        return questaoRepository.findById(id).orElse(null);
    }
    
    /**
     * Busca questões por disciplina
     */
    public List<Questao> buscarPorDisciplina(Long disciplinaId) {
        return questaoRepository.findByDisciplinaId(disciplinaId);
    }
    
    /**
     * Busca questões por lista de IDs (NOVO - necessário para o ProvaController)
     */
    public List<Questao> buscarPorIds(List<Long> ids) {
        return questaoRepository.findAllById(ids);
    }
    
    /**
     * Apaga questão por ID
     */
    public void apagarPorId(Long id) {
        questaoRepository.deleteById(id);
    }
    
    /**
     * Verifica se uma questão existe
     */
    public boolean existe(Long id) {
        return questaoRepository.existsById(id);
    }
    
    /**
     * Busca questões por criador
     */
    public List<Questao> buscarPorCriador(Long criadorId) {
        return questaoRepository.findByCriadorId(criadorId);
    }
    
    /**
     * Conta questões por disciplina
     */
    public long contarPorDisciplina(Long disciplinaId) {
        return questaoRepository.countByDisciplinaId(disciplinaId);
    }
}