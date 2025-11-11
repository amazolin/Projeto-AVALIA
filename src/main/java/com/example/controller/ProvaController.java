package com.example.controller;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/provas")
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

    /**
     * Redireciona acessos diretos incorretos para o caminho correto.
     */
    @GetMapping("/gerar-provas")
    public String redirecionarGerarProvas() {
        return "redirect:/provas/gerar";
    }

    /**
     * Página de geração de provas com filtro de disciplinas.
     */
    @GetMapping("/gerar")
    public String exibirPaginaGerarProvas(Model model,
                                          @RequestParam(required = false) Long disciplinaId,
                                          HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        // Lista de disciplinas (todas para coordenador, somente as do professor caso contrário)
        List<Disciplina> disciplinas = (usuarioLogado.getTipoUsuario() != null &&
                usuarioLogado.getTipoUsuario().getId() == 1)
                ? disciplinaService.buscarTodas()
                : disciplinaService.buscarPorProfessor(usuarioLogado.getId());

        model.addAttribute("listaDisciplinas", disciplinas);
        model.addAttribute("usuarioLogado", usuarioLogado);

        // Caso o professor selecione uma disciplina
        if (disciplinaId != null) {
            boolean temAcesso = disciplinas.stream()
                    .anyMatch(d -> d.getId().equals(disciplinaId));

            if (!temAcesso && usuarioLogado.getTipoUsuario().getId() != 1) {
                model.addAttribute("erro", "Você não tem acesso a esta disciplina!");
                return "gerar-provas";
            }

            List<Questao> questoes = questaoService.buscarPorDisciplina(disciplinaId);
            model.addAttribute("questoes", questoes);
            model.addAttribute("disciplinaSelecionada", disciplinaId);
        }

        return "gerar-provas"; // nome do template em src/main/resources/templates/
    }

    /**
     * Página de configuração da prova (após seleção de questões).
     */
    @GetMapping("/configurar")
    public String configurarProva(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        model.addAttribute("usuarioLogado", usuarioLogado);
        return "configurar-prova";
    }

    /**
     * Lista todas as provas visíveis ao usuário logado.
     */
    @GetMapping
    public String listarProvas(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        List<Prova> provas = (usuarioLogado.getTipoUsuario() != null &&
                usuarioLogado.getTipoUsuario().getId() == 1)
                ? provaService.buscarTodas()
                : provaService.buscarPorCriador(usuarioLogado.getId());

        model.addAttribute("provas", provas);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "provas";
    }

    /**
     * Visualiza uma prova específica.
     */
    @GetMapping("/ver/{id}")
    public String visualizarProva(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Prova prova = provaService.buscarPorId(id);
        if (prova == null) {
            model.addAttribute("erro", "Prova não encontrada!");
            return "redirect:/provas";
        }

        model.addAttribute("prova", prova);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "visualizar-prova";
    }

    // ==================== APIs REST ====================

    @PostMapping("/api/criar")
    public ResponseEntity<?> criarProva(@RequestBody CriarProvaDTO dto, HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null)
                return ResponseEntity.status(401).body(Map.of("erro", "Usuário não autenticado"));

            if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("erro", "Título obrigatório"));

            if (dto.getQuestoes() == null || dto.getQuestoes().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("erro", "Selecione pelo menos uma questão"));

            Long provaId = provaService.criarProva(dto, usuarioLogado);

            return ResponseEntity.ok(Map.of(
                    "id", provaId,
                    "titulo", dto.getTitulo(),
                    "mensagem", "Prova criada com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao criar prova: " + e.getMessage()));
        }
    }

    @GetMapping("/api/{id}/pdf")
    public ResponseEntity<?> gerarPDF(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");

            Prova prova = provaService.buscarPorId(id);
            if (prova == null) return ResponseEntity.notFound().build();

            byte[] pdf = provaService.gerarProvaPDF(id);
            String nomeArquivo = prova.getTitulo()
                    .replaceAll("[^a-zA-Z0-9-_]", "_")
                    .replaceAll("_{2,}", "_")
                    .substring(0, Math.min(50, prova.getTitulo().length()));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + nomeArquivo + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(new ByteArrayInputStream(pdf)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    @PostMapping("/api/editar/{id}")
    public ResponseEntity<?> editarProva(@PathVariable Long id,
                                         @RequestBody Map<String, Object> dados,
                                         HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null)
                return ResponseEntity.status(401).body("Usuário não autenticado");

            Prova prova = provaService.buscarPorId(id);
            if (prova == null) return ResponseEntity.notFound().build();

            boolean isCoord = usuarioLogado.getTipoUsuario() != null &&
                    usuarioLogado.getTipoUsuario().getId() == 1;
            boolean isCriador = prova.getCriador().getId().equals(usuarioLogado.getId());
            if (!isCoord && !isCriador)
                return ResponseEntity.status(403).body("Sem permissão para editar esta prova");

            if (dados.containsKey("titulo"))
                prova.setTitulo(((String) dados.get("titulo")).trim());
            if (dados.containsKey("descricao"))
                prova.setDescricao(((String) dados.get("descricao")).trim());
            if (dados.containsKey("questoes")) {
                @SuppressWarnings("unchecked")
                List<Long> ids = (List<Long>) dados.get("questoes");
                if (ids != null && !ids.isEmpty()) {
                    List<Questao> questoes = questaoService.buscarPorIds(ids);
                    prova.setQuestoes(questoes);
                }
            }

            provaService.salvar(prova);
            return ResponseEntity.ok("Prova editada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao editar prova: " + e.getMessage());
        }
    }

    @PostMapping("/api/excluir/{id}")
    public ResponseEntity<?> excluirProva(@PathVariable Long id, HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null)
                return ResponseEntity.status(401).body("Usuário não autenticado");

            Prova prova = provaService.buscarPorId(id);
            if (prova == null) return ResponseEntity.notFound().build();

            boolean isCoord = usuarioLogado.getTipoUsuario() != null &&
                    usuarioLogado.getTipoUsuario().getId() == 1;
            if (!isCoord)
                return ResponseEntity.status(403).body("Apenas coordenadores podem excluir provas");

            provaService.excluir(id);
            return ResponseEntity.ok("Prova excluída com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir prova: " + e.getMessage());
        }
    }
}
