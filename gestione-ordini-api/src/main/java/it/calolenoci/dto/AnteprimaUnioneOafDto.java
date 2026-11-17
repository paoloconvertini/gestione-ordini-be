package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AnteprimaUnioneOafDto {
    private List<OrdineFornitoreDto> ordini;
    private boolean fornitoriDiversi;
    private String versione;
}
