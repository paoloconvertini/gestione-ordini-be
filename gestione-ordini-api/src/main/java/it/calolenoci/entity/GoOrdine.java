package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
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

    public static List<GoOrdine> findOrdiniByStatus(List<String> param) {
        return list("status in (:param)", Parameters.with("param", param));
    }

    public static GoOrdine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }

}
