package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.FatturaDto;
import it.calolenoci.entity.FattureDettaglio;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class FatturaService {

    @Inject
    EntityManager em;

    public List<FatturaDto> getBolle(){
        return FattureDettaglio
                .find("Select f.progrOrdCli, SUM(f.quantita) as qta " +
                        "FROM FattureDettaglio f " +
                        "WHERE f.progrOrdCli <> 0 GROUP BY f.progrOrdCli")
                .project(FatturaDto.class)
                .list();
    }

    public List<FatturaDto> getBolle(Integer progrCliente){
        return FattureDettaglio
                .find("Select f.numeroBolla, f.dataBolla, f2.quantita as qta FROM FattureDettaglio f2 " +
                        "INNER JOIN Fatture f ON f.anno = f2.anno and f.serie = f2.serie and f.progressivo = f2.progressivo " +
                        " WHERE f2.progrOrdCli = :progCliente", Parameters.with("progCliente", progrCliente))
                .project(FatturaDto.class)
                .list();
    }

    public List<AccontoDto> getAcconti(String sottoConto) {
        return em.createNamedQuery("AccontoDto").setParameter("sottoConto", sottoConto).getResultList();
    }
}
