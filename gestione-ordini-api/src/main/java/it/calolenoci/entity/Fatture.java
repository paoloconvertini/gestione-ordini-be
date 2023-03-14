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
@Table(name = "FATTURE")
@IdClass(FattureId.class)
@Getter
@Setter
public class Fatture extends PanacheEntityBase {

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
    private Date dataBolla;

    @Column(length = 7)
    private String numeroBolla;
}
