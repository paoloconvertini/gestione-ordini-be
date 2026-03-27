package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.DdtNettoDto;
import it.calolenoci.dto.StornoDto;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "FATTURE")
@IdClass(FattureId.class)
@Getter
@Setter
@NamedNativeQuery(
        name = "DdtNettiPerOrdine",
        query = "SELECT " +
                "    o.ANNO AS anno, " +
                "    o.SERIE AS serie, " +
                "    o.PROGRESSIVO AS progressivo, " +
                "    f2.FCODICEIVA AS fCodiceIva, " +
                "    ISNULL(SUM(ISNULL(f2.PREZZO, 0) * ISNULL(f2.QUANTITA, 0) * " +
                "        (1 - ISNULL(f2.SCONTOARTICOLO, 0)/100) * " +
                "        (1 - ISNULL(f2.SCONTOC1, 0)/100) * " +
                "        (1 - ISNULL(f2.SCONTOC2, 0)/100) * " +
                "        (1 - ISNULL(f2.SCONTOP, 0)/100)), 0) AS importoDdtNetto " +
                "FROM FATTURE2 f2 " +
                "JOIN ORDCLI2 o2 ON f2.PROGRORDCLI = o2.PROGRGENERALE " +
                "JOIN ORDCLI o ON o.ANNO = o2.ANNO AND o.SERIE = o2.SERIE AND o.PROGRESSIVO = o2.PROGRESSIVO " +
                "WHERE f2.TIPORIGO <> 'V' " +
                "AND o.ANNO = :anno " +
                "AND o.SERIE = :serie " +
                "AND o.PROGRESSIVO = :progressivo " +
                "AND f2.FCODICEIVA = :fCodiceIva " +
                "GROUP BY o.ANNO, o.SERIE, o.PROGRESSIVO, f2.FCODICEIVA",
        resultSetMapping = "DdtNettoDtoMapping"
)

@NamedNativeQuery(
        name = "StorniAcconti",
        query = "SELECT f2.FCODICEIVA AS fCodiceIva, -SUM(f2.PREZZO) AS importo " +
                " FROM FATTURE2 f2 " +
                " JOIN FATTURE f ON f2.PROGRESSIVO = f.PROGRESSIVO AND f2.SERIE = f.SERIE AND f2.ANNO = f.ANNO " +
                " WHERE f2.TIPORIGO = 'V' " +
                " AND f2.FDESCRARTICOLO LIKE CONCAT('%', :numeroFattura, '%')"
                + " and f2.FDESCRARTICOLO LIKE CONCAT('%', :dataAcconto, '%')" +
                " AND f.CONTOCLIENTE = :sottoConto" +
                " GROUP BY f2.FCODICEIVA",
        resultSetMapping = "StornoAccontoDtoMapping"
)

@NamedNativeQuery(
        name = "AccontoDto",
        query =
                "select f.CONTOCLIENTE as contoCliente, f.ANNO as anno, F.SERIE as serie, F.PROGRESSIVO as progressivo, f.DATABOLLA as dataBolla, f.DATAFATTURA as dataFattura, f.NUMEROFATTURA as numeroFattura, "
                        + " f2.FDESCRARTICOLO as operazione, f2.PREZZO as prezzo, f2.FCODICEIVA as iva, f2.FARTICOLO  as fArticolo, f.NUMEROBOLLA "
                        + " FROM FATTURE f"
                        + " JOIN FATTURE2 f2 ON f.ANNO = f2.ANNO AND f.SERIE = f2.SERIE AND f.PROGRESSIVO = f2.PROGRESSIVO"
                        + " WHERE 1=1 and f.serie = 'A' and f.PROGRESSIVO > 0"
                        + " and f.CONTOCLIENTE = :sottoConto " +
                        "   ORDER BY f2.anno, f2.serie, f2.progressivo, f2.rigo ",
        resultSetMapping = "AccontoDto"
)
@NamedNativeQuery(
        name = "AccontoPerOrdineANDIva",
        query =
                "select f.CONTOCLIENTE as contoCliente, f.ANNO as anno, F.SERIE as serie, F.PROGRESSIVO as progressivo, f.DATAFATTURA as dataFattura, f.NUMEROFATTURA as numeroFattura, "
                        + " f2.FDESCRARTICOLO as operazione, f2.PREZZO as prezzo, f2.FCODICEIVA as iva, f2.FARTICOLO  as fArticolo"
                        + " FROM FATTURE f"
                        + " JOIN FATTURE2 f2 ON f.ANNO = f2.ANNO AND f.SERIE = f2.SERIE AND f.PROGRESSIVO = f2.PROGRESSIVO"
                        + " JOIN FATTURE2 f5 ON f5.ANNO = f2.ANNO AND f5.SERIE = f2.SERIE AND f2.PROGRESSIVO = f5.PROGRESSIVO"
                        + " WHERE 1=1 and f.serie = 'A' and f.PROGRESSIVO > 0"
                        + " and f5.FDESCRARTICOLO like :oper and f2.FCODICEIVA =:i ",
        resultSetMapping = "AccontoDto"
)
@NamedNativeQuery(
        name = "StornoDto",
        query = " select distinct f.CONTOCLIENTE as contoCliente, f.ANNO as anno, F.SERIE as serie, F.PROGRESSIVO as progressivo, f.DATABOLLA as dataBolla, f.DATAFATTURA as dataFattura, f.NUMEROFATTURA as numeroFattura"
                        + " , concat('ns.ordine n.', o.anno,'/', o.serie,'/',  o.PROGRESSIVO, ' del ', FORMAT(o.DATAORDINE, 'dd.MM.yyyy')) as rifOrdCliente"
                        + " , f4.FDESCRARTICOLO as operazione, f4.PREZZO as prezzo, f4.FCODICEIVA as iva, concat(o.anno,'/', o.serie,'/',  o.PROGRESSIVO) as ordineCliente, f.NUMEROBOLLA "
                        + " FROM FATTURE f"
                        + " JOIN FATTURE2 f4 ON f.ANNO = f4.ANNO AND f.SERIE = f4.SERIE AND f.PROGRESSIVO = f4.PROGRESSIVO"
                        + " JOIN FATTURE2 f5 ON f5.ANNO = f4.ANNO AND f5.SERIE = f4.SERIE AND f.PROGRESSIVO = f5.PROGRESSIVO"
                        + " join ORDCLI2 o2 ON o2.PROGRGENERALE = f5.PROGRORDCLI"
                        + " join ORDCLI o ON o2.ANNO = o.ANNO AND o2.SERIE = o.SERIE AND o2.PROGRESSIVO = o.PROGRESSIVO"
                        +  " WHERE "
                        +  " f.CONTOCLIENTE = :sottoConto "
                        +  " AND f4.FCODICEIVA = :iva "
                        +  " AND f4.FDESCRARTICOLO LIKE CONCAT('%', :numeroFattura, '%') "
                        +  " AND ( "
                        +  "         REPLACE(REPLACE(f4.FDESCRARTICOLO, '.', '/'), '-', '/') LIKE CONCAT('%', :data1, '%') "
                        +  " OR "
                        +  " REPLACE(REPLACE(f4.FDESCRARTICOLO, '.', '/'), '-', '/') LIKE CONCAT('%', :data2, '%') "
                        +  "     ) ",
        resultSetMapping = "StornoDto"
)
@SqlResultSetMapping(
        name="StornoAccontoDtoMapping",
        classes = @ConstructorResult(
                targetClass = StornoDto.class,
                columns = {
                        @ColumnResult(name = "fCodiceIva", type = String.class),
                        @ColumnResult(name = "importo", type = Double.class),
                }
        )
)
@SqlResultSetMapping(
        name = "DdtNettoDtoMapping",
        classes = @ConstructorResult(
                targetClass = DdtNettoDto.class,
                columns = {
                        @ColumnResult(name = "anno", type = Integer.class),
                        @ColumnResult(name = "serie", type = String.class),
                        @ColumnResult(name = "progressivo", type = Integer.class),
                        @ColumnResult(name = "fCodiceIva", type = String.class),
                        @ColumnResult(name = "importoDdtNetto", type = Double.class)
                }
        )
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
                        @ColumnResult(name = "dataBolla"),
                        @ColumnResult(name = "dataFattura"),
                        @ColumnResult(name = "numeroFattura"),
                        @ColumnResult(name = "operazione"),
                        @ColumnResult(name = "prezzo"),
                        @ColumnResult(name = "iva"),
                        @ColumnResult(name= "fArticolo"),
                        @ColumnResult(name = "numeroBolla")
                }
        )
)
@SqlResultSetMapping(
        name = "AccontoPerOrdineANDIva",
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
                        @ColumnResult(name = "dataBolla"),
                        @ColumnResult(name = "dataFattura"),
                        @ColumnResult(name = "numeroFattura"),
                        @ColumnResult(name = "rifOrdCliente"),
                        @ColumnResult(name = "operazione"),
                        @ColumnResult(name = "prezzo"),
                        @ColumnResult(name = "iva"),
                        @ColumnResult(name= "ordineCliente"),
                        @ColumnResult(name= "numeroBolla")

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

    @Column(name = "FNOTEPIEDE", length = -1)
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

    @Column(name = "DATASCONTRINO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataScontrino;

    @Column(name = "FLDDTEMAIL")
    private String flDttEmail;
}
