package com.example.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.model.Prova;
import com.example.model.Questao;
import com.example.model.Disciplina;
import com.example.model.Usuario;
import com.example.service.ProvaService;
import com.example.service.QuestaoService;
import com.example.service.DisciplinaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class ProvaController {

    private final ProvaService provaService;
    private final QuestaoService questaoService;
    private final DisciplinaService disciplinaService;

    public ProvaController(ProvaService provaService, 
                          QuestaoService questaoService,
                          DisciplinaService disciplinaService) {
        this.provaService = provaService;
        this.questaoService = questaoService;
        this.disciplinaService = disciplinaService;
    }
    
 // ADICIONE este método no ProvaController.java

    @GetMapping("/gerar-provas")
    public String exibirPaginaGerarProvas(Model model, 
                                          @RequestParam(required = false) Long disciplinaId,
                                          HttpSession session) {
        
        // Verifica se usuário está logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // Lista todas as disciplinas para o filtro
        List<Disciplina> disciplinas = disciplinaService.buscarTodas();
        model.addAttribute("listaDisciplinas", disciplinas);
        model.addAttribute("usuarioLogado", usuarioLogado);
        
        // Se uma disciplina foi selecionada, busca suas questões
        if (disciplinaId != null) {
            List<Questao> questoes = questaoService.buscarPorDisciplina(disciplinaId);
            model.addAttribute("questoes", questoes);
            model.addAttribute("disciplinaSelecionada", disciplinaId);
        }
        
        return "gerar-provas";
    }

    // SUBSTITUA o método existente que só retorna "gerar-provas"

    /**
     * Página de configuração da prova (após selecionar questões)
     */
    @GetMapping("/configurar")
    public String configurarProva(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuarioLogado", usuarioLogado);
        return "configurar-prova";
    }

    /**
     * Lista todas as provas
     */
    @GetMapping
    public String listarProvas(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        List<Prova> provas;
        
        // Se for coordenador, mostra todas as provas
        if (usuarioLogado.getTipoUsuario() != null && 
            usuarioLogado.getTipoUsuario().getId() == 1) {
            provas = provaService.buscarTodas();
        } else {
            // Se for professor, mostra apenas suas provas
            provas = provaService.buscarPorCriador(usuarioLogado.getId());
        }
        
        model.addAttribute("provas", provas);
        model.addAttribute("usuarioLogado", usuarioLogado);
        
        return "provas";
    }

    /**
     * Visualiza uma prova específica
     */
    @GetMapping("/ver/{id}")
    public String visualizarProva(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        Prova prova = provaService.buscarPorId(id);
        if (prova == null) {
            model.addAttribute("erro", "Prova não encontrada!");
            return "redirect:/provas";
        }
        
        model.addAttribute("prova", prova);
        model.addAttribute("usuarioLogado", usuarioLogado);
        
        return "visualizar-prova";
    }

    // ==================== API REST ====================

    /**
     * API: Criar nova prova
     */
    @PostMapping("/api/criar")
    public ResponseEntity<?> criarProvaAPI(@RequestBody Map<String, Object> dados, 
                                          HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return ResponseEntity.status(401).body("Usuário não autenticado");
            }
            
            // Extrai dados do JSON
            String titulo = (String) dados.get("titulo");
            String descricao = (String) dados.get("descricao");
            @SuppressWarnings("unchecked")
            List<Long> questoesIds = (List<Long>) dados.get("questoes");
            
            // Validações
            if (titulo == null || titulo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Título é obrigatório!");
            }
            
            if (questoesIds == null || questoesIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Selecione pelo menos uma questão!");
            }
            
            // Cria a prova
            Prova novaProva = new Prova();
            novaProva.setTitulo(titulo.trim());
            novaProva.setDescricao(descricao != null ? descricao.trim() : "");
            novaProva.setCriador(usuarioLogado);
            
            // Busca as questões selecionadas
            List<Questao> questoes = questaoService.buscarPorIds(questoesIds);
            novaProva.setQuestoes(questoes);
            
            // Salva a prova
            Prova provaSalva = provaService.salvar(novaProva);
            
            // Retorna resposta
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("id", provaSalva.getId());
            resposta.put("mensagem", "Prova criada com sucesso!");
            
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erro ao criar prova: " + e.getMessage());
        }
    }

    /**
     * API: Buscar questões por disciplina
     */
    @GetMapping("/api/questoes")
    public ResponseEntity<?> buscarQuestoes(@RequestParam Long disciplinaId) {
        try {
            List<Questao> questoes = questaoService.buscarPorDisciplina(disciplinaId);
            return ResponseEntity.ok(questoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erro ao buscar questões: " + e.getMessage());
        }
    }

    /**
     * API: Buscar detalhes de uma questão
     */
    @GetMapping("/api/questoes/{id}")
    public ResponseEntity<?> buscarQuestao(@PathVariable Long id) {
        try {
            Questao questao = questaoService.buscarPorId(id);
            if (questao == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(questao);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erro ao buscar questão: " + e.getMessage());
        }
    }

    /**
     * API: Editar prova
     */
    @PostMapping("/api/editar/{id}")
    public ResponseEntity<?> editarProvaAPI(@PathVariable Long id,
                                           @RequestBody Map<String, Object> dados,
                                           HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return ResponseEntity.status(401).body("Usuário não autenticado");
            }
            
            Prova prova = provaService.buscarPorId(id);
            if (prova == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verifica permissão (apenas criador ou coordenador)
            boolean isCoordenador = usuarioLogado.getTipoUsuario() != null && 
                                   usuarioLogado.getTipoUsuario().getId() == 1;
            boolean isCriador = prova.getCriador().getId().equals(usuarioLogado.getId());
            
            if (!isCoordenador && !isCriador) {
                return ResponseEntity.status(403).body("Sem permissão para editar esta prova");
            }
            
            // Atualiza dados
            String titulo = (String) dados.get("titulo");
            String descricao = (String) dados.get("descricao");
            @SuppressWarnings("unchecked")
            List<Long> questoesIds = (List<Long>) dados.get("questoes");
            
            if (titulo != null && !titulo.trim().isEmpty()) {
                prova.setTitulo(titulo.trim());
            }
            
            if (descricao != null) {
                prova.setDescricao(descricao.trim());
            }
            
            if (questoesIds != null && !questoesIds.isEmpty()) {
                List<Questao> questoes = questaoService.buscarPorIds(questoesIds);
                prova.setQuestoes(questoes);
            }
            
            provaService.salvar(prova);
            
            return ResponseEntity.ok("Prova editada com sucesso!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erro ao editar prova: " + e.getMessage());
        }
    }

    /**
     * API: Excluir prova
     */
    @PostMapping("/api/excluir/{id}")
    public ResponseEntity<?> excluirProvaAPI(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return ResponseEntity.status(401).body("Usuário não autenticado");
            }
            
            Prova prova = provaService.buscarPorId(id);
            if (prova == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Apenas coordenador pode excluir
            boolean isCoordenador = usuarioLogado.getTipoUsuario() != null && 
                                   usuarioLogado.getTipoUsuario().getId() == 1;
            
            if (!isCoordenador) {
                return ResponseEntity.status(403)
                    .body("Apenas coordenadores podem excluir provas");
            }
            
            provaService.excluir(id);
            
            return ResponseEntity.ok("Prova excluída com sucesso!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erro ao excluir prova: " + e.getMessage());
        }
    }
}