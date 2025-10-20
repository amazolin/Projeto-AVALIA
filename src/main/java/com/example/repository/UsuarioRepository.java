package com.example.repository;

import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 🔹 Busca usuário pelo e-mail e senha (para login)
    Usuario findByEmailAndSenha(String email, String senha);

    // 🔹 Busca usuário apenas pelo e-mail (para verificação e cadastro inicial)
    Usuario findByEmail(String email);
}
