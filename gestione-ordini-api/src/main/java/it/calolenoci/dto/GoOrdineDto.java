package it.calolenoci.dto;

import it.calolenoci.entity.GoOrdine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link GoOrdine} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoOrdineDto implements Serializable {
    private GoOrdine go;
    private String saldoAcconto;
    private Boolean flProntoConsegna;

    public String creaId(){
        return this.go.getAnno() + this.go.getSerie() + this.go.getProgressivo();
    }

    public GoOrdineDto(GoOrdine go, String saldoAcconto) {
        this.go = go;
        this.saldoAcconto = saldoAcconto;
    }

    public GoOrdineDto(GoOrdine go, Boolean flProntoConsegna) {
        this.go = go;
        this.flProntoConsegna = flProntoConsegna;
    }
}
