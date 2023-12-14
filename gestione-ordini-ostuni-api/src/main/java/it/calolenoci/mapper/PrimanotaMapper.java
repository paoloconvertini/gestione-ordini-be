package it.calolenoci.mapper;

import it.calolenoci.dto.CespiteDBDto;
import it.calolenoci.dto.PrimanotaDto;
import it.calolenoci.entity.Primanota;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.Year;

@ApplicationScoped
public class PrimanotaMapper {

    public Primanota buildPrimanota(PrimanotaDto dto, Primanota p, Integer progrGenerale) {
        Primanota primanota = new Primanota();
        primanota.setAnno(dto.getAnno());
        primanota.setGiornale(dto.getGiornale());
        primanota.setProtocollo(dto.getProtocollo());
        primanota.setProgrprimanota(dto.getProgrprimanota());
        primanota.setProgrgenerale(progrGenerale + 1);
        primanota.setDivisione(p.getDivisione());
        primanota.setAnnopartita(p.getAnnopartita());
        primanota.setNumpartita(p.getAnnopartita());
        primanota.setDataoperazione(p.getDataoperazione());
        primanota.setDatamovimento(p.getDatamovimento());
        primanota.setNumerodocumento(p.getNumerodocumento());
        primanota.setCausale(p.getCausale());
        primanota.setGruppoconto(dto.getGruppoconto());
        primanota.setSottoconto(dto.getSottoconto());
        primanota.setDescrsuppl(dto.getDescrsuppl());
        primanota.setImporto(dto.getImporto());
        primanota.setRigogiornale(p.getRigogiornale());
        primanota.setCodicepagamento(p.getCodicepagamento());
        primanota.setCoddiffpag(p.getCoddiffpag());
        primanota.setIban(p.getIban());
        primanota.setIvaprimascad(p.getIvaprimascad());
        primanota.setImportove(p.getImportove());
        primanota.setImpprenotato(p.getImpprenotato());
        primanota.setUserautorizza(p.getUserautorizza());
        primanota.setTipocompenso(p.getTipocompenso());
        primanota.setOnorario(p.getOnorario());
        primanota.setSpese(p.getSpese());
        primanota.setSpeseenasarco(p.getSpeseenasarco());
        primanota.setSpesecod8(p.getSpesecod8());
        primanota.setSpeseno770(p.getSpeseno770());
        primanota.setAnnodiritto(p.getAnnodiritto());
        primanota.setRitenuta(p.getRitenuta());
        primanota.setRitinps(p.getRitinps());
        primanota.setRitinail(p.getRitinail());
        primanota.setRitenasarco(p.getRitenasarco());
        primanota.setAnnoenasarco(p.getAnnoenasarco());
        primanota.setTrimenasarco(p.getTrimenasarco());
        primanota.setIvasp(p.getIvasp());
        primanota.setFlagtrasferito(p.getFlagtrasferito());
        primanota.setNumerorata(p.getNumerorata());
        primanota.setNumconciliazione(p.getNumconciliazione());
        primanota.setFlagprovvisoria(p.getFlagprovvisoria());
        primanota.setFlagftbloccata(p.getFlagftbloccata());
        primanota.setValuta(p.getValuta());
        primanota.setCambio(p.getCambio());
        primanota.setFlaggenerico(p.getFlaggenerico());
        primanota.setControllobf(p.getControllobf());
        primanota.setAgente(p.getAgente());
        primanota.setBancapag(p.getBancapag());
        primanota.setCig(p.getCig());
        primanota.setCup(p.getCup());
        primanota.setValuser1(p.getValuser1());
        primanota.setValuser2(p.getValuser2());
        primanota.setValuser3(p.getValuser3());
        primanota.setValuser4(p.getValuser4());
        primanota.setValuser5(p.getValuser5());
        primanota.setCampouser1(p.getCampouser1());
        primanota.setCampouser2(p.getCampouser2());
        primanota.setCampouser3(p.getCampouser3());
        primanota.setCampouser4(p.getCampouser4());
        primanota.setCampouser5(p.getCampouser5());
        primanota.setNoteprimanota(p.getNoteprimanota());
        primanota.setProvenienza(p.getProvenienza());
        primanota.setPid(p.getPid());
        primanota.setUsername(p.getUsername());
        primanota.setSysCreatedate(LocalDate.now());
        primanota.setSysCreateuser(p.getSysCreateuser());
        primanota.setSysUpdatedate(LocalDate.now());
        primanota.setSysUpdateuser(p.getSysUpdateuser());
        return primanota;
    }

    public Primanota buildPrimanotaContabile(CespiteDBDto dto, Integer protocollo, Integer rigo, Integer progrGenerale, String desc, Integer gruppo, String conto, Double quota) {
        Primanota primanota = new Primanota();
        primanota.setAnno(Year.now().getValue());
        primanota.setGiornale("");
        primanota.setProtocollo(protocollo);
        primanota.setProgrprimanota(rigo);
        primanota.setProgrgenerale(progrGenerale);
        primanota.setDivisione("");
        primanota.setAnnopartita(0);
        primanota.setNumpartita(0);
        primanota.setDataoperazione(LocalDate.now());
        primanota.setDatamovimento(LocalDate.now());
        primanota.setNumerodocumento(dto.getCespite().getProgressivo1() + "." + dto.getCespite().getProgressivo2());
        primanota.setCausale("GVM");
        primanota.setGruppoconto(gruppo);
        primanota.setSottoconto(conto);
        primanota.setDescrsuppl(desc);
        primanota.setImporto(quota);
        primanota.setRigogiornale(0);
        primanota.setCodicepagamento("");
        primanota.setCoddiffpag("");
        primanota.setIban("");
        primanota.setIvaprimascad("N");
        primanota.setImportove(0D);
        primanota.setImpprenotato(0D);
        primanota.setUserautorizza("");
        primanota.setTipocompenso("");
        primanota.setOnorario(0D);
        primanota.setSpese(0D);
        primanota.setSpeseenasarco(0D);
        primanota.setSpesecod8(0D);
        primanota.setSpeseno770(0D);
        primanota.setAnnodiritto(0D);
        primanota.setRitenuta(0D);
        primanota.setRitinps(0D);
        primanota.setRitinail(0D);
        primanota.setRitenasarco(0D);
        primanota.setAnnoenasarco(0);
        primanota.setTrimenasarco(0);
        primanota.setIvasp(0D);
        primanota.setFlagtrasferito("N");
        primanota.setNumerorata(0);
        primanota.setNumconciliazione(0);
        primanota.setFlagprovvisoria("");
        primanota.setFlagftbloccata("N");
        primanota.setValuta("");
        primanota.setCambio(0D);
        primanota.setFlaggenerico("");
        primanota.setControllobf("");
        primanota.setAgente("");
        primanota.setBancapag("");
        primanota.setCig("");
        primanota.setCup("");
        primanota.setValuser1(0D);
        primanota.setValuser2(0D);
        primanota.setValuser3(0D);
        primanota.setValuser4(0D);
        primanota.setValuser5(0D);
        primanota.setCampouser1("");
        primanota.setCampouser2("");
        primanota.setCampouser3("");
        primanota.setCampouser4("");
        primanota.setCampouser5("");
        primanota.setNoteprimanota("");
        primanota.setProvenienza("");
        primanota.setPid(0);
        primanota.setUsername("NICO");
        primanota.setSysCreatedate(LocalDate.now());
        primanota.setSysCreateuser("NICO");
        primanota.setSysUpdatedate(LocalDate.now());
        primanota.setSysUpdateuser("NICO");
        return primanota;
    }
}