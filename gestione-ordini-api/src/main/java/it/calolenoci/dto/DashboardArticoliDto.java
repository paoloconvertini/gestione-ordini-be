package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardArticoliDto {
    private List<OrdineNotaDaOrdinareDto> righe;
    private long totaleOrdini;
}
