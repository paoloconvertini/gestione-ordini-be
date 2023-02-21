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
public class PianoContiId implements Serializable {

    @Column
    private Integer gruppoConto;

    @Column(length = 6)
    private String sottoConto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PianoContiId that = (PianoContiId) o;
        return Objects.equals(gruppoConto, that.gruppoConto) && Objects.equals(sottoConto, that.sottoConto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gruppoConto, sottoConto);
    }
}
