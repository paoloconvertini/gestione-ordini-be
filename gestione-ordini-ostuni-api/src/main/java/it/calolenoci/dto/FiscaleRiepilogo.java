package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiscaleRiepilogo {


    Double valoreAggiornato = 0D;
    Double ammortamentoOrdinario = 0D;
    Double ammortamentoAnticipato = 0D;
    Double totaleAmmortamento = 0D;
    Double nonAmmortabile = 0D;
    Double fondoAmmortamenti = 0D;
    Double residuo = 0D;

}
