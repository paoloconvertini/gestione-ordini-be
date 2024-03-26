package it.calolenoci.dto;

import it.calolenoci.entity.PianoConti;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link PianoConti} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PianoContiDto implements Serializable {
    private Integer gruppoConto;
    private String sottoConto;
    private String intestazione;
    private String codiceArticolo;
    private String indirizzo;
    private String localita;
    private String cap;
    private String provincia;
    private Double latitudine;
    private Double longitudine;

    public PianoContiDto(Integer gruppoConto, String sottoConto, String indirizzo, String localita, String cap) {
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
    }

    public PianoContiDto(Integer gruppoConto, String sottoConto, String intestazione) {
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.intestazione = intestazione;
    }

    public PianoContiDto(Integer gruppoConto, String sottoConto, String intestazione,
                         String indirizzo, String localita, String cap, String provincia,
                         Double latitudine, Double longitudine) {
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.intestazione = intestazione;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }
}