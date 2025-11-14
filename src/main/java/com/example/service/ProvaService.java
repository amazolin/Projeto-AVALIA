package com.example.service;

import com.example.dto.CriarProvaDTO;
import com.example.model.Prova;
import com.example.model.Questao;
import com.example.model.Usuario;
import com.example.repository.ProvaRepository;
import com.example.repository.QuestaoRepository;
import com.example.repository.UsuarioRepository;

// Imports do iText7
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

// Imports do Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProvaService {

    private final ProvaRepository provaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ProvaService(ProvaRepository provaRepository) {
        this.provaRepository = provaRepository;
    }

    // ==================== MÉTODOS EXISTENTES ====================

    public List<Prova> buscarTodas() {
        return provaRepository.findAllByOrderByDataCriacaoDesc();
    }

    public Prova buscarPorId(Long id) {
        return provaRepository.findById(id).orElse(null);
    }

    public List<Prova> buscarPorCriador(Long criadorId) {
        return provaRepository.findByCriadorId(criadorId);
    }

    public Prova salvar(Prova prova) {
        return provaRepository.save(prova);
    }

    public Prova salvarProva(Prova novaProva) {
        return provaRepository.save(novaProva);
    }

    public void excluir(Long id) {
        provaRepository.deleteById(id);
    }

    public List<Prova> buscarPorTitulo(String titulo) {
        return provaRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public boolean existe(Long id) {
        return provaRepository.existsById(id);
    }

    // ==================== NOVOS MÉTODOS ====================

    @Transactional
    public Long criarProva(CriarProvaDTO dto, Usuario usuarioLogado) {
        if (usuarioLogado == null)
            throw new RuntimeException("Usuário não está logado!");

        List<Questao> questoes = questaoRepository.findAllById(dto.getQuestoes());
        if (questoes.isEmpty())
            throw new RuntimeException("Nenhuma questão válida foi encontrada!");

        Prova prova = new Prova();
        prova.setTitulo(dto.getTitulo());
        prova.setCriador(usuarioLogado);
        prova.setQuestoes(questoes);

        prova = provaRepository.save(prova);
        return prova.getId();
    }

    /**
     * Gera o PDF com cabeçalho + questões.
     */
    public byte[] gerarProvaPDF(Long provaId) throws Exception {

        Prova prova = provaRepository.findById(provaId)
                .orElseThrow(() -> new RuntimeException("Prova não encontrada!"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // =================== CABEÇALHO COM IMAGEM (REDIMENSIONADA) ===================

        String caminhoImagem = "img/fatec-logo.png";

        InputStream is = ProvaService.class.getResourceAsStream("/img/fatec-logo.png");
        if (is != null) {
            byte[] bytes = is.readAllBytes();
            ImageData imageData = ImageDataFactory.create(bytes);
            Image cabecalhoImg = new Image(imageData)
                    .setWidth(240)  
                    .setHeight(100)
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

            document.add(cabecalhoImg);
            document.add(new Paragraph("\n"));
        } else {
            System.out.println("⚠️ ATENÇÃO: Imagem não encontrada em: " + caminhoImagem);
        }

        // =================== TÍTULO DA PROVA ===================
        Paragraph titulo = new Paragraph(prova.getTitulo())
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);
        document.add(new Paragraph("\n"));

        // =================== CAMPOS DE PREENCHIMENTO ===================
        Paragraph camposPreenchimento = new Paragraph()
                .add("Data: ___/___/____     ")
                .add("Nome: __________________________________     ")
                .add("Nota: _________")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(camposPreenchimento);

        // Linha separadora
        document.add(new Paragraph("\n___________________________________________\n")
                .setTextAlignment(TextAlignment.CENTER));

        // =================== QUESTÕES ===================

        int numero = 1;
        for (Questao questao : prova.getQuestoes()) {

            Paragraph cabecalho = new Paragraph()
                    .add("Questão " + numero)
                    .setBold()
                    .setFontSize(12);

            if (questao.getDisciplina() != null) {
                cabecalho.add(new Paragraph(" - " + questao.getDisciplina().getNome())
                        .setItalic()
                        .setFontSize(9)
                        .setFontColor(ColorConstants.BLUE));
            }

            document.add(cabecalho);

            // Enunciado
            document.add(new Paragraph(questao.getEnunciado())
                    .setFontSize(11)
                    .setMarginLeft(10)
                    .setMarginTop(5));

            // Linhas para resposta
            document.add(new Paragraph("\n___________________________________________________")
                    .setFontSize(10)
                    .setMarginLeft(10));
            document.add(new Paragraph("___________________________________________________\n")
                    .setFontSize(10)
                    .setMarginLeft(10));

            document.add(new Paragraph("\n"));
            numero++;
        }

        // Rodapé
        document.add(new Paragraph("\n___________________________________________")
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Total de questões: " + prova.getQuestoes().size())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9)
                .setItalic());

        document.close();
        return baos.toByteArray();
    }
}
