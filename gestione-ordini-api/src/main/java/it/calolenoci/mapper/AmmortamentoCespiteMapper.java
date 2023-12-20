package it.calolenoci.mapper;

import it.calolenoci.dto.RegistroCespiteDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AmmortamentoCespiteMapper {
    public AmmortamentoCespite buildAmmortamento(String id, double perc, double ammortamentoAnnuo, double ammortamentoCumulativo, double valoreResiduo, LocalDate dataCorrente) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(id);
        a.setDataAmm(dataCorrente);
        a.setDescrizione("Ammortamento ordinario deducibile");
        a.setPercAmm(perc);
        a.setFondo(ammortamentoCumulativo);
        a.setQuota(ammortamentoAnnuo);
        a.setResiduo(valoreResiduo);
        a.setAnno(dataCorrente.getYear());
        return a;
    }

    public AmmortamentoCespite buildEliminato(String id, LocalDate dataVendita){
        AmmortamentoCespite ammEliminato = new AmmortamentoCespite();
        ammEliminato.setIdAmmortamento(id);
        ammEliminato.setDescrizione("ELIMINAZIONE CESPITE");
        ammEliminato.setDataAmm(dataVendita);
        int anno = dataVendita.getYear();
        ammEliminato.setAnno(anno);
        return ammEliminato;
    }

    public List<AmmortamentoCespite> buildVenduto(Cespite cespite, Double residuo){
        int anno = cespite.getDataVendita().getYear();
        List<AmmortamentoCespite> list = new ArrayList<>();
        AmmortamentoCespite ammVenduto = new AmmortamentoCespite();
        ammVenduto.setIdAmmortamento(cespite.getId());
        ammVenduto.setDescrizione("VENDITA CESPITE");
        ammVenduto.setDataAmm(cespite.getDataVendita());
        ammVenduto.setAnno(anno);
        AmmortamentoCespite ammVenduto1 = new AmmortamentoCespite();
        ammVenduto1.setIdAmmortamento(cespite.getId());
        ammVenduto1.setDescrizione("venduto a " + cespite.getIntestatarioVendita() + " n. fatt.: " + cespite.getNumDocVendita());
        ammVenduto1.setQuota(cespite.getImportoVendita());
        ammVenduto1.setAnno(anno);
        AmmortamentoCespite ammVenduto2 = new AmmortamentoCespite();
        ammVenduto2.setIdAmmortamento(cespite.getId());
        ammVenduto2.setDescrizione("Plus/Minus valenza");
        ammVenduto2.setQuota(cespite.getImportoVendita() - residuo);
        ammVenduto2.setAnno(anno);
        list.add(ammVenduto);
        list.add(ammVenduto1);
        list.add(ammVenduto2);
        return list;
    }

    public AmmortamentoCespite buildAmmortamento(RegistroCespiteDto d) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        //a.setIdAmmortamento(id);
        a.setDataAmm(d.getDataAmm());
        a.setDescrizione(d.getDescrAmm());
        a.setPercAmm(d.getPercAmm());
        a.setFondo(d.getFondo());
        a.setQuota(d.getQuota());
        a.setResiduo(d.getResiduo());
        a.setAnno(d.getAnnoAmm());
        a.setSuperQuota(d.getQuotaSuper());
        a.setSuperPercentuale(d.getPercSuper());
        return a;
    }
}

