package com.example.repository;

import com.example.model.ProfessorDisciplina;
import com.example.model.Usuario;
import com.example.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorDisciplinaRepository extends JpaRepository<ProfessorDisciplina, Long> {
    
    // 🔹 Busca todos os vínculos de disciplinas de um professor (por entidade)
    List<ProfessorDisciplina> findByUsuario(Usuario usuario);
    
    // 🔹 Busca todos os vínculos de disciplinas de um professor (por ID)
    List<ProfessorDisciplina> findByUsuarioId(Long idUsuario);
    
    // 🔹 Exclui todos os vínculos de disciplinas de um professor
    void deleteByUsuario(Usuario usuario);
    
    // 🔹 Exclui todos os vínculos de disciplinas de um professor (por ID)
    void deleteByUsuarioId(Long idUsuario);

    // 🔹 Busca todas as disciplinas de um professor (retorna apenas as disciplinas)
    @Query("SELECT pd.disciplina FROM ProfessorDisciplina pd WHERE pd.usuario.id = :idProfessor")
    List<Disciplina> findDisciplinasByProfessorId(@Param("idProfessor") Long idProfessor);

    // 🔹 Busca todos os professores de uma disciplina (retorna apenas os usuários)
    @Query("SELECT pd.usuario FROM ProfessorDisciplina pd WHERE pd.disciplina.id = :idDisciplina")
    List<Usuario> findProfessoresByDisciplinaId(@Param("idDisciplina") Long idDisciplina);
}
