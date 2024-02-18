package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.entity.GoOrdineFornitoreBK;
import it.calolenoci.entity.OrdineFornitore;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.time.Year;
import java.util.Date;


@ApplicationScoped
public class OrdineFornitoreMapper {

        public OrdineFornitore buildOAF(ArticoloDto articoloDto, int prog, String serieOAF, String user) {
            OrdineFornitore o = new OrdineFornitore();
            o.setProgressivo(prog);
            o.setAnno(Year.now().getValue());
            o.setSerie(serieOAF);
            o.setDataOrdine(new Date());
            o.setCreateDate(new Date());
            o.setUpdateDate(new Date());
            o.setMagazzino("B");
            o.setNumRevisione(0);
            o.setGruppo(articoloDto.getGruppoConto());
            o.setConto(articoloDto.getSottoConto());
            o.setCodicePagamento(articoloDto.getCodPagamento());
            o.setBancaPagamento(articoloDto.getBanca());
            o.setCreateUser(StringUtils.upperCase(user));
            o.setUpdateUser(StringUtils.upperCase(user));
            o.setProvvisorio("F");
            o.setNumConfOrdine(" ");
            o.setOtipocompenso(" ");
            o.setTcommessa(" ");
            o.setOivaprimascad(" ");
            o.setIban(" ");
            o.setDescrbanca(" ");
            o.setSwift(" ");
            o.setOmodoconsegna(" ");
            o.setTcodiceiva(" ");
            o.setScontofornitor1(0D);
            o.setScontofornitor2(0D);
            o.setFscontopagament(0D);
            o.setTprovvarticolo(0D);
            o.setTprovvfornitore(0D);
            o.setRefInterno(StringUtils.truncate(StringUtils.upperCase(user),6));
            o.setOagente(" ");
            o.setOlingua(" ");
            o.setOvaluta(" ");
            o.setOcambio(0D);
            o.setOggetto(" ");
            o.setCig(" ");
            o.setCup(" ");
            o.setVettore(" ");
            o.setTarga(" ");
            o.setTargarimorchio(" ");
            o.setNoteinterne(" ");
            o.setProgressivogen(0);
            o.setReferente(" ");
            o.setOggetto(" ");
            o.setGruppocarico(0);
            o.setContocarico(" ");
            o.setPrefazione(" ");
            o.setPiedepagina(" ");
            o.setGruppomediatore(0);
            o.setContomediatore(" ");
            o.setFlagtrasferito(" ");
            o.setValoreuser(0D);
            o.setFlpalmari(" ");
            o.setUtentepalmare(" ");
            o.setUsername(" ");
            o.setOoggetto(" ");
            return o;
        }

    public GoOrdineFornitoreBK copyOAF(OrdineFornitore f) {
        GoOrdineFornitoreBK o = new GoOrdineFornitoreBK();
        o.setProgressivo(f.getProgressivo());
        o.setAnno(f.getAnno());
        o.setSerie(f.getSerie());
        o.setDataOrdine(f.getDataOrdine());
        o.setCreateDate(f.getCreateDate());
        o.setUpdateDate(f.getUpdateDate());
        o.setMagazzino(f.getMagazzino());
        o.setNumRevisione(f.getNumRevisione());
        o.setGruppo(f.getGruppo());
        o.setConto(f.getConto());
        o.setCodicePagamento(f.getCodicePagamento());
        o.setBancaPagamento(f.getBancaPagamento());
        o.setCreateUser(f.getCreateUser());
        o.setUpdateUser(f.getUpdateUser());
        o.setProvvisorio(f.getProvvisorio());
        o.setNumConfOrdine(f.getNumConfOrdine());
        o.setOtipocompenso(f.getOtipocompenso());
        o.setTcommessa(f.getTcommessa());
        o.setOivaprimascad(f.getOivaprimascad());
        o.setIban(f.getIban());
        o.setDescrbanca(f.getDescrbanca());
        o.setSwift(f.getSwift());
        o.setOmodoconsegna(f.getOmodoconsegna());
        o.setTcodiceiva(f.getTcodiceiva());
        o.setScontofornitor1(f.getScontofornitor1());
        o.setScontofornitor2(f.getScontofornitor2());
        o.setFscontopagament(f.getFscontopagament());
        o.setTprovvarticolo(f.getTprovvarticolo());
        o.setTprovvfornitore(f.getTprovvfornitore());
        o.setRefInterno(f.getRefInterno());
        o.setOagente(f.getOagente());
        o.setOlingua(f.getOlingua());
        o.setOvaluta(f.getOvaluta());
        o.setOcambio(f.getOcambio());
        o.setOggetto(f.getOggetto());
        o.setCig(f.getCig());
        o.setCup(f.getCup());
        o.setVettore(f.getVettore());
        o.setTarga(f.getTarga());
        o.setTargarimorchio(f.getTargarimorchio());
        o.setNoteinterne(f.getNoteinterne());
        o.setProgressivogen(f.getProgressivogen());
        o.setReferente(f.getReferente());
        o.setOggetto(f.getOggetto());
        o.setGruppocarico(f.getGruppocarico());
        o.setContocarico(f.getContocarico());
        o.setPrefazione(f.getPrefazione());
        o.setPiedepagina(f.getPiedepagina());
        o.setGruppomediatore(f.getGruppomediatore());
        o.setContomediatore(f.getContomediatore());
        o.setFlagtrasferito(f.getFlagtrasferito());
        o.setValoreuser(f.getValoreuser());
        o.setFlpalmari(f.getFlpalmari());
        o.setUtentepalmare(f.getUtentepalmare());
        o.setUsername(f.getUsername());
        o.setOoggetto(f.getOoggetto());
        return o;
    }
}
