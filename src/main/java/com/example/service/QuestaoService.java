package com.example.service;

import com.example.model.Questao; 
import com.example.repository.QuestaoRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

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
    
    public Questao buscarPorId(Integer id) {
        return questaoRepository.findById(id).orElse(null);
    }
}