package com.example.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;

@Entity 
@Table(name = "provas") 
public class Prova {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prova")
    private Long id;
    
    @Column(name = "nome_prova")
    private String titulo;
    
    // Relacionamento com o criador da prova
    @ManyToOne
    @JoinColumn(name = "id_coordenador")
    private Usuario criador;
    
    // Relacionamento com as questões da prova
    @ManyToMany
    @JoinTable(
        name = "provas_questoes",
        joinColumns = @JoinColumn(name = "id_prova"),        // ✅ Corrigido
        inverseJoinColumns = @JoinColumn(name = "id_questao") // ✅ Corrigido
    )
    private List<Questao> questoes;
    
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;
    
    // Métodos automáticos para datas
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
    
    // Construtores
    public Prova() {
    }
    
    public Prova(String titulo) {
        this.titulo = titulo;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    // Alias para compatibilidade
    public String getNome() {
        return titulo;
    }
    
    public void setNome(String nome) {
        this.titulo = nome;
    }
    
    // Método getDescricao para compatibilidade (retorna string vazia)
    public String getDescricao() {
        return "";
    }
    
    // Método setDescricao para compatibilidade (não faz nada)
    public void setDescricao(String descricao) {
        // Ignora - não usa descrição
    }
    
    public Usuario getCriador() {
        return criador;
    }
    
    public void setCriador(Usuario criador) {
        this.criador = criador;
    }
    
    public List<Questao> getQuestoes() {
        return questoes;
    }
    
    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    // Método getDataAtualizacao para compatibilidade (retorna data_criacao)
    public LocalDateTime getDataAtualizacao() {
        return dataCriacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        // Ignora
    }
}