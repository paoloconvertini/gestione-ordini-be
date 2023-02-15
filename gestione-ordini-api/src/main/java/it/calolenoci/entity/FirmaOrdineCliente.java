package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "FIRMAORDINECLIENTE")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrdineId.class)
public class FirmaOrdineCliente extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    public Integer anno;

    @Column(length = 3)
    @Id
    public String serie;

    @Column
    @Id
    public Integer progressivo;

    @Column(length = 100)
    public String fileName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirmaOrdineCliente that = (FirmaOrdineCliente) o;
        return Objects.equals(anno, that.anno) && Objects.equals(serie, that.serie) && Objects.equals(progressivo, that.progressivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anno, serie, progressivo);
    }
}
