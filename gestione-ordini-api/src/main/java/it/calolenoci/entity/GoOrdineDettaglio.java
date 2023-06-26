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
import java.util.Optional;


@Entity
@Table(name = "GO_ORDINE_DETTAGLIO")
@Getter
@Setter
@IdClass(OrdineDettaglioId.class)
public class GoOrdineDettaglio extends PanacheEntityBase {

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
    @Id
    private Integer rigo;

    @Column(name = "QUANTITA_DA_CONSEGNARE")
    private Double qtaDaConsegnare;

    @Column(name = "QTA_CONS_NO_BOLLA")
    private Double qtaConsegnatoSenzaBolla;

    @Column(length = 1, name = "FLAG_RISERVATO", columnDefinition = "CHAR(1)")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean flagRiservato;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLAG_NON_DISPONIBILE", columnDefinition = "CHAR(1)")
    private Boolean flagNonDisponibile;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLAG_ORDINATO", columnDefinition = "CHAR(1)")
    private Boolean flagOrdinato;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLAG_CONSEGNATO", columnDefinition = "CHAR(1)")
    private Boolean flagConsegnato;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "FLAG_PRONTO_CONSEGNA", columnDefinition = "CHAR(1)")
    private Boolean flProntoConsegna;

    @Column(length = 20, name = "TONO")
    private String tono;

    @Column(length = 50, name = "STATUS")
    private String status;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_BOLLA", columnDefinition = "CHAR(1)")
    private Boolean flBolla;

    @Column(length = 2000, name = "NOTE")
    private String note;

    @Column(name = "QTA_RISERVATA")
    private Double qtaRiservata;

    @Column(name = "QTA_PRONTO_CONSEGNA")
    private Double qtaProntoConsegna;


    public static Optional<GoOrdineDettaglio> getById(Integer anno, String serie, Integer progressivo, Integer rigo) {
        return find("anno = :anno AND serie = :serie AND progressivo = :progressivo AND rigo = :rigo",
                Parameters.with("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo).and("rigo", rigo)).singleResultOptional();
    }

    public static void updateStatus(Integer anno, String serie, Integer progressivo, String stato) {
        update("status = :stato where anno = :anno AND serie = :serie AND progressivo = :progressivo",
                Parameters.with("stato", stato).and("anno", anno).and("serie", serie)
                        .and("progressivo", progressivo));
    }
}