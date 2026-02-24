package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.converter.TrueFalseConverter;
import it.calolenoci.dto.GoOrdineDto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "GO_ORDINE")
@IdClass(OrdineId.class)
@Getter
@Setter
public class GoOrdine extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;


    @Column(length = 30, name = "STATUS")
    private String status;

    @Convert(converter = TrueFalseConverter.class)
    @Column(length = 1, name = "WARN_NO_BOLLA", columnDefinition = "CHAR(1)")
    private Boolean warnNoBolla;

    @Convert(converter = TrueFalseConverter.class)
    @Column(length = 1, name = "LOCKED", columnDefinition = "CHAR(1)")
    private Boolean locked;

    @Column(length = 100, name= "USER_LOCK")
    private String userLock;

    @Convert(converter = TrueFalseConverter.class)
    @Column(length = 1, name = "HAS_FIRMA", columnDefinition = "CHAR(1)")
    private Boolean hasFirma;


    @Convert(converter = TrueFalseConverter.class)
    @Column(length = 1, name = "HAS_PRONTO_CONSEGNA", columnDefinition = "CHAR(1)")
    private Boolean hasProntoConsegna;

    @Column(length = 2000, name = "NOTE")
    private String note;

    @Column(length = 2000, name = "NOTELOGISTICA")
    private String noteLogistica;

    @Convert(converter = TrueFalseConverter.class)
    @Column(length = 1, name = "HAS_CARICO", columnDefinition = "CHAR(1)")
    private Boolean hasCarico;

    @Column(name = "DATA_NOTE")
    private LocalDateTime dataNote;

    @Column(name = "USER_NOTE")
    private String userNote;

    @Column(name = "DATA_NOTE_LOGISTICA")
    private LocalDateTime dataNoteLogistica;

    @Column(name = "USER_NOTE_LOGISTICA")
    private String userNoteLogistica;

    public static List<GoOrdineDto> findOrdiniNoProntaConsegnaByStatus(List<String> param) {
        return find("select go.anno, go.serie, go.progressivo, go.status, go.warnNoBolla, go.locked, " +
                "go.userLock, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica, go.hasCarico, go.dataNote, " +
                "go.userNote, go.dataNoteLogistica, go.userNoteLogistica, god.flProntoConsegna " +
                "from GoOrdine go " +
                "join GoOrdineDettaglio god on go.anno = god.anno AND  go.progressivo = god.progressivo AND go.serie = god.serie " +
                "where exists (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = god.progrGenerale and o.tipoRigo = ' ') AND go.status IN (:param) " +
                "and go.hasProntoConsegna = true", Parameters.with("param", param)).project(GoOrdineDto.class).list();
    }

    public static List<GoOrdineDto> findOrdiniConsegnatiByStatus(List<String> param) {
        return find("select go.anno, go.serie, go.progressivo, go.status, go.warnNoBolla, go.locked, " +
                "go.userLock, go.hasFirma, go.hasProntoConsegna, go.note, go.noteLogistica, go.hasCarico, go.dataNote, " +
                "go.userNote, go.dataNoteLogistica, go.userNoteLogistica, o.saldoAcconto " +
                "from GoOrdine go " +
                "join OrdineDettaglio o on go.anno = o.anno AND  go.progressivo = o.progressivo AND go.serie = o.serie " +
                "where go.status IN (:param)" +
                "and o.tipoRigo = ' '", Parameters.with("param", param)).project(GoOrdineDto.class).list();
    }

    public static GoOrdine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }

}
