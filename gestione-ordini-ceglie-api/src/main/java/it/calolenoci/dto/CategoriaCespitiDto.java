package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaCespitiDto implements Serializable {

    private String tipoCespite;

    private String categoria;

    private Double perc;

    private CespiteSommaDto somma;

    private List<CespiteProgressivoDto> cespiteProgressivoDtoList;
}
