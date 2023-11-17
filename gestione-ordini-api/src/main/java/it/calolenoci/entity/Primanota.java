package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@IdClass(PrimanotaPK.class)
@Table(name = "PRIMANOTA")
@Getter
@Setter
public class Primanota extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ANNO", nullable = false)
    private Integer anno;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "GIORNALE", nullable = false, length = 1)
    private String giornale;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PROTOCOLLO", nullable = false)
    private Integer protocollo;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PROGRPRIMANOTA", nullable = false)
    private Integer progrprimanota;
    
    @Column(name = "PROGRGENERALE", nullable = true)
    private Integer progrgenerale;
    
    @Column(name = "DIVISIONE", nullable = true, length = 5)
    private String divisione;
    
    @Column(name = "ANNOPARTITA", nullable = true)
    private Integer annopartita;
    
    @Column(name = "NUMPARTITA", nullable = true)
    private Integer numpartita;
    
    @Column(name = "DATAOPERAZIONE", nullable = true)
    private LocalDate dataoperazione;
    
    @Column(name = "DATAMOVIMENTO", nullable = true)
    private LocalDate datamovimento;
    
    @Column(name = "NUMERODOCUMENTO", nullable = true, length = 15)
    private String numerodocumento;
    
    @Column(name = "CAUSALE", nullable = true, length = 3)
    private String causale;
    
    @Column(name = "GRUPPOCONTO", nullable = true)
    private Integer gruppoconto;
    
    @Column(name = "SOTTOCONTO", nullable = true, length = 6)
    private String sottoconto;
    
    @Column(name = "DESCRSUPPL", nullable = true, length = 25)
    private String descrsuppl;
    
    @Column(name = "IMPORTO", nullable = true, precision = 0)
    private Double importo;
    
    @Column(name = "RIGOGIORNALE", nullable = true)
    private Integer rigogiornale;
    
    @Column(name = "CODICEPAGAMENTO", nullable = true, length = 3)
    private String codicepagamento;
    
    @Column(name = "CODDIFFPAG", nullable = true, length = 3)
    private String coddiffpag;
    
    @Column(name = "IVAPRIMASCAD", nullable = true, length = 1)
    private String ivaprimascad;
    
    @Column(name = "DATAPRIMASCAD", nullable = true)
    private LocalDate dataprimascad;
    
    @Column(name = "IMPORTOVE", nullable = true, precision = 0)
    private Double importove;
    
    @Column(name = "DATAPRENOTATO", nullable = true)
    private LocalDate dataprenotato;
    
    @Column(name = "IMPPRENOTATO", nullable = true, precision = 0)
    private Double impprenotato;
    
    @Column(name = "DATAAUTORIZZA", nullable = true)
    private LocalDate dataautorizza;
    
    @Column(name = "USERAUTORIZZA", nullable = true, length = 20)
    private String userautorizza;
    
    @Column(name = "INIZIOPERIODO", nullable = true)
    private LocalDate inizioperiodo;
    
    @Column(name = "FINEPERIODO", nullable = true)
    private LocalDate fineperiodo;
    
    @Column(name = "ONORARIO", nullable = true, precision = 0)
    private Double onorario;
    
    @Column(name = "SPESE", nullable = true, precision = 0)
    private Double spese;
    
    @Column(name = "SPESENO770", nullable = true, precision = 0)
    private Double speseno770;
    
    @Column(name = "TIPOCOMPENSO", nullable = true, length = 3)
    private String tipocompenso;
    
    @Column(name = "RITENUTA", nullable = true, precision = 0)
    private Double ritenuta;
    
    @Column(name = "RITINPS", nullable = true, precision = 0)
    private Double ritinps;
    
    @Column(name = "RITINAIL", nullable = true, precision = 0)
    private Double ritinail;
    
    @Column(name = "RITENASARCO", nullable = true, precision = 0)
    private Double ritenasarco;
    
    @Column(name = "ANNOENASARCO", nullable = true)
    private Integer annoenasarco;
    
    @Column(name = "TRIMENASARCO", nullable = true)
    private Integer trimenasarco;
    
    @Column(name = "FLAGTRASFERITO", nullable = true, length = 1)
    private String flagtrasferito;
    
    @Column(name = "NUMERORATA", nullable = true)
    private Integer numerorata;
    
    @Column(name = "NUMCONCILIAZIONE", nullable = true)
    private Integer numconciliazione;
    
    @Column(name = "FLAGPROVVISORIA", nullable = true, length = 1)
    private String flagprovvisoria;
    
    @Column(name = "FLAGFTBLOCCATA", nullable = true, length = 1)
    private String flagftbloccata;
    
    @Column(name = "VALUTA", nullable = true, length = 3)
    private String valuta;
    
    @Column(name = "CAMBIO", nullable = true, precision = 0)
    private Double cambio;
    
    @Column(name = "FLAGGENERICO", nullable = true, length = 1)
    private String flaggenerico;
    
    @Column(name = "CONTROLLOBF", nullable = true, length = 1)
    private String controllobf;
    
    @Column(name = "AGENTE", nullable = true, length = 3)
    private String agente;
    
    @Column(name = "BANCAPAG", nullable = true, length = 3)
    private String bancapag;
    
    @Column(name = "DATAVALUTA", nullable = true)
    private LocalDate datavaluta;
    
    @Column(name = "CIG", nullable = true, length = 15)
    private String cig;
    
    @Column(name = "CUP", nullable = true, length = 15)
    private String cup;
    
    @Column(name = "VALUSER1", nullable = true, precision = 0)
    private Double valuser1;
    
    @Column(name = "VALUSER2", nullable = true, precision = 0)
    private Double valuser2;
    
    @Column(name = "VALUSER3", nullable = true, precision = 0)
    private Double valuser3;
    
    @Column(name = "VALUSER4", nullable = true, precision = 0)
    private Double valuser4;
    
    @Column(name = "VALUSER5", nullable = true, precision = 0)
    private Double valuser5;
    
    @Column(name = "CAMPOUSER1", nullable = true, length = 30)
    private String campouser1;
    
    @Column(name = "CAMPOUSER2", nullable = true, length = 30)
    private String campouser2;
    
    @Column(name = "CAMPOUSER3", nullable = true, length = 30)
    private String campouser3;
    
    @Column(name = "CAMPOUSER4", nullable = true, length = 30)
    private String campouser4;
    
    @Column(name = "CAMPOUSER5", nullable = true, length = 30)
    private String campouser5;
    
    @Column(name = "USERNAME", nullable = true, length = 20)
    private String username;
    
    @Column(name = "DATAMODIFICA", nullable = true)
    private LocalDate datamodifica;
    
    @Column(name = "NOTEPRIMANOTA", nullable = true, length = -1)
    private String noteprimanota;
    
    @Column(name = "PROVENIENZA", nullable = true, length = 1)
    private String provenienza;
    
    @Column(name = "SYS_CREATEDATE", nullable = true)
    private LocalDate sysCreatedate;
    
    @Column(name = "SYS_CREATEUSER", nullable = true, length = 20)
    private String sysCreateuser;
    
    @Column(name = "SYS_UPDATEDATE", nullable = true)
    private LocalDate sysUpdatedate;
    
    @Column(name = "SYS_UPDATEUSER", nullable = true, length = 20)
    private String sysUpdateuser;
    
    @Column(name = "SPESEENASARCO", nullable = true, precision = 0)
    private Double speseenasarco;
    
    @Column(name = "IBAN", nullable = true, length = 40)
    private String iban;
    
    @Column(name = "ANNODIRITTO", nullable = true, precision = 0)
    private Double annodiritto;
    
    @Column(name = "PID", nullable = true)
    private Integer pid;
    
    @Column(name = "IVASP", nullable = true, precision = 0)
    private Double ivasp;
    
    @Column(name = "SPESECOD8", nullable = true, precision = 0)
    private Double spesecod8;
}
