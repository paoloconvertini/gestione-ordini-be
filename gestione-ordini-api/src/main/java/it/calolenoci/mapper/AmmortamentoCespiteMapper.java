package it.calolenoci.mapper;

import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.service.AmmortamentoCespiteService;
import net.bytebuddy.asm.Advice;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.calolenoci.service.AmmortamentoCespiteService.df;

@ApplicationScoped
public class AmmortamentoCespiteMapper {
    public AmmortamentoCespite buildAmmortamento(String id, double perc, double ammortamentoAnnuo, double ammortamentoCumulativo, double valoreResiduo, int anno, LocalDate dataCorrente) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(id);
        a.setDataAmm(Objects.requireNonNullElseGet(dataCorrente, () -> LocalDate.of(anno, 12, 31)));
        a.setDescrizione("Ammortamento ordinario deducibile");
        a.setPercAmm(perc);
        a.setFondo(ammortamentoCumulativo);
        a.setQuota(ammortamentoAnnuo);
        a.setResiduo(valoreResiduo);
        a.setAnno(anno);
        return a;
    }

    public AmmortamentoCespite buildEliminato(String id, LocalDate dataVendita){
        AmmortamentoCespite ammEliminato = new AmmortamentoCespite();
        ammEliminato.setIdAmmortamento(id);
        ammEliminato.setDescrizione("ELIMINAZIONE CESPITE");
        ammEliminato.setDataAmm(dataVendita);
        return ammEliminato;
    }

    public List<AmmortamentoCespite> buildVenduto(Cespite cespite, Double residuo){
        List<AmmortamentoCespite> list = new ArrayList<>();
        AmmortamentoCespite ammVenduto = new AmmortamentoCespite();
        ammVenduto.setIdAmmortamento(cespite.getId());
        ammVenduto.setDescrizione("Vendita CESPITE");
        ammVenduto.setDataAmm(cespite.getDataVendita());
        AmmortamentoCespite ammVenduto1 = new AmmortamentoCespite();
        ammVenduto1.setDescrizione("venduto a " + cespite.getIntestatarioVendita());
        ammVenduto1.setQuota(cespite.getImportoVendita());
        AmmortamentoCespite ammVenduto2 = new AmmortamentoCespite();
        ammVenduto2.setDescrizione("Plus/Minus valenza");
        ammVenduto2.setQuota(cespite.getImportoVendita() - residuo);
        list.add(ammVenduto);
        list.add(ammVenduto1);
        list.add(ammVenduto2);
        return list;
    }

}

