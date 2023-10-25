package it.calolenoci.mapper;

import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

@ApplicationScoped
public class AmmortamentoCespiteMapper {
    public AmmortamentoCespite buildAmmortamento(String id, double perc, double ammortamentoAnnuo, double ammortamentoCumulativo, double valoreResiduo, int anno, LocalDate dataCorrente) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(id);
        a.setDataAmm(Date.valueOf(Objects.requireNonNullElseGet(dataCorrente, () -> LocalDate.of(anno, 12, 31))));
        a.setDescrizione("Ammortamento ordinario deducibile");
        a.setPercAmm(perc);
        a.setFondo(ammortamentoCumulativo);
        a.setQuota(ammortamentoAnnuo);
        a.setResiduo(valoreResiduo);
        a.setAnno(anno);
        return a;
    }

}

