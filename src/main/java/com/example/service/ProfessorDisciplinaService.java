package com.example.service;

import com.example.model.Disciplina;
import com.example.model.ProfessorDisciplina;
import com.example.model.Usuario;
import com.example.repository.ProfessorDisciplinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessorDisciplinaService {

    private final ProfessorDisciplinaRepository professorDisciplinaRepository;

    public ProfessorDisciplinaService(ProfessorDisciplinaRepository professorDisciplinaRepository) {
        this.professorDisciplinaRepository = professorDisciplinaRepository;
    }

    /** Retorna todas as disciplinas vinculadas a um professor. */
    public List<Disciplina> buscarDisciplinasDoProfessor(Usuario professor) {
        return professorDisciplinaRepository.findByUsuario(professor).stream()
                .map(ProfessorDisciplina::getDisciplina)
                .collect(Collectors.toList());
    }

    /** Retorna todas as disciplinas de um professor usando apenas o ID. */
    public List<Disciplina> buscarDisciplinasPorId(Long idProfessor) {
        return professorDisciplinaRepository.findDisciplinasByProfessorId(idProfessor);
    }

    /** Associa um professor a uma disciplina. */
    @Transactional
    public ProfessorDisciplina vincularProfessorADisciplina(Usuario professor, Disciplina disciplina) {
        ProfessorDisciplina vinculo = new ProfessorDisciplina(professor, disciplina);
        return professorDisciplinaRepository.save(vinculo);
    }

    /** Remove todas as disciplinas vinculadas a um professor. */
    @Transactional
    public void removerVinculosDoProfessor(Usuario professor) {
        professorDisciplinaRepository.deleteByUsuario(professor);
    }

    /** Remove todas as disciplinas de um professor pelo ID. */
    @Transactional
    public void removerVinculosPorId(Long idProfessor) {
        professorDisciplinaRepository.deleteByUsuarioId(idProfessor);
    }
}
