package com.example.service;

import com.example.model.SimuladoQuestao; 
import com.example.model.SimuladoQuestaoId; // Importa a chave composta
import com.example.repository.SimuladoQuestaoRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

@Service
public class SimuladoQuestaoService {

    private final SimuladoQuestaoRepository simuladoQuestaoRepository;

    public SimuladoQuestaoService(SimuladoQuestaoRepository simuladoQuestaoRepository) {
        this.simuladoQuestaoRepository = simuladoQuestaoRepository;
    }

    public List<SimuladoQuestao> buscarTodas() {
        return simuladoQuestaoRepository.findAll();
    }
    
    public SimuladoQuestao salvarRelacionamento(SimuladoQuestao novoRelacionamento) {
        // Lógica para garantir que o relacionamento é válido
        return simuladoQuestaoRepository.save(novoRelacionamento);
    }
    
    // Método para buscar pelo ID composto
    public SimuladoQuestao buscarPorId(SimuladoQuestaoId id) {
        return simuladoQuestaoRepository.findById(id).orElse(null);
    }
}