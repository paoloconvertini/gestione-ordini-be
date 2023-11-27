package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteProgressivoDto implements Serializable {

    private Integer progressivo;

    private List<CespiteViewDto> cespiteViewDtoList;
}
