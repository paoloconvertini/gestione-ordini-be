package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.enums.StatoOrdineEnum;
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

    @Column(length = 1, name = "GE_FLAG_RISERVATO")
    private Character geFlagRiservato;

    @Column(length = 1, name = "GE_FLAG_NON_DISPONIBILE")
    private Character geFlagNonDisponibile;

    @Column(length = 1, name = "GE_FLAG_ORDINATO")
    private Character geFlagOrdinato;

    @Column(length = 1, name = "GE_FLAG_CONSEGNATO")
    private Character geFlagConsegnato;

    @Column(length = 20, name="GE_TONO")
    private String geTono;

    @Column(length = 50, name = "GE_STATUS")
    private String geStatus;

    public static void updateStatus(Integer anno, String serie, Integer progressivo, String stato){
        update("geStatus = :stato where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("stato", stato).and("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo));
    }

    public static OrdineDettaglio getById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return find("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo = :rigo",
        Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo).and("rigo", rigo)).firstResult();
    }

}
