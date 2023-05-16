package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "ORDCLI")
@IdClass(OrdineId.class)
@Getter
@Setter
public class Ordine extends PanacheEntityBase {

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
    private Integer gruppoCliente;

    @Column(length = 6)
    private String contoCliente;

    @Column
    private Integer gruppoFattura;

    @Column(length = 6)
    private String contoFattura;

    @Column(length = 1)
    private String tipoFattura;

    @Column(length = 1)
    private String provvisorio;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataOrdine;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRichiesta;

    @Column(length = 15)
    private String numeroConferma;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConferma;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfermaCli;

    @Column(length = 30, name = "GE_STATUS")
    private String geStatus;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "GE_WARN_NO_BOLLA", columnDefinition = "CHAR(1)")
    private Boolean geWarnNoBolla;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "GE_LOCKED", columnDefinition = "CHAR(1)")
    private Boolean geLocked;

    @Column(length = 100, name= "GE_USER_LOCK")
    private String geUserLock;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(length = 1, name = "HAS_FIRMA", columnDefinition = "CHAR(1)")
    private Boolean hasFirma;

    @Column(length = 2000)
    private String note;

    public static List<Ordine> findOrdiniByStatus(List<String> param) {
        return list("geStatus in (:param)", Parameters.with("param", param));
    }

    public static Ordine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }



}
