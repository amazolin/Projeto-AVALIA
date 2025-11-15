package com.example.service;

import com.example.dto.CriarProvaDTO;
import com.example.model.OpcaoQuestao;
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
	 * Gera o PDF com cabeçalho + questões + alternativas + gabaritos.
	 */
	public byte[] gerarProvaPDF(Long provaId) throws Exception {

		Prova prova = provaRepository.findById(provaId)
				.orElseThrow(() -> new RuntimeException("Prova não encontrada!"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(baos);
		PdfDocument pdfDoc = new PdfDocument(writer);
		Document document = new Document(pdfDoc);

		// =================== CABEÇALHO COM IMAGEM ===================

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
			System.out.println("⚠️ ATENÇÃO: Imagem não encontrada");
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

		document.add(new Paragraph("\n"));

		// =================== GABARITO EM BRANCO (PARA O ALUNO) ===================
		adicionarGabaritoEmBranco(document, prova);

		// Linha separadora
		document.add(new Paragraph("\n___________________________________________\n")
				.setTextAlignment(TextAlignment.CENTER));

		// =================== QUESTÕES + ALTERNATIVAS ===================

		int numero = 1;
		for (Questao questao : prova.getQuestoes()) {

			// Cabeçalho da questão
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

			document.add(new Paragraph("\n"));

			// Alternativas
			if (questao.getOpcoes() != null && !questao.getOpcoes().isEmpty()) {
				for (OpcaoQuestao opcao : questao.getOpcoes()) {
					String textoOpcao = opcao.getLetra() + ") " + opcao.getTexto();

					Paragraph paragrafoOpcao = new Paragraph(textoOpcao)
							.setFontSize(10)
							.setMarginLeft(20)
							.setMarginTop(3);

					document.add(paragrafoOpcao);
				}
			} else {
				// Se não houver alternativas, adiciona linhas para resposta dissertativa
				document.add(new Paragraph("___________________________________________________")
						.setFontSize(10)
						.setMarginLeft(10));
				document.add(new Paragraph("___________________________________________________")
						.setFontSize(10)
						.setMarginLeft(10));
			}

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

		// =================== NOVA PÁGINA - GABARITO DO PROFESSOR ===================
		document.add(new com.itextpdf.layout.element.AreaBreak());

		adicionarGabaritoProfessor(document, prova);

		document.close();
		return baos.toByteArray();
	}

	/**
	 * Adiciona gabarito em branco para o aluno preencher.
	 */
	private void adicionarGabaritoEmBranco(Document document, Prova prova) {
		Paragraph tituloGabarito = new Paragraph("GABARITO")
				.setBold()
				.setFontSize(12)
				.setTextAlignment(TextAlignment.CENTER);
		document.add(tituloGabarito);

		int numQuestoes = prova.getQuestoes().size();

		// Determinar número máximo de alternativas
		int maxAlternativas = calcularMaxAlternativas(prova);

		// Tabela
		com.itextpdf.layout.element.Table tabela = new com.itextpdf.layout.element.Table(maxAlternativas + 1);
		tabela.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(60));
		tabela.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

		// Cabeçalho
		tabela.addHeaderCell(new com.itextpdf.layout.element.Cell()
				.add(new Paragraph("Nº DAS\nQUESTÕES").setFontSize(8).setBold())
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(3));

		tabela.addHeaderCell(new com.itextpdf.layout.element.Cell(1, maxAlternativas)
				.add(new Paragraph("RESPOSTAS").setFontSize(8).setBold())
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(3));

		// Segunda linha do cabeçalho (letras)
		tabela.addCell(new com.itextpdf.layout.element.Cell()
				.add(new Paragraph("").setFontSize(7))
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(3));

		for (int i = 0; i < maxAlternativas; i++) {
			char letra = (char) ('A' + i);
			tabela.addCell(new com.itextpdf.layout.element.Cell()
					.add(new Paragraph(String.valueOf(letra)).setFontSize(8).setBold())
					.setTextAlignment(TextAlignment.CENTER)
					.setPadding(3));
		}

		// Linhas das questões (EM BRANCO)
		for (int numQuestao = 1; numQuestao <= numQuestoes; numQuestao++) {
			tabela.addCell(new com.itextpdf.layout.element.Cell()
					.add(new Paragraph(String.format("%02d", numQuestao)).setFontSize(8))
					.setTextAlignment(TextAlignment.CENTER)
					.setPadding(3));

			for (int i = 0; i < maxAlternativas; i++) {
				char letra = (char) ('A' + i);
				tabela.addCell(new com.itextpdf.layout.element.Cell()
						.add(new Paragraph(String.valueOf(letra)).setFontSize(8))
						.setTextAlignment(TextAlignment.CENTER)
						.setPadding(3));
			}
		}

		document.add(tabela);
	}

	/**
	 * Adiciona gabarito preenchido para o professor.
	 */
	private void adicionarGabaritoProfessor(Document document, Prova prova) {
		Paragraph tituloGabarito = new Paragraph("GABARITO DO PROFESSOR")
				.setBold()
				.setFontSize(16)
				.setTextAlignment(TextAlignment.CENTER);
		document.add(tituloGabarito);
		document.add(new Paragraph("\n"));

		int maxAlternativas = calcularMaxAlternativas(prova);

		// Tabela
		com.itextpdf.layout.element.Table tabela = new com.itextpdf.layout.element.Table(maxAlternativas + 1);
		tabela.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(60));
		tabela.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

		// Cabeçalho
		tabela.addHeaderCell(new com.itextpdf.layout.element.Cell()
				.add(new Paragraph("Nº DAS\nQUESTÕES").setFontSize(9).setBold())
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(5));

		tabela.addHeaderCell(new com.itextpdf.layout.element.Cell(1, maxAlternativas)
				.add(new Paragraph("RESPOSTAS").setFontSize(9).setBold())
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(5));

		// Segunda linha do cabeçalho (letras)
		tabela.addCell(new com.itextpdf.layout.element.Cell()
				.add(new Paragraph("").setFontSize(8))
				.setTextAlignment(TextAlignment.CENTER)
				.setPadding(5));

		for (int i = 0; i < maxAlternativas; i++) {
			char letra = (char) ('A' + i);
			tabela.addCell(new com.itextpdf.layout.element.Cell()
					.add(new Paragraph(String.valueOf(letra)).setFontSize(9).setBold())
					.setTextAlignment(TextAlignment.CENTER)
					.setPadding(5));
		}

		// Linhas das questões COM RESPOSTAS CORRETAS
		int numQuestao = 1;
		for (Questao questao : prova.getQuestoes()) {
			tabela.addCell(new com.itextpdf.layout.element.Cell()
					.add(new Paragraph(String.format("%02d", numQuestao)).setFontSize(9))
					.setTextAlignment(TextAlignment.CENTER)
					.setPadding(5));

			for (int i = 0; i < maxAlternativas; i++) {
				char letra = (char) ('A' + i);

				// Verifica se esta letra é a resposta correta
				boolean isCorreta = false;
				if (questao.getOpcoes() != null) {
					for (OpcaoQuestao opcao : questao.getOpcoes()) {
						if (opcao.getLetra() != null &&
								opcao.getLetra().equalsIgnoreCase(String.valueOf(letra)) &&
								opcao.isCorreta()) {
							isCorreta = true;
							break;
						}
					}
				}

				com.itextpdf.layout.element.Cell celula = new com.itextpdf.layout.element.Cell()
						.add(new Paragraph(String.valueOf(letra)).setFontSize(9))
						.setTextAlignment(TextAlignment.CENTER)
						.setPadding(5);

				// Marca a resposta correta com fundo cinza
				if (isCorreta) {
					celula.setBackgroundColor(ColorConstants.LIGHT_GRAY);
					celula.setBold();
				}

				tabela.addCell(celula);
			}

			numQuestao++;
		}

		document.add(tabela);
	}

	/**
	 * Calcula o número máximo de alternativas entre todas as questões.
	 */
	private int calcularMaxAlternativas(Prova prova) {
		int maxAlternativas = 0;
		for (Questao q : prova.getQuestoes()) {
			if (q.getOpcoes() != null) {
				maxAlternativas = Math.max(maxAlternativas, q.getOpcoes().size());
			}
		}
		// Se não houver alternativas, usa 4 por padrão (A, B, C, D)
		if (maxAlternativas == 0)
			maxAlternativas = 4;
		return maxAlternativas;
	}
}
