package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable 
public class SimuladoQuestaoId implements Serializable {

  

    @Column(name = "id_simulado") 
    private Integer idSimulado;

    @Column(name = "id_questao") 
    private Integer idQuestao;

    
    public SimuladoQuestaoId() {
    }

   
    public SimuladoQuestaoId(Integer idSimulado, Integer idQuestao) {
        this.idSimulado = idSimulado;
        this.idQuestao = idQuestao;
    }

    
    public Integer getIdSimulado() {
        return idSimulado;
    }

    public void setIdSimulado(Integer idSimulado) {
        this.idSimulado = idSimulado;
    }

    public Integer getIdQuestao() {
        return idQuestao;
    }

    public void setIdQuestao(Integer idQuestao) {
        this.idQuestao = idQuestao;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimuladoQuestaoId that = (SimuladoQuestaoId) o;
        return Objects.equals(idSimulado, that.idSimulado) && Objects.equals(idQuestao, that.idQuestao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSimulado, idQuestao);
    }
}