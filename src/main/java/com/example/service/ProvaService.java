package com.example.service;

import com.example.model.Prova;
import com.example.repository.ProvaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProvaService {

	private final ProvaRepository provaRepository;

	public ProvaService(ProvaRepository provaRepository) {
		this.provaRepository = provaRepository;
	}

	/**
	 * Busca todas as provas
	 */
	public List<Prova> buscarTodas() {
		return provaRepository.findAllByOrderByDataCriacaoDesc();
	}

	/**
	 * Busca prova por ID (mudei de Integer para Long)
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
}