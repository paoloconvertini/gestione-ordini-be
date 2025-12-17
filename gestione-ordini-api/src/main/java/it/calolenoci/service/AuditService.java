package it.calolenoci.service;

import it.calolenoci.entity.Audit;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

@ApplicationScoped
public class AuditService {

    public void logChange(
            String entityName,
            Integer anno,
            String serie,
            Integer progressivo,
            Integer rigo,
            Integer progrGenerale,
            String fieldName,
            Object oldVal,
            Object newVal,
            String source,
            String note
    ) {
        Audit a = new Audit();
        a.setEntityName(entityName);
        a.setAnno(anno);
        a.setSerie(serie);
        a.setProgressivo(progressivo);
        a.setRigo(rigo);
        a.setProgrGenerale(progrGenerale);
        a.setFieldName(fieldName);
        a.setOldValue(oldVal != null ? oldVal.toString() : null);
        a.setNewValue(newVal != null ? newVal.toString() : null);
        a.setActionType("UPDATE");
        a.setOperationSource(source);
        a.setNote(note);
        a.setCreateDate(new Date());
        a.persist();
    }
}

