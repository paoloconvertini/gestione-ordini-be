package it.calolenoci.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CaricoInfissiDto extends CaricoMagazzinoDto {
    private BigDecimal totaleImponibile;
}
