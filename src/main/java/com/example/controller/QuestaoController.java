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

    /**
     * Exibe o formulário de cadastro de questões
     */
    @GetMapping("/questoes")
    public String exibirCadastroQuestao(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        model.addAttribute("listaDisciplinas", disciplinaService.findAllByUsuario(usuarioLogado));
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "questoes";
    }

    /**
     * Mostra o banco de questões, filtrando por disciplina (se houver)
     */
    @GetMapping("/questoes/banco")
    public String mostrarBancoDeQuestoes(@RequestParam(required = false) Long disciplinaId,
            Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        model.addAttribute("listaDisciplinas", disciplinaService.findAllByUsuario(usuarioLogado));
        model.addAttribute("usuarioLogado", usuarioLogado);

        if (disciplinaId != null) {
            model.addAttribute("questoes", disciplinaService.buscarQuestoesPorDisciplina(disciplinaId));
            model.addAttribute("disciplinaSelecionada", disciplinaId);
        } else {
            model.addAttribute("questoes", java.util.Collections.emptyList());
        }

        return "banco-questoes";
    }

    /**
     * Salva uma nova questão (com alternativas)
     */
    @PostMapping("/questoes/salvar")
    public String salvarQuestao(
            @RequestParam Long disciplinaId,
            @RequestParam String enunciado,
            @RequestParam String tipoQuestao,
            @RequestParam(name = "alternativaText", required = false) java.util.List<String> alternativaText,
            @RequestParam(name = "alternativaCorreta", required = false) String alternativaCorreta,
            HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        com.example.model.Questao q = new com.example.model.Questao();
        com.example.model.Disciplina d = disciplinaService.buscarPorId(disciplinaId);
        q.setDisciplina(d);
        q.setEnunciado(enunciado);
        q.setTipoQuestao(com.example.model.Questao.TipoQuestao.valueOf(tipoQuestao));
        q.setCriador(usuarioLogado);
        com.example.model.Questao saved = questaoService.salvarQuestao(q);

        // Criação das alternativas
        if ("verdadeiro_falso".equals(tipoQuestao)) {
            String[] textos = { "Verdadeiro", "Falso" };
            String[] letras = { "a", "b" };
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
            if (alternativaText != null) {
                String[] letras = { "a", "b", "c", "d", "e" };
                for (int i = 0; i < alternativaText.size() && i < letras.length; i++) {
                    String texto = alternativaText.get(i);
                    if (texto == null || texto.trim().isEmpty())
                        continue;
                    boolean correta = false;
                    if (alternativaCorreta != null && !alternativaCorreta.isEmpty()) {
                        char c = alternativaCorreta.charAt(0);
                        int idx = c - 'A';
                        correta = (idx == i);
                    }
                    com.example.model.OpcaoQuestao op = new com.example.model.OpcaoQuestao();
                    op.setTexto(texto);
                    op.setLetra(letras[i]);
                    op.setCorreta(correta);
                    op.setQuestao(saved);
                    opcaoQuestaoService.salvarOpcao(op);
                }
            }
        }

        return "redirect:/questoes/banco?disciplinaId=" + disciplinaId;
    }

    /**
     * Exibe a tela de visualização da questão
     */
    @GetMapping("/questoes/ver/{id}")
    public String verQuestao(
            @PathVariable Long id,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "disciplinaId", required = false) Long disciplinaId,
            Model model,
            HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            return "redirect:/banco-questoes";
        }

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
        model.addAttribute("origem", origem);
        model.addAttribute("disciplinaId", disciplinaId);

        return "questao-view";
    }

    /**
     * Exibe o formulário de edição de questão
     */
    @GetMapping("/questoes/editar/{id}")
    public String editarQuestaoForm(@PathVariable Long id,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "disciplinaId", required = false) Long disciplinaId,
            Model model,
            HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            return "redirect:/banco-questoes";
        }

        boolean ehCoordenador = usuarioLogado.getTipoUsuario() != null &&
                usuarioLogado.getTipoUsuario().getId() == 1;
        boolean ehCriador = q.getCriador() != null &&
                q.getCriador().getId().equals(usuarioLogado.getId());

        if (!ehCoordenador && !ehCriador) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar esta questão.");
            return "redirect:/banco-questoes?disciplinaId=" + q.getDisciplina().getId();
        }

        // Determina a URL de retorno
        String urlVoltar = "/banco-questoes";
        if ("gerar-provas".equals(origem)) {
            urlVoltar = "/gerar-provas";
            if (disciplinaId != null) {
                urlVoltar += "?disciplinaId=" + disciplinaId;
            }
        } else if (q.getDisciplina() != null) {
            urlVoltar += "?disciplinaId=" + q.getDisciplina().getId();
        }

        model.addAttribute("questao", q);
        model.addAttribute("listaDisciplinas", disciplinaService.findAllByUsuario(usuarioLogado));
        model.addAttribute("opcoes", opcaoQuestaoService.buscarPorQuestaoId(id));
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("urlVoltar", urlVoltar);
        model.addAttribute("origem", origem);
        model.addAttribute("origemDisciplinaId", disciplinaId);

        return "questoes";
    }

    /**
     * Salva as alterações da questão editada
     */
    @PostMapping("/questoes/editar/{id}")
    public String editarQuestaoSalvar(@PathVariable Long id,
            @RequestParam Long disciplinaId,
            @RequestParam String enunciado,
            @RequestParam(name = "alternativaText", required = false) java.util.List<String> alternativaText,
            @RequestParam(name = "alternativaCorreta", required = false) String alternativaCorreta,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "origemDisciplinaId", required = false) Long origemDisciplinaId,
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
            String[] letras = { "a", "b", "c", "d", "e" };
            for (int i = 0; i < alternativaText.size() && i < letras.length; i++) {
                String texto = alternativaText.get(i);
                if (texto == null || texto.trim().isEmpty())
                    continue;

                boolean correta = false;
                if (alternativaCorreta != null && !alternativaCorreta.isEmpty()) {
                    char c = alternativaCorreta.charAt(0);
                    int idx = c - 'A';
                    correta = (idx == i);
                }
                com.example.model.OpcaoQuestao op = new com.example.model.OpcaoQuestao();
                op.setTexto(texto);
                op.setLetra(letras[i]);
                op.setCorreta(correta);
                op.setQuestao(q);
                opcaoQuestaoService.salvarOpcao(op);
            }
        }

        // Redireciona para a origem correta
        if ("gerar-provas".equals(origem)) {
            String redirect = "redirect:/gerar-provas";
            if (origemDisciplinaId != null) {
                redirect += "?disciplinaId=" + origemDisciplinaId;
            }
            return redirect;
        }
        return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
    }

    /**
     * Exclui uma questão
     */
    @PostMapping("/questoes/excluir/{id}")
    public String excluirQuestao(@PathVariable Long id,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "disciplinaId", required = false) Long origemDisciplinaId,
            RedirectAttributes redirect) {
        com.example.model.Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            redirect.addFlashAttribute("erro", "Questão não encontrada.");
            return "redirect:/questoes/banco";
        }

        Long disciplinaId = q.getDisciplina().getId();

        // Apaga alternativas
        opcaoQuestaoService.apagarPorQuestaoId(id);

        // Apaga a questão
        questaoService.excluirPorId(id);

        redirect.addFlashAttribute("mensagem", "Questão excluída com sucesso!");

        // Redireciona para a origem correta
        if ("gerar-provas".equals(origem)) {
            String redirectUrl = "redirect:/gerar-provas";
            if (origemDisciplinaId != null) {
                redirectUrl += "?disciplinaId=" + origemDisciplinaId;
            }
            return redirectUrl;
        }
        return "redirect:/questoes/banco?disciplinaId=" + disciplinaId;
    }
}
