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
    private Integer progrGenerale;

    @Column
    @Id
    private Integer rigo;

    @Column
    private Integer pid;

    @Column(length = 5000, name = "NOTEORDFOR2", columnDefinition = "text")
    private String nota;

    @Column(length = 13)
    private String oArticolo;

    @Column(length = 50)
    private String oDescrArticolo;

    @Column(length = 2, name = "TIPORIGOOAF")
    private String tipoRigo;

    @Column
    private Double oQuantita;

    @Column(name = "OQUANTITAV")
    private Double oQuantitaV;

    @Column
    private Double oPrezzo;

    @Column(name = "FSCONTOARTICOLO")
    private Double fScontoArticolo;

    @Column
    private Double scontoF1;

    @Column
    private Double scontoF2;

    @Column
    private Double fScontoP;

    @Column(length = 2)
    private String oUnitaMisura;

    @Column
    private Integer oColli;

    @Column(length = 3)
    private String oCodiceIva;

    @Column(length = 25, name = "CAMPOUSER5")
    private String campoUser5;

    @Column(length = 1)
    private String provenienza;

}
