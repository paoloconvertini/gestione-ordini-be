package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ORDFOR")
@IdClass(FornitoreId.class)
@Getter
@Setter
public class OrdineFornitore extends PanacheEntityBase {

    @Column(length = 4, name = "ANNOOAF")
    @Id
    private  Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    @Id
    private String serie;

    @Column(name="PROGRESSIVOOAF")
    @Id
    private Integer progressivo;

}
