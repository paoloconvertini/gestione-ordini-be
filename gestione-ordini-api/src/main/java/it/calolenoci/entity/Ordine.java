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

    @Column(length = 80)
    private String riferimento;

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
    public static Ordine findByOrdineId(Integer anno, String serie,  Integer progressivo) {
        return find("anno = :anno and progressivo = :progressivo and serie = :serie",
                Parameters.with("anno", anno).and("serie", serie).and("progressivo", progressivo)).firstResult();
    }



}
