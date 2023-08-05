package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
public class AccontoDto {
    
    private Integer anno;
    
    private Integer progressivo;

    private String serie;

    private String contoCliente;

    private Date dataFattura;

    private String numeroFattura;

    private String rifOrdCliente;

    private String operazione;

    private Double prezzo;

    private String iva;

    private String ordineCliente;

    private String fArticolo;

    private List<String> rifOrdClienteList;

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
}
