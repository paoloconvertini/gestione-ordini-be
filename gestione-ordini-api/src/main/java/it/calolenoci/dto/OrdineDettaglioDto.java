package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class OrdineDettaglioDto implements Serializable {
    private  Integer anno;
    private  Integer progressivo;

    private  Integer progrGenerale;
    private  String tipoRigo;
    private  Integer rigo;
    private  String serie;
    private  String fArticolo;
    private  String codArtFornitore;
    private  String fDescrArticolo;
    private  Double quantita;
    private  Float prezzo;
    private  String fUnitaMisura;

    private Float scontoArticolo;

    private Float scontoC1;

    private Float scontoC2;

    private Float scontoP;

    private String fCodiceIva;

    private Integer fColli;
    private  Boolean geFlagRiservato;
    private  Boolean geFlagNonDisponibile;
    private  Boolean geFlagOrdinato;
    private  Boolean geFlagConsegnato;
    private  String geTono;

    private Integer annoOAF;

    private String serieOAF;

    private Integer progressivoOAF;

    private String numeroBolla;

    private Date dataBolla;

    private Date dataOrdineOAF;

    private String articolo;

    private Double qtaDaConsegnare;

    private Double qtaConsegnatoSenzaBolla;

    private Boolean flBolla;

    private String intestazione;

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, Float prezzo, String fUnitaMisura,
                              Boolean geFlagRiservato, Boolean geFlagNonDisponibile, Boolean geFlagOrdinato, Boolean geFlagConsegnato,
                              String geTono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.geFlagRiservato = geFlagRiservato;
        this.geFlagNonDisponibile = geFlagNonDisponibile;
        this.geFlagOrdinato = geFlagOrdinato;
        this.geFlagConsegnato = geFlagConsegnato;
        this.geTono = geTono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
    }

    public OrdineDettaglioDto(Integer anno, Integer progressivo, Integer progrGenerale, String tipoRigo, Integer rigo, String serie, String fArticolo,
                              String codArtFornitore, String fDescrArticolo, Double quantita, Float prezzo, String fUnitaMisura,
                              Boolean geFlagRiservato, Boolean geFlagNonDisponibile, Boolean geFlagOrdinato, Boolean geFlagConsegnato,
                              String geTono, String articolo, Integer annoOAF, String serieOAF, Integer progressivoOAF,
                              Date dataOrdineOAF, Double qtaConsegnatoSenzaBolla, Double qtaDaConsegnare, Boolean flBolla, String intestazione) {
        this.anno = anno;
        this.progressivo = progressivo;
        this.progrGenerale = progrGenerale;
        this.tipoRigo = tipoRigo;
        this.rigo = rigo;
        this.serie = serie;
        this.fArticolo = fArticolo;
        this.codArtFornitore = codArtFornitore;
        this.fDescrArticolo = fDescrArticolo;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.fUnitaMisura = fUnitaMisura;
        this.geFlagRiservato = geFlagRiservato;
        this.geFlagNonDisponibile = geFlagNonDisponibile;
        this.geFlagOrdinato = geFlagOrdinato;
        this.geFlagConsegnato = geFlagConsegnato;
        this.geTono = geTono;
        this.articolo = articolo;
        this.annoOAF = annoOAF;
        this.serieOAF = serieOAF;
        this.progressivoOAF = progressivoOAF;
        this.dataOrdineOAF = dataOrdineOAF;
        this.qtaConsegnatoSenzaBolla = qtaConsegnatoSenzaBolla;
        this.qtaDaConsegnare = qtaDaConsegnare;
        this.flBolla = flBolla;
        this.intestazione = intestazione;
    }

    public OrdineDettaglioDto (Integer anno, String serie, Integer progressivo, Integer rigo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
    }
}
