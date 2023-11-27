package it.calolenoci.mapper;

import it.calolenoci.dto.PrimanotaDto;
import it.calolenoci.entity.Primanota;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
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
}
