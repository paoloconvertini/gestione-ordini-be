package it.calolenoci.mapper;

import it.calolenoci.dto.RegistroCespiteDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.entity.Cespite;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AmmortamentoCespiteMapper {
    public AmmortamentoCespite buildAmmortamento(String id, double perc, double quota, double quotaRivalutazione, double fondo, double fondoRivalutazione, double valoreResiduo, LocalDate dataCorrente) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(id);
        a.setDataAmm(dataCorrente);
        a.setDescrizione("Ammortamento ordinario deducibile");
        a.setPercAmm(Math.round(perc * 100.0) / 100.0);
        a.setFondo(Math.round(fondo * 100.0) / 100.0);
        a.setFondoRivalutazione(Math.round(fondoRivalutazione * 100.0) / 100.0);
        a.setQuota(Math.round(quota* 100.0) / 100.0);
        a.setQuotaRivalutazione(Math.round(quotaRivalutazione * 100.0) / 100.0);
        a.setResiduo(Math.round(valoreResiduo * 100.0) / 100.0);
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

    public List<AmmortamentoCespite> buildVenduto(RegistroCespiteDto cespite, Double residuo){
        int anno = cespite.getDataVendita().getYear();
        List<AmmortamentoCespite> list = new ArrayList<>();
        AmmortamentoCespite ammVenduto = new AmmortamentoCespite();
        ammVenduto.setIdAmmortamento(cespite.getId());
        ammVenduto.setDescrizione("VENDITA CESPITE");
        ammVenduto.setDataAmm(cespite.getDataVendita());
        ammVenduto.setAnno(anno);
        AmmortamentoCespite ammVenduto1 = new AmmortamentoCespite();
        ammVenduto1.setIdAmmortamento(cespite.getId());
        ammVenduto1.setDescrizione("venduto a " + cespite.getIntestatarioVendita() + " n. fatt.: " + cespite.getNumDocVend());
        ammVenduto1.setQuota(Math.round(cespite.getImportoVendita() * 100.0) / 100.0);
        ammVenduto1.setAnno(anno);
        AmmortamentoCespite ammVenduto2 = new AmmortamentoCespite();
        ammVenduto2.setIdAmmortamento(cespite.getId());
        ammVenduto2.setDescrizione("Plus/Minus valenza");
        ammVenduto2.setQuota(Math.round((cespite.getImportoVendita() - residuo) * 100.0) / 100.0);
        ammVenduto2.setAnno(anno);
        list.add(ammVenduto);
        list.add(ammVenduto1);
        list.add(ammVenduto2);
        return list;
    }

    public AmmortamentoCespite buildAmmortamento(RegistroCespiteDto d) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(d.getIdAmmortamento());
        a.setDataAmm(d.getDataAmm());
        a.setDescrizione(d.getDescrAmm());
        a.setPercAmm(Math.round(d.getPercAmm() * 100.0) / 100.0);
        a.setFondo(Math.round(d.getFondo() * 100.0) / 100.0);
        a.setFondoRivalutazione(Math.round(d.getFondoRivAmm() * 100.0) / 100.0);
        a.setQuota(Math.round(d.getQuota() * 100.0) / 100.0);
        a.setQuotaRivalutazione(Math.round(d.getQuotaRivalutazione() * 100.0) / 100.0);
        a.setResiduo(Math.round(d.getResiduo() * 100.0) / 100.0);
        a.setAnno(d.getAnnoAmm());
        a.setSuperQuota(d.getQuotaSuper());
        a.setSuperPercentuale(d.getPercSuper());
        return a;
    }

    public RegistroCespiteDto fromCespiteToRegistroCespiteDto(Cespite c, CategoriaCespite tipo) {
        RegistroCespiteDto d = new RegistroCespiteDto();
        d.setCespite(c.getCespite());
        d.setId(c.getId());
        d.setProgressivo1(c.getProgressivo1());
        d.setProgressivo2(c.getProgressivo2());
        d.setDataAcq(c.getDataAcq());
        d.setProtocollo(c.getProtocollo());
        d.setGiornale(c.getGiornale());
        d.setAnno(c.getAnno());
        d.setFlPrimoAnno(c.getFlPrimoAnno());
        d.setTipoCespite(c.getTipoCespite());
        d.setImporto(c.getImporto());
        d.setDataAcq(c.getDataAcq());
        d.setNumDocAcq(c.getNumDocAcq());
        d.setPercAmmortamento(tipo.getPercAmmortamento());
        d.setImporto(c.getImporto());
        d.setImportoRivalutazione(c.getImportoRivalutazione());
        d.setFondoRivalutazione(c.getFondoRivalutazione());
        return d;
    }
}

