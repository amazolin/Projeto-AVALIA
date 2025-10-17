package com.example.service; 

import com.example.model.Disciplina;
import com.example.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service 
public class DisciplinaService {

    @Autowired
    
    private DisciplinaRepository disciplinaRepository;

    
    public List<Disciplina> findAll() {
        
        return disciplinaRepository.findAll();
    }
}