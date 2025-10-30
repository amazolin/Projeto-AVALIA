package com.example.service;

import java.util.List;
 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.example.model.OpcaoQuestao;
import com.example.repository.OpcaoQuestaoRepository; 

@Service
public class OpcaoQuestaoService {

    private final OpcaoQuestaoRepository opcaoQuestaoRepository;

    public OpcaoQuestaoService(OpcaoQuestaoRepository opcaoQuestaoRepository) {
        this.opcaoQuestaoRepository = opcaoQuestaoRepository;
    }

    public List<OpcaoQuestao> buscarTodas() {
        return opcaoQuestaoRepository.findAll();
    }
    
    public OpcaoQuestao salvarOpcao(OpcaoQuestao novaOpcao) {
        return opcaoQuestaoRepository.save(novaOpcao);
    }
    
    public OpcaoQuestao buscarPorId(Long id) {
        return opcaoQuestaoRepository.findById(id).orElse(null);
    }

    public List<OpcaoQuestao> buscarPorQuestaoId(Long questaoId) {
        return opcaoQuestaoRepository.findByQuestaoId(questaoId);
    }

    @Transactional
    public void apagarPorQuestaoId(Long questaoId) {
        opcaoQuestaoRepository.deleteByQuestaoId(questaoId);
    }
}