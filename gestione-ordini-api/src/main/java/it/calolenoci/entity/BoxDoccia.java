package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "GO_BOX_DOCCIA")
public class BoxDoccia extends PanacheEntityBase {

    @Column(length = 36)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
    private String id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "profilo")
    private String profilo;

    @Column(name = "estensibilita")
    private String estensibilita;

    @Column(name = "versione")
    private String versione;

    @Column(name = "qta")
    private Integer qta;

    @Column(name = "prezzo")
    private Double prezzo;

    @Column(name = "extra")
    private String extra;

    @Column(name = "foto")
    private String foto;

    @Column(name = "posa")
    private String posa;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "venduto", columnDefinition = "CHAR(1)")
    private String venduto;

}
