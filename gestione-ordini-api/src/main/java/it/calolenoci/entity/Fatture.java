package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.dto.AccontoDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "FATTURE")
@IdClass(FattureId.class)
@Getter
@Setter
@NamedNativeQuery(
        name = "AccontoDto",
        query =
                "select f.CONTOCLIENTE as contoCliente, f.ANNO as anno, F.SERIE as serie, F.PROGRESSIVO as progressivo, f.DATAFATTURA as dataFattura, f.NUMEROFATTURA as numeroFattura, "
                        + " f2.FDESCRARTICOLO as operazione, f2.PREZZO as prezzo, f2.FCODICEIVA as iva, f2.FARTICOLO "
                        + " FROM FATTURE f"
                        + " JOIN FATTURE2 f2 ON f.ANNO = f2.ANNO AND f.SERIE = f2.SERIE AND f.PROGRESSIVO = f2.PROGRESSIVO"
                        + " WHERE 1=1 and f.serie = 'A' and F.PROGRESSIVO > 0"
                        + " and f.CONTOCLIENTE = :sottoConto ",
        resultSetMapping = "AccontoDto"
)
@NamedNativeQuery(
        name = "StornoDto",
        query = " select distinct f.CONTOCLIENTE as contoCliente, f.ANNO as anno, F.SERIE as serie, F.PROGRESSIVO as progressivo, f.DATAFATTURA as dataFattura, f.NUMEROFATTURA as numeroFattura"
                        + " , concat('ns.ordine n.', o.anno,'/', o.serie,'/',  o.PROGRESSIVO, ' del ', FORMAT(o.DATAORDINE, 'dd.MM.yyyy')) as rifOrdCliente"
                        + " , f4.FDESCRARTICOLO as operazione, f4.PREZZO as prezzo, f4.FCODICEIVA as iva, concat(o.anno,'/', o.serie,'/',  o.PROGRESSIVO) as ordineCliente"
                        + " FROM FATTURE f"
                        + " JOIN FATTURE2 f4 ON f.ANNO = f4.ANNO AND f.SERIE = f4.SERIE AND f.PROGRESSIVO = f4.PROGRESSIVO"
                        + " JOIN FATTURE2 f5 ON f5.ANNO = f4.ANNO AND f5.SERIE = f4.SERIE AND f.PROGRESSIVO = f5.PROGRESSIVO"
                        + " join ORDCLI2 o2 ON o2.PROGRGENERALE = f5.PROGRORDCLI"
                        + " join ORDCLI o ON o2.ANNO = o.ANNO AND o2.SERIE = o.SERIE AND o2.PROGRESSIVO = o.PROGRESSIVO"

                        + " WHERE f4.FDESCRARTICOLO like  '%Storno%'"
                        + " and f.CONTOCLIENTE = :sottoConto",
        resultSetMapping = "StornoDto"
)
@SqlResultSetMapping(
        name = "AccontoDto",
        classes = @ConstructorResult(
                targetClass = AccontoDto.class,
                columns = {
                        @ColumnResult(name = "contoCliente"),
                        @ColumnResult(name = "anno"),
                        @ColumnResult(name = "serie"),
                        @ColumnResult(name = "progressivo"),
                        @ColumnResult(name = "dataFattura"),
                        @ColumnResult(name = "numeroFattura"),
                        @ColumnResult(name = "operazione"),
                        @ColumnResult(name = "prezzo"),
                        @ColumnResult(name = "iva"),
                        @ColumnResult(name= "fArticolo")
                }
        )
)
@SqlResultSetMapping(
        name = "StornoDto",
        classes = @ConstructorResult(
                targetClass = AccontoDto.class,
                columns = {
                        @ColumnResult(name = "contoCliente"),
                        @ColumnResult(name = "anno"),
                        @ColumnResult(name = "serie"),
                        @ColumnResult(name = "progressivo"),
                        @ColumnResult(name = "dataFattura"),
                        @ColumnResult(name = "numeroFattura"),
                        @ColumnResult(name = "rifOrdCliente"),
                        @ColumnResult(name = "operazione"),
                        @ColumnResult(name = "prezzo"),
                        @ColumnResult(name = "iva"),
                        @ColumnResult(name= "ordineCliente")
                }
        )
)
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

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataFattura;

    @Column(length = 7)
    private String numeroFattura;

    @Column(name = "DATAOPERAZIONE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataoperazione;

    @Column(name = "NUMEROALLEGATO")
    private String numeroallegato;

    @Column(name = "NUMFATFORNITORE")
    private Integer numfatfornitore;

    @Column(name = "FCODICEPAGAMENT")
    private String fcodicepagament;

    @Column(name = "FCODDIFFPAG")
    private String fcoddiffpag;

    @Column(name = "FDATAPRIMASCAD")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fdataprimascad;

    @Column(name = "OGGETTO")
    private String oggetto;

    @Column(name = "BANCAAPPOGGIO")
    private Double bancaappoggio;

    @Column(name = "NSBANCAINCASSO")
    private String nsbancaincasso;

    @Column(name = "SPESEBOLLO")
    private String spesebollo;

    @Column(name = "IVAPRIMASCAD")
    private String ivaprimascad;

    @Column(name = "AGENTE")
    private String agente;

    @Column(name = "LISTINO")
    private String listino;

    @Column(name = "MODOCONSEGNA")
    private String modoconsegna;

    @Column(name = "VETTORE")
    private String vettore;

    @Column(name = "TARGA")
    private String targa;

    @Column(name = "TARGARIMORCHIO")
    private String targarimorchio;

    @Column(name = "VETTORE2")
    private String vettore2;

    @Column(name = "CAUSALETRASP")
    private String causaletrasp;

    @Column(name = "ASPETTOBENE")
    private String aspettobene;

    @Column(name = "CODICECOLLI")
    private String codicecolli;

    @Column(name = "NUMEROCOLLI")
    private Integer numerocolli;

    @Column(name = "DATATRASPORTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datatrasporto;

    @Column(name = "ORATRASPORTO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date oratrasporto;

    @Column(name = "STATOCONSEGNA")
    private String statoconsegna;

    @Column(name = "DATACONSEGNAEFF")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataconsegnaeff;

    @Column(name = "ORACONSEGNAEFF")
    @Temporal(TemporalType.TIMESTAMP)
    private Date oraconsegnaeff;

    @Column(name = "DATAORABOLLA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataorabolla;

    @Column(name = "DATAORAALLEST")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataoraallest;

    @Column(name = "TEMPOALLESTIMENTO")
    private Integer tempoallestimento;

    @Column(name = "FLAGVETTORE")
    private String flagvettore;

    @Column(name = "PROGRINVIOVETTORE")
    private Integer progrinviovettore;

    @Column(name = "TOTPESO")
    private Double totpeso;

    @Column(name = "TOTPESONETTO")
    private Double totpesonetto;

    @Column(name = "TOTPEDANE")
    private Integer totpedane;

    @Column(name = "TOTVOLUME")
    private Double totvolume;

    @Column(name = "INTESTDIVERSE")
    private String intestdiverse;

    @Column(name = "INDIRDIVERSE")
    private String indirdiverse;

    @Column(name = "LOCDIVERSE")
    private String locdiverse;

    @Column(name = "CAPDIVERSE")
    private String capdiverse;

    @Column(name = "PROVDIVERSE")
    private String provdiverse;

    @Column(name = "FLAGFATTURA")
    private String flagfattura;

    @Column(name = "FLAGBOLLA")
    private String flagbolla;

    @Column(name = "BOLLASOLA")
    private String bollasola;

    @Column(name = "SCONTOCLIENTE1")
    private Double scontocliente1;

    @Column(name = "SCONTOCLIENTE2")
    private Double scontocliente2;

    @Column(name = "SCONTOPAGAMENTO")
    private Double scontopagamento;

    @Column(name = "MAGAZZINO")
    private String magazzino;

    @Column(name = "FVALUTA")
    private String fvaluta;

    @Column(name = "FLINGUA")
    private String flingua;

    @Column(name = "FCAMBIO")
    private Double fcambio;

    @Column(name = "FLAGTRASFERITO")
    private String flagtrasferito;

    @Column(name = "FLAGEFFETTI")
    private String flageffetti;

    @Column(name = "FCODICEIVAT")
    private String fcodiceivat;

    @Column(name = "FPROVVARTICOLO")
    private Double fprovvarticolo;

    @Column(name = "FPROVVCLIENTE")
    private Double fprovvcliente;

    @Column(name = "TCOMMESSA")
    private String tcommessa;

    @Column(name = "TCENTROCOSTO")
    private String tcentrocosto;

    @Column(name = "TVOCESPESA")
    private String tvocespesa;

    @Column(name = "TCOMPETENZA")
    private Integer tcompetenza;

    @Column(name = "FDATAPAGPROV")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fdatapagprov;

    @Column(name = "FDATAPAGCAPO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fdatapagcapo;

    @Column(name = "FDATAPAGCAPOAG")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fdatapagcapoag;

    @Column(name = "FDATAPAGAGENZIA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fdatapagagenzia;

    @Column(name = "FLTIPOLIQUIDAPROV")
    private String fltipoliquidaprov;

    @Column(name = "SETTORE")
    private String settore;

    @Column(name = "ANNOPARTITA")
    private Integer annopartita;

    @Column(name = "NUMPARTITA")
    private Integer numpartita;

    @Column(name = "FLBLOCCOPAG")
    private String flbloccopag;

    @Column(name = "FLVARIAZIONE")
    private String flvariazione;

    @Column(name = "GRUPPOCOMPENSA")
    private Integer gruppocompensa;

    @Column(name = "CONTOCOMPENSA")
    private String contocompensa;

    @Column(name = "FLSPEDITO")
    private String flspedito;

    @Column(name = "DATACONSEGNA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataconsegna;

    @Column(name = "FLENTRO")
    private Integer flentro;

    @Column(name = "PRIORITA")
    private Integer priorita;

    @Column(name = "PROGRESSIVOGEN")
    private Integer progressivogen;

    @Column(name = "VALUSERN1")
    private Double valusern1;

    @Column(name = "VALUSERN2")
    private Double valusern2;

    @Column(name = "VALUSERALFA1")
    private String valuseralfa1;

    @Column(name = "VALUSERALFA2")
    private String valuseralfa2;

    @Column(length = 5000, name = "FNOTEFATTURA", columnDefinition = "text")
    private String fnotefattura;

    @Column(name = "FNOTEPIEDE")
    private String fnotepiede;

    @Column(name = "DATAPDF")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datapdf;

    @Column(name = "DATAINVIOPDF")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datainviopdf;

    @Column(name = "PROGRREGBOLLI")
    private Integer progrregbolli;

    @Column(name = "TPROVENIENZA")
    private String tprovenienza;

    @Column(name = "TPID")
    private Integer tpid;

    @Column(name = "CIG")
    private String cig;

    @Column(name = "CUP")
    private String cup;

    @Column(name = "DATAMODIFICA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datamodifica;

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

    @Column(name = "FLINVIORIFATT")
    private String flinviorifatt;

}
