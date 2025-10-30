package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "opcoes_questao") 
public class OpcaoQuestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcao")
    private Long id;

    @Column(name = "texto_opcao")
    private String texto;
    
    @Column(name = "letra")
    private String letra;
    
    @Column(name = "correta")
    private boolean correta; 
    
    @ManyToOne
    @JoinColumn(name = "id_questao")
    private Questao questao;

    public OpcaoQuestao() {
    }
    
    public OpcaoQuestao(String texto, boolean correta, Questao questao) {
        this.texto = texto;
        this.correta = correta;
        this.questao = questao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }
}