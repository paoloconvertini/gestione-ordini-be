package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * A DTO for the {@link it.calolenoci.entity.OrdineFornitoreDettaglio} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdineFornitoreDettaglioDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;

    private Integer progrGenerale;
    private Integer rigo;

    private String tipoRigo;

    private Integer pid;
    private String nota;
    private String oArticolo;
    private String oDescrArticolo;
    private Double oQuantita;
    private Double oPrezzo;
    private String oUnitaMisura;
    private Integer oColli;
    private String oCodiceIva;

    private Double scontoF1;

    private Double scontoF2;

    private Double fScontoP;

    private String noteOrdCli;

    public OrdineFornitoreDettaglioDto(Integer anno, String serie, Integer progressivo, Integer rigo, String nota, String oArticolo, String oDescrArticolo, Double oQuantita, Double oPrezzo, String oUnitaMisura) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
        this.nota = nota;
        this.oArticolo = oArticolo;
        this.oDescrArticolo = oDescrArticolo;
        this.oQuantita = oQuantita;
        this.oPrezzo = oPrezzo;
        this.oUnitaMisura = oUnitaMisura;
    }

    public OrdineFornitoreDettaglioDto(Integer anno, String serie, Integer progressivo, Integer rigo, String nota,
                                       String oArticolo, String oDescrArticolo, Double oQuantita, Double oPrezzo,
                                       String oUnitaMisura, Double scontoF1, Double scontoF2, Double fScontoP, String tipoRigo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
        this.nota = nota;
        this.oArticolo = oArticolo;
        this.oDescrArticolo = oDescrArticolo;
        this.oQuantita = oQuantita;
        this.oPrezzo = oPrezzo;
        this.oUnitaMisura = oUnitaMisura;
        this.scontoF1 = scontoF1;
        this.scontoF2 = scontoF2;
        this.fScontoP = fScontoP;
        this.tipoRigo = tipoRigo;
    }

    public OrdineFornitoreDettaglioDto(Integer anno, String serie, Integer progressivo, Integer rigo, String nota,
                                       String oArticolo, String oDescrArticolo, Double oQuantita, Double oPrezzo,
                                       String oUnitaMisura, Double scontoF1, Double scontoF2, Double fScontoP,
                                       String tipoRigo, String noteOrdCli) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
        this.nota = nota;
        this.oArticolo = oArticolo;
        this.oDescrArticolo = oDescrArticolo;
        this.oQuantita = oQuantita;
        this.oPrezzo = oPrezzo;
        this.oUnitaMisura = oUnitaMisura;
        this.scontoF1 = scontoF1;
        this.scontoF2 = scontoF2;
        this.fScontoP = fScontoP;
        this.tipoRigo = tipoRigo;
        this.noteOrdCli = noteOrdCli;
    }
}