package it.calolenoci.mapper;

import it.calolenoci.dto.SommaCategoria;
import it.calolenoci.entity.FiscaleRiepilogo;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;

import java.util.List;

import static it.calolenoci.enums.FiscaleRiepilogoEnum.INIZIO_ESERCIZIO;

@ApplicationScoped
public class FiscaleRiepilogoMapper {

    public FiscaleRiepilogo buildFiscaleRiepilogo(String descr, String tipoCespite, double costoStorico, double fondo, double fondoRiv){
        FiscaleRiepilogo f = new FiscaleRiepilogo();
        if(StringUtils.isNotBlank(tipoCespite)) {
            f.setTipoCespite(tipoCespite);
        }
        if(StringUtils.isNotBlank(descr)){
            f.setDescrizione(descr);
        }
        f.setValoreAggiornato(costoStorico);
        f.setFondoAmmortamenti(fondo);
        f.setFondoAmmortamentiRiv(fondoRiv);
        f.setResiduo(f.getValoreAggiornato() - f.getFondoAmmortamenti() - f.getFondoAmmortamentiRiv());
        return f;
    }

    public FiscaleRiepilogo buildAmmortamento(String descr, String tipoCespite, double ammOrd, double ammRiv, double fondo, double fondoRiv){
        FiscaleRiepilogo f = new FiscaleRiepilogo();
        if(StringUtils.isNotBlank(tipoCespite)) {
            f.setTipoCespite(tipoCespite);
        }
        if(StringUtils.isNotBlank(descr)){
            f.setDescrizione(descr);
        }
        f.setAmmortamentoOrdinario(ammOrd);
        f.setAmmortamentoRivalutato(ammRiv);
        f.setFondoAmmortamenti(fondo);
        f.setFondoAmmortamentiRiv(fondoRiv);
        return f;
    }

    public FiscaleRiepilogo buildVendita(String descr, String tipoCespite, double costoStorico, double totaleAmmortamenti, double fondo){
        FiscaleRiepilogo f = new FiscaleRiepilogo();
        if(StringUtils.isNotBlank(tipoCespite)) {
            f.setTipoCespite(tipoCespite);
        }
        if(StringUtils.isNotBlank(descr)){
            f.setDescrizione(descr);
        }
        f.setValoreAggiornato(costoStorico);
        f.setTotaleAmmortamento(totaleAmmortamenti);
        f.setFondoAmmortamenti(fondo);
        return f;
    }

    public FiscaleRiepilogo buildSuperAmmortamento(String descr, String tipoCespite, Double superAmm1, Double superAmm2, Double superAmm3, Double superAmm4){
        FiscaleRiepilogo f = new FiscaleRiepilogo();
        if(StringUtils.isNotBlank(tipoCespite)) {
            f.setTipoCespite(tipoCespite);
        }
        if(StringUtils.isNotBlank(descr)){
            f.setDescrizione(descr);
        }
        if(superAmm1 != null){
            f.setSuperAmm1(superAmm1);
        }
        if(superAmm2 != null){
            f.setSuperAmm2(superAmm2);
        }
        if(superAmm3 != null){
            f.setSuperAmm3(superAmm3);
        }
        if(superAmm4 != null){
            f.setSuperAmm4(superAmm4);
        }
        return f;
    }
}
