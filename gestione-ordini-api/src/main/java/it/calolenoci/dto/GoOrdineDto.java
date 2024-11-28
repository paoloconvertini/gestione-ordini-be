package it.calolenoci.dto;

import it.calolenoci.entity.GoOrdine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A DTO for the {@link GoOrdine} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoOrdineDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String status;
    private Boolean warnNoBolla;
    private Boolean locked;
    private String userLock;
    private Boolean hasFirma;
    private Boolean hasProntoConsegna;
    private String note;
    private String noteLogistica;
    private Boolean hasCarico;
    private LocalDateTime dataNote;
    private String userNote;
    private LocalDateTime dataNoteLogistica;
    private String userNoteLogistica;
    private String saldoAcconto;
    private Boolean flProntoConsegna;

    public GoOrdineDto(Integer anno, String serie, Integer progressivo, String status, Boolean warnNoBolla, Boolean locked, String userLock, Boolean hasFirma, Boolean hasProntoConsegna, String note, String noteLogistica, Boolean hasCarico, LocalDateTime dataNote, String userNote, LocalDateTime dataNoteLogistica, String userNoteLogistica, String saldoAcconto) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.status = status;
        this.warnNoBolla = warnNoBolla;
        this.locked = locked;
        this.userLock = userLock;
        this.hasFirma = hasFirma;
        this.hasProntoConsegna = hasProntoConsegna;
        this.note = note;
        this.noteLogistica = noteLogistica;
        this.hasCarico = hasCarico;
        this.dataNote = dataNote;
        this.userNote = userNote;
        this.dataNoteLogistica = dataNoteLogistica;
        this.userNoteLogistica = userNoteLogistica;
        this.saldoAcconto = saldoAcconto;
    }

    public GoOrdineDto(Integer anno, String serie, Integer progressivo, String status) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.status = status;
    }

    public GoOrdineDto(Integer anno, String serie, Integer progressivo, String status, Boolean hasProntoConsegna) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.status = status;
        this.hasProntoConsegna = hasProntoConsegna;
    }


}
