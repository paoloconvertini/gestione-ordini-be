package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "FATTURE2")
@Getter
@Setter
@IdClass(FattureDettaglioId.class)
public class FattureDettaglio extends PanacheEntityBase {

    @Column(length = 4)
    @Id
    private  Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Column(name = "PROGRGENERALE")
    private Integer progrGenerale;

    @Column
    @Id
    private Integer rigo;

    @Column(length = 2)
    private String tipoRigo;

    @Column(length = 13)
    private String fArticolo;

    @Column
    private Integer progrOrdCli;

    @Column(length = 50)
    private String fDescrArticolo;

    @Column
    private Double quantita;

    @Column
    private Double prezzo;

    @Column(name = "FCODICEIVA", length = 3)
    private String iva;

    @Column(name = "VARIANTE1")
    private String variante1;

    @Column(name = "VARIANTE2")
    private String variante2;

    @Column(name = "VARIANTE3")
    private String variante3;

    @Column(name = "VARIANTE4")
    private String variante4;

    @Column(name = "VARIANTE5")
    private String variante5;

    @Column(name = "CODICEEAN")
    private String codiceean;


    @Column(name = "QUANTITA2")
    private Double quantita2;

    @Column(name = "QTAOMAGGIO")
    private Double qtaomaggio;

    @Column(name = "QTAINEVASA")
    private Double qtainevasa;

    @Column(name = "CAUSALEINEVASO")
    private String causaleinevaso;

    @Column(name = "FUNITAMISURA")
    private String funitamisura;

    @Column(name = "FCOEFFICIENTE")
    private Double fcoefficiente;

    @Column(name = "SCONTOARTICOLO")
    private Double scontoarticolo;

    @Column(name = "SCONTOC1")
    private Double scontoc1;

    @Column(name = "SCONTOC2")
    private Double scontoc2;

    @Column(name = "SCONTOP")
    private Double scontop;

    @Column(name = "PREZZOEXTRA")
    private Double prezzoextra;

    @Column(name = "MAGAZZ")
    private String magazz;

    @Column(name = "LOTTOMAGF")
    private String lottomagf;

    @Column(name = "IMPPROVVFISSO")
    private Double impprovvfisso;

    @Column(name = "FPROVVARTICOLO")
    private Double fprovvarticolo;

    @Column(name = "FPROVVCLIENTE")
    private Double fprovvcliente;

    @Column(name = "FCOLLI")
    private Integer fcolli;

    @Column(name = "FPALLET")
    private Double fpallet;

    @Column(name = "COEFPREZZO")
    private Double coefprezzo;

    @Column(name = "FCENTROCOSTOR")
    private String fcentrocostor;

    @Column(name = "FVOCESPESA")
    private String fvocespesa;

    @Column(name = "FCOMMESSA")
    private String fcommessa;

    @Column(name = "FCOMPETENZA")
    private Integer fcompetenza;

    @Column(name = "FGRUPPORICAVO")
    private Integer fgrupporicavo;

    @Column(name = "FCONTORICAVO")
    private String fcontoricavo;

    @Column(name = "FPROVENIENZA")
    private String fprovenienza;

    @Column(name = "FPID")
    private Integer fpid;

    @Column(name = "QTYUSER1")
    private Double qtyuser1;

    @Column(name = "QTYUSER2")
    private Double qtyuser2;

    @Column(name = "QTYUSER3")
    private Double qtyuser3;

    @Column(name = "QTYUSER4")
    private Double qtyuser4;

    @Column(name = "QTYUSER5")
    private Double qtyuser5;

    @Column(name = "QTYUSER6")
    private Double qtyuser6;

    @Column(name = "QTYUSER7")
    private Double qtyuser7;

    @Column(name = "DESCRUSER1")
    private String descruser1;

    @Column(name = "DESCRUSER2")
    private String descruser2;

    @Column(name = "DESCRUSER3")
    private String descruser3;

    @Column(name = "DESCRUSER4")
    private String descruser4;

    @Column(name = "DESCRUSER5")
    private String descruser5;

    @Column(name = "DESCRUSER6")
    private String descruser6;

    @Column(name = "DESCRUSER7")
    private String descruser7;

    @Column(name = "DATAUSER1")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser1;

    @Column(name = "DATAUSER2")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser2;

    @Column(name = "DATAUSER3")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser3;

    @Column(name = "DATAUSER4")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser4;

    @Column(name = "DATAUSER5")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser5;

    @Column(name = "DATAUSER6")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser6;

    @Column(name = "DATAUSER7")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datauser7;

    @Column(name = "DESCRUSER1E")
    private String descruser1E;

    @Column(name = "DESCRUSER2E")
    private String descruser2E;

    @Column(name = "DESCRUSER3E")
    private String descruser3E;

    @Column(name = "DESCRUSER4E")
    private String descruser4E;

    @Column(name = "DESCRUSER5E")
    private String descruser5E;

    @Column(name = "NOTERIGO")
    private String noterigo;

    @Column(name = "CONTROMARCA")
    private String contromarca;

    @Column(name = "FEORDID")
    private String feordid;

    @Column(name = "FEORDDATA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date feorddata;

    @Column(name = "FEORDITEM")
    private String feorditem;

    @Column(name = "FEINTENTO")
    private Integer feintento;

    @Column(name = "PROGRDEPOSITO")
    private Integer progrdeposito;

    @Column(name = "PROGRPREV")
    private Integer progrprev;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "DATAMODIFICA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datamodifica;

    @Column(name = "SYS_CREATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sysCreatedate;

    @Column(name = "SYS_CREATEUSER")
    private String sysCreateuser;

    @Column(name = "SYS_UPDATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sysUpdatedate;

    @Column(name = "SYS_UPDATEUSER")
    private String sysUpdateuser;

}
