package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

    @Column(length = 30)
    private String status;

    public static List<Ordine> findOrdiniByStatus(String status) {
        if(StringUtils.isBlank(status)) {
            return list("status is null", Sort.descending("dataOrdine"));
        } else {
            return list("status", Sort.descending("dataOrdine"), status);
        }
    }

    public static Ordine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }

}
