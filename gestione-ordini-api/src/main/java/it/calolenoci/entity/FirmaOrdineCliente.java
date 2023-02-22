package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "FIRMAORDINECLIENTE")
@IdClass(OrdineId.class)
@Getter
@Setter
public class FirmaOrdineCliente extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Column(length = 100)
    private String fileName;

    public static FirmaOrdineCliente findById(Integer anno, String serie, Integer progressivo) {
       return find("anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }

}
