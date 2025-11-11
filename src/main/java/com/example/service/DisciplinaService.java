package com.example.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.model.Disciplina;
import com.example.model.Questao;
import com.example.model.Usuario;
import com.example.repository.DisciplinaRepository;
import com.example.repository.QuestaoRepository;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final QuestaoRepository questaoRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository,
                             QuestaoRepository questaoRepository) {
        this.disciplinaRepository = disciplinaRepository;
        this.questaoRepository = questaoRepository;
    }

    public List<Disciplina> findAllByUsuario(Usuario usuario) {
        if (usuario == null) return List.of();
        if (usuario.getTipoUsuario() == null) return List.of();

        // 🔹 Agora pegamos a descrição do tipo
        String tipo = usuario.getTipoUsuario().getDescricao();

        if (tipo == null) return List.of();

        if (tipo.equalsIgnoreCase("coordenador")) {
            return disciplinaRepository.findAll();
        } else if (tipo.equalsIgnoreCase("professor")) {
            return disciplinaRepository.findByProfessorId(usuario.getId());
        } else {
            return List.of(); // outros tipos, como aluno
        }
    }


    public List<Disciplina> findAll() {
        return disciplinaRepository.findAll();
    }

    public List<Disciplina> buscarTodas() {
        return disciplinaRepository.findAll();
    }

    public List<Disciplina> buscarPorProfessor(Long professorId) {
        return disciplinaRepository.findByProfessorId(professorId);
    }

    public Disciplina buscarPorId(Long id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    public List<Questao> buscarQuestoesPorDisciplina(Long disciplinaId) {
        return questaoRepository.findByDisciplinaId(disciplinaId);
    }

    public Disciplina salvar(Disciplina disciplina) {
        return disciplinaRepository.save(disciplina);
    }

    public void excluir(Long id) {
        disciplinaRepository.deleteById(id);
    }

    public boolean existe(Long id) {
        return disciplinaRepository.existsById(id);
    }

    public long contarQuestoes(Long disciplinaId) {
        return questaoRepository.countByDisciplinaId(disciplinaId);
    }
}
