package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FatturaAccontoDto {

    private Integer anno;

    private Integer progressivo;

    private String serie;

    private String contoCliente;


    private Double nuovoAcconto;

    private Double nuovoAccontoIvato;

    private String iva;

    @JsonProperty("aSaldo")
    private boolean aSaldo;

}
