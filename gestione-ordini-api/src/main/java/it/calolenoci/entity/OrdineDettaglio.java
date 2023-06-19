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

    @Column(length = 5000, name = "NOTEORDCLI2", columnDefinition = "text")
    private String noteOrdCli;

    public static OrdineDettaglio getById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return find("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo = :rigo",
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).firstResult();
    }

    public static List<OrdineDettaglioDto> findArticoliById(FiltroArticoli filtro) {
        String query = "SELECT o.anno,  o.progressivo, o.progrGenerale,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo,  o.fUnitaMisura,  " +
                "god.flagRiservato, god.flagNonDisponibile, god.flagOrdinato, god.flagConsegnato,  god.tono, a.fornitoreArticoloId.articolo, " +
                "f.anno as annoOAF, f.serie as serieOAF, f.progressivo as progressivoOAF, f.dataOrdine as dataOrdineOAF,  " +
                "god.qtaConsegnatoSenzaBolla, (CASE WHEN god.qtaDaConsegnare IS NULL THEN o.quantita ELSE god.qtaDaConsegnare END) as qtaDaConsegnare, god.flBolla, pc.intestazione, " +
                "god.note, god.qtaRiservata, god.flProntoConsegna, god.qtaProntoConsegna, o.noteOrdCli  " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN GoOrdineDettaglio god ON o.anno = god.anno AND o.serie = god.serie AND o.progressivo = god.progressivo AND o.rigo = god.rigo " +
                "LEFT JOIN OrdineFornitoreDettaglio f2 ON f2.nota like CONCAT('Riferimento n. ', trim(str(o.anno)), '/', o.serie, '/', trim(str(o.progressivo)), '-', trim(str(o.rigo))) " +
                "LEFT JOIN FornitoreArticolo a ON a.fornitoreArticoloId.articolo = o.fArticolo " +
                "LEFT JOIN OrdineFornitore f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "LEFT JOIN PianoConti pc ON f.gruppo = pc.gruppoConto AND f.conto = pc.sottoConto  " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo";
        if (filtro.getFlNonDisponibile() != null && filtro.getFlNonDisponibile()) {
            query += " AND god.flagNonDisponibile = 'T' ";
        }
        switch (filtro.getFlConsegna()) {
            case 0 ->
                    query += " AND (god.flagConsegnato = 'F' OR god.flagConsegnato IS NULL OR god.flagConsegnato = '')";
            case 1 -> query += " AND god.flagConsegnato = 'T' ";
            case 2 -> query += " AND god.flProntoConsegna = 'T' ";
            default -> {
            }
        }
        if (filtro.getFlDaRiservare() != null && filtro.getFlDaRiservare()) {
                query += " AND (god.flagRiservato = 'F' OR god.flagRiservato IS NULL OR god.flagRiservato = '')";
        }
        Log.info("Query articoli: " + query);
        return find(query, Sort.ascending("o.rigo"), Parameters.with("anno", filtro.getAnno()).and("serie", filtro.getSerie())
                .and("progressivo", filtro.getProgressivo())).project(OrdineDettaglioDto.class).list();

    }

}
