package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "ORDCLI2")
@Getter
@Setter
@IdClass(OrdineDettaglioId.class)
public class OrdineDettaglio extends PanacheEntityBase {

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

    @Column(length = 25)
    private String codArtFornitore;

    @Column(length = 50)
    private String fDescrArticolo;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfConsegna;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRichConsegna;

    @Column
    private Float quantita;

    @Column
    private Float quantitaV;

    @Column
    private Float quantita2;

    @Column
    private Float prezzo;

    @Column
    private Integer fColli;

    @Column(length = 2)
    private String fUnitaMisura;

    @Column
    private Float scontoArticolo;

    @Column
    private Float scontoC1;

    @Column
    private Float scontoC2;

    @Column
    private Float scontoP;

    @Column(length = 3)
    private String fCodiceIva;

    @Column(name="GE_UPDATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date geUpdateDate;

    @Column(length = 500, name = "GE_UPDATEUSER")
    private String geUpdateUser;

    @Column(length = 1, name = "GE_FLAG_RISERVATO")
    private Character geFlagRiservato;

    @Column(length = 1, name = "GE_FLAG_NON_DISPONIBILE")
    private Character geFlagNonDisponibile;

    @Column(length = 1, name = "GE_FLAG_ORDINATO")
    private Character geFlagOrdinato;

    @Column(length = 20, name="GE_TONO")
    private String geTono;

}
