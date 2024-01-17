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
        SuperAmmDto p = c.getPlusMinus();


        r.setTotInizioEsercizioTipoCespite(i.getTipoCespite());
        r.setTotInizioEsercizioValoreAggiornato(i.getValoreAggiornato());
        r.setTotInizioEsercizioAmmortamentoOrdinario(i.getAmmortamentoOrdinario());
        r.setTotInizioEsercizioTotaleAmmortamento(i.getTotaleAmmortamento());
        r.setTotInizioEsercizioFondoAmmortamenti(i.getFondoAmmortamenti());
        r.setTotInizioEsercizioResiduo(i.getResiduo());


        if (a != null) {
            r.setTotAcquistiValoreAggiornato(a.getValoreAggiornato());
            r.setTotAcquistiAmmortamentoOrdinario(a.getAmmortamentoOrdinario());
            r.setTotAcquistiTotaleAmmortamento(a.getTotaleAmmortamento());
            r.setTotAcquistiFondoAmmortamenti(a.getFondoAmmortamenti());
            r.setTotAcquistiResiduo(a.getResiduo());
        }


        if (v != null) {
            r.setTotVenditeValoreAggiornato(v.getValoreAggiornato());
            r.setTotVenditeAmmortamentoOrdinario(v.getAmmortamentoOrdinario());
            r.setTotVenditeTotaleAmmortamento(v.getTotaleAmmortamento());
            r.setTotVenditeFondoAmmortamenti(v.getFondoAmmortamenti());
            r.setTotVenditeResiduo(v.getResiduo());
        }


        r.setTotAmmortamentiDeducibiliTipoCespite(am.getTipoCespite());
        r.setTotAmmortamentiDeducibiliValoreAggiornato(am.getValoreAggiornato());
        r.setTotAmmortamentiDeducibiliAmmortamentoOrdinario(am.getAmmortamentoOrdinario());
        r.setTotAmmortamentiDeducibiliTotaleAmmortamento(am.getTotaleAmmortamento());
        r.setTotAmmortamentiDeducibiliFondoAmmortamenti(am.getFondoAmmortamenti());
        r.setTotAmmortamentiDeducibiliResiduo(am.getResiduo());

        r.setTotFineEsercizioValoreAggiornato(f.getValoreAggiornato());
        r.setTotFineEsercizioAmmortamentoOrdinario(f.getAmmortamentoOrdinario());
        r.setTotFineEsercizioTotaleAmmortamento(f.getTotaleAmmortamento());
        r.setTotFineEsercizioFondoAmmortamenti(f.getFondoAmmortamenti());
        r.setTotFineEsercizioResiduo(f.getResiduo());

        if (s1 != null) {
            r.setTotSuperAmm1Desc(s1.getDesc());
            r.setTotSuperAmm1Totale(s1.getTotale());
        }
        if (s2 != null) {
            r.setTotSuperAmm2Desc(s2.getDesc());
            r.setTotSuperAmm2Totale(s2.getTotale());
        }
        if (s3 != null) {
            r.setTotSuperAmm3Desc(s3.getDesc());
            r.setTotSuperAmm3Totale(s3.getTotale());
        }
        if(r.getTotSuperAmm1Totale() + r.getTotSuperAmm2Totale() + r.getTotSuperAmm3Totale() != 0){
            r.setTotSuperAmmortamenti(r.getTotSuperAmm1Totale() + r.getTotSuperAmm2Totale() + r.getTotSuperAmm3Totale());
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
        SuperAmmDto p = somma.getPlusMinus();


        r.setCatInizioEsercizioTipoCespite(i.getTipoCespite());
        r.setCatInizioEsercizioValoreAggiornato(i.getValoreAggiornato());
        r.setCatInizioEsercizioAmmortamentoOrdinario(i.getAmmortamentoOrdinario());
        r.setCatInizioEsercizioTotaleAmmortamento(i.getTotaleAmmortamento());
        r.setCatInizioEsercizioFondoAmmortamenti(i.getFondoAmmortamenti());
        r.setCatInizioEsercizioResiduo(i.getResiduo());


        r.setCatAcquistiValoreAggiornato(a.getValoreAggiornato());
        r.setCatAcquistiAmmortamentoOrdinario(a.getAmmortamentoOrdinario());
        r.setCatAcquistiTotaleAmmortamento(a.getTotaleAmmortamento());
        r.setCatAcquistiFondoAmmortamenti(a.getFondoAmmortamenti());
        r.setCatAcquistiResiduo(a.getResiduo());


        if (v != null) {
            r.setCatVenditeValoreAggiornato(v.getValoreAggiornato());
            r.setCatVenditeAmmortamentoOrdinario(v.getAmmortamentoOrdinario());
            r.setCatVenditeTotaleAmmortamento(v.getTotaleAmmortamento());
            r.setCatVenditeFondoAmmortamenti(v.getFondoAmmortamenti());
            r.setCatVenditeResiduo(v.getResiduo());
        }


        if (n != null) {
            r.setCatAmmortamentiNonDeducibiliValoreAggiornato(n.getValoreAggiornato());
            r.setCatAmmortamentiNonDeducibiliAmmortamentoOrdinario(n.getAmmortamentoOrdinario());
            r.setCatAmmortamentiNonDeducibiliTotaleAmmortamento(n.getTotaleAmmortamento());
            r.setCatAmmortamentiNonDeducibiliFondoAmmortamenti(n.getFondoAmmortamenti());
            r.setCatAmmortamentiNonDeducibiliResiduo(n.getResiduo());
        }

        r.setCatAmmortamentiDeducibiliValoreAggiornato(am.getValoreAggiornato());
        r.setCatAmmortamentiDeducibiliAmmortamentoOrdinario(am.getAmmortamentoOrdinario());
        r.setCatAmmortamentiDeducibiliTotaleAmmortamento(am.getTotaleAmmortamento());
        r.setCatAmmortamentiDeducibiliFondoAmmortamenti(am.getFondoAmmortamenti());
        r.setCatAmmortamentiDeducibiliResiduo(am.getResiduo());

        r.setCatFineEsercizioValoreAggiornato(f.getValoreAggiornato());
        r.setCatFineEsercizioAmmortamentoOrdinario(f.getAmmortamentoOrdinario());
        r.setCatFineEsercizioTotaleAmmortamento(f.getTotaleAmmortamento());
        r.setCatFineEsercizioFondoAmmortamenti(f.getFondoAmmortamenti());
        r.setCatFineEsercizioResiduo(f.getResiduo());

        if (s1 != null) {
            r.setCatSuperAmm1Desc(s1.getDesc());
            r.setCatSuperAmm1Totale(s1.getTotale());
        }


        if (s2 != null) {
            r.setCatSuperAmm2Desc(s2.getDesc());
            r.setCatSuperAmm2Totale(s2.getTotale());
        }

        if (s3 != null) {
            r.setCatSuperAmm3Desc(s3.getDesc());
            r.setCatSuperAmm3Totale(s3.getTotale());
        }


        if((r.getCatSuperAmm1Totale() + r.getCatSuperAmm2Totale() + r.getCatSuperAmm3Totale()) != 0){
            r.setCatSuperAmmortamenti(r.getCatSuperAmm1Totale() + r.getCatSuperAmm2Totale() + r.getCatSuperAmm3Totale() );
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
        c.setAmmortamento(d.getAmmortamento());
        c.setAnno(d.getAnno());
        c.setSuperAmmDesc(d.getSuperAmmDesc());
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
        a.setResiduo(d.getResiduo());
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

