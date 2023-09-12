package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MAGAZZINO")
@Getter
@Setter
public class Magazzino extends PanacheEntityBase {

    @EmbeddedId
    private MagazzinoId magazzinoId;

    @Column(length = 13)
    private String mArticolo;

    @Column(name="VALOREUNITARIO")
    private Double valoreUnitario;

    @Column(name = "DATAOPMAGAZZINO")
    private Date dataMagazzino;

    @Column(name = "PROGRGENERALE", nullable = true)
    private Integer progrgenerale;

    @Column(name = "NUMDOCMAGAZZINO", nullable = true, length = 15)
    private String numdocmagazzino;

    @Column(name = "DATADOCMAG", nullable = true)
    private java.sql.Date datadocmag;

    @Column(name = "NUMFATTURAMAG", nullable = true, length = 15)
    private String numfatturamag;

    @Column(name = "DATAFATTURAMAG", nullable = true)
    private java.sql.Date datafatturamag;

    @Column(name = "MCAUSALE", nullable = true, length = 3)
    private String mcausale;

    @Column(name = "MMAGAZZINO", nullable = true, length = 3)
    private String mmagazzino;

    @Column(name = "GRUPPOMAG", nullable = true)
    private Integer gruppomag;

    @Column(name = "CONTOMAG", nullable = true, length = 6)
    private String contomag;

    @Column(name = "GRUPPOPROP", nullable = true)
    private Integer gruppoprop;

    @Column(name = "CONTOPROP", nullable = true, length = 6)
    private String contoprop;

    @Column(name = "GRUPPOFATTURA", nullable = true)
    private Integer gruppofattura;

    @Column(name = "CONTOFATTURA", nullable = true, length = 6)
    private String contofattura;

    @Column(name = "CONTROMAG", nullable = true, length = 3)
    private String contromag;

    @Column(name = "TIPORIGOMAG", nullable = true, length = 2)
    private String tiporigomag;

    @Column(name = "CODICEEAN", nullable = true, length = 60)
    private String codiceean;

    @Column(name = "VARIANTE1", nullable = true, length = 3)
    private String variante1;

    @Column(name = "VARIANTE2", nullable = true, length = 3)
    private String variante2;

    @Column(name = "VARIANTE3", nullable = true, length = 3)
    private String variante3;

    @Column(name = "VARIANTE4", nullable = true, length = 3)
    private String variante4;

    @Column(name = "VARIANTE5", nullable = true, length = 3)
    private String variante5;

    @Column(name = "MDESCRARTICOLO", nullable = true, length = 60)
    private String mdescrarticolo;

    @Column(name = "MUNITA", nullable = true, length = 2)
    private String munita;

    @Column(name = "MCOEFFICIENTE", nullable = true, precision = 0)
    private Double mcoefficiente;

    @Column(name = "MQUANTITA", nullable = true, precision = 0)
    private Double mquantita;

    @Column(name = "MQUANTITAV", nullable = true, precision = 0)
    private Double mquantitav;

    @Column(name = "MQUANTITA2", nullable = true, precision = 0)
    private Double mquantita2;

    @Column(name = "VALORE", nullable = true, precision = 0)
    private Double valore;

    @Column(name = "PREZZO", nullable = true, precision = 0)
    private Double prezzo;

    @Column(name = "PREZZOEXTRA", nullable = true, precision = 0)
    private Double prezzoextra;

    @Column(name = "MVALUTA", nullable = true, length = 3)
    private String mvaluta;

    @Column(name = "MCAMBIO", nullable = true, precision = 0)
    private Double mcambio;

    @Column(name = "SCONTOARTICOLO", nullable = true, precision = 0)
    private Double scontoarticolo;

    @Column(name = "SCONTOC1", nullable = true, precision = 0)
    private Double scontoc1;

    @Column(name = "SCONTOC2", nullable = true, precision = 0)
    private Double scontoc2;

    @Column(name = "SCONTOP", nullable = true, precision = 0)
    private Double scontop;

    @Column(name = "MPROVVARTICOLO", nullable = true, precision = 0)
    private Double mprovvarticolo;

    @Column(name = "MPROVVCLIENTE", nullable = true, precision = 0)
    private Double mprovvcliente;

    @Column(name = "LOTTOMAG", nullable = true, length = 10)
    private String lottomag;

    @Column(name = "CONDPAGMAG", nullable = true, length = 3)
    private String condpagmag;

    @Column(name = "IVAMAG", nullable = true, length = 3)
    private String ivamag;

    @Column(name = "MAGENTE", nullable = true, length = 3)
    private String magente;

    @Column(name = "DATAPRIMAMAG", nullable = true)
    private java.sql.Date dataprimamag;

    @Column(name = "MCENTROCOSTO", nullable = true, length = 10)
    private String mcentrocosto;

    @Column(name = "MVOCESPESA", nullable = true, length = 10)
    private String mvocespesa;

    @Column(name = "MCOMMESSA", nullable = true, length = 10)
    private String mcommessa;

    @Column(name = "MCIG", nullable = true, length = 15)
    private String mcig;

    @Column(name = "MCUP", nullable = true, length = 15)
    private String mcup;

    @Column(name = "MCOLLI", nullable = true)
    private Integer mcolli;

    @Column(name = "MPALLET", nullable = true, precision = 0)
    private Double mpallet;

    @Column(name = "MMODOCONSEGNA", nullable = true, length = 3)
    private String mmodoconsegna;

    @Column(name = "MVETTORE", nullable = true, length = 3)
    private String mvettore;

    @Column(name = "VALOREUSER", nullable = true, precision = 0)
    private Double valoreuser;

    @Column(name = "DESCRUSER1", nullable = true, length = 60)
    private String descruser1;

    @Column(name = "DESCRUSER2", nullable = true, length = 60)
    private String descruser2;

    @Column(name = "DESCRUSER3", nullable = true, length = 60)
    private String descruser3;

    @Column(name = "DESCRUSER4", nullable = true, length = 60)
    private String descruser4;

    @Column(name = "DESCRUSER5", nullable = true, length = 60)
    private String descruser5;

    @Column(name = "QUANTITAUSER01", nullable = true, precision = 0)
    private Double quantitauser01;

    @Column(name = "QUANTITAUSER02", nullable = true, precision = 0)
    private Double quantitauser02;

    @Column(name = "QUANTITAUSER03", nullable = true, precision = 0)
    private Double quantitauser03;

    @Column(name = "QUANTITAUSER04", nullable = true, precision = 0)
    private Double quantitauser04;

    @Column(name = "QUANTITAUSER05", nullable = true, precision = 0)
    private Double quantitauser05;

    @Column(name = "DATAUSER1", nullable = true)
    private java.sql.Date datauser1;

    @Column(name = "DATAUSER2", nullable = true)
    private java.sql.Date datauser2;

    @Column(name = "DATAUSER3", nullable = true)
    private java.sql.Date datauser3;

    @Column(name = "DATAUSER4", nullable = true)
    private java.sql.Date datauser4;

    @Column(name = "DATAUSER5", nullable = true)
    private java.sql.Date datauser5;

    @Column(name = "FLAGTRASFERITO", nullable = true, length = 1)
    private String flagtrasferito;

    @Column(name = "NOTEMAG", nullable = true, length = -1)
    private String notemag;

    @Column(name = "PROVENIENZA", nullable = true, length = 3)
    private String provenienza;

    @Column(name = "PID", nullable = true)
    private Integer pid;

    @Column(name = "RIF_RIGA_COMMESSA", nullable = true)
    private Integer rifRigaCommessa;

    @Column(name = "PARTITACDEPOSITO", nullable = true)
    private Integer partitacdeposito;

    @Column(name = "CONTROLLOBF", nullable = true, length = 1)
    private String controllobf;

    @Column(name = "BFRIGO", nullable = true)
    private Integer bfrigo;

    @Column(name = "BFVERIFICA", nullable = true, length = 1)
    private String bfverifica;

    @Column(name = "SETTORE", nullable = true, length = 3)
    private String settore;

    @Column(name = "OGGETTO", nullable = true, length = 5)
    private String oggetto;

    @Column(name = "RIGOGIORNALE", nullable = true)
    private Integer rigogiornale;

    @Column(name = "DATAINSERIMENTO", nullable = true)
    private java.sql.Date datainserimento;

    @Column(name = "USERNAME", nullable = true, length = 20)
    private String username;

    @Column(name = "DATAMODIFICA", nullable = true)
    private java.sql.Date datamodifica;

    @Column(name = "COSTOMEDIO", nullable = true, precision = 0)
    private Double costomedio;

    @Column(name = "SYS_CREATEDATE", nullable = true)
    private java.sql.Date sysCreatedate;

    @Column(name = "SYS_CREATEUSER", nullable = true, length = 20)
    private String sysCreateuser;

    @Column(name = "SYS_UPDATEDATE", nullable = true)
    private java.sql.Date sysUpdatedate;

    @Column(name = "SYS_UPDATEUSER", nullable = true, length = 20)
    private String sysUpdateuser;

    @Column(name = "DATASCONTRINO", nullable = true)
    private java.sql.Date datascontrino;

    @Column(name = "PID_PRIMANOTA", nullable = true)
    private Integer pidPrimanota;
}
