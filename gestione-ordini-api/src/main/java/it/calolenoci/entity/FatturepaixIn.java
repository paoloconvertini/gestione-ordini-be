package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "FATTUREPAIX_IN")
@Getter
@Setter
public class FatturepaixIn extends PanacheEntityBase {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ID_FATTUREPAIX_IN", nullable = false)
    private int id;
    
    @Column(name = "IDENTIFICATIVOSDI", nullable = true, length = 40)
    private String identificativosdi;
    
    @Column(name = "IDENTIFICATIVOIX", nullable = true, length = 40)
    private String identificativoix;
    
    @Column(name = "DATACONSEGNA", nullable = true)
    private Date dataconsegna;
    
    @Column(name = "NOMEFILE", nullable = true, length = 100)
    private String nomefile;
    
    @Column(name = "NOMEFILEMETADATI", nullable = true, length = 25)
    private String nomefilemetadati;
    
    @Column(name = "HASH", nullable = true, length = -1)
    private String hash;
    
    @Column(name = "CODICEDESTINATARIO", nullable = true, length = 7)
    private String codicedestinatario;
    
    @Column(name = "FORMATO", nullable = true, length = 5)
    private String formato;
    
    @Column(name = "TENTATIVIINVIO", nullable = true)
    private Integer tentativiinvio;
    
    @Column(name = "MESSAGEID", nullable = true, length = 36)
    private String messageid;
    
    @Column(name = "MESSAGGIO", nullable = true, length = -1)
    private String messaggio;
    
    @Column(name = "VERSIONE", nullable = true, length = 10)
    private String versione;
    
    @Column(name = "DATAORA", nullable = true)
    private Date dataora;
    
    @Column(name = "DATAORADOWNLOAD", nullable = true)
    private Date dataoradownload;
    
    @Column(name = "FORNITORE_DENOM", nullable = true, length = 40)
    private String fornitoreDenom;
    
    @Column(name = "FORNITORE_PIVA", nullable = true, length = 16)
    private String fornitorePiva;
    
    @Column(name = "DATAORAIMPORTAZIONE", nullable = true)
    private Date dataoraimportazione;
    
    @Column(name = "FLNOCIG", nullable = true, length = 1)
    private String flnocig;
    
    @Column(name = "SYS_CREATEDATE", nullable = true)
    private Date sysCreatedate;
    
    @Column(name = "SYS_CREATEUSER", nullable = true, length = 20)
    private String sysCreateuser;
    
    @Column(name = "SYS_UPDATEDATE", nullable = true)
    private Date sysUpdatedate;
    
    @Column(name = "SYS_UPDATEUSER", nullable = true, length = 20)
    private String sysUpdateuser;

}
