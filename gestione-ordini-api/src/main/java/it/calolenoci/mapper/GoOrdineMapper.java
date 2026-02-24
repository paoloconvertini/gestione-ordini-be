package it.calolenoci.mapper;

import it.calolenoci.dto.GoOrdineDto;
import it.calolenoci.entity.Ordine;
import it.calolenoci.entity.GoOrdine;
import it.calolenoci.enums.StatoOrdineEnum;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;


@ApplicationScoped
public class GoOrdineMapper {

    public GoOrdine fromOrdineToGoOrdine (Ordine o) {
        GoOrdine go = new GoOrdine();
        go.setAnno(o.getAnno());
        go.setSerie(o.getSerie());
        go.setProgressivo(o.getProgressivo());
        go.setStatus(StatoOrdineEnum.DA_PROCESSARE.getDescrizione());
        go.setLocked(Boolean.FALSE);
        go.setHasFirma(Boolean.FALSE);
        go.setWarnNoBolla(Boolean.FALSE);
        go.setHasCarico(Boolean.FALSE);
        return go;
    }

    public GoOrdine fromDtoToEntity(GoOrdineDto dto) {
        GoOrdine ordine = new GoOrdine();
       ordine.setAnno(dto.getAnno());
       ordine.setSerie(dto.getSerie());
       ordine.setProgressivo(dto.getProgressivo());
       ordine.setStatus(dto.getStatus());
       ordine.setWarnNoBolla(dto.getWarnNoBolla());
       ordine.setLocked(dto.getLocked());
       ordine.setUserLock(dto.getUserLock());
       ordine.setHasFirma(dto.getHasFirma());
       ordine.setHasProntoConsegna(dto.getHasProntoConsegna());
       ordine.setNote(dto.getNote());
       ordine.setNoteLogistica(dto.getNoteLogistica());
       ordine.setHasCarico(dto.getHasCarico());
       ordine.setDataNote(dto.getDataNote());
       ordine.setUserNote(dto.getUserNote());
       ordine.setDataNoteLogistica(dto.getDataNoteLogistica());
       ordine.setUserNoteLogistica(dto.getUserNoteLogistica());
       return ordine;
    }
}
