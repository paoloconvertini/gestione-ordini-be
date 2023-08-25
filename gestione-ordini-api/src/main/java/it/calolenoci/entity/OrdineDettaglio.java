package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.FiltroArticoli;
import it.calolenoci.dto.OrdineDettaglioDto;
import lombok.Getter;
import lombok.Setter;

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
    private Double quantita2;

    @Column
    private Double prezzo;

    @Column(name = "FCOLLI")
    private Integer fColli;

    @Column(name = "LOTTOMAGF", length = 10)
    private String tono;

    @Column(length = 2, name = "FUNITAMISURA")
    private String fUnitaMisura;

    @Column(name = "SCONTOARTICOLO")
    private Double scontoArticolo;

    @Column
    private Double scontoC1;

    @Column
    private Double scontoC2;

    @Column
    private Double scontoP;

    @Column(length = 3)
    private String fCodiceIva;

    @Column(length = 5000, name = "NOTEORDCLI2", columnDefinition = "text")
    private String noteOrdCli;

    @Column(name = "VARIANTE1")
    private String variante1;

    @Column(name = "VARIANTE2")
    private String variante2;

    @Column(name = "VARIANTE3")
    private String variante3;

    @Column(name = "VARIANTE4")
    private String variante4;

    @Column(name = "VARIANTE5")
    private String variante5;

    @Column(name = "CODICEEAN")
    private String codiceean;

    @Column(name = "QTAOMAGGIO")
    private Double qtaomaggio;

    @Column(name = "FCOEFFICIENTE")
    private Double fcoefficiente;

    @Column(name = "PREZZOEXTRA")
    private Double prezzoextra;

    @Column(name = "MAGAZZ")
    private String magazz;

    @Column(name = "IMPPROVVFISSO")
    private Double impprovvfisso;

    @Column(name = "FPROVVARTICOLO")
    private Double fprovvarticolo;

    @Column(name = "FPROVVCLIENTE")
    private Double fprovvcliente;

    @Column(name = "FPALLET")
    private Double fpallet;

    @Column(name = "COEFPREZZO")
    private Double coefprezzo;

    @Column(name = "FCENTROCOSTOR")
    private String fcentrocostor;

    @Column(name = "FCOMMESSA")
    private String fcommessa;

    @Column(name = "FGRUPPORICAVO")
    private Integer fgrupporicavo;

    @Column(name = "FCONTORICAVO")
    private String fcontoricavo;

    @Column(name = "FPROVENIENZA")
    private String fprovenienza;

    @Column(name = "FPID")
    private Integer fpid;

    @Column(name = "QTYUSER1")
    private Double qtyuser1;

    @Column(name = "QTYUSER2")
    private Double qtyuser2;

    @Column(name = "QTYUSER3")
    private Double qtyuser3;

    @Column(name = "QTYUSER4")
    private Double qtyuser4;

    @Column(name = "QTYUSER5")
    private Double qtyuser5;

    @Column(name = "QTYUSER6")
    private Double qtyuser6;

    @Column(name = "QTYUSER7")
    private Double qtyuser7;

    @Column(name = "DESCRUSER1")
    private String descruser1;

    @Column(name = "DESCRUSER2")
    private String descruser2;

    @Column(name = "DESCRUSER3")
    private String descruser3;

    @Column(name = "DESCRUSER4")
    private String descruser4;

    @Column(name = "DESCRUSER5")
    private String descruser5;

    @Column(name = "DESCRUSER6")
    private String descruser6;

    @Column(name = "DESCRUSER7")
    private String descruser7;

    @Column(name = "DATAUSER1")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser1;

    @Column(name = "DATAUSER2")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser2;

    @Column(name = "DATAUSER3")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser3;

    @Column(name = "DATAUSER4")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser4;

    @Column(name = "DATAUSER5")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser5;

    @Column(name = "DATAUSER6")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser6;

    @Column(name = "DATAUSER7")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser7;

    @Column(name = "CONTROMARCA")
    private String contromarca;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "DATAMODIFICA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datamodifica;

    @Column(name = "SYS_CREATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sysCreatedate;

    @Column(name = "SYS_CREATEUSER")
    private String sysCreateuser;

    @Column(name = "SYS_UPDATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sysUpdatedate;

    @Column(name = "SYS_UPDATEUSER")
    private String sysUpdateuser;

    public static OrdineDettaglio getById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return find("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo = :rigo",
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).firstResult();
    }

    public static List<OrdineDettaglioDto> findArticoliById(FiltroArticoli filtro) {
        String query = "SELECT o.anno,  o.progressivo, o.progrGenerale,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo, o.prezzo*(1-o.scontoArticolo/100)*(1-o.scontoC1/100)*(1-o.scontoC2/100)*(1-o.scontoP/100), " +
                "  o.fUnitaMisura,  " +
                "god.flagRiservato, god.flagNonDisponibile, god.flagOrdinato, god.flagConsegnato,  o.tono, a.fornitoreArticoloId.articolo, " +
                "f.anno as annoOAF, f.serie as serieOAF, f.progressivo as progressivoOAF, f.dataOrdine as dataOrdineOAF,  " +
                "god.qtaConsegnatoSenzaBolla, (CASE WHEN god.qtaDaConsegnare IS NULL THEN o.quantita ELSE god.qtaDaConsegnare END) as qtaDaConsegnare, god.flBolla, " +
                "god.note, god.qtaRiservata, god.flProntoConsegna, god.qtaProntoConsegna, o.noteOrdCli  " +
                "FROM OrdineDettaglio o " +
                "LEFT JOIN GoOrdineDettaglio god ON o.progrGenerale = god.progrGenerale " +
                "LEFT JOIN OrdineFornitoreDettaglio f2 ON f2.pid = o.progrGenerale " +
                "LEFT JOIN FornitoreArticolo a ON a.fornitoreArticoloId.articolo = o.fArticolo " +
                "LEFT JOIN OrdineFornitore f ON f.anno = f2.anno AND f.serie = f2.serie AND f.progressivo = f2.progressivo " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo";
        if (filtro.getFlNonDisponibile() != null && filtro.getFlNonDisponibile()) {
            query += " AND god.flagNonDisponibile = 'T' ";
        }
        if(filtro.getFlConsegna() != null) {
            switch (filtro.getFlConsegna()) {
                case 0 ->
                        query += " AND (god.flagConsegnato = 'F' OR god.flagConsegnato IS NULL OR god.flagConsegnato = '')";
                case 1 -> query += " AND god.flagConsegnato = 'T' ";
                case 2 -> query += " AND god.flProntoConsegna = 'T' ";
                default -> {
                }
            }
        }
        if (filtro.getFlDaRiservare() != null && filtro.getFlDaRiservare()) {
                query += " AND (god.flagRiservato = 'F' OR god.flagRiservato IS NULL OR god.flagRiservato = '')";
        }
        Log.info("Query articoli: " + query);
        return find(query, Sort.ascending("o.rigo"), Parameters.with("anno", filtro.getAnno()).and("serie", filtro.getSerie())
                .and("progressivo", filtro.getProgressivo())).project(OrdineDettaglioDto.class).list();

    }

    public static List<OrdineDettaglioDto> findArticoliForReport(Integer anno, String serie, Integer progressivo) {
        String query = "SELECT o.anno,  o.progressivo, o.progrGenerale,  o.tipoRigo,  o.rigo,  o.serie,  o.fArticolo,  " +
                "o.codArtFornitore,  o.fDescrArticolo,  o.quantita,  o.prezzo,  o.fUnitaMisura, o.fColli, o.scontoArticolo," +
                "o.scontoC1, o.scontoC2, o.scontoP, o.fCodiceIva " +
                "FROM OrdineDettaglio o " +
                "WHERE o.anno = :anno AND o.serie = :serie AND o.progressivo = :progressivo";
        return find(query, Sort.ascending("o.rigo"), Parameters.with("anno", anno).and("serie", serie)
                .and("progressivo", progressivo)).project(OrdineDettaglioDto.class).list();

    }

}
