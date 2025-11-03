package com.example.dto;

import java.util.List;

/**
 * DTO para receber os dados do frontend ao criar uma prova.
 * Contém apenas os campos necessários vindos do JavaScript.
 */
public class CriarProvaDTO {
    
    private String titulo;
    private String descricao;
    private List<Long> questoes;  // IDs das questões selecionadas
    
    // Construtores
    public CriarProvaDTO() {
    }
    
    public CriarProvaDTO(String titulo, String descricao, List<Long> questoes) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.questoes = questoes;
    }
    
    // Getters e Setters
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public List<Long> getQuestoes() {
        return questoes;
    }
    
    public void setQuestoes(List<Long> questoes) {
        this.questoes = questoes;
    }
    
    @Override
    public String toString() {
        return "CriarProvaDTO{" +
                "titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", questoes=" + questoes +
                '}';
    }
}