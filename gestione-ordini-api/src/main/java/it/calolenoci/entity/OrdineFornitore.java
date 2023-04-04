package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ORDFOR")
@IdClass(FornitoreId.class)
@Getter
@Setter
public class OrdineFornitore extends PanacheEntityBase {

    @Column(length = 4, name = "ANNOOAF")
    @Id
    private  Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    @Id
    private String serie;

    @Column(name="PROGRESSIVOOAF")
    @Id
    private Integer progressivo;

    @Column(name = "GRUPPOFOAF")
    private Integer gruppo;

    @Column(name = "CONTOFOAF", length = 6)
    private String conto;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATAORDINE")
    private Date dataOrdine;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfOrdine;

    @Column(length = 7)
    private String numConfOrdine;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRevisione;

    @Column
    private Integer numRevisione;

    @Column(name = "OCODICEPAGAMENT", length = 3)
    private String codicePagamento;

    @Column(name = "OBANCAPAGAMENTO", length = 3)
    private String bancaPagamento;

    @Column(length = 3)
    private String magazzino;

    @Column(length = 1)
    private String provvisorio;

    @Column(length = 6)
    private String refInterno;

    @Column(length = 20)
    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataModifica;

    @Column(length = 20, name = "SYS_CREATEUSER")
    private String createUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SYS_CREATEDATE")
    private Date createDate;


    @Column(length = 20, name = "SYS_UPDATEUSER")
    private String updateUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SYS_UPDATEDATE")
    private Date updateDate;

}
