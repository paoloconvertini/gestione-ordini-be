package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class FatturaAccontoView implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private List<FatturaAccontoIvaView> fatturaAccontoIvaViewList;
    private List<AccontoDto> acconti;
}
