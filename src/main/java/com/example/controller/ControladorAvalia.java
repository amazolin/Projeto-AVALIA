package com.example.controller;

import com.example.model.Usuario;
import com.example.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class ControladorAvalia {

    private final UsuarioService usuarioService;

    public ControladorAvalia(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // --- TELAS ---
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

    // --- API: LISTAR USUÁRIOS ---
    @GetMapping("/api/usuarios")
    public ResponseEntity<List<Usuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.buscarTodos();
        return ResponseEntity.ok(usuarios);
    }

    // --- LOGIN (POST) ---
    @PostMapping("/login")
    public String realizarLogin(@RequestParam String email,
                                @RequestParam String senha,
                                HttpSession session,
                                Model model) {

        Usuario usuario = usuarioService.buscarPorEmailSenha(email.trim(), senha.trim());

        if (usuario != null) {
            session.setAttribute("usuarioLogado", usuario);

            // Verifica se é o coordenador
            if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == 1) {
                return "redirect:/coordenador";
            } else {
                model.addAttribute("erro", "Acesso restrito ao coordenador.");
                return "login";
            }
        } else {
            model.addAttribute("erro", "E-mail ou senha inválidos.");
            return "login";
        }
    }


}
