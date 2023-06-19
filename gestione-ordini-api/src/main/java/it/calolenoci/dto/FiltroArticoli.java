package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FiltroArticoli implements Serializable {

    private Boolean flNonDisponibile;

    private Integer flConsegna;

    private Boolean flDaRiservare;

    private Boolean view;

    private Integer anno;

    private String serie;

    private Integer progressivo;

    public FiltroArticoli(Integer anno, String serie, Integer progressivo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
    }

    public FiltroArticoli(Integer anno, String serie, Integer progressivo, Integer flConsegna) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.flConsegna = flConsegna;
    }
}
