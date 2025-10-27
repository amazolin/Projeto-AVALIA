package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	
	@GetMapping("/professor")
	public String professor() {
		return "professor";
	}

	@GetMapping("/cadastro")
	public String cadastro(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "cadastro";
	}

	// LISTAR USUÁRIOS (exceto coordenador)
	@GetMapping("/usuarios")
	public String usuarios(Model model) {
		List<Usuario> usuarios = usuarioService.buscarTodos();

		// Filtra todos os usuários, exceto o coordenador (id_tipo = 1)
		List<Usuario> usuariosFiltrados = usuarios.stream()
		    .filter(u -> u.getTipoUsuario() == null || u.getTipoUsuario().getId() != 1)
		    .collect(Collectors.toList());

		model.addAttribute("usuarios", usuariosFiltrados);
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

	// --- CADASTRAR USUÁRIO (COMPLETO) ---
	@PostMapping("/cadastro-usuario")
	public String cadastrarUsuario(@ModelAttribute("usuario") Usuario usuario) {
		usuarioService.salvarUsuario(usuario);
		return "cadastro"; // redireciona após salvar
	}

	// --- CADASTRAR USUÁRIO (API) ---
	@PostMapping("/api/usuarios/cadastrar")
	public ResponseEntity<String> cadastrarUsuario(
	        @RequestParam String nomeCompleto,
	        @RequestParam String email,
	        @RequestParam String rgm,
	        @RequestParam String senha,
	        @RequestParam String confirmarSenha) {

	    // Remover espaços extras
	    nomeCompleto = nomeCompleto.trim();
	    email = email.trim();
	    rgm = rgm.trim();

	    // Validações básicas
	    if (nomeCompleto.isEmpty() || email.isEmpty() || rgm.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
	        return ResponseEntity.badRequest().body("Todos os campos são obrigatórios!");
	    }

	    if (!senha.equals(confirmarSenha)) {
	        return ResponseEntity.badRequest().body("As senhas não coincidem!");
	    }

	    // Verifica se o e-mail já existe
	    if (usuarioService.buscarPorEmail(email) != null) {
	        return ResponseEntity.badRequest().body("E-mail já cadastrado!");
	    }

	    // Cria novo usuário
	    Usuario novoUsuario = new Usuario();
	    novoUsuario.setNome(nomeCompleto);
	    novoUsuario.setEmail(email);
	    novoUsuario.setRgm(rgm);
	    novoUsuario.setSenha(senha);

	    // Força o TipoUsuario com id 2 (Professor)
	    TipoUsuario tipoPadrao = usuarioService.buscarTipoUsuarioPorId(2L);
	    if (tipoPadrao == null) {
	        return ResponseEntity.badRequest().body("Tipo de usuário padrão não encontrado!");
	    }
	    novoUsuario.setTipoUsuario(tipoPadrao);

	    // Salva no banco
	    usuarioService.salvarUsuario(novoUsuario);

	    return ResponseEntity.ok("Usuário cadastrado com sucesso!");
	}


	// --- APAGAR E-MAIL ---
	@PostMapping("/api/usuarios/apagar-email")
	public ResponseEntity<String> apagarEmail(@RequestParam String email) {
		email = email.trim();

		if (email.isEmpty()) {
			return ResponseEntity.badRequest().body("E-mail vazio!");
		}

		Usuario usuario = usuarioService.buscarPorEmail(email);
		if (usuario == null) {
			return ResponseEntity.badRequest().body("E-mail não encontrado!");
		}

		usuarioService.apagarUsuario(usuario);
		return ResponseEntity.ok("E-mail apagado com sucesso!");
	}
	
	// --- BUSCAR USUÁRIO POR EMAIL (API) ---
	@GetMapping("/api/usuarios/buscar")
	public ResponseEntity<Usuario> buscarUsuarioPorEmail(@RequestParam String email) {
	    email = email.trim();

	    if (email.isEmpty()) {
	        return ResponseEntity.badRequest().build();
	    }

	    Usuario usuario = usuarioService.buscarPorEmail(email);

	    if (usuario == null) {
	        return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.ok(usuario);
	}
	



}
