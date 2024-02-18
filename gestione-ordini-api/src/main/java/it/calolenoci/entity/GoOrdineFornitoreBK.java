package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "GO_ORDFOR")
@IdClass(FornitoreId.class)
@Getter
@Setter
public class GoOrdineFornitoreBK extends PanacheEntityBase {

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

    @Column(name = "OTIPOCOMPENSO", nullable = true, length = 3)
    private String otipocompenso;

    @Column(name = "TCOMMESSA", nullable = true, length = 10)
    private String tcommessa;

    @Column(name = "OIVAPRIMASCAD", nullable = true, length = 1)
    private String oivaprimascad;

    @Column(name = "IBAN", nullable = true, length = 50)
    private String iban;

    @Column(name = "DESCRBANCA", nullable = true, length = 50)
    private String descrbanca;

    @Column(name = "SWIFT", nullable = true, length = 11)
    private String swift;

    @Column(name = "OMODOCONSEGNA", nullable = true, length = 3)
    private String omodoconsegna;

    @Column(name = "TCODICEIVA", nullable = true, length = 3)
    private String tcodiceiva;

    @Column(name = "SCONTOFORNITOR1", nullable = true, precision = 0)
    private Double scontofornitor1;

    @Column(name = "SCONTOFORNITOR2", nullable = true, precision = 0)
    private Double scontofornitor2;

    @Column(name = "FSCONTOPAGAMENT", nullable = true, precision = 0)
    private Double fscontopagament;

    @Column(name = "TPROVVARTICOLO", nullable = true, precision = 0)
    private Double tprovvarticolo;

    @Column(name = "TPROVVFORNITORE", nullable = true, precision = 0)
    private Double tprovvfornitore;

    @Column(name = "OAGENTE", nullable = true, length = 3)
    private String oagente;

    @Column(name = "OLINGUA", nullable = true, length = 3)
    private String olingua;

    @Column(name = "OVALUTA", nullable = true, length = 3)
    private String ovaluta;

    @Column(name = "OCAMBIO", nullable = true, precision = 0)
    private Double ocambio;

    @Column(name = "OOGGETTO", nullable = true, length = 5)
    private String ooggetto;

    @Column(name = "CIG", nullable = true, length = 15)
    private String cig;

    @Column(name = "CUP", nullable = true, length = 15)
    private String cup;

    @Column(name = "VETTORE", nullable = true, length = 3)
    private String vettore;

    @Column(name = "TARGA", nullable = true, length = 20)
    private String targa;

    @Column(name = "TARGARIMORCHIO", nullable = true, length = 20)
    private String targarimorchio;

    @Column(name = "NOTEINTERNE", nullable = true, length = -1)
    private String noteinterne;

    @Column(name = "PROGRESSIVOGEN", nullable = true)
    private Integer progressivogen;

    @Column(name = "REFERENTE", nullable = true, length = 50)
    private String referente;

    @Column(name = "OGGETTO", nullable = true, length = -1)
    private String oggetto;

    @Column(name = "GRUPPOCARICO", nullable = true)
    private Integer gruppocarico;

    @Column(name = "CONTOCARICO", nullable = true, length = 6)
    private String contocarico;

    @Column(name = "PREFAZIONE", nullable = true, length = -1)
    private String prefazione;

    @Column(name = "PIEDEPAGINA", nullable = true, length = -1)
    private String piedepagina;

    @Column(name = "GRUPPOMEDIATORE", nullable = true)
    private Integer gruppomediatore;

    @Column(name = "CONTOMEDIATORE", nullable = true, length = 6)
    private String contomediatore;

    @Column(name = "FLAGTRASFERITO", nullable = true, length = 1)
    private String flagtrasferito;

    @Column(name = "DATAUSER1", nullable = true)
    private java.sql.Date datauser1;

    @Column(name = "DATAUSER2", nullable = true)
    private java.sql.Date datauser2;

    @Column(name = "VALOREUSER", nullable = true, precision = 0)
    private Double valoreuser;

    @Column(name = "FLPALMARI", nullable = true, length = 1)
    private String flpalmari;

    @Column(name = "UTENTEPALMARE", nullable = true, length = 20)
    private String utentepalmare;

}
