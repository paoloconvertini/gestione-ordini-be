package it.calolenoci.dto;

import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteDto implements Serializable {

    private String tipoCespite;

    private String categoria;

    private List<CespiteProgressivoDto> cespiteProgressivoDtoList;
}
