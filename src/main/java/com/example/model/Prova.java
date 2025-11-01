package com.example.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity 
@Table(name = "provas") 
public class Prova {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome")
    private String titulo;  // Mantive compatível com o controller
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    // Relacionamento com o criador da prova
    @ManyToOne
    @JoinColumn(name = "criador_id")
    private Usuario criador;
    
    // Relacionamento com as questões da prova
    @ManyToMany
    @JoinTable(
        name = "prova_questoes",
        joinColumns = @JoinColumn(name = "prova_id"),
        inverseJoinColumns = @JoinColumn(name = "questao_id")
    )
    private List<Questao> questoes;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    // Métodos automáticos para datas
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtores
    public Prova() {
    }
    
    public Prova(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
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
    
    // Alias para compatibilidade (se você usar "nome" em algum lugar)
    public String getNome() {
        return titulo;
    }
    
    public void setNome(String nome) {
        this.titulo = nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}