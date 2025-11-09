package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.model.Usuario;
import com.example.service.DisciplinaService;
import com.example.service.OpcaoQuestaoService;
import com.example.service.QuestaoService;

import jakarta.servlet.http.HttpSession;

@Controller 
public class QuestaoController {

    @Autowired
    private DisciplinaService disciplinaService; 

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private OpcaoQuestaoService opcaoQuestaoService;


    @GetMapping("/questoes") 
    public String exibirCadastroQuestao(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // Carrega a lista do BD e a insere no Model 
        model.addAttribute("listaDisciplinas", disciplinaService.findAll());
        model.addAttribute("usuarioLogado", usuarioLogado);
        
        // Retorna o template questoes.html
        return "questoes"; 
    }

    @GetMapping("/banco-questoes")
    public String mostrarBancoDeQuestoes(@RequestParam(required = false) Long disciplinaId, 
                                         Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("listaDisciplinas", disciplinaService.findAll());
        model.addAttribute("usuarioLogado", usuarioLogado);

        if (disciplinaId != null) {
            // Filtro das questões puxando por disciplina
            model.addAttribute("questoes", disciplinaService.buscarQuestoesPorDisciplina(disciplinaId));
            model.addAttribute("disciplinaSelecionada", disciplinaId);
        } else {
            model.addAttribute("questoes", java.util.Collections.emptyList());
        }

        return "banco-questoes";
    }

    @PostMapping("/questoes/salvar")
    public String salvarQuestao(
            @RequestParam Long disciplinaId,
            @RequestParam String enunciado,
            @RequestParam String tipoQuestao,
            @RequestParam(name = "alternativaText", required = false) java.util.List<String> alternativaText,
            @RequestParam(name = "alternativaCorreta", required = false) String alternativaCorreta,
            HttpSession session
    ) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        com.example.model.Questao q = new com.example.model.Questao();
        com.example.model.Disciplina d = disciplinaService.buscarPorId(disciplinaId);
        q.setDisciplina(d);
        q.setEnunciado(enunciado);
        q.setTipoQuestao(com.example.model.Questao.TipoQuestao.valueOf(tipoQuestao));
        q.setCriador(usuarioLogado); // Define o criador
        com.example.model.Questao saved = questaoService.salvarQuestao(q);

        // Validação pelo tipo de questão
        if ("verdadeiro_falso".equals(tipoQuestao)) {
            String[] textos = {"Verdadeiro", "Falso"};
            String[] letras = {"a", "b"};
            for (int i = 0; i < 2; i++) {
                boolean correta = false;
                if (alternativaCorreta != null && !alternativaCorreta.isEmpty()) {
                    char c = alternativaCorreta.charAt(0);
                    int idx = c - 'A';
                    correta = (idx == i);
                }
                com.example.model.OpcaoQuestao op = new com.example.model.OpcaoQuestao();
                op.setTexto(textos[i]);
                op.setLetra(letras[i]);
                op.setCorreta(correta);
                op.setQuestao(saved);
                opcaoQuestaoService.salvarOpcao(op);
            }
        } else {
            // Para múltipla escolha, usa as alternativas informadas
            if (alternativaText != null) {
                String[] letras = {"a", "b", "c", "d", "e"};
                for (int i = 0; i < alternativaText.size() && i < letras.length; i++) {
                    String texto = alternativaText.get(i);
                    // Pula alternativas vazias
                    if (texto == null || texto.trim().isEmpty()) {
                        continue;
                    }
                    boolean correta = false;
                    if (alternativaCorreta != null && !alternativaCorreta.isEmpty()) {
                        char c = alternativaCorreta.charAt(0);
                        int idx = c - 'A';
                        correta = (idx == i);
                    }
                    com.example.model.OpcaoQuestao op = new com.example.model.OpcaoQuestao();
                    op.setTexto(texto);
                    op.setLetra(letras[i]); // Define a letra baseada no índice
                    op.setCorreta(correta);
                    op.setQuestao(saved);
                    opcaoQuestaoService.salvarOpcao(op);
                }
            }
        }

        return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
    }

    @GetMapping("/questoes/editar/{id}")
    public String editarQuestaoForm(@PathVariable Long id, Model model, 
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            return "redirect:/banco-questoes";
        }
        
        // Verifica permissões: apenas o criador ou coordenador podem editar
        boolean ehCoordenador = usuarioLogado.getTipoUsuario() != null && 
                                usuarioLogado.getTipoUsuario().getId() == 1;
        boolean ehCriador = q.getCriador() != null && 
                           q.getCriador().getId().equals(usuarioLogado.getId());
        
        if (!ehCoordenador && !ehCriador) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar esta questão.");
            return "redirect:/banco-questoes?disciplinaId=" + q.getDisciplina().getId();
        }
        
        model.addAttribute("questao", q);
        model.addAttribute("listaDisciplinas", disciplinaService.findAll());
        model.addAttribute("opcoes", opcaoQuestaoService.buscarPorQuestaoId(id));
        model.addAttribute("usuarioLogado", usuarioLogado);
        return "questoes";
    }

    @PostMapping("/questoes/editar/{id}")
    public String editarQuestaoSalvar(@PathVariable Long id,
                                      @RequestParam Long disciplinaId,
                                      @RequestParam String enunciado,
                                      @RequestParam(name = "alternativaText", required = false) java.util.List<String> alternativaText,
                                      @RequestParam(name = "alternativaCorreta", required = false) String alternativaCorreta,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            return "redirect:/banco-questoes";
        }
        
        // Verifica permissões
        boolean ehCoordenador = usuarioLogado.getTipoUsuario() != null && 
                                usuarioLogado.getTipoUsuario().getId() == 1;
        boolean ehCriador = q.getCriador() != null && 
                           q.getCriador().getId().equals(usuarioLogado.getId());
        
        if (!ehCoordenador && !ehCriador) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar esta questão.");
            return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
        }
        
        com.example.model.Disciplina d = disciplinaService.buscarPorId(disciplinaId);
        q.setDisciplina(d);
        q.setEnunciado(enunciado);
        questaoService.salvarQuestao(q);

        opcaoQuestaoService.apagarPorQuestaoId(id);
        if (alternativaText != null) {
            String[] letras = {"a", "b", "c", "d", "e"};
            for (int i = 0; i < alternativaText.size() && i < letras.length; i++) {
                String texto = alternativaText.get(i);
                // Pula alternativas vazias (permite questões V/F com só 2 opções)
                if (texto == null || texto.trim().isEmpty()) {
                    continue;
                }
                boolean correta = false;
                if (alternativaCorreta != null && !alternativaCorreta.isEmpty()) {
                    char c = alternativaCorreta.charAt(0);
                    int idx = c - 'A';
                    correta = (idx == i);
                }
                com.example.model.OpcaoQuestao op = new com.example.model.OpcaoQuestao();
                op.setTexto(texto);
                op.setLetra(letras[i]); // Define a letra baseada no índice
                op.setCorreta(correta);
                op.setQuestao(q);
                opcaoQuestaoService.salvarOpcao(op);
            }
        }

        return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
    }

    @PostMapping("/questoes/excluir/{id}")
    public String excluirQuestao(@PathVariable Long id, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        com.example.model.Questao q = questaoService.buscarPorId(id);
        Long disciplinaId = null;
        if (q != null && q.getDisciplina() != null) {
            disciplinaId = q.getDisciplina().getId();
        }
        
        // Apenas coordenador pode excluir
        boolean ehCoordenador = usuarioLogado.getTipoUsuario() != null && 
                                usuarioLogado.getTipoUsuario().getId() == 1;
        
        if (!ehCoordenador) {
            redirectAttributes.addFlashAttribute("erro", "Apenas o coordenador pode remover questões.");
            if (disciplinaId != null) {
                return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
            }
            return "redirect:/banco-questoes";
        }
        
        opcaoQuestaoService.apagarPorQuestaoId(id);
        questaoService.apagarPorId(id);
        
        if (disciplinaId != null) {
            return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
        }
        return "redirect:/banco-questoes";
    }

    @GetMapping("/questoes/ver/{id}")
    public String verQuestao(@PathVariable Long id, 
                            @RequestParam(value = "origem", required = false) String origem,
                            @RequestParam(value = "disciplinaId", required = false) Long disciplinaId,
                            Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) return "redirect:/banco-questoes";
        
        // Determina para onde o botão "Voltar" deve levar
        String urlVoltar = "/banco-questoes";
        if ("gerar-provas".equals(origem)) {
            urlVoltar = "/gerar-provas";
            if (disciplinaId != null) {
                urlVoltar += "?disciplinaId=" + disciplinaId;
            }
        } else if (disciplinaId != null) {
            urlVoltar += "?disciplinaId=" + disciplinaId;
        }
        
        model.addAttribute("questao", q);
        model.addAttribute("opcoes", opcaoQuestaoService.buscarPorQuestaoId(id));
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("urlVoltar", urlVoltar);
        return "questao-view";
    }
}