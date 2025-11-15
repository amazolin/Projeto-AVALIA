package com.example.controller;

import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import com.example.model.Disciplina;
import com.example.model.OpcaoQuestao;
import com.example.model.Questao;
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
            @RequestParam(name = "imagem", required = false) MultipartFile imagem,
            HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // 1️⃣ Criar questão
        Questao q = new Questao();
        Disciplina d = disciplinaService.buscarPorId(disciplinaId);
        q.setDisciplina(d);
        q.setEnunciado(enunciado);
        q.setTipoQuestao(Questao.TipoQuestao.valueOf(tipoQuestao));
        q.setCriador(usuarioLogado);

        // 2️⃣ UPLOAD DA IMAGEM
        if (imagem != null && !imagem.isEmpty()) {
            try {
                // Cria um nome único
                String nomeArquivo = System.currentTimeMillis() + "_" + imagem.getOriginalFilename();

                // 📁 Pasta fora de resources (funciona em produção!)
                Path destino = Paths.get("uploads/questoes/" + nomeArquivo).toAbsolutePath();

                // cria diretórios se não existirem
                Files.createDirectories(destino.getParent());

                // salva imagem fisicamente
                imagem.transferTo(destino.toFile());

                // salva nome no banco
                q.setImagem(nomeArquivo);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro ao salvar imagem da questão!");
            }
        }

        // 3️⃣ Salva a questão
        Questao saved = questaoService.salvarQuestao(q);

        // 4️⃣ Criar alternativas
        if ("verdadeiro_falso".equalsIgnoreCase(tipoQuestao)) {

            String[] textos = { "Verdadeiro", "Falso" };
            String[] letras = { "A", "B" };

            for (int i = 0; i < 2; i++) {
                boolean correta = alternativaCorreta != null && alternativaCorreta.equalsIgnoreCase(letras[i]);

                OpcaoQuestao op = new OpcaoQuestao();
                op.setTexto(textos[i]);
                op.setLetra(letras[i]);
                op.setCorreta(correta);
                op.setQuestao(saved);
                opcaoQuestaoService.salvarOpcao(op);
            }

        } else {

            if (alternativaText != null) {

                String[] letras = { "A", "B", "C", "D", "E" };

                for (int i = 0; i < alternativaText.size() && i < letras.length; i++) {

                    String texto = alternativaText.get(i);
                    if (texto == null || texto.isBlank())
                        continue;

                    boolean correta = alternativaCorreta != null && alternativaCorreta.equalsIgnoreCase(letras[i]);

                    OpcaoQuestao op = new OpcaoQuestao();
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

        return "questao-view";
    }

    /**
     * Exibe o formulário de edição de questão
     */
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

        boolean ehCoordenador = usuarioLogado.getTipoUsuario() != null &&
                usuarioLogado.getTipoUsuario().getId() == 1;
        boolean ehCriador = q.getCriador() != null &&
                q.getCriador().getId().equals(usuarioLogado.getId());

        if (!ehCoordenador && !ehCriador) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar esta questão.");
            return "redirect:/banco-questoes?disciplinaId=" + q.getDisciplina().getId();
        }

        model.addAttribute("questao", q);
        model.addAttribute("listaDisciplinas", disciplinaService.findAllByUsuario(usuarioLogado));
        model.addAttribute("opcoes", opcaoQuestaoService.buscarPorQuestaoId(id));
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "questoes";
    }

    /**
     * Salva as alterações da questão editada
     */
    @PostMapping("/questoes/editar/{id}")
    public String editarQuestaoSalvar(
            @PathVariable Long id,
            @RequestParam Long disciplinaId,
            @RequestParam String enunciado,
            @RequestParam(name = "alternativaText", required = false) java.util.List<String> alternativaText,
            @RequestParam(name = "alternativaCorreta", required = false) String alternativaCorreta,
            @RequestParam(name = "imagem", required = false) MultipartFile novaImagem, // ← ADICIONADO
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Questao q = questaoService.buscarPorId(id);
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

        // Atualiza dados básicos
        q.setDisciplina(disciplinaService.buscarPorId(disciplinaId));
        q.setEnunciado(enunciado);

        // -------------------------------------------
        // 1️⃣ TRATAMENTO DA IMAGEM NA EDIÇÃO
        // -------------------------------------------
        try {
            if (novaImagem != null && !novaImagem.isEmpty()) {

                // excluir imagem antiga
                if (q.getImagem() != null) {
                    Path antiga = Paths.get("uploads/questoes/" + q.getImagem()).toAbsolutePath();
                    Files.deleteIfExists(antiga);
                }

                // salvar nova imagem
                String nomeArquivo = System.currentTimeMillis() + "_" + novaImagem.getOriginalFilename();
                Path destino = Paths.get("uploads/questoes/" + nomeArquivo).toAbsolutePath();

                Files.createDirectories(destino.getParent());
                novaImagem.transferTo(destino.toFile());

                q.setImagem(nomeArquivo); // atualiza no banco
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // salva a questão
        questaoService.salvarQuestao(q);

        // -------------------------------------------
        // 2️⃣ RECRIAR AS ALTERNATIVAS
        // -------------------------------------------
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

                OpcaoQuestao op = new OpcaoQuestao();
                op.setTexto(texto);
                op.setLetra(letras[i]);
                op.setCorreta(correta);
                op.setQuestao(q);

                opcaoQuestaoService.salvarOpcao(op);
            }
        }

        return "redirect:/banco-questoes?disciplinaId=" + disciplinaId;
    }

    @PostMapping("/questoes/excluir/{id}")
    public String excluirQuestao(@PathVariable Long id, RedirectAttributes redirect) {

        Questao q = questaoService.buscarPorId(id);
        if (q == null) {
            redirect.addFlashAttribute("erro", "Questão não encontrada.");
            return "redirect:/questoes/banco";
        }

        // Apaga a imagem física
        try {
            if (q.getImagem() != null) {
                Path imagem = Paths.get("uploads/questoes/" + q.getImagem()).toAbsolutePath();
                Files.deleteIfExists(imagem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Apaga alternativas
        opcaoQuestaoService.apagarPorQuestaoId(id);

        // Apaga a questão
        questaoService.excluirPorId(id);

        redirect.addFlashAttribute("mensagem", "Questão excluída com sucesso!");

        return "redirect:/questoes/banco?disciplinaId=" + q.getDisciplina().getId();
    }

}
