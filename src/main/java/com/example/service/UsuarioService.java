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
}