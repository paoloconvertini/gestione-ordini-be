package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineId;
import lombok.*;
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
public class OrdineClienteDto {

    private OrdineDettaglio ordineDettaglio;

    private GoOrdineDettaglio goOrdineDettaglio;

}
