package it.calolenoci.mapper;

import it.calolenoci.entity.*;

import javax.enterprise.context.ApplicationScoped;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


@ApplicationScoped
public class SaldiMagazzinoMapper {


    public SaldiMagazzino buildSaldiMagazzino(OrdineDettaglio articolo, String user){
        SaldiMagazzino saldiMagazzino = new SaldiMagazzino();
        saldiMagazzino.setMarticolo(articolo.getFArticolo());
        saldiMagazzino.setQty("1");
        saldiMagazzino.setVar1("");
        saldiMagazzino.setVar2("");
        saldiMagazzino.setVar3("");
        saldiMagazzino.setVar4("");
        saldiMagazzino.setVar5("");
        saldiMagazzino.setMmagazzino("B");
        saldiMagazzino.setVcarichi(0D);
        saldiMagazzino.setVscarichi(0D);
        saldiMagazzino.setQscarichi(0D);
        saldiMagazzino.setQfiscale(0D);
        saldiMagazzino.setVfiscale(0D);
        saldiMagazzino.setScortamin(0D);
        saldiMagazzino.setQcarichiu(0D);
        saldiMagazzino.setQscarichiu(0D);
        saldiMagazzino.setVcarichiu(0D);
        saldiMagazzino.setVscarichiu(0D);
        saldiMagazzino.setQfiscaleu(0D);
        saldiMagazzino.setVfiscaleu(0D);
        saldiMagazzino.setQgiacenzau(0D);
        saldiMagazzino.setVgiacenzau(0D);
        saldiMagazzino.setCostoultacquu(0D);
        saldiMagazzino.setSysCreatedate(new Date());
        saldiMagazzino.setSysUpdatedate(new Date());
        saldiMagazzino.setSysUpdateuser(user);
        saldiMagazzino.setSysCreateuser(user);
        Double qtaCarico = (articolo.getQuantita()==null?0:articolo.getQuantita());
        qtaCarico = Math.round(qtaCarico * 100.0) / 100.0;
        Double qtaGiacenza = qtaCarico - (saldiMagazzino.getQscarichi()==null?0: saldiMagazzino.getQscarichi());
        saldiMagazzino.setQcarichi(qtaCarico);
        saldiMagazzino.setQgiacenza(qtaGiacenza);
        return saldiMagazzino;
    }
}
