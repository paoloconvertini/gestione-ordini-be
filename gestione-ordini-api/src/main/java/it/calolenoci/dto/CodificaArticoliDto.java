package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.OrdineId;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@ToString
public class CodificaArticoliDto implements Serializable {

    private Boolean showTCA;

    private List<String>errors;

}
