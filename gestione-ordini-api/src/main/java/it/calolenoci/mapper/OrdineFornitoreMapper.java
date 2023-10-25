package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloDto;
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
}
