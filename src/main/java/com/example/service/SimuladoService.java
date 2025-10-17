package com.example.service;

import com.example.model.Simulado; 
import com.example.repository.SimuladoRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

@Service
public class SimuladoService {

    private final SimuladoRepository simuladoRepository;

    public SimuladoService(SimuladoRepository simuladoRepository) {
        this.simuladoRepository = simuladoRepository;
    }

    public List<Simulado> buscarTodos() {
        return simuladoRepository.findAll();
    }
    
    public Simulado salvarSimulado(Simulado novoSimulado) {
        // Lógica para gerar o simulado ou validar antes de salvar
        return simuladoRepository.save(novoSimulado);
    }
    
    public Simulado buscarPorId(Integer id) {
        return simuladoRepository.findById(id).orElse(null);
    }
}