package it.calolenoci;

import io.quarkus.runtime.annotations.RegisterForReflection;
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
public class FornitoreAlternativoDto {

    private Integer gruppo;

    private String conto;

}
