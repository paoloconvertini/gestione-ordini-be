package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link it.calolenoci.entity.BoxDoccia} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class BoxDocciaDto implements Serializable {
    private String id;
    private String codice;
    private String descrizione;
    private String profilo;
    private String estensibilita;
    private String versione;
    private Integer qta;
    private Double prezzo;
    private String extra;
    private String foto;
    private String posa;

    public BoxDocciaDto(String id, String codice, String descrizione,
                        String profilo, String estensibilita, String versione) {
        this.id = id;
        this.codice = codice;
        this.descrizione = descrizione;
        this.profilo = profilo;
        this.estensibilita = estensibilita;
        this.versione = versione;
    }

    public BoxDocciaDto(String profilo, String estensibilita, String versione, Integer qta, Double prezzo, String extra, String foto, String posa) {
        this.profilo = profilo;
        this.estensibilita = estensibilita;
        this.versione = versione;
        this.qta = qta;
        this.prezzo = prezzo;
        this.extra = extra;
        this.foto = foto;
        this.posa = posa;
    }
}