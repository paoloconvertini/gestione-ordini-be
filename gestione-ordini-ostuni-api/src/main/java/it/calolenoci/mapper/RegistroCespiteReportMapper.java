package it.calolenoci.mapper;

import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RegistroCespiteReportMapper {


    public RegistroCespiteReportDto buildRegistroCespiteReport(RegistroCespitiDto d) {
        RegistroCespiteReportDto r = new RegistroCespiteReportDto();
        CespiteSommaDto c = d.getCespiteSommaDto();
        List<CategoriaCespitiDto> cespiteList = d.getCespiteList();

        FiscaleRiepilogoDto i = c.getInizioEsercizio();
        FiscaleRiepilogoDto a = c.getAcquisti();
        FiscaleRiepilogoDto v = c.getVendite();
        FiscaleRiepilogoDto am = c.getAmmortamentiDeducibili();
        FiscaleRiepilogoDto n = c.getAmmortamentiNonDeducibili();
        FiscaleRiepilogoDto f = c.getFineEsercizio();
        SuperAmmDto s1 = c.getSuperAmm1();
        SuperAmmDto s2 = c.getSuperAmm2();
        SuperAmmDto s3 = c.getSuperAmm3();
        SuperAmmDto s4 = c.getSuperAmm4();
        SuperAmmDto p = c.getPlusMinus();


        r.setTotInizioEsercizioTipoCespite(i.getTipoCespite());
        r.setTotInizioEsercizioValoreAggiornato(i.getValoreAggiornato());
        r.setTotInizioEsercizioAmmortamentoOrdinario(i.getAmmortamentoOrdinario());
        r.setTotInizioEsercizioAmmortamentoAnticipato(i.getAmmortamentoAnticipato());
        r.setTotInizioEsercizioTotaleAmmortamento(i.getTotaleAmmortamento());
        r.setTotInizioEsercizioNonAmmortabile(i.getNonAmmortabile());
        r.setTotInizioEsercizioFondoAmmortamenti(i.getFondoAmmortamenti());
        r.setTotInizioEsercizioFondoAmmortamentiRiv(i.getFondoAmmortamentiRiv());
        r.setTotInizioEsercizioFondoAmmortamentiTot(i.getFondoAmmortamentiTot());
        r.setTotInizioEsercizioResiduo(i.getResiduo());


        if (a != null) {
            r.setTotAcquistiTipoCespite(a.getTipoCespite());
            r.setTotAcquistiValoreAggiornato(a.getValoreAggiornato());
            r.setTotAcquistiAmmortamentoOrdinario(a.getAmmortamentoOrdinario());
            r.setTotAcquistiAmmortamentoAnticipato(a.getAmmortamentoAnticipato());
            r.setTotAcquistiTotaleAmmortamento(a.getTotaleAmmortamento());
            r.setTotAcquistiNonAmmortabile(a.getNonAmmortabile());
            r.setTotAcquistiFondoAmmortamenti(a.getFondoAmmortamenti());
            r.setTotAcquistiFondoAmmortamentiRiv(a.getFondoAmmortamentiRiv());
            r.setTotAcquistiFondoAmmortamentiTot(a.getFondoAmmortamentiTot());
            r.setTotAcquistiResiduo(a.getResiduo());
        }


        if (v != null) {
            r.setTotVenditeTipoCespite(v.getTipoCespite());
            r.setTotVenditeValoreAggiornato(v.getValoreAggiornato());
            r.setTotVenditeAmmortamentoOrdinario(v.getAmmortamentoOrdinario());
            r.setTotVenditeAmmortamentoAnticipato(v.getAmmortamentoAnticipato());
            r.setTotVenditeTotaleAmmortamento(v.getTotaleAmmortamento());
            r.setTotVenditeNonAmmortabile(v.getNonAmmortabile());
            r.setTotVenditeFondoAmmortamenti(v.getFondoAmmortamenti());
            r.setTotVenditeFondoAmmortamentiRiv(v.getFondoAmmortamentiRiv());
            r.setTotVenditeFondoAmmortamentiTot(v.getFondoAmmortamentiTot());
            r.setTotVenditeResiduo(v.getResiduo());
        }


        if (n != null) {
            r.setTotAmmortamentiNonDeducibiliTipoCespite(n.getTipoCespite());
            r.setTotAmmortamentiNonDeducibiliValoreAggiornato(n.getValoreAggiornato());
            r.setTotAmmortamentiNonDeducibiliAmmortamentoOrdinario(n.getAmmortamentoOrdinario());
            r.setTotAmmortamentiNonDeducibiliAmmortamentoAnticipato(n.getAmmortamentoAnticipato());
            r.setTotAmmortamentiNonDeducibiliTotaleAmmortamento(n.getTotaleAmmortamento());
            r.setTotAmmortamentiNonDeducibiliNonAmmortabile(n.getNonAmmortabile());
            r.setTotAmmortamentiNonDeducibiliFondoAmmortamenti(n.getFondoAmmortamenti());
            r.setTotAmmortamentiNonDeducibiliFondoAmmortamentiRiv(n.getFondoAmmortamentiRiv());
            r.setTotAmmortamentiNonDeducibiliFondoAmmortamentiTot(n.getFondoAmmortamentiTot());
            r.setTotAmmortamentiNonDeducibiliResiduo(n.getResiduo());
        }

        r.setTotAmmortamentiDeducibiliTipoCespite(am.getTipoCespite());
        r.setTotAmmortamentiDeducibiliValoreAggiornato(am.getValoreAggiornato());
        r.setTotAmmortamentiDeducibiliAmmortamentoOrdinario(am.getAmmortamentoOrdinario());
        r.setTotAmmortamentiDeducibiliAmmortamentoAnticipato(am.getAmmortamentoAnticipato());
        r.setTotAmmortamentiDeducibiliTotaleAmmortamento(am.getTotaleAmmortamento());
        r.setTotAmmortamentiDeducibiliNonAmmortabile(am.getNonAmmortabile());
        r.setTotAmmortamentiDeducibiliFondoAmmortamenti(am.getFondoAmmortamenti());
        r.setTotAmmortamentiDeducibiliFondoAmmortamentiRiv(am.getFondoAmmortamentiRiv());
        r.setTotAmmortamentiDeducibiliFondoAmmortamentiTot(am.getFondoAmmortamentiTot());
        r.setTotAmmortamentiDeducibiliResiduo(am.getResiduo());

        r.setTotFineEsercizioTipoCespite(f.getTipoCespite());
        r.setTotFineEsercizioValoreAggiornato(f.getValoreAggiornato());
        r.setTotFineEsercizioAmmortamentoOrdinario(f.getAmmortamentoOrdinario());
        r.setTotFineEsercizioAmmortamentoAnticipato(f.getAmmortamentoAnticipato());
        r.setTotFineEsercizioTotaleAmmortamento(f.getTotaleAmmortamento());
        r.setTotFineEsercizioNonAmmortabile(f.getNonAmmortabile());
        r.setTotFineEsercizioFondoAmmortamenti(f.getFondoAmmortamenti());
        r.setTotFineEsercizioFondoAmmortamentiRiv(f.getFondoAmmortamentiRiv());
        r.setTotFineEsercizioFondoAmmortamentiTot(f.getFondoAmmortamentiTot());
        r.setTotFineEsercizioResiduo(f.getResiduo());

        if (s1 != null) {
            r.setTotSuperAmm1Desc(s1.getDesc()+ ":");
            r.setTotSuperAmm1Totale(s1.getTotale());
        }
        if (s2 != null) {
            r.setTotSuperAmm2Desc(s2.getDesc()+ ":");
            r.setTotSuperAmm2Totale(s2.getTotale());
        }
        if (s3 != null) {
            r.setTotSuperAmm3Desc(s3.getDesc()+ ":");
            r.setTotSuperAmm3Totale(s3.getTotale());
        }
        if (s4 != null) {
            r.setTotSuperAmm4Desc(s4.getDesc());
            r.setTotSuperAmm4Totale(s4.getTotale());
        }
        if(r.getTotSuperAmm1Totale() + r.getTotSuperAmm2Totale() + r.getTotSuperAmm3Totale() + r.getTotSuperAmm4Totale() != 0){
            r.setTotSuperAmmortamenti(r.getTotSuperAmm1Totale() + r.getTotSuperAmm2Totale() + r.getTotSuperAmm3Totale() + r.getTotSuperAmm4Totale());
            r.setShowTotali(Boolean.TRUE);
        }

        if (p != null) {
            r.setTotPlusMinusDesc(p.getDesc());
            r.setTotPlusMinusTotale(p.getTotale());
            r.setShowPlus(Boolean.TRUE);
        }

        List<CategoriaCespiteReportDto> categoriaCespiteReportDtoList = new ArrayList<>();
        for (CategoriaCespitiDto o : cespiteList) {
            categoriaCespiteReportDtoList.add(buildCategoriaCespitiDto(o));
        }
        r.setCategoriaCespiteReportDtoList(categoriaCespiteReportDtoList);
        return r;
    }

    private CategoriaCespiteReportDto buildCategoriaCespitiDto(CategoriaCespitiDto o) {
        CategoriaCespiteReportDto r = new CategoriaCespiteReportDto();
        r.setTipoCespite(o.getTipoCespite());
        r.setCategoria(o.getCategoria());
        r.setPerc(o.getPerc());
        CespiteSommaDto somma = o.getSomma();

        FiscaleRiepilogoDto i = somma.getInizioEsercizio();
        FiscaleRiepilogoDto a = somma.getAcquisti();
        FiscaleRiepilogoDto v = somma.getVendite();
        FiscaleRiepilogoDto am = somma.getAmmortamentiDeducibili();
        FiscaleRiepilogoDto n = somma.getAmmortamentiNonDeducibili();
        FiscaleRiepilogoDto f = somma.getFineEsercizio();
        SuperAmmDto s1 = somma.getSuperAmm1();
        SuperAmmDto s2 = somma.getSuperAmm2();
        SuperAmmDto s3 = somma.getSuperAmm3();
        SuperAmmDto s4 = somma.getSuperAmm4();
        SuperAmmDto p = somma.getPlusMinus();


        r.setCatInizioEsercizioTipoCespite(i.getTipoCespite());
        r.setCatInizioEsercizioValoreAggiornato(i.getValoreAggiornato());
        r.setCatInizioEsercizioAmmortamentoOrdinario(i.getAmmortamentoOrdinario());
        r.setCatInizioEsercizioAmmortamentoAnticipato(i.getAmmortamentoAnticipato());
        r.setCatInizioEsercizioTotaleAmmortamento(i.getTotaleAmmortamento());
        r.setCatInizioEsercizioNonAmmortabile(i.getNonAmmortabile());
        r.setCatInizioEsercizioFondoAmmortamenti(i.getFondoAmmortamenti());
        r.setCatInizioEsercizioFondoAmmortamentiRiv(i.getFondoAmmortamentiRiv());
        r.setCatInizioEsercizioFondoAmmortamentiTot(i.getFondoAmmortamentiTot());
        r.setCatInizioEsercizioResiduo(i.getResiduo());


        r.setCatAcquistiTipoCespite(a.getTipoCespite());
        r.setCatAcquistiValoreAggiornato(a.getValoreAggiornato());
        r.setCatAcquistiAmmortamentoOrdinario(a.getAmmortamentoOrdinario());
        r.setCatAcquistiAmmortamentoAnticipato(a.getAmmortamentoAnticipato());
        r.setCatAcquistiTotaleAmmortamento(a.getTotaleAmmortamento());
        r.setCatAcquistiNonAmmortabile(a.getNonAmmortabile());
        r.setCatAcquistiFondoAmmortamenti(a.getFondoAmmortamenti());
        r.setCatAcquistiFondoAmmortamentiRiv(a.getFondoAmmortamentiRiv());
        r.setCatAcquistiFondoAmmortamentiTot(a.getFondoAmmortamentiTot());
        r.setCatAcquistiResiduo(a.getResiduo());


        if (v != null) {
            r.setCatVenditeTipoCespite(v.getTipoCespite());
            r.setCatVenditeValoreAggiornato(v.getValoreAggiornato());
            r.setCatVenditeAmmortamentoOrdinario(v.getAmmortamentoOrdinario());
            r.setCatVenditeAmmortamentoAnticipato(v.getAmmortamentoAnticipato());
            r.setCatVenditeTotaleAmmortamento(v.getTotaleAmmortamento());
            r.setCatVenditeNonAmmortabile(v.getNonAmmortabile());
            r.setCatVenditeFondoAmmortamenti(v.getFondoAmmortamenti());
            r.setCatVenditeFondoAmmortamentiRiv(v.getFondoAmmortamentiRiv());
            r.setCatVenditeFondoAmmortamentiTot(v.getFondoAmmortamentiTot());
            r.setCatVenditeResiduo(v.getResiduo());
        }


        if (n != null) {
            r.setCatAmmortamentiNonDeducibiliTipoCespite(n.getTipoCespite());
            r.setCatAmmortamentiNonDeducibiliValoreAggiornato(n.getValoreAggiornato());
            r.setCatAmmortamentiNonDeducibiliAmmortamentoOrdinario(n.getAmmortamentoOrdinario());
            r.setCatAmmortamentiNonDeducibiliAmmortamentoAnticipato(n.getAmmortamentoAnticipato());
            r.setCatAmmortamentiNonDeducibiliTotaleAmmortamento(n.getTotaleAmmortamento());
            r.setCatAmmortamentiNonDeducibiliNonAmmortabile(n.getNonAmmortabile());
            r.setCatAmmortamentiNonDeducibiliFondoAmmortamenti(n.getFondoAmmortamenti());
            r.setCatAmmortamentiNonDeducibiliFondoAmmortamentiRiv(n.getFondoAmmortamentiRiv());
            r.setCatAmmortamentiNonDeducibiliFondoAmmortamentiTot(n.getFondoAmmortamentiTot());
            r.setCatAmmortamentiNonDeducibiliResiduo(n.getResiduo());
        }

        r.setCatAmmortamentiDeducibiliTipoCespite(am.getTipoCespite());
        r.setCatAmmortamentiDeducibiliValoreAggiornato(am.getValoreAggiornato());
        r.setCatAmmortamentiDeducibiliAmmortamentoOrdinario(am.getAmmortamentoOrdinario());
        r.setCatAmmortamentiDeducibiliAmmortamentoAnticipato(am.getAmmortamentoAnticipato());
        r.setCatAmmortamentiDeducibiliTotaleAmmortamento(am.getTotaleAmmortamento());
        r.setCatAmmortamentiDeducibiliNonAmmortabile(am.getNonAmmortabile());
        r.setCatAmmortamentiDeducibiliFondoAmmortamenti(am.getFondoAmmortamenti());
        r.setCatAmmortamentiDeducibiliFondoAmmortamentiRiv(am.getFondoAmmortamentiRiv());
        r.setCatAmmortamentiDeducibiliFondoAmmortamentiTot(am.getFondoAmmortamentiTot());
        r.setCatAmmortamentiDeducibiliResiduo(am.getResiduo());

        r.setCatFineEsercizioTipoCespite(f.getTipoCespite());
        r.setCatFineEsercizioValoreAggiornato(f.getValoreAggiornato());
        r.setCatFineEsercizioAmmortamentoOrdinario(f.getAmmortamentoOrdinario());
        r.setCatFineEsercizioAmmortamentoAnticipato(f.getAmmortamentoAnticipato());
        r.setCatFineEsercizioTotaleAmmortamento(f.getTotaleAmmortamento());
        r.setCatFineEsercizioNonAmmortabile(f.getNonAmmortabile());
        r.setCatFineEsercizioFondoAmmortamenti(f.getFondoAmmortamenti());
        r.setCatFineEsercizioFondoAmmortamentiRiv(f.getFondoAmmortamentiRiv());
        r.setCatFineEsercizioFondoAmmortamentiTot(f.getFondoAmmortamentiTot());
        r.setCatFineEsercizioResiduo(f.getResiduo());

        if (s1 != null) {
            r.setCatSuperAmm1Desc(s1.getDesc()+ ":");
            r.setCatSuperAmm1Totale(s1.getTotale());
        }


        if (s2 != null) {
            r.setCatSuperAmm2Desc(s2.getDesc()+ ":");
            r.setCatSuperAmm2Totale(s2.getTotale());
        }

        if (s3 != null) {
            r.setCatSuperAmm3Desc(s3.getDesc()+ ":");
            r.setCatSuperAmm3Totale(s3.getTotale());
        }

        if (s4 != null) {
            r.setCatSuperAmm4Desc(s4.getDesc() + ":");
            r.setCatSuperAmm4Totale(s4.getTotale());
        }

        if((r.getCatSuperAmm1Totale() + r.getCatSuperAmm2Totale() + r.getCatSuperAmm3Totale() + r.getCatSuperAmm4Totale()) != 0){
            r.setCatSuperAmmortamenti(r.getCatSuperAmm1Totale() + r.getCatSuperAmm2Totale() + r.getCatSuperAmm3Totale() + r.getCatSuperAmm4Totale());
            r.setCatShowTotali(Boolean.TRUE);
        }

        if (p != null) {
            r.setCatPlusMinusDesc(p.getDesc());
            r.setCatPlusMinusTotale(p.getTotale());
            r.setCatShowPlus(Boolean.TRUE);
        }

        List<CespiteReportDto> cespiteReportDtoList = new ArrayList<>();

        List<CespiteDto> cespiteDtoList = new ArrayList<>();
        o.getCespiteProgressivoDtoList().forEach(pr -> cespiteDtoList.addAll(pr.getCespiteDtoList()));

        for (CespiteDto cespiteDto : cespiteDtoList) {
            cespiteReportDtoList.add(buildCespiteDto(cespiteDto));
        }

        r.setCespiteDtoList(cespiteReportDtoList);

        return r;
    }

    private CespiteReportDto buildCespiteDto(CespiteDto d) {
        CespiteReportDto c = new CespiteReportDto();
        c.setId(d.getId());
        c.setCodice(d.getCodice());
        c.setProgressivo1(d.getProgressivo1());
        c.setProgressivo2(d.getProgressivo2());
        c.setCespite(d.getCespite());
        if(d.getDataAcq() != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dtFormattata = d.getDataAcq().format(formatter);
            c.setDataAcq(dtFormattata);
        }
        c.setNumDocAcq(d.getNumDocAcq());
        c.setFornitore(d.getFornitore());
        c.setImporto(d.getImporto());
        c.setImportoRivalutazione(d.getImportoRivalutazione());
        c.setAmmortamento(d.getAmmortamento());
        c.setAnno(d.getAnno());
        String desc = "Q.ta Super";
        if(StringUtils.isNotBlank(d.getSuperAmmDesc())) {
            desc += " " + d.getSuperAmmDesc();
        }
        c.setSuperAmmDesc(desc);
        c.setImportoVendita(d.getImportoVendita());
        if(d.getDataVend() != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dtVFormattata = d.getDataVend().format(formatter);
            c.setDataVend(dtVFormattata);
        }
        if(d.getProtocollo() != null) {
            c.setProtocollo(StringUtils.join("Prot. ", d.getProtocollo(), "/", d.getGiornale(), "/", d.getAnnoProtocollo()));
        }

        List<AmmortamentoCespiteDto> ammortamentoCespiteDtos = new ArrayList<>();
        for (AmmortamentoCespite o : d.getAmmortamentoCespiteList()) {
            ammortamentoCespiteDtos.add(buildAmmortamento(o));
        }
        c.setAmmortamentoCespiteList(ammortamentoCespiteDtos);
        return c;

    }

    private AmmortamentoCespiteDto buildAmmortamento(AmmortamentoCespite d) {
        AmmortamentoCespiteDto a = new AmmortamentoCespiteDto();
        a.setId(d.getId());
        a.setIdAmmortamento(d.getIdAmmortamento());
        if(StringUtils.startsWith(d.getDescrizione(), "Ammortamento")) {
            a.setDescrizione("Amm. ord. deducibile");
        } else {
            a.setDescrizione(d.getDescrizione());
        }
        a.setFondo(d.getFondo());
        a.setQuota(d.getQuota());
        a.setPercAmm(d.getPercAmm());
        a.setFondoRivalutazione(d.getFondoRivalutazione());
        a.setFondoTot((a.getFondo()!=null?a.getFondo():0) + (a.getFondoRivalutazione()!=null?a.getFondoRivalutazione():0));
        a.setResiduo(d.getResiduo());
        a.setQuotaRivalutazione(d.getQuotaRivalutazione());
        a.setSuperQuota(d.getSuperQuota());
        a.setSuperPercentuale(d.getSuperPercentuale());
        a.setAnno(d.getAnno());
        if(d.getDataAmm() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dtFormattata = d.getDataAmm().format(formatter);
            a.setDataAmm(dtFormattata);
        }
        return a;
    }

}

