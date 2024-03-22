package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private  String riferimento;
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

    private Double latitudine;

    private Double longitudine;

    private String sottoConto;

    private String contoFattura;

    private Boolean warnNoBolla = Boolean.FALSE;

    private Boolean locked= Boolean.FALSE;

    private String userLock;

    private Boolean hasFirma= Boolean.FALSE;

    private Boolean hasProntoConsegna = Boolean.FALSE;

    private String note;

    private String noteLogistica;

    private String modalitaPagamento;

    private Boolean hasCarico = Boolean.FALSE;

    private Integer veicolo;

    private Double importoRiservati = 0D;

    private LocalDate dataConsegna;

    private LocalDateTime dataNote;

    private LocalDateTime dataNoteLogistica;

    private String userNote;

    private String userNoteLogistica;

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataOrdine, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo, String localita, String cap, String provincia,
                     String statoResidenza, String statoEstero, String telefono, String cellulare, String email, String pec) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.riferimento = riferimento;
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
        this.sottoConto = sottoConto;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataOrdine, String numeroConferma,
                     String intestazione, String status, String sottoConto, Boolean warnNoBolla,
                     Boolean locked, String userLock, Boolean hasFirma, String note) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.status = status;
        this.sottoConto = sottoConto;
        this.warnNoBolla = warnNoBolla;
        this.locked = locked;
        this.userLock = userLock;
        this.hasFirma = hasFirma;
        this.note = note;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataOrdine, String numeroConferma, String modalitaPagamento,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataOrdine = dataOrdine;
        this.numeroConferma = numeroConferma;
        this.modalitaPagamento = modalitaPagamento;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
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

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, Double latitudine, Double longitudine, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma,
                     Boolean hasProntoConsegna, String note, String noteLogistica) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
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
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, Double latitudine, Double longitudine, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma,
                     Boolean hasProntoConsegna, String note, String noteLogistica, Integer veicolo, LocalDate dataConsegna) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
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
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.veicolo = veicolo;
        this.dataConsegna = dataConsegna;
    }

    //OrdineService findAllByStati
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, Double latitudine, Double longitudine, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma,
                     Boolean hasProntoConsegna, String note, String noteLogistica, Integer veicolo, LocalDate dataConsegna
            , LocalDateTime dataNote, String userNote, LocalDateTime dataNoteLogistica, String userNoteLogistica) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
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
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.veicolo = veicolo;
        this.dataConsegna = dataConsegna;
        this.dataNote = dataNote;
        this.userNote = userNote;
        this.dataNoteLogistica = dataNoteLogistica;
        this.userNoteLogistica = userNoteLogistica;
    }
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String status,
                     String note, String noteLogistica, Double importoRiservati) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.statoResidenza = statoResidenza;
        this.statoEstero = statoEstero;
        this.telefono = telefono;
        this.cellulare = cellulare;
        this.status = status;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.importoRiservati = importoRiservati;
    }

    //OrdineService findAllRiservati
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String status,
                     String note, String noteLogistica, Double importoRiservati, LocalDateTime dataNote, String userNote, LocalDateTime dataNoteLogistica, String userNoteLogistica) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.cap = cap;
        this.provincia = provincia;
        this.statoResidenza = statoResidenza;
        this.statoEstero = statoEstero;
        this.telefono = telefono;
        this.cellulare = cellulare;
        this.status = status;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.importoRiservati = importoRiservati;
        this.dataNote = dataNote;
        this.userNote = userNote;
        this.dataNoteLogistica = dataNoteLogistica;
        this.userNoteLogistica = userNoteLogistica;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma,
                     Boolean hasProntoConsegna, String note, String noteLogistica
            , Boolean hasCarico) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
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
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.hasCarico = hasCarico;
    }

    //Ordine service findAllByStatus
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String indirizzo,
                     String localita, String cap, String provincia, String statoResidenza, String statoEstero,
                     String telefono, String cellulare, String email, String pec, String status,
                     Boolean locked, String userLock, Boolean warnNoBolla, Boolean hasFirma,
                     Boolean hasProntoConsegna, String note, String noteLogistica
            , Boolean hasCarico, LocalDateTime dataNote, String userNote, LocalDateTime dataNoteLogistica, String userNoteLogistica) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
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
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.hasCarico = hasCarico;
        this.dataNote = dataNote;
        this.userNote = userNote;
        this.dataNoteLogistica = dataNoteLogistica;
        this.userNoteLogistica = userNoteLogistica;
    }

    public OrdineDTO(String intestazione, String localita, String provincia, String telefono, String email) {
        this.intestazione = intestazione;
        this.localita = localita;
        this.provincia = provincia;
        this.telefono = telefono;
        this.email = email;
    }

    public OrdineDTO(String intestazione, String localita, String provincia, String telefono, String email, String sottoConto) {
        this.intestazione = intestazione;
        this.localita = localita;
        this.provincia = provincia;
        this.telefono = telefono;
        this.email = email;
        this.sottoConto = sottoConto;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
    }

    //getAllPregressi
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String localita, String provincia,
                    String telefono, String cellulare) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.localita = localita;
        this.provincia = provincia;
        this.telefono = telefono;
        this.cellulare = cellulare;
    }

    //findAllByStati
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String intestazione, String sottoConto, String riferimento, String localita, String provincia,
                     String telefono, String cellulare, Integer veicolo, LocalDate dataConsegna) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.intestazione = intestazione;
        this.sottoConto = sottoConto;
        this.riferimento = riferimento;
        this.localita = localita;
        this.provincia = provincia;
        this.telefono = telefono;
        this.cellulare = cellulare;
        this.veicolo = veicolo;
        this.dataConsegna = dataConsegna;
    }

    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                      String status, Boolean hasProntoConsegna, String note, String noteLogistica,
                     String intestazione, String riferimento,
                     String localita, String provincia
                     ) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.status = status;
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.intestazione = intestazione;
        this.riferimento = riferimento;
        this.localita = localita;
        this.provincia = provincia;
    }


    //OrdineService findAltriOrdiniCliente
    public OrdineDTO(Integer anno, String serie, Integer progressivo, Date dataConferma, String numeroConferma,
                     String status, Boolean hasProntoConsegna, String note, String noteLogistica,
                     String intestazione, String riferimento,
                     String localita, String provincia, LocalDateTime dataNote, String userNote, LocalDateTime dataNoteLogistica, String userNoteLogistica
    ) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.dataConferma = dataConferma;
        this.numeroConferma = numeroConferma;
        this.status = status;
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.intestazione = intestazione;
        this.riferimento = riferimento;
        this.localita = localita;
        this.provincia = provincia;
        this.dataNote = dataNote;
        this.userNote = userNote;
        this.dataNoteLogistica = dataNoteLogistica;
        this.userNoteLogistica = userNoteLogistica;
    }
}
