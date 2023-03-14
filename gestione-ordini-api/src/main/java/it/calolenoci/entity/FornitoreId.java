package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class FornitoreId implements Serializable {

    @Column(length = 4, name = "ANNOOAF")
    private  Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    private String serie;

    @Column(name="PROGRESSIVOOAF")
    private Integer progressivo;

    @Override
    public String toString() {
        return anno + "_" + serie + "_" + progressivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FornitoreId ordineId = (FornitoreId) o;
        return Objects.equals(anno, ordineId.anno) && Objects.equals(serie, ordineId.serie) && Objects.equals(progressivo, ordineId.progressivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anno, serie, progressivo);
    }
}
