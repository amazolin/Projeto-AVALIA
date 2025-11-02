package com.example.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.Usuario;
import com.example.model.Disciplina;
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

	// ✅ Torna o usuário logado acessível a todas as páginas Thymeleaf
	@ModelAttribute
	public void adicionarUsuarioLogadoAoModelo(HttpSession session, Model model) {
		Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		model.addAttribute("usuarioLogado", usuarioLogado);
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

	// --- LISTAR USUÁRIOS (apenas coordenador) ---
	@GetMapping("/usuarios")
	public String usuarios(Model model, HttpSession session) {
	    Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

	    // 🔒 Bloqueia acesso se não for coordenador
	    if (usuarioLogado == null || usuarioLogado.getTipoUsuario() == null || usuarioLogado.getTipoUsuario().getId() != 1) {
	        return "redirect:/login"; // ou "redirect:/professor" se preferir
	    }

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

	// --- API: LISTAR TODAS AS DISCIPLINAS ---
	@GetMapping("/api/disciplinas")
	public ResponseEntity<List<Disciplina>> listarTodasDisciplinas() {
		List<Disciplina> disciplinas = usuarioService.buscarTodasDisciplinas();
		return ResponseEntity.ok(disciplinas);
	}

	// --- API: BUSCAR USUÁRIO POR EMAIL COM DISCIPLINAS ---
	@GetMapping("/api/usuarios/buscar-completo")
	public ResponseEntity<?> buscarUsuarioPorEmailCompleto(@RequestParam String email) {
		email = email.trim();

		if (email.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		Usuario usuario = usuarioService.buscarPorEmail(email);

		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}

		// Busca as disciplinas do usuário
		List<Disciplina> disciplinas = usuarioService.buscarDisciplinasDoUsuario(usuario.getId());
		List<Integer> disciplinasIds = disciplinas.stream()
				.map(d -> d.getId().intValue())
				.collect(Collectors.toList());

		// Cria um mapa com os dados do usuário e suas disciplinas
		Map<String, Object> response = new HashMap<>();
		response.put("id", usuario.getId());
		response.put("nome", usuario.getNome());
		response.put("email", usuario.getEmail());
		response.put("rgm", usuario.getRgm());
		response.put("status", usuario.getStatus());
		response.put("tipoUsuario", usuario.getTipoUsuario());
		response.put("disciplinas", disciplinasIds); // IDs das disciplinas

		return ResponseEntity.ok(response);
	}

	// --- API: BUSCAR USUÁRIO POR ID COM DISCIPLINAS ---
	@GetMapping("/api/usuarios/id/{id}")
	public ResponseEntity<?> buscarUsuarioPorId(@PathVariable Long id) {
		Usuario usuario = usuarioService.buscarPorId(id);

		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}

		List<Disciplina> disciplinas = usuarioService.buscarDisciplinasDoUsuario(id);
		List<Integer> disciplinasIds = disciplinas.stream()
				.map(d -> d.getId().intValue())
				.collect(Collectors.toList());

		Map<String, Object> response = new HashMap<>();
		response.put("id", usuario.getId());
		response.put("nome", usuario.getNome());
		response.put("email", usuario.getEmail());
		response.put("rgm", usuario.getRgm());
		response.put("status", usuario.getStatus());
		response.put("tipoUsuario", usuario.getTipoUsuario());
		response.put("disciplinas", disciplinasIds);

		return ResponseEntity.ok(response);
	}

	// --- LOGIN (POST) ---
	@PostMapping("/login")
	public String realizarLogin(@RequestParam String email, @RequestParam String senha, 
	                           HttpSession session, Model model) {

	    Usuario usuario = usuarioService.buscarPorEmailSenha(email.trim(), senha.trim());

	    if (usuario != null) {
	        // Coordenador (id_tipo = 1)
	        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == 1) {
	            session.setAttribute("usuarioLogado", usuario);
	            return "redirect:/coordenador";
	        } 
	        // Professor (id_tipo = 2)
	        else if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == 2) {
	            if ("Concluído".equalsIgnoreCase(usuario.getStatus())) {
	                session.setAttribute("usuarioLogado", usuario);
	                return "redirect:/professor";
	            } else {
	                model.addAttribute("erro", "Seu cadastro ainda não foi aprovado pelo coordenador. Aguarde a aprovação.");
	                return "login";
	            }
	        } 
	        // Tipo de usuário não reconhecido
	        else {
	            model.addAttribute("erro", "Tipo de usuário inválido.");
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
		return "cadastro";
	}

	// --- API: CADASTRAR USUÁRIO ---
	@PostMapping("/api/usuarios/cadastrar")
	public ResponseEntity<String> cadastrarUsuario(@RequestParam String nomeCompleto, @RequestParam String email,
			@RequestParam String rgm, @RequestParam String senha, @RequestParam String confirmarSenha) {

		nomeCompleto = nomeCompleto.trim();
		email = email.trim();
		rgm = rgm.trim();

		if (nomeCompleto.isEmpty() || email.isEmpty() || rgm.isEmpty() || senha.isEmpty()
				|| confirmarSenha.isEmpty()) {
			return ResponseEntity.badRequest().body("Todos os campos são obrigatórios!");
		}

		if (!senha.equals(confirmarSenha)) {
			return ResponseEntity.badRequest().body("As senhas não coincidem!");
		}

		if (usuarioService.buscarPorEmail(email) != null) {
			return ResponseEntity.badRequest().body("E-mail já cadastrado!");
		}

		Usuario novoUsuario = new Usuario();
		novoUsuario.setNome(nomeCompleto);
		novoUsuario.setEmail(email);
		novoUsuario.setRgm(rgm);
		novoUsuario.setSenha(senha);

		// Tipo padrão: professor (id = 2)
		TipoUsuario tipoPadrao = usuarioService.buscarTipoUsuarioPorId(2L);
		if (tipoPadrao == null) {
			return ResponseEntity.badRequest().body("Tipo de usuário padrão não encontrado!");
		}
		novoUsuario.setTipoUsuario(tipoPadrao);

		usuarioService.salvarUsuario(novoUsuario);
		return ResponseEntity.ok("Usuário cadastrado com sucesso!");
	}

	// --- API: EDITAR USUÁRIO ---
	@PostMapping("/api/usuarios/editar")
	public ResponseEntity<String> editarUsuario(@RequestParam Long id, @RequestParam String nomeCompleto,
			@RequestParam String email, @RequestParam String rgm, @RequestParam(required = false) String senha,
			@RequestParam(required = false) String status, @RequestParam(required = false) List<Integer> disciplinas) {

		nomeCompleto = nomeCompleto.trim();
		email = email.trim();
		rgm = rgm.trim();

		if (nomeCompleto.isEmpty() || email.isEmpty() || rgm.isEmpty()) {
			return ResponseEntity.badRequest().body("Nome, e-mail e RGM são obrigatórios!");
		}

		Usuario usuarioExistente = usuarioService.buscarPorEmail(email);
		if (usuarioExistente != null && !usuarioExistente.getId().equals(id)) {
			return ResponseEntity.badRequest().body("E-mail já cadastrado em outro usuário!");
		}

		try {
			Usuario usuario = usuarioService.buscarPorId(id);
			if (usuario == null) {
				return ResponseEntity.badRequest().body("Usuário não encontrado!");
			}

			usuario.setNome(nomeCompleto);
			usuario.setEmail(email);
			usuario.setRgm(rgm);

			if (senha != null && !senha.trim().isEmpty()) {
				usuario.setSenha(senha);
			}

			if (status != null && !status.trim().isEmpty()) {
				usuario.setStatus(status);
			}

			usuarioService.salvarUsuario(usuario);

			if (disciplinas != null) {
				usuarioService.atualizarDisciplinasDoUsuario(usuario, disciplinas);
			}

			return ResponseEntity.ok("Usuário editado com sucesso!");

		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erro ao editar usuário: " + e.getMessage());
		}
	}

	// --- API: APAGAR USUÁRIO POR EMAIL ---
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
}
