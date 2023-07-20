package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.OrdineFornitore} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdineFornitoreDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer gruppo;
    private String conto;
    private Date dataOrdine;
    private Date dataConfOrdine;

    private Date updateDate;
    private String numConfOrdine;
    private String provvisorio;

    private String intestazione;

    private Boolean flInviato;

    private String note;

    private Date dataInvio;

    private String nota;

    private String articolo;

    private String descrArticolo;

    private Double quantita;

    private Double prezzo;

    private String unitaMisura;

    private String codiceIva;

    private Double scontoArticolo;

    private Double scontoF1;

    private Double scontoF2;

    private Double scontoP;

    private String descrBanca;

    private String abiBanca;

    private String codice;

    private String descrizione;

    private String campoUser5;

    private String user;

    private String descrArtSuppl;

    private String indirizzo;

    private String localita;

    private String cap;

    private String provincia;

    private String telefono;

    private String fax;

    private String tipoRigo;

    private Double valoreTotale;

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, String conto) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.conto = conto;
    }

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, Date dataOrdine, String intestazione,
                              Date dataConfOrdine, String numConfOrdine, String provvisorio, Date updateDate, String note) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.intestazione = intestazione;
        this.dataOrdine = dataOrdine;
        this.dataConfOrdine = dataConfOrdine;
        this.numConfOrdine = numConfOrdine;
        this.provvisorio = provvisorio;
        this.updateDate = updateDate;
        this.note = note;
    }

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, Date dataOrdine, String intestazione,
                              Date dataConfOrdine, String numConfOrdine, String provvisorio, Date updateDate,String note,
                              Boolean flInviato, Date dataInvio) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.intestazione = intestazione;
        this.dataOrdine = dataOrdine;
        this.dataConfOrdine = dataConfOrdine;
        this.numConfOrdine = numConfOrdine;
        this.provvisorio = provvisorio;
        this.updateDate = updateDate;
        this.note = note;
        this.flInviato = flInviato;
        this.dataInvio = dataInvio;
    }

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, Integer gruppo, String conto, String intestazione) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.gruppo = gruppo;
        this.conto = conto;
        this.intestazione = intestazione;
    }

    public OrdineFornitoreDto(Integer anno, String serie, Integer progressivo, Date dataOrdine, String numConfOrdine, Date dataConfOrdine, String articolo, String descrArticolo,
    String campoUser5, String nota, Double prezzo, String unitaMisura, Double quantita,
     Double scontoArticolo, Double scontoF1, Double scontoF2, Double scontoP, String codiceIva,
                              String descrBanca, Double abiBanca, String codice, String descrizione, String user, String descrArtSuppl,
                              String intestazione, String telefono, String fax, String indirizzo, String localita, String cap, String provincia, String tipoRigo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.numConfOrdine = numConfOrdine;
        this.dataConfOrdine = dataConfOrdine;
        this.articolo = articolo;
        this.descrArticolo = descrArticolo;
        this.campoUser5 = campoUser5;
        this.nota = nota;
        this.prezzo = prezzo;
        this.unitaMisura = unitaMisura;
        this.quantita = quantita;
        this.scontoArticolo = scontoArticolo;
        this.scontoF1 = scontoF1;
        this.scontoF2 = scontoF2;
        this.scontoP = scontoP;
        this.codiceIva = codiceIva;
        this.descrBanca = descrBanca;
        this.abiBanca = String.format("%.0f", abiBanca);
        this.codice = codice;
        this.descrizione = descrizione;
        this.user = user;
        this.descrArtSuppl = descrArtSuppl;
        this.intestazione = intestazione;
        this.telefono = telefono;
        this.fax = fax;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.tipoRigo = tipoRigo;
    }
}