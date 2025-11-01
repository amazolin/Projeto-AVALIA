package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Disciplina;
import com.example.model.Questao;
import com.example.repository.DisciplinaRepository;
import com.example.repository.QuestaoRepository;

@Service 
public class DisciplinaService {
    
    private final DisciplinaRepository disciplinaRepository;
    private final QuestaoRepository questaoRepository;
    
    // Usando construtor ao invés de @Autowired (boa prática)
    public DisciplinaService(DisciplinaRepository disciplinaRepository, 
                            QuestaoRepository questaoRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.questaoRepository = questaoRepository;
    }
    
    /**
     * Busca todas as disciplinas
     */
    public List<Disciplina> findAll() {
        return disciplinaRepository.findAll();
    }
    
    /**
     * Alias para compatibilidade com ProvaController
     */
    public List<Disciplina> buscarTodas() {
        return disciplinaRepository.findAll();
    }
    
    /**
     * Busca disciplina por ID
     */
    public Disciplina buscarPorId(Long id) {
        return disciplinaRepository.findById(id).orElse(null);
    }
    
    /**
     * Busca questões por disciplina
     */
    public List<Questao> buscarQuestoesPorDisciplina(Long disciplinaId) {
        return questaoRepository.findByDisciplinaId(disciplinaId);
    }
    
    /**
     * Salva ou atualiza uma disciplina
     */
    public Disciplina salvar(Disciplina disciplina) {
        return disciplinaRepository.save(disciplina);
    }
    
    /**
     * Exclui uma disciplina
     */
    public void excluir(Long id) {
        disciplinaRepository.deleteById(id);
    }
    
    /**
     * Verifica se uma disciplina existe
     */
    public boolean existe(Long id) {
        return disciplinaRepository.existsById(id);
    }
    
    /**
     * Conta quantas questões uma disciplina possui
     */
    public long contarQuestoes(Long disciplinaId) {
        return questaoRepository.countByDisciplinaId(disciplinaId);
    }
}