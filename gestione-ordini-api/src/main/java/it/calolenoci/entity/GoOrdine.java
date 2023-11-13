package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.GoOrdineDto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "WARN_NO_BOLLA", columnDefinition = "CHAR(1)")
    private Boolean warnNoBolla;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "LOCKED", columnDefinition = "CHAR(1)")
    private Boolean locked;

    @Column(length = 100, name= "USER_LOCK")
    private String userLock;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_FIRMA", columnDefinition = "CHAR(1)")
    private Boolean hasFirma;


    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_PRONTO_CONSEGNA", columnDefinition = "CHAR(1)")
    private Boolean hasProntoConsegna;

    @Column(length = 2000, name = "NOTE")
    private String note;

    @Column(length = 2000, name = "NOTELOGISTICA")
    private String noteLogistica;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_CARICO", columnDefinition = "CHAR(1)")
    private Boolean hasCarico;

    public static List<GoOrdine> findOrdiniByStatus(List<String> param) {
        return list("status in (:param)", Parameters.with("param", param));
    }

    public static List<GoOrdine> findOrdiniWithNewItems(List<String> param) {
        return list("SELECT distinct o FROM GoOrdine o " +
                "JOIN OrdineDettaglio o2 ON o2.anno = o.anno " +
                "and o2.serie = o.serie AND o2.progressivo = o.progressivo " +
                "WHERE NOT EXISTS (SELECT 1 FROM GoOrdineDettaglio god WHERE o2.progrGenerale = god.progrGenerale) " +
                "and o.status in (:param) and o2.tipoRigo = ' '", Parameters.with("param", param));
    }

    public static List<GoOrdineDto> findOrdiniNoProntaConsegnaByStatus(List<String> param) {
        return find("select go, god.flProntoConsegna " +
                "from GoOrdine go " +
                "join GoOrdineDettaglio god on go.anno = god.anno AND  go.progressivo = god.progressivo AND go.serie = god.serie " +
                "where exists (SELECT 1 FROM OrdineDettaglio o WHERE o.progrGenerale = god.progrGenerale and o.tipoRigo = ' ') AND go.status IN (:param) " +
                "and go.hasProntoConsegna = true", Parameters.with("param", param)).project(GoOrdineDto.class).list();
    }

    public static List<GoOrdineDto> findOrdiniConsegnatiByStatus(List<String> param) {
        return find("select go, o.saldoAcconto " +
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
