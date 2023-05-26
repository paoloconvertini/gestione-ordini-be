package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.FiltroArticoli;
import it.calolenoci.dto.OrdineDettaglioDto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.jfree.util.Log;

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
    private Integer anno;

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
    private Double quantita;

    @Column(name = "QUANTITA_DA_CONSEGNARE")
    private Double qtaDaConsegnare;

    @Column(name = "QTA_CONS_NO_BOLLA")
    private Double qtaConsegnatoSenzaBolla;

    @Column(name = "QUANTITAV")
    private Double quantitaV;

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

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLAG_PRONTO_CONSEGNA", columnDefinition = "CHAR(1)")
    private Boolean flProntoConsegna;

    @Column(length = 20, name = "GE_TONO")
    private String geTono;

    @Column(length = 50, name = "GE_STATUS")
    private String geStatus;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_BOLLA", columnDefinition = "CHAR(1)")
    private Boolean flBolla;

    @Column(length = 2000)
    private String note;

    @Column(name = "QTA_RISERVATA")
    private Double qtaRiservata;

    @Column(name = "QTA_PRONTO_CONSEGNA")
    private Double qtaProntoConsegna;

    public static void updateStatus(Integer anno, String serie, Integer progressivo, String stato) {
        update("geStatus = :stato where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("stato", stato).and("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo));
    }

    public static OrdineDettaglio getById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return find("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo = :rigo",
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).firstResult();
    }

    public static List<OrdineDettaglioDto> findArticoliById(FiltroArticoli filtro) {
        String query = "SELECT o.anno,  o.progressivo, o.progrGenerale,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo,  o.fUnitaMisura,  " +
                "o.geFlagRiservato, o.geFlagNonDisponibile, o.geFlagOrdinato, o.geFlagConsegnato,  o.geTono, a.fornitoreArticoloId.articolo, " +
                "f.anno as annoOAF, f.serie as serieOAF, f.progressivo as progressivoOAF, f.dataOrdine as dataOrdineOAF,  " +
                "o.qtaConsegnatoSenzaBolla, (CASE WHEN o.qtaDaConsegnare IS NULL THEN o.quantita ELSE o.qtaDaConsegnare END) as qtaDaConsegnare, o.flBolla, pc.intestazione, " +
                "o.note, o.qtaRiservata, o.flProntoConsegna, o.qtaProntoConsegna  " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN OrdineFornitoreDettaglio f2 ON " +
                "f2.nota like CONCAT('Riferimento n. ', trim(str(o.anno)), '/', o.serie, '/', trim(str(o.progressivo)), '-', trim(str(o.rigo))) " +
                "LEFT JOIN FornitoreArticolo a ON a.fornitoreArticoloId.articolo = o.fArticolo " +
                "LEFT JOIN OrdineFornitore f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "LEFT JOIN PianoConti pc ON f.gruppo = pc.gruppoConto AND f.conto = pc.sottoConto  " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo";
        if (filtro.getFlNonDisponibile() != null && filtro.getFlNonDisponibile()) {
            query += " AND o.geFlagNonDisponibile = 'T' ";
        }
        switch (filtro.getFlConsegna()) {
            case 0 ->
                    query += " AND (o.geFlagConsegnato = 'F' OR o.geFlagConsegnato IS NULL OR o.geFlagConsegnato = '')";
            case 1 -> query += " AND o.geFlagConsegnato = 'T' ";
            case 2 -> query += " AND o.flProntoConsegna = 'T' ";
            default -> {
            }
        }
        if (filtro.getFlDaRiservare() != null && filtro.getFlDaRiservare()) {
                query += " AND (o.geFlagRiservato = 'F' OR o.geFlagRiservato IS NULL OR o.geFlagRiservato = '')";
        }
        Log.info("Query articoli: " + query);
        return find(query, Sort.ascending("o.rigo"), Parameters.with("anno", filtro.getAnno()).and("serie", filtro.getSerie())
                .and("progressivo", filtro.getProgressivo())).project(OrdineDettaglioDto.class).list();

    }

}
