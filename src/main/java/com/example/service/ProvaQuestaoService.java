package com.example.service;

import com.example.model.ProvaQuestao; 
import com.example.model.ProvaQuestaoId;
import com.example.repository.ProvaQuestaoRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

@Service
public class ProvaQuestaoService {

    private final ProvaQuestaoRepository provaQuestaoRepository;

    public ProvaQuestaoService(ProvaQuestaoRepository provaQuestaoRepository) {
        this.provaQuestaoRepository = provaQuestaoRepository;
    }

    public List<ProvaQuestao> buscarTodas() {
        return provaQuestaoRepository.findAll();
    }
    
    public ProvaQuestao salvarRelacionamento(ProvaQuestao novoRelacionamento) {
        return provaQuestaoRepository.save(novoRelacionamento);
    }
    
    // Método para buscar pelo ID composto
    public ProvaQuestao buscarPorId(ProvaQuestaoId id) {
        return provaQuestaoRepository.findById(id).orElse(null);
    }
}