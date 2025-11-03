package com.example.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;           
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;             
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dto.CriarProvaDTO;
import com.example.model.Disciplina;
import com.example.model.Prova;
import com.example.model.Questao;
import com.example.model.Usuario;
import com.example.service.DisciplinaService;
import com.example.service.ProvaService;
import com.example.service.QuestaoService;

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
    public ResponseEntity<?> criarProva(@RequestBody CriarProvaDTO dto, HttpSession session) {
        try {
            System.out.println("📥 Recebendo requisição para criar prova: " + dto.getTitulo());
            
            // Verifica autenticação
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("erro", "Usuário não autenticado"));
            }
            
            // Validações básicas
            if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O título da prova é obrigatório!"));
            }
            
            if (dto.getQuestoes() == null || dto.getQuestoes().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Selecione pelo menos uma questão!"));
            }
            
            // Cria a prova PASSANDO O USUÁRIO para o Service
            Long provaId = provaService.criarProva(dto, usuarioLogado);
            
            // Monta resposta
            Map<String, Object> response = new HashMap<>();
            response.put("id", provaId);
            response.put("titulo", dto.getTitulo());
            response.put("mensagem", "Prova criada com sucesso!");
            
            System.out.println("✅ Prova criada com ID: " + provaId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar prova: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao criar prova: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }

    // ==================== MÉTODO 2: GERAR PDF ====================

    /**
     * API: Gerar e baixar o PDF da prova
     * ENDPOINT NOVO - adicione após os outros métodos da API
     */
    @GetMapping("/api/prova/{id}/pdf")
    public ResponseEntity<?> gerarPDF(@PathVariable Long id, HttpSession session) {
        try {
            System.out.println("📄 Gerando PDF da prova ID: " + id);
            
            // Verifica autenticação
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não autenticado");
            }
            
            // Verifica se a prova existe
            Prova prova = provaService.buscarPorId(id);
            if (prova == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Gera o PDF
            byte[] pdf = provaService.gerarProvaPDF(id);
            ByteArrayInputStream bis = new ByteArrayInputStream(pdf);

            // Nome do arquivo personalizado (remove caracteres especiais)
            String nomeArquivo = prova.getTitulo()
                    .replaceAll("[^a-zA-Z0-9-_]", "_") // Remove caracteres especiais
                    .replaceAll("_{2,}", "_")           // Remove underscores duplicados
                    .substring(0, Math.min(50, prova.getTitulo().length())); // Limita a 50 chars
            
            // Configura o cabeçalho para download
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + nomeArquivo + ".pdf");

            System.out.println("✅ PDF gerado com sucesso!");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
                    
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar PDF: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar PDF: " + e.getMessage());
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