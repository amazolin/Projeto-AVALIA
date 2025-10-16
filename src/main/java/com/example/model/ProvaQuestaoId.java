package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable 
public class ProvaQuestaoId implements Serializable {

   

    @Column(name = "id_prova") 
    private Integer idProva;

    @Column(name = "id_questao") 
    private Integer idQuestao;

   
    public ProvaQuestaoId() {
    }

    
    public ProvaQuestaoId(Integer idProva, Integer idQuestao) {
        this.idProva = idProva;
        this.idQuestao = idQuestao;
    }

  
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProvaQuestaoId that = (ProvaQuestaoId) o;
        return Objects.equals(idProva, that.idProva) && Objects.equals(idQuestao, that.idQuestao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProva, idQuestao);
    }
}