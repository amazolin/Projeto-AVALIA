package com.example.repository;

import com.example.model.ProfessorDisciplina;
import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorDisciplinaRepository extends JpaRepository<ProfessorDisciplina, Long> {
    
    // Busca todas as disciplinas de um professor
    List<ProfessorDisciplina> findByUsuario(Usuario usuario);
    
    // Busca por usuário ID
    List<ProfessorDisciplina> findByUsuarioId(Long idUsuario);
    
    // Deleta todas as disciplinas de um professor
    void deleteByUsuario(Usuario usuario);
    
    // Deleta por usuário ID
    void deleteByUsuarioId(Long idUsuario);
}