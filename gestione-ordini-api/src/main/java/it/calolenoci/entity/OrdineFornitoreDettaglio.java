package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "ORDFOR2")
@Getter
@Setter
@IdClass(FornitoreDettaglioId.class)
public class OrdineFornitoreDettaglio extends PanacheEntityBase {

    @Column(length = 4, name = "ANNOOAF")
    @Id
    private Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    @Id
    private String serie;

    @Column(name = "PROGRESSIVOOAF")
    @Id
    private Integer progressivo;

    @Column
    @Id
    private Integer rigo;

    @Column
    private Integer pid;

    @Column(length = 5000, name = "NOTEORDFOR2", columnDefinition = "text")
    private String nota;

}
