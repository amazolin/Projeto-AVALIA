package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 

import com.example.model.Disciplina; 

@Repository 
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    
  
}