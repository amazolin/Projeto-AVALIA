package com.example.repository;

import com.example.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvaRepository extends JpaRepository<Prova, Long> {  // Mudei de Integer para Long
    
    // Buscar provas por criador
    List<Prova> findByCriadorId(Long criadorId);
    
    // Buscar provas por título (útil para busca)
    List<Prova> findByTituloContainingIgnoreCase(String titulo);
    
    // Buscar todas ordenadas por data de criação (mais recentes primeiro)
    List<Prova> findAllByOrderByDataCriacaoDesc();
}