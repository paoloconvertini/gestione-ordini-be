package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
public class QuadraturaCespiteRequest implements Serializable {

    private String id;
    private String idCespite;
    private Integer anno;
    private Double ammortamento;


}
