package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.enums.StatoOrdineEnum;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


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
    private Integer progrGenerale;

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

    @Column(length = 1, name = "GE_FLAG_RISERVATO", columnDefinition = "CHAR(1)")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean geFlagRiservato;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "GE_FLAG_NON_DISPONIBILE", columnDefinition = "CHAR(1)")
    private Boolean geFlagNonDisponibile;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "GE_FLAG_ORDINATO", columnDefinition = "CHAR(1)")
    private Boolean geFlagOrdinato;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "GE_FLAG_CONSEGNATO", columnDefinition = "CHAR(1)")
    private Boolean geFlagConsegnato;

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

    public static List<OrdineDettaglioDto> findOnlyArticoliById(Integer anno, String serie, Integer progressivo, Boolean filtro){
        String query = "SELECT anno,  progressivo,  tipoRigo,  rigo,  serie,  fArticolo,  " +
                "codArtFornitore,  fDescrArticolo,  quantita,  prezzo,  fUnitaMisura,  " +
                "scontoArticolo,  scontoC1,  scontoC2,  scontoP,  fCodiceIva,  fColli, " +
                "geFlagRiservato, geFlagNonDisponibile, geFlagOrdinato, geFlagConsegnato,  geTono " +
                "FROM OrdineDettaglio o " +
                "WHERE anno = :anno AND serie = :serie AND progressivo = :progressivo";
        if(filtro) {
            query += " AND geFlagNonDisponibile = 'T'";
        }
        return find(query, Sort.ascending("rigo"), Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();

    }

    public static List<OrdineDettaglioDto> findArticoliOrdinatiById(Integer anno, String serie, Integer progressivo){
        String query = "SELECT  o.anno,  o.progressivo,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo,  o.fUnitaMisura,  " +
                "o.scontoArticolo,  o.scontoC1,  o.scontoC2,  o.scontoP,  o.fCodiceIva,  o.fColli, " +
                "o.geFlagRiservato,  o.geFlagNonDisponibile,  o.geFlagOrdinato,  o.geFlagConsegnato,  o.geTono, f.nota " +
                "FROM OrdineDettaglio o LEFT JOIN ORDFOR2 f ON f.pid = o.progrGenerale " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo ";
        return find(query, Sort.ascending("rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();

    }

    public static List<OrdineDettaglioDto> findArticoliConsegnatiById(Integer anno, String serie, Integer progressivo){
        String query = "SELECT  o.anno,  o.progressivo,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo,  o.fUnitaMisura,  " +
                "o.scontoArticolo,  o.scontoC1,  o.scontoC2,  o.scontoP,  o.fCodiceIva,  o.fColli, " +
                "o.geFlagRiservato,  o.geFlagNonDisponibile,  o.geFlagOrdinato,  o.geFlagConsegnato,  o.geTono, " +
                "f.numeroBolla, f.dataBolla " +
                "FROM OrdineDettaglio o LEFT JOIN FattureDettaglio f2 ON o.progrGenerale = f2.progrOrdCli " +
                "LEFT JOIN Fatture f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo ";
        return find(query, Sort.ascending("o.rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();

    }

}
