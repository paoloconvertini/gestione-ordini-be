package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "ORDFOR2")
@Getter
@Setter
@IdClass(FornitoreDettaglioId.class)
public class OrdineFornitoreDettaglio extends PanacheEntityBase {

    @Column(length = 4, name = "ANNOOAF")
    @Id
    private Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    @Id
    private String serie;

    @Column(name = "PROGRESSIVOOAF")
    @Id
    private Integer progressivo;

    @Column
    private Integer progrGenerale;

    @Column
    @Id
    private Integer rigo;

    @Column
    private Integer pid;

    @Column(length = 5000, name = "NOTEORDFOR2", columnDefinition = "text")
    private String nota;

    @Column(length = 13)
    private String oArticolo;

    @Column(length = 50)
    private String oDescrArticolo;

    @Column(length = 2, name = "TIPORIGOOAF")
    private String tipoRigo;

    @Column(length = 1, name = "OSALDOACCONTO")
    private String saldo;

    @Column
    private Double oQuantita;

    @Column(name = "OQUANTITAV")
    private Double oQuantitaV;

    @Column
    private Double oPrezzo;

    @Column(name = "FSCONTOARTICOLO")
    private Double fScontoArticolo;

    @Column
    private Double scontoF1;

    @Column
    private Double scontoF2;

    @Column
    private Double fScontoP;

    @Column(length = 2)
    private String oUnitaMisura;

    @Column
    private Integer oColli;

    @Column(length = 3)
    private String oCodiceIva;

    @Column(length = 25, name = "CAMPOUSER5")
    private String campoUser5;

    @Column(length = 1)
    private String provenienza;

    @Column(length = 3, name="OMAGAZZ")
    private String magazzino;

    @Column(name = "OVALORE")
    private Double valoreTotale;

    @Column(name = "SINONIMO1")
    private Integer sinonimo1;

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

    @Column(name = "OQUANTITA2")
    private Double oquantita2;

    @Column(name = "OCOEFFICIENTE")
    private Double ocoefficiente;

    @Column(name = "PREZZOEXTRA")
    private Double prezzoextra;

    @Column(name = "DATARICHCONSEG")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datarichconseg;

    @Column(name = "DATACONFCONSEG")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataconfconseg;

    @Column(name = "OLOTTOMAGF")
    private String olottomagf;

    @Column(name = "OPALLET")
    private Double opallet;

    @Column(name = "OCOMMESSA")
    private String ocommessa;

    @Column(name = "OCENTROCOSTOR")
    private String ocentrocostor;

    @Column(name = "OVOCESPESA")
    private String ovocespesa;

    @Column(name = "IMPPROVVFISSO")
    private Double impprovvfisso;

    @Column(name = "OPROVVARTICOLO")
    private Double oprovvarticolo;

    @Column(name = "OPROVVFORNITORE")
    private Double oprovvfornitore;

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

    @Column(name = "CAMPOUSER1")
    private String campouser1;

    @Column(name = "CAMPOUSER2")
    private String campouser2;

    @Column(name = "CAMPOUSER3")
    private String campouser3;

    @Column(name = "CAMPOUSER4")
    private String campouser4;

    @Column(name = "PID_PRIMANOTA")
    private Integer pidPrimanota;

    @Column(name = "USERNAME")
    private String username;

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
