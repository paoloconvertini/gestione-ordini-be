package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.Date;

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

    public AccontoDto(String contoCliente, Integer anno,String serie, Integer progressivo,
                      Date dataFattura, String numeroFattura, String rifOrdCliente,
                      String operazione, Double prezzo) {
        this.contoCliente = contoCliente;
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataFattura = dataFattura;
        this.numeroFattura = numeroFattura;
        this.rifOrdCliente = rifOrdCliente;
        this.operazione = operazione;
        this.prezzo = prezzo;
    }
}
