package com.example.service;

import com.example.model.OpcaoQuestao; 
import com.example.repository.OpcaoQuestaoRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 
import java.util.Optional;

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
    
    public OpcaoQuestao buscarPorId(Integer id) {
        return opcaoQuestaoRepository.findById(id).orElse(null);
    }
}