package com.example.service;

import com.example.model.Prova; 
import com.example.repository.ProvaRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

@Service
public class ProvaService {

    private final ProvaRepository provaRepository;

    public ProvaService(ProvaRepository provaRepository) {
        this.provaRepository = provaRepository;
    }

    public List<Prova> buscarTodas() {
        return provaRepository.findAll();
    }
    
    public Prova salvarProva(Prova novaProva) {
        return provaRepository.save(novaProva);
    }
    
    public Prova buscarPorId(Integer id) {
        return provaRepository.findById(id).orElse(null);
    }
}