package com.example.repository;

import com.example.model.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {
    // JpaRepository já fornece findById, findAll, save, etc.
}
