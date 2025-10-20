package com.example.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.Usuario;
import com.example.model.TipoUsuario;
import com.example.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

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

	@GetMapping("/questoes")
	public String questoes() {
		return "questoes";
	}

	// --- API: LISTAR USUÁRIOS ---
	@GetMapping("/api/usuarios")
	public ResponseEntity<List<Usuario>> listarTodos() {
		List<Usuario> usuarios = usuarioService.buscarTodos();
		return ResponseEntity.ok(usuarios);
	}

	// --- LOGIN (POST) ---
	@PostMapping("/login")
	public String realizarLogin(@RequestParam String email, @RequestParam String senha, HttpSession session,
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

	// --- CADASTRAR USUÁRIO (APENAS E-MAIL) ---
	@PostMapping("/api/usuarios/cadastrar-email")
	public ResponseEntity<String> cadastrarEmail(@RequestParam String email) {
		// Remove espaços extras
		email = email.trim();

		// Verifica se o e-mail foi informado
		if (email.isEmpty()) {
			return ResponseEntity.badRequest().body("E-mail vazio!");
		}

		// Verifica se o e-mail já existe
		if (usuarioService.buscarPorEmail(email) != null) {
			return ResponseEntity.badRequest().body("E-mail já cadastrado!");
		}

		// Cria novo usuário com valores temporários
		Usuario novoUsuario = new Usuario();
		novoUsuario.setEmail(email);
		novoUsuario.setNome("Nome temporário");
		novoUsuario.setSenha("senha123");

		// Define TipoUsuario PROFESSOR (id = 2)
		TipoUsuario tipoProfessor = usuarioService.buscarTipoUsuarioPorId(2L);
		if (tipoProfessor == null) {
			return ResponseEntity.badRequest().body("Tipo de usuário PROFESSOR não encontrado!");
		}
		novoUsuario.setTipoUsuario(tipoProfessor);

		// Salva no banco
		usuarioService.salvarUsuario(novoUsuario);

		return ResponseEntity.ok("E-mail cadastrado com sucesso!");
	}
	
	// APAGAR E-MAIL
	@PostMapping("/api/usuarios/apagar-email")
	public ResponseEntity<String> apagarEmail(@RequestParam String email) {
		// Remove espaços extras
		email = email.trim();

		// Verifica se o e-mail foi informado
		if (email.isEmpty()) {
			return ResponseEntity.badRequest().body("E-mail vazio!");
		}

		// Busca o usuário pelo e-mail
		Usuario usuario = usuarioService.buscarPorEmail(email);
		if (usuario == null) {
			return ResponseEntity.badRequest().body("E-mail não encontrado!");
		}

		// Apaga o usuário do banco
		usuarioService.apagarUsuario(usuario);

		return ResponseEntity.ok("E-mail apagado com sucesso!");
	}

}
