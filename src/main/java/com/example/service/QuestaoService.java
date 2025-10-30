package com.example.service;

import java.util.List;
 
import org.springframework.stereotype.Service;
 
import com.example.model.Questao;
import com.example.repository.QuestaoRepository; 

@Service 
public class QuestaoService {

    private final QuestaoRepository questaoRepository;

    public QuestaoService(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    public List<Questao> buscarTodas() {
        return questaoRepository.findAll();
    }
    
    public Questao salvarQuestao(Questao novaQuestao) {
        return questaoRepository.save(novaQuestao);
    }
    
    public Questao buscarPorId(Long id) {
        return questaoRepository.findById(id).orElse(null);
    }

    public List<Questao> buscarPorDisciplina(Long disciplinaId) {
        return questaoRepository.findByDisciplinaId(disciplinaId);
    }

    public void apagarPorId(Long id) {
        questaoRepository.deleteById(id);
    }
}