package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DdtNettoDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String fCodiceIva;
    private Double importoDdtNetto;
}
