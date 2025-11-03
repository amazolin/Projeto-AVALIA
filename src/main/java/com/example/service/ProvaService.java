package com.example.service;

import com.example.dto.CriarProvaDTO;
import com.example.model.Prova;
import com.example.model.Questao;
import com.example.model.Usuario;
import com.example.repository.ProvaRepository;
import com.example.repository.QuestaoRepository;
import com.example.repository.UsuarioRepository;

// Imports do iText7 para PDF
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

// Imports do Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
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
	
	/**
	 * Busca todas as provas
	 */
	public List<Prova> buscarTodas() {
		return provaRepository.findAllByOrderByDataCriacaoDesc();
	}
	
	/**
	 * Busca prova por ID
	 */
	public Prova buscarPorId(Long id) {
		return provaRepository.findById(id).orElse(null);
	}
	
	/**
	 * Busca provas por criador
	 */
	public List<Prova> buscarPorCriador(Long criadorId) {
		return provaRepository.findByCriadorId(criadorId);
	}
	
	/**
	 * Salva uma prova (criar ou atualizar)
	 */
	public Prova salvar(Prova prova) {
		return provaRepository.save(prova);
	}
	
	/**
	 * Alias para compatibilidade com código antigo
	 */
	public Prova salvarProva(Prova novaProva) {
		return provaRepository.save(novaProva);
	}
	
	/**
	 * Exclui uma prova
	 */
	public void excluir(Long id) {
		provaRepository.deleteById(id);
	}
	
	/**
	 * Busca provas por título
	 */
	public List<Prova> buscarPorTitulo(String titulo) {
		return provaRepository.findByTituloContainingIgnoreCase(titulo);
	}
	
	/**
	 * Verifica se uma prova existe
	 */
	public boolean existe(Long id) {
		return provaRepository.existsById(id);
	}
	
	// ==================== NOVOS MÉTODOS ====================
	
	/**
	 * Cria uma nova prova a partir do DTO vindo do frontend.
	 * Converte CriarProvaDTO -> Prova (Entity)
	 */
	@Transactional
	public Long criarProva(CriarProvaDTO dto, Usuario usuarioLogado) {
		System.out.println("🔨 Criando prova: " + dto.getTitulo());
		
		// 1. Valida o usuário
		if (usuarioLogado == null) {
			throw new RuntimeException("Usuário não está logado!");
		}
		System.out.println("👤 Usuário logado: " + usuarioLogado.getNome());
		
		// 2. Busca as questões selecionadas pelo ID
		List<Questao> questoes = questaoRepository.findAllById(dto.getQuestoes());
		
		if (questoes.isEmpty()) {
			throw new RuntimeException("Nenhuma questão válida foi encontrada!");
		}
		System.out.println("📝 Questões encontradas: " + questoes.size());
		
		// 3. Cria a entidade Prova
		Prova prova = new Prova();
		prova.setTitulo(dto.getTitulo());
		prova.setCriador(usuarioLogado);
		prova.setQuestoes(questoes);
		// dataCriacao é preenchida automaticamente pelo @PrePersist
		
		// 4. Salva no banco
		prova = provaRepository.save(prova);
		
		System.out.println("✅ Prova salva com ID: " + prova.getId());
		return prova.getId();
	}
	
	/**
	 * Gera o PDF da prova usando iText7.
	 * Versão adaptada para questões SEM alternativas (apenas enunciados)
	 */
	public byte[] gerarProvaPDF(Long provaId) throws Exception {
		System.out.println("🔍 Iniciando geração do PDF para prova ID: " + provaId);
		
		// Busca a prova no banco
		Prova prova = provaRepository.findById(provaId)
				.orElseThrow(() -> new RuntimeException("Prova não encontrada!"));
		
		System.out.println("✅ Prova encontrada: " + prova.getTitulo());
		System.out.println("📝 Total de questões: " + prova.getQuestoes().size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(baos);
		PdfDocument pdfDoc = new PdfDocument(writer);
		Document document = new Document(pdfDoc);
		
		// === CABEÇALHO ===
		
		// Título da prova
		Paragraph titulo = new Paragraph(prova.getTitulo())
				.setBold()
				.setFontSize(18)
				.setTextAlignment(TextAlignment.CENTER);
		document.add(titulo);
		
		// Data de criação
		if (prova.getDataCriacao() != null) {
			String dataFormatada = prova.getDataCriacao()
					.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Paragraph data = new Paragraph("Data: " + dataFormatada)
					.setFontSize(10)
					.setTextAlignment(TextAlignment.RIGHT);
			document.add(data);
		}
		
		// Criador
		if (prova.getCriador() != null) {
			Paragraph criador = new Paragraph("Criado por: " + prova.getCriador().getNome())
					.setFontSize(10)
					.setTextAlignment(TextAlignment.RIGHT);
			document.add(criador);
		}
		
		// Linha separadora
		document.add(new Paragraph("\n___________________________________________\n")
				.setTextAlignment(TextAlignment.CENTER));
		
		// === QUESTÕES ===
		
		int numero = 1;
		for (Questao questao : prova.getQuestoes()) {
			// Número da questão + disciplina
			Paragraph cabecalho = new Paragraph()
					.add(new Text("Questão " + numero)
							.setBold()
							.setFontSize(12));
			
			// Adiciona disciplina se existir
			if (questao.getDisciplina() != null) {
				cabecalho.add(new Text(" - " + questao.getDisciplina().getNome())
						.setItalic()
						.setFontSize(9)
						.setFontColor(ColorConstants.BLUE));
			}
			
			document.add(cabecalho);
			
			// Enunciado
			Paragraph enunciado = new Paragraph(questao.getEnunciado())
					.setFontSize(11)
					.setMarginLeft(10)
					.setMarginTop(5);
			document.add(enunciado);
			
			// Espaço para resposta (linhas em branco)
			document.add(new Paragraph("\n_____________________________________________________\n")
					.setFontSize(10)
					.setMarginLeft(10));
			document.add(new Paragraph("_____________________________________________________\n")
					.setFontSize(10)
					.setMarginLeft(10));
			
			// Espaço entre questões
			document.add(new Paragraph("\n"));
			numero++;
		}
		
		// Rodapé
		document.add(new Paragraph("\n\n___________________________________________")
				.setTextAlignment(TextAlignment.CENTER));
		document.add(new Paragraph("Total de questões: " + prova.getQuestoes().size())
				.setTextAlignment(TextAlignment.CENTER)
				.setFontSize(9)
				.setItalic());
		
		document.close();
		
		System.out.println("✅ PDF gerado com sucesso!");
		return baos.toByteArray();
	}
}