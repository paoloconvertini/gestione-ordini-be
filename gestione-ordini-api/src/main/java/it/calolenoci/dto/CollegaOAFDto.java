package it.calolenoci.dto;

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
public class CollegaOAFDto {

    private Integer anno;

    private Integer progressivo;

    private String serie;

    private Integer annoOAF;

    private Integer progressivoOAF;

    private String serieOAF;

    private Integer rigo;

    private String codice;

    private String descrArtSuppl;

    private Integer progrGenerale;

    private Double qta;

}
