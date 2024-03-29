package it.calolenoci.mapper;

import it.calolenoci.dto.AccontoDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.*;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@ApplicationScoped
public class FattureMapper {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Fatture buildFatture(Integer progressivoFatt, Ordine ordine) {
        String serie = "B";
        Fatture f = new Fatture();
        f.setAnno(Year.now().getValue());
        f.setSerie(serie);
        f.setProgressivo(progressivoFatt + 1);
        f.setGruppoCliente(1231);
        f.setGruppoFattura(1231);
        f.setContoCliente(ordine.getContoCliente());
        f.setContoFattura(ordine.getContoFattura());
        f.setTipoFattura("V");
        f.setNumeroallegato("");
        f.setNumfatfornitore(0);
        f.setFcodicepagament(ordine.getCodicePagamento());
        f.setFcoddiffpag(ordine.getFcoddiffpag());
        f.setOggetto(ordine.getOggetto());
        f.setBancaappoggio(ordine.getBancaAppoggio());
        f.setNsbancaincasso(ordine.getNsBancaIncasso());
        f.setSpesebollo(ordine.getSpesebollo());
        f.setIvaprimascad(ordine.getIvaprimascad());
        f.setAgente(ordine.getAgente());
        f.setListino(ordine.getListino());
        f.setModoconsegna(" ");
        f.setVettore(" ");
        f.setTarga(" ");
        f.setTargarimorchio(" ");
        f.setVettore2(" ");
        f.setCausaletrasp("V");
        f.setAspettobene("A vista");
        f.setCodicecolli(" ");
        f.setNumerocolli(ordine.getNumerocolli());
        ZoneId zone = ZoneId.of("Europe/Paris");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
        ZonedDateTime d = zonedDateTime.withHour(0).withMinute(0).withSecond(0);
        f.setDatatrasporto(java.sql.Date.valueOf(d.toLocalDate()));
        ZonedDateTime t = ZonedDateTime.now(zone).withYear(1899).withMonth(12).withDayOfMonth(30);
        f.setOratrasporto(Timestamp.valueOf(t.toLocalDateTime()));
        f.setStatoconsegna("");
        f.setDataorabolla(Timestamp.valueOf(ZonedDateTime.now(zone).toLocalDateTime()));
        f.setTempoallestimento(0);
        f.setFlagvettore("N");
        f.setProgrinviovettore(0);
        f.setTotpeso(0D);
        f.setTotpedane(0);
        f.setTotvolume(0D);
        f.setIntestdiverse("");
        f.setIndirdiverse("");
        f.setLocdiverse("");
        f.setCapdiverse("");
        f.setProvdiverse("");
        if(StringUtils.isNotBlank(ordine.getIntestdiverse())){
            f.setIntestdiverse(ordine.getIntestdiverse());
        }
        if(StringUtils.isNotBlank(ordine.getIndirdiverse())){
            f.setIndirdiverse(ordine.getIndirdiverse());
        }
        if(StringUtils.isNotBlank(ordine.getLocdiverse())){
            f.setLocdiverse(ordine.getLocdiverse());
        }
        if(StringUtils.isNotBlank(ordine.getCapdiverse())){
            f.setCapdiverse(ordine.getCapdiverse());
        }
        if(StringUtils.isNotBlank(ordine.getProvdiverse())){
            f.setProvdiverse(ordine.getProvdiverse());
        }
        f.setFlagfattura("");
        f.setFlagbolla("");
        f.setBollasola("N");
        f.setScontocliente1(0D);
        f.setScontocliente2(0D);
        f.setScontopagamento(0D);
        f.setMagazzino("B");
        f.setFvaluta("");
        f.setFlingua("");
        f.setFcambio(0D);
        f.setFlagtrasferito("N");
        f.setFlageffetti("");
        f.setFcodiceivat("");
        f.setFprovvarticolo(0D);
        f.setFprovvcliente(0D);
        f.setTcommessa("");
        f.setTcentrocosto("");
        f.setTvocespesa("");
        f.setTcompetenza(0);
        f.setFltipoliquidaprov("");
        f.setSettore("");
        f.setAnnopartita(0);
        f.setNumpartita(0);
        f.setFlbloccopag("");
        f.setFlvariazione("N");
        f.setGruppocompensa(0);
        f.setContocompensa("");
        f.setFlentro(0);
        f.setPriorita(0);
        f.setProgressivogen(0);
        f.setValusern1(0D);
        f.setValusern2(0D);
        f.setValuseralfa1("");
        f.setValuseralfa2("");
        f.setFnotefattura("");
        f.setFnotepiede("");
        f.setProgrregbolli(0);
        f.setTprovenienza("");
        f.setTpid(0);
        f.setCig("");
        f.setCup("");
        f.setUsername("");
        f.setSysCreatedate(Timestamp.valueOf(ZonedDateTime.now(zone).toLocalDateTime()));
        f.setSysCreateuser("daniele");
        f.setSysUpdatedate(Timestamp.valueOf(ZonedDateTime.now(zone).toLocalDateTime()));
        f.setSysUpdateuser("daniele");
        f.setFlinviorifatt("");
        return f;
    }

    public FattureDettaglio buildFattureDettaglio(OrdineDettaglioDto dto, Fatture f, OrdineDettaglio o, Integer progressivoFattDettaglio, Integer i, String user){
        FattureDettaglio fd = new FattureDettaglio();
        fd.setAnno(f.getAnno());
        fd.setSerie(f.getSerie());
        fd.setProgressivo(f.getProgressivo());
        fd.setRigo(i + 1);
        fd.setProgrGenerale(progressivoFattDettaglio + i + 1);
        fd.setProgrOrdCli(o.getProgrGenerale());
        fd.setTipoRigo(o.getTipoRigo());
        fd.setFArticolo(o.getFArticolo());
        fd.setVariante1(o.getVariante1());
        fd.setVariante2(o.getVariante2());
        fd.setVariante3(o.getVariante3());
        fd.setVariante4(o.getVariante4());
        fd.setVariante5(o.getVariante5());
        fd.setFDescrArticolo(o.getFDescrArticolo());
        fd.setCodiceean(o.getCodiceean());
        fd.setQuantita(dto.getQtaProntoConsegna()!=null?dto.getQtaProntoConsegna():dto.getQtaDaConsegnare()!=null?dto.getQtaDaConsegnare():dto.getQuantita());
        fd.setQuantita2(0D);
        fd.setQtaomaggio(o.getQtaomaggio());
        fd.setQtainevasa(0D);
        fd.setCausaleinevaso(" ");
        fd.setPrezzo(o.getPrezzo());
        fd.setFunitamisura(o.getFUnitaMisura());
        fd.setFcoefficiente(o.getFcoefficiente());
        fd.setScontoarticolo(o.getScontoArticolo());
        fd.setScontoc1(o.getScontoC1());
        fd.setScontoc2(o.getScontoC2());
        fd.setScontop(o.getScontoP());
        fd.setPrezzoextra(o.getPrezzoextra());
        fd.setMagazz(o.getMagazz());
        fd.setLottomagf(o.getTono());
        fd.setIva(o.getFCodiceIva());
        fd.setImpprovvfisso(o.getImpprovvfisso());
        fd.setFprovvarticolo(o.getFprovvarticolo());
        fd.setFprovvcliente(o.getFprovvcliente());
        fd.setFcolli(o.getFColli());
        fd.setFpallet(o.getFpallet());
        fd.setCoefprezzo(o.getCoefprezzo());
        fd.setFcentrocostor(" ");
        fd.setFvocespesa(" ");
        fd.setFcommessa(o.getFcommessa());
        fd.setFcompetenza(0);
        fd.setFgrupporicavo(o.getFgrupporicavo());
        fd.setFcontoricavo(o.getFcontoricavo());
        fd.setFprovenienza(o.getFprovenienza());
        fd.setFpid(o.getFpid());
        fd.setQtyuser1(o.getQtyuser1());
        fd.setQtyuser2(o.getQtyuser2());
        fd.setQtyuser3(o.getQtyuser3());
        fd.setQtyuser4(o.getQtyuser4());
        fd.setQtyuser5(o.getQtyuser5());
        fd.setQtyuser6(o.getQtyuser6());
        fd.setQtyuser7(o.getQtyuser7());
        fd.setDescruser1(o.getDescruser1());
        fd.setDescruser2(o.getDescruser2());
        fd.setDescruser3(o.getDescruser3());
        fd.setDescruser4(o.getDescruser4());
        fd.setDescruser5(o.getDescruser5());
        fd.setDescruser6(o.getDescruser6());
        fd.setDescruser7(o.getDescruser7());
        fd.setDescruser1E(" ");
        fd.setDescruser2E(" ");
        fd.setDescruser3E(" ");
        fd.setDescruser4E(" ");
        fd.setDescruser5E(" ");
        fd.setNoterigo(" ");
        fd.setContromarca(o.getContromarca());
        fd.setFeordid(" ");
        fd.setFeorditem(" ");
        fd.setFeintento(0);
        fd.setProgrdeposito(0);
        fd.setProgrprev(0);
        fd.setUsername(user);
        fd.setDatamodifica(new Date());
        fd.setSysCreatedate(new Date());
        fd.setSysUpdatedate(new Date());
        fd.setSysCreateuser(user);
        fd.setSysUpdateuser(user);
        return fd;
    }

    public OrdineDettaglioDto fromAccontoToOrdineDettaglio(AccontoDto dto, OrdineId o){

        OrdineDettaglioDto fd = new OrdineDettaglioDto();
        fd.setAnno(o.getAnno());
        fd.setSerie(o.getSerie());
        fd.setProgressivo(o.getProgressivo());
        fd.setTipoRigo("V");
        fd.setFArticolo("*ACC");
        fd.setFDescrArticolo("Storno fattura acconto nr. " + StringUtils.trim(dto.getNumeroFattura()) + " del " + sdf.format(dto.getDataFattura()));
        fd.setQuantita(0D);
        fd.setPrezzo(-dto.getPrezzo());
        fd.setFUnitaMisura(".");
        fd.setScontoArticolo(0D);
        fd.setScontoC1(0D);
        fd.setScontoC2(0D);
        fd.setScontoP(0D);
        fd.setFCodiceIva(dto.getIva());
        fd.setFColli(0);
        fd.setDataFattura(dto.getDataFattura());
        fd.setNumeroFattura(dto.getNumeroFattura());
        return fd;
    }
    public FattureDettaglio buildStorno(OrdineDettaglioDto dto, Fatture f, Integer progressivoFattDettaglio, Integer i, String user){
        FattureDettaglio fd = new FattureDettaglio();
        fd.setAnno(f.getAnno());
        fd.setSerie(f.getSerie());
        fd.setProgressivo(f.getProgressivo());
        fd.setRigo(i + 1);
        fd.setProgrGenerale(progressivoFattDettaglio + i + 1);
        fd.setProgrOrdCli(0);
        fd.setTipoRigo("V");
        fd.setFArticolo("*ACC");
        fd.setVariante1(" ");
        fd.setVariante2(" ");
        fd.setVariante3(" ");
        fd.setVariante4(" ");
        fd.setVariante5(" ");
        fd.setFDescrArticolo(dto.getFDescrArticolo());
        fd.setCodiceean("");
        fd.setQuantita(0D);
        fd.setQuantita2(0D);
        fd.setQtaomaggio(0D);
        fd.setQtainevasa(0D);
        fd.setCausaleinevaso(" ");
        fd.setPrezzo(dto.getPrezzo());
        fd.setFunitamisura(".");
        fd.setFcoefficiente(0D);
        fd.setScontoarticolo(0D);
        fd.setScontoc1(0D);
        fd.setScontoc2(0D);
        fd.setScontop(0D);
        fd.setPrezzoextra(0D);
        fd.setMagazz("B");
        fd.setLottomagf(" ");
        fd.setIva(dto.getFCodiceIva());
        fd.setImpprovvfisso(0D);
        fd.setFprovvarticolo(0D);
        fd.setFprovvcliente(0D);
        fd.setFcolli(0);
        fd.setFpallet(0D);
        fd.setCoefprezzo(0D);
        fd.setFcentrocostor(" ");
        fd.setFvocespesa(" ");
        fd.setFcommessa(" ");
        fd.setFcompetenza(0);
        fd.setFgrupporicavo(0);
        fd.setFcontoricavo("");
        fd.setFprovenienza("");
        fd.setFpid(0);
        fd.setQtyuser1(0D);
        fd.setQtyuser2(0D);
        fd.setQtyuser3(0D);
        fd.setQtyuser4(0D);
        fd.setQtyuser5(0D);
        fd.setQtyuser6(0D);
        fd.setQtyuser7(0D);
        fd.setDescruser1(" ");
        fd.setDescruser2(" ");
        fd.setDescruser3(" ");
        fd.setDescruser4(" ");
        fd.setDescruser5(" ");
        fd.setDescruser6(" ");
        fd.setDescruser7(" ");
        fd.setDescruser1E(" ");
        fd.setDescruser2E(" ");
        fd.setDescruser3E(" ");
        fd.setDescruser4E(" ");
        fd.setDescruser5E(" ");
        fd.setNoterigo(" ");
        fd.setContromarca(" ");
        fd.setFeordid(" ");
        fd.setFeorditem(" ");
        fd.setFeintento(0);
        fd.setProgrdeposito(0);
        fd.setProgrprev(0);
        fd.setUsername(user);
        fd.setDatamodifica(new Date());
        fd.setSysCreatedate(new Date());
        fd.setSysUpdatedate(new Date());
        fd.setSysCreateuser(user);
        fd.setSysUpdateuser(user);
        return fd;
    }
}
