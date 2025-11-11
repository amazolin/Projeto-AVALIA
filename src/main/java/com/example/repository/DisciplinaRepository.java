package com.example.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.model.Disciplina;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    @Query("""
        SELECT d
        FROM Disciplina d
        JOIN ProfessorDisciplina pd ON pd.disciplina.id = d.id
        WHERE pd.usuario.id = :professorId
    """)
    List<Disciplina> findByProfessorId(@Param("professorId") Long professorId);
}
