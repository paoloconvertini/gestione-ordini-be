package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@ToString
public class OrdineClienteDto {

    private OrdineDettaglio ordineDettaglio;

    private GoOrdineDettaglio goOrdineDettaglio;

}
