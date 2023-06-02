package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "FATTURE2")
@Getter
@Setter
@IdClass(FattureDettaglioId.class)
public class FattureDettaglio extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Column
    @Id
    private Integer rigo;

    @Column(length = 2)
    private String tipoRigo;

    @Column(length = 13)
    private String fArticolo;

    @Column
    private Integer progrOrdCli;

    @Column(length = 50)
    private String fDescrArticolo;

    @Column
    private Double quantita;

    @Column
    private Double prezzo;

}
