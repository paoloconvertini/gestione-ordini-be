package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link it.calolenoci.entity.PianoConti} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PianoContiDto implements Serializable {
    private Integer gruppoConto;
    private String sottoConto;
    private String intestazione;
    private String codiceArticolo;

    public PianoContiDto(Integer gruppoConto, String sottoConto, String intestazione) {
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.intestazione = intestazione;
    }
}