package com.example.service;

import com.example.model.TipoUsuario;
import com.example.repository.TipoUsuarioRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List; 

@Service // 
public class TipoUsuarioService {

    
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public TipoUsuarioService(TipoUsuarioRepository tipoUsuarioRepository) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    public List<TipoUsuario> buscarTodos() {
       
        return tipoUsuarioRepository.findAll();
    }
    
  
    public TipoUsuario buscarPorId(Long id) {
        return tipoUsuarioRepository.findById(id).orElse(null);
    }

}