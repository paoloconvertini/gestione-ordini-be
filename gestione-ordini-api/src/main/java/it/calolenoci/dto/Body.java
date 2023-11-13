package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
public class Body {

    private List<OrdineDettaglioDto> list;
    private List<AccontoDto> accontoDtos;

}
