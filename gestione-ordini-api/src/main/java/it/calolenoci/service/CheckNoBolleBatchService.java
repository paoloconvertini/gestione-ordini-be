package it.calolenoci.service;

import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class CheckNoBolleBatchService {

    @Inject
    AuditService auditService;

    @Transactional
    public void processBatch(List<GoOrdineDettaglio> daReset) {

        for (GoOrdineDettaglio god : daReset) {

            OrdineDettaglio od = OrdineDettaglio.find(
                    "progrGenerale = ?1", god.getProgrGenerale()
            ).firstResult();

            if (od == null) continue;

            Boolean oldFlBolla = god.getFlBolla();
            Boolean oldFlagConsegnato = god.getFlagConsegnato();
            Double oldQtaDaConsegnare = god.getQtaDaConsegnare();

            Double newQtaDaConsegnare = od.getQuantita();

            god.setFlBolla(false);
            god.setFlagConsegnato(false);
            god.setQtaDaConsegnare(newQtaDaConsegnare);

            String entity = "GO_ORDINE_DETTAGLIO";

            if (!Objects.equals(oldFlBolla, god.getFlBolla())) {
                auditService.logChange(entity,
                        god.getAnno(),
                        god.getSerie(),
                        god.getProgressivo(),
                        god.getRigo(),
                        god.getProgrGenerale(),
                        "flBolla",
                        oldFlBolla,
                        god.getFlBolla(),
                        "checkNoBolle",
                        null);
            }

            if (!Objects.equals(oldFlagConsegnato, god.getFlagConsegnato())) {
                auditService.logChange(entity,
                        god.getAnno(),
                        god.getSerie(),
                        god.getProgressivo(),
                        god.getRigo(),
                        god.getProgrGenerale(),
                        "flagConsegnato",
                        oldFlagConsegnato,
                        god.getFlagConsegnato(),
                        "checkNoBolle",
                        null);
            }

            if (!Objects.equals(oldQtaDaConsegnare, god.getQtaDaConsegnare())) {
                auditService.logChange(entity,
                        god.getAnno(),
                        god.getSerie(),
                        god.getProgressivo(),
                        god.getRigo(),
                        god.getProgrGenerale(),
                        "qtaDaConsegnare",
                        oldQtaDaConsegnare,
                        god.getQtaDaConsegnare(),
                        "checkNoBolle",
                        null);
            }
        }

        GoOrdineDettaglio.persist(daReset);

        auditService.flush();
    }
}