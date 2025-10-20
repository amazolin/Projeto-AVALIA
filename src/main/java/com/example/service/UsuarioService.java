package com.example.service;

import com.example.model.Usuario;
import com.example.model.TipoUsuario;
import com.example.repository.UsuarioRepository;
import com.example.repository.TipoUsuarioRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TipoUsuarioRepository tipoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    // 🔹 Lista todos os usuários
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    // 🔹 Salva qualquer tipo de usuário (completo ou só e-mail)
    public Usuario salvarUsuario(Usuario novoUsuario) {
        return usuarioRepository.save(novoUsuario);
    }

    // 🔹 Busca usuário por e-mail e senha (para login)
    public Usuario buscarPorEmailSenha(String email, String senha) {
        return usuarioRepository.findByEmailAndSenha(email, senha);
    }

    // 🔹 Busca usuário apenas pelo e-mail
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // 🔹 Busca TipoUsuario por ID
    public TipoUsuario buscarTipoUsuarioPorId(Long id) {
        return tipoUsuarioRepository.findById(id).orElse(null);
    }
    // 🔹 Apagar usuário
    public void apagarUsuario(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

}
