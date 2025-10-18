package com.example.service;

import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

   
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    
    public List<Usuario> buscarTodos() {
        
        return usuarioRepository.findAll(); 
    }
    

    public Usuario salvarUsuario(Usuario novoUsuario) {
        
  
        
        return usuarioRepository.save(novoUsuario);
    }
    
    public Usuario buscarPorEmailSenha(String email, String senha) {
        System.out.println("🔎 Buscando no banco: " + email + " | " + senha);
        Usuario u = usuarioRepository.findByEmailAndSenha(email, senha);
        System.out.println("👉 Resultado: " + (u != null ? u.getNome() : "nenhum usuário encontrado"));
        return u;
    }


}