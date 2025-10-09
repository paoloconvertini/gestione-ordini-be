package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.OrdineId;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"anno", "serie", "progressivo", "numeroFattura", "dataFattura", "iva", "rifOrdCliente"})
public class AccontoDto {
    
    private Integer anno;
    
    private Integer progressivo;

    private String serie;

    private String contoCliente;

    private Date dataFattura;

    private Date dataBolla;

    private String numeroFattura;
    private String numeroBolla;

    private String rifOrdCliente;

    private String operazione;

    private Double prezzo;

    private String iva;

    private String ordineCliente;

    private String fArticolo;

    private List<String> rifOrdClienteList;

    private String uuidAS;

    private Double importoResiduo; // importo residuo (imponibile -

    private Double importoResiduoIvato; // importo residuo (ivato -

    private List<AccontoDto> storni;


    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo,
                      Date dataFattura, String numeroFattura, String rifOrdCliente,
                      String operazione, Double prezzo, String iva, String ordineCliente) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.rifOrdCliente = rifOrdCliente;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.ordineCliente = ordineCliente;
    }

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo, Date dataBolla,
                      Date dataFattura, String numeroFattura, String rifOrdCliente,
                      String operazione, Double prezzo, String iva, String ordineCliente) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataBolla = dataBolla;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.rifOrdCliente = rifOrdCliente;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.ordineCliente = ordineCliente;
    }

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo, Date dataBolla,
                      Date dataFattura, String numeroFattura, String rifOrdCliente,
                      String operazione, Double prezzo, String iva, String ordineCliente, String numeroBolla) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataBolla = dataBolla;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.numeroBolla = numeroBolla;
        this.rifOrdCliente = rifOrdCliente;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.ordineCliente = ordineCliente;
    }

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo,
                      Date dataFattura, String numeroFattura,
                      String operazione, Double prezzo, String iva, String fArticolo) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.fArticolo = fArticolo;
    }

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo, Date dataBolla,
                      Date dataFattura, String numeroFattura,
                      String operazione, Double prezzo, String iva, String fArticolo) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataBolla = dataBolla;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.fArticolo = fArticolo;
    }

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo, Date dataBolla,
                      Date dataFattura, String numeroFattura,
                      String operazione, Double prezzo, String iva, String fArticolo, String numeroBolla) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataBolla = dataBolla;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.numeroBolla = numeroBolla;
        this.operazione = operazione;
        this.prezzo = prezzo;
        this.iva = iva;
        this.fArticolo = fArticolo;
    }

    public static boolean checkOrdineEsiste(AccontoDto a, OrdinePerIva id){
        return StringUtils.contains(a.getRifOrdCliente(), StringUtils.join(id.getAnno(), "/", id.getSerie(), "/", id.getProgressivo()))
                || (StringUtils.contains(a.getRifOrdCliente(), StringUtils.join(id.getAnno(), "-", id.getSerie(), "-", id.getProgressivo())));
    }

}
