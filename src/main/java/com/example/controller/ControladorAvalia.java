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

	// --- API: LISTAR TODAS AS DISCIPLINAS ---
	@GetMapping("/api/disciplinas")
	public ResponseEntity<List<Disciplina>> listarTodasDisciplinas() {
		List<Disciplina> disciplinas = usuarioService.buscarTodasDisciplinas();
		return ResponseEntity.ok(disciplinas);
	}

	// --- API: BUSCAR USUÁRIO POR EMAIL COM DISCIPLINAS ---
	// ⚠️ Este endpoint DEVE vir ANTES do /api/usuarios/id/{id}
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
	// Mudei para /api/usuarios/id/{id} para evitar conflitos
	@GetMapping("/api/usuarios/id/{id}")
	public ResponseEntity<?> buscarUsuarioPorId(@PathVariable Long id) {
		Usuario usuario = usuarioService.buscarPorId(id);

		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}

		// Busca as disciplinas do usuário
		List<Disciplina> disciplinas = usuarioService.buscarDisciplinasDoUsuario(id);
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

	// --- LOGIN (POST) ---
	@PostMapping("/login")
	public String realizarLogin(@RequestParam String email, @RequestParam String senha, 
	                           HttpSession session, Model model) {

	    Usuario usuario = usuarioService.buscarPorEmailSenha(email.trim(), senha.trim());

	    if (usuario != null) {
	        // Verifica se é o coordenador (id_tipo = 1)
	        if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == 1) {
	            session.setAttribute("usuarioLogado", usuario);
	            return "redirect:/coordenador";
	        } 
	        // Verifica se é professor (id_tipo = 2)
	        else if (usuario.getTipoUsuario() != null && usuario.getTipoUsuario().getId() == 2) {
	            // Verifica o status do professor
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
		return "cadastro"; // redireciona após salvar
	}

	// --- API: CADASTRAR USUÁRIO ---
	@PostMapping("/api/usuarios/cadastrar")
	public ResponseEntity<String> cadastrarUsuario(@RequestParam String nomeCompleto, @RequestParam String email,
			@RequestParam String rgm, @RequestParam String senha, @RequestParam String confirmarSenha) {

		// Remover espaços extras
		nomeCompleto = nomeCompleto.trim();
		email = email.trim();
		rgm = rgm.trim();

		// Validações básicas
		if (nomeCompleto.isEmpty() || email.isEmpty() || rgm.isEmpty() || senha.isEmpty()
				|| confirmarSenha.isEmpty()) {
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

	// --- API: EDITAR USUÁRIO ---
	@PostMapping("/api/usuarios/editar")
	public ResponseEntity<String> editarUsuario(@RequestParam Long id, @RequestParam String nomeCompleto,
			@RequestParam String email, @RequestParam String rgm, @RequestParam(required = false) String senha,
			@RequestParam(required = false) String status, @RequestParam(required = false) List<Integer> disciplinas) {

		// Remover espaços extras
		nomeCompleto = nomeCompleto.trim();
		email = email.trim();
		rgm = rgm.trim();

		// Validações básicas
		if (nomeCompleto.isEmpty() || email.isEmpty() || rgm.isEmpty()) {
			return ResponseEntity.badRequest().body("Nome, e-mail e RGM são obrigatórios!");
		}

		// Verifica se o e-mail já existe em outro usuário
		Usuario usuarioExistente = usuarioService.buscarPorEmail(email);
		if (usuarioExistente != null && !usuarioExistente.getId().equals(id)) {
			return ResponseEntity.badRequest().body("E-mail já cadastrado em outro usuário!");
		}

		try {
			// Busca o usuário a ser editado
			Usuario usuario = usuarioService.buscarPorId(id);
			if (usuario == null) {
				return ResponseEntity.badRequest().body("Usuário não encontrado!");
			}

			// Atualiza os campos básicos
			usuario.setNome(nomeCompleto);
			usuario.setEmail(email);
			usuario.setRgm(rgm);

			// Atualiza senha apenas se foi fornecida
			if (senha != null && !senha.trim().isEmpty()) {
				usuario.setSenha(senha);
			}

			// Atualiza status se fornecido
			if (status != null && !status.trim().isEmpty()) {
				usuario.setStatus(status);
			}

			// Salva o usuário
			usuarioService.salvarUsuario(usuario);

			// Atualiza as disciplinas
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