package com.example.service;

import com.example.model.Ranking; 
import com.example.repository.RankingRepository; 
import org.springframework.stereotype.Service;
import java.util.List; 

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public List<Ranking> buscarTodos() {
        return rankingRepository.findAll();
    }
    
    public Ranking salvarRegistro(Ranking novoRegistro) {
        // Lógica para calcular a pontuação ou validar o registro de ranking
        return rankingRepository.save(novoRegistro);
    }
    
    public Ranking buscarPorId(Integer id) {
        return rankingRepository.findById(id).orElse(null);
    }
}