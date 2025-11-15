package com.example.model;

import jakarta.persistence.*;
import java.util.List;

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
    
    // ✅ ADICIONE ESTE RELACIONAMENTO
    @OneToMany(mappedBy = "questao", fetch = FetchType.EAGER)
    @OrderBy("letra ASC")
    private List<OpcaoQuestao> opcoes;

    @Column(name = "imagem")
    private String imagem;

    public String getImagem() {
        return imagem;
    }
    
    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
   
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
    
    // ✅ GETTER E SETTER PARA AS OPÇÕES
    public List<OpcaoQuestao> getOpcoes() {
        return opcoes;
    }
    
    public void setOpcoes(List<OpcaoQuestao> opcoes) {
        this.opcoes = opcoes;
    }
}