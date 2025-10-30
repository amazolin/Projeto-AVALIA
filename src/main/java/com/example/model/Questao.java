package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "questoes") 
public class Questao {

    public enum TipoQuestao {
        multipla_escolha, verdadeiro_falso
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_questao")
    private Long id;

    @Column(name = "texto_questao")
    private String enunciado; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_questao")
    private TipoQuestao tipoQuestao = TipoQuestao.multipla_escolha;
    
    @ManyToOne
    @JoinColumn(name = "id_disciplina")
    private Disciplina disciplina;
    
    @ManyToOne
    @JoinColumn(name = "id_criador")
    private Usuario criador;
   
    public Questao() {
    }

    public Questao(String enunciado) {
        this.enunciado = enunciado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }
    
    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }
    
    public TipoQuestao getTipoQuestao() {
        return tipoQuestao;
    }

    public void setTipoQuestao(TipoQuestao tipoQuestao) {
        this.tipoQuestao = tipoQuestao;
    }
}