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
public class FatturaAccontoIvaView implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String fCodiceIva;
    private Long importo;
}
