package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class AccontoLightDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
}
