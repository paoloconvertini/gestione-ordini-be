package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiscaleRiepilogoDto {


    String tipoCespite;
    Double valoreAggiornato = 0D;
    Double ammortamentoOrdinario = 0D;
    Double ammortamentoAnticipato = 0D;
    Double totaleAmmortamento = 0D;
    Double nonAmmortabile = 0D;
    Double fondoAmmortamenti = 0D;
    Double fondoAmmortamentiRiv = 0D;
    Double residuo = 0D;

    public FiscaleRiepilogoDto(String tipoCespite, Double valoreAggiornato) {
        this.tipoCespite = tipoCespite;
        this.valoreAggiornato = valoreAggiornato;
    }


}
