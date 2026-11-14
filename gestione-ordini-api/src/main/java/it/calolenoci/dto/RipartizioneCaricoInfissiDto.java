package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class RipartizioneCaricoInfissiDto {
    private BigDecimal totaleImponibile;
    private BigDecimal quantitaTotale;
    private BigDecimal costoUnitario;
    private List<Riga> righe;

    @Data
    @AllArgsConstructor
    public static class Riga {
        private Integer progrGenerale;
        private Integer rigo;
        private String articolo;
        private String descrizione;
        private BigDecimal quantita;
        private BigDecimal imponibile;
    }
}
