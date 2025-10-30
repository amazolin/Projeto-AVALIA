package com.example.service; 

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Disciplina;
import com.example.model.Questao;
import com.example.repository.DisciplinaRepository;
import com.example.repository.QuestaoRepository;

@Service 
public class DisciplinaService {

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    
    public List<Disciplina> findAll() {
        return disciplinaRepository.findAll();
    }

    public Disciplina buscarPorId(Long id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    public List<Questao> buscarQuestoesPorDisciplina(Long disciplinaId) {
        return questaoRepository.findByDisciplinaId(disciplinaId);
    }
}