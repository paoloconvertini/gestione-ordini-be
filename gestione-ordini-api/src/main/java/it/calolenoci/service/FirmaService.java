package it.calolenoci.service;

import it.calolenoci.entity.FirmaOrdineCliente;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirmaService {


    public void save(Integer anno, String serie, Integer progressivo, String filename) {
        FirmaOrdineCliente firmaOrdineCliente = new FirmaOrdineCliente();
        firmaOrdineCliente.setAnno(anno);
        firmaOrdineCliente.setSerie(serie);
        firmaOrdineCliente.setProgressivo(progressivo);
        firmaOrdineCliente.setFileName(filename);
        firmaOrdineCliente.persist();
    }

}
