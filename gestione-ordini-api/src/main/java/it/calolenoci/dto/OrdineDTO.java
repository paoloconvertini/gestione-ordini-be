package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@RegisterForReflection
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrdineDTO implements Serializable {

    private  Integer anno;
    private  String serie;
    private  Integer progressivo;
    private Date dataOrdine;
    private  String numeroConferma;
    private  String tipoFattura;
    private  Date dataConferma;
    private  String intestazione;
    private  String continuaIntest;
    private  String indirizzo;
    private  String localita;
    private  String cap;
    private  String provincia;
    private  String statoResidenza;
    private  String statoEstero;
    private  String telefono;
    private  String cellulare;
    private  String email;
    private  String pec;
    private String status;

    private String sottoConto;

    private Boolean warnNoBolla;

    private Boolean locked;

    private String userLock;

    private Boolean hasFirma;

    private String note;


    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataOrdine, String numeroConferma,
                     String intestazione, String sottoConto, String continuaIntest, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.continuaIntest = continuaIntest;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.statoResidenza = statoResidenza;
        this.statoEstero = statoEstero;
        this.telefono = telefono;
        this.cellulare = cellulare;
        this.email = email;
        this.pec = pec;
        this.status = status;
        this.locked = locked;
        this.userLock = userLock;
        this.warnNoBolla = warnNoBolla;
    }


    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataOrdine, String numeroConferma,
                     String intestazione, String sottoConto, String continuaIntest, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma, String note) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.continuaIntest = continuaIntest;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.statoResidenza = statoResidenza;
        this.statoEstero = statoEstero;
        this.telefono = telefono;
        this.cellulare = cellulare;
        this.email = email;
        this.pec = pec;
        this.status = status;
        this.locked = locked;
        this.userLock = userLock;
        this.warnNoBolla = warnNoBolla;
        this.hasFirma = hasFirma;
        this.note = note;
    }

    public OrdineDTO(String intestazione, String localita, String provincia, String telefono, String email) {
        this.intestazione = intestazione;
        this.localita = localita;
        this.provincia = provincia;
        this.telefono = telefono;
        this.email = email;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
    }
}
