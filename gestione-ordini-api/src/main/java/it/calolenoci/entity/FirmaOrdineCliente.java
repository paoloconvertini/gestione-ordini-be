package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "FIRMAORDINECLIENTE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FirmaOrdineCliente implements Serializable {

    @EmbeddedId
    OrdineId ordineId;

    @Column(length = 100)
    public String fileName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirmaOrdineCliente that = (FirmaOrdineCliente) o;
        return Objects.equals(ordineId, that.ordineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordineId);
    }
}
