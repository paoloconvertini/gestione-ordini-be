package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.entity.FirmaOrdineCliente;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class FirmaService {

    @Transactional
    public void save(Integer anno, String serie, Integer progressivo, String filename) {
        FirmaOrdineCliente firmaOrdineCliente = FirmaOrdineCliente.findById(anno, serie, progressivo);
        if(firmaOrdineCliente == null) {
            firmaOrdineCliente = new FirmaOrdineCliente();
        }
        firmaOrdineCliente.setAnno(anno);
        firmaOrdineCliente.setSerie(serie);
        firmaOrdineCliente.setProgressivo(progressivo);
        firmaOrdineCliente.setFileName(filename);
        firmaOrdineCliente.persist();
    }

}
