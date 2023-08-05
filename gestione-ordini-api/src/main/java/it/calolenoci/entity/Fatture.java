package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.AccontoDto;
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

}
