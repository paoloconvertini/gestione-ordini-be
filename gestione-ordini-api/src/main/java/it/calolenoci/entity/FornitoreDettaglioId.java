package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class FornitoreDettaglioId implements Serializable {

    @Column(length = 4, name = "ANNOOAF")
    private  Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    private String serie;

    @Column(name="PROGRESSIVOOAF")
    private Integer progressivo;

    @Column
    private Integer rigo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FornitoreDettaglioId that = (FornitoreDettaglioId) o;
        return Objects.equals(anno, that.anno) && Objects.equals(serie, that.serie) && Objects.equals(progressivo, that.progressivo) && Objects.equals(rigo, that.rigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anno, serie, progressivo, rigo);
    }
}
