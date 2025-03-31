package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class FatturaAccontoView implements Serializable {
    private Double importo;
    private Double fCodiceIva;
    private Integer anno;
    private String serie;
    private Integer progressivo;
}
