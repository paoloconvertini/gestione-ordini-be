package it.calolenoci.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class OrdineDettaglioId implements Serializable {

    @Column(length = 4)
    private  Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;

    @Column
    private Integer rigo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdineDettaglioId that = (OrdineDettaglioId) o;
        return Objects.equals(anno, that.anno) && Objects.equals(serie, that.serie) && Objects.equals(progressivo, that.progressivo) && Objects.equals(rigo, that.rigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anno, serie, progressivo, rigo);
    }
}
