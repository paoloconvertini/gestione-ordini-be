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
public class FornitoreArticoloId implements Serializable {

    @Column(length = 13)
    private String articolo;

    @Column(name = "GRUPPOF")
    private Integer gruppo;

    @Column(length = 6, name = "CONTOF")
    private String conto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FornitoreArticoloId that = (FornitoreArticoloId) o;
        return Objects.equals(articolo, that.articolo) && Objects.equals(gruppo, that.gruppo) && Objects.equals(conto, that.conto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articolo, gruppo, conto);
    }
}
