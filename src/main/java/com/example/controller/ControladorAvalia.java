package com.example.controller;

import com.example.model.Usuario; 
import com.example.service.UsuarioService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController; 
import java.util.List; 

//@RestController -- Causa erro ao tentar retornar uma view
@Controller 
@RequestMapping 
public class ControladorAvalia {

   
    private final UsuarioService usuarioService;

    
    public ControladorAvalia(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/coordenador")
    public String coordenador() {
        return "coordenador";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }
    
    @GetMapping("/usuarios")
    public String usuarios() {
        return "usuarios";
    }
   
    @GetMapping("/api/usuarios") 
    public ResponseEntity<List<Usuario>> listarTodos() {
        
        
        List<Usuario> usuarios = usuarioService.buscarTodos(); 
        
        
        return ResponseEntity.ok(usuarios);
    }
}