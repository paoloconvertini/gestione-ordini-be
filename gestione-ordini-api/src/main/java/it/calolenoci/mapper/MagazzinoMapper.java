package it.calolenoci.mapper;

import it.calolenoci.entity.*;

import javax.enterprise.context.ApplicationScoped;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


@ApplicationScoped
public class MagazzinoMapper {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public Magazzino buildMagazzino(MagazzinoId id, Integer progrGen, FattureDettaglio fd, Fatture f, Ordine ordine){
        Magazzino m = new Magazzino();
        try {
            m.setMagazzinoId(id);
            m.setProgrgenerale(progrGen);
            m.setDataMagazzino(sdf.parse("2050-07-31"));
            m.setNumdocmagazzino(" ");
            m.setDatadocmag(new Date());
            m.setNumfatturamag(" ");
            m.setDatafatturamag(null);
            m.setMcausale("S04");
            m.setMmagazzino("B");
            m.setGruppomag(ordine.getGruppoCliente());
            m.setContomag(ordine.getContoCliente());
            m.setGruppoprop(0);
            m.setContoprop(" ");
            m.setGruppofattura(ordine.getGruppoFattura());
            m.setContofattura(ordine.getContoFattura());
            m.setContromag(" ");
            m.setTiporigomag(" ");
            m.setMArticolo(fd.getFArticolo());
            m.setCodiceean(" ");
            m.setVariante1(" ");
            m.setVariante2(" ");
            m.setVariante3(" ");
            m.setVariante4(" ");
            m.setVariante5(" ");
            m.setMdescrarticolo(fd.getFDescrArticolo());
            m.setMunita(fd.getFunitamisura());
            m.setMcoefficiente(0D);
            m.setMquantita(fd.getQuantita());
            m.setMquantitav(0D);
            m.setMquantita2(0D);
            m.setValore(fd.getPrezzo());
            m.setValoreUnitario(fd.getPrezzo());
            m.setPrezzo(fd.getPrezzo());
            m.setPrezzoextra(fd.getPrezzoextra());
            m.setMvaluta(" ");
            m.setMcambio(0D);
            m.setScontoarticolo(0D);
            m.setScontoc1(0D);
            m.setScontoc2(0D);
            m.setScontop(0D);
            m.setMprovvarticolo(fd.getFprovvarticolo());
            m.setMprovvcliente(fd.getFprovvcliente());
            m.setLottomag(fd.getLottomagf());
            m.setCondpagmag(ordine.getCodicePagamento());
            m.setIvamag(fd.getIva());
            m.setMagente(ordine.getAgente());
            m.setDataprimamag(null);
            m.setMcentrocosto(fd.getFcentrocostor());
            m.setMvocespesa(fd.getFvocespesa());
            m.setMcommessa(fd.getFcommessa());
            m.setMcig(" ");
            m.setMcup(" ");
            m.setMcolli(fd.getFcolli());
            m.setMpallet(fd.getFpallet());
            m.setMmodoconsegna(f.getModoconsegna());
            m.setMvettore(f.getVettore());
            m.setValoreuser(0D);
            m.setDescruser1(" ");
            m.setDescruser2(" ");
            m.setDescruser3(" ");
            m.setDescruser4(" ");
            m.setDescruser5(" ");
            m.setQuantitauser01(0D);
            m.setQuantitauser02(0D);
            m.setQuantitauser03(0D);
            m.setQuantitauser04(0D);
            m.setQuantitauser05(0D);
            m.setDatauser1(null);
            m.setDatauser2(null);
            m.setDatauser3(null);
            m.setDatauser4(null);
            m.setDatauser5(null);
            m.setFlagtrasferito(f.getFlagtrasferito());
            m.setNotemag(" ");
            m.setProvenienza("F");
            m.setPid(fd.getProgrGenerale());
            m.setRifRigaCommessa(0);
            m.setPartitacdeposito(0);
            m.setControllobf(" ");
            m.setBfrigo(0);
            m.setBfverifica(" ");
            m.setSettore(" ");
            m.setOggetto(" ");
            m.setRigogiornale(0);
            m.setDatainserimento(null);
            m.setUsername(" ");
            m.setDatamodifica(null);
            m.setCostomedio(0D);
            m.setSysCreatedate(f.getSysCreatedate());
            m.setSysCreateuser(f.getSysCreateuser());
            m.setSysUpdatedate(f.getSysUpdatedate());
            m.setSysUpdateuser(f.getSysUpdateuser());
            m.setDatascontrino(null);
            m.setPidPrimanota(0);
            return m;
        }catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public Magazzino buildMagazzino(MagazzinoId id, Integer progrGen, OrdineDettaglio o, FattureDettaglio fd, Fatture f, Ordine ordine){
        Magazzino m = new Magazzino();
        try {
            m.setMagazzinoId(id);
            m.setProgrgenerale(progrGen);
            m.setDataMagazzino(sdf.parse("2050-07-31"));
            m.setNumdocmagazzino(" ");
            m.setDatadocmag(new Date());
            m.setNumfatturamag(" ");
            m.setDatafatturamag(null);
            m.setMcausale("S01");
            m.setMmagazzino("B");
            m.setGruppomag(ordine.getGruppoCliente());
            m.setContomag(ordine.getContoCliente());
            m.setGruppoprop(0);
            m.setContoprop(" ");
            m.setGruppofattura(ordine.getGruppoFattura());
            m.setContofattura(ordine.getContoFattura());
            m.setContromag(" ");
            m.setTiporigomag(" ");
            m.setMArticolo(fd.getFArticolo());
            m.setCodiceean(" ");
            m.setVariante1(" ");
            m.setVariante2(" ");
            m.setVariante3(" ");
            m.setVariante4(" ");
            m.setVariante5(" ");
            m.setMdescrarticolo(fd.getFDescrArticolo());
            m.setMunita(fd.getFunitamisura());
            m.setMcoefficiente(0D);
            m.setMquantita(fd.getQuantita());
            m.setMquantitav(o.getQuantitaV());
            m.setMquantita2(o.getQuantita2());
            Double valoreUnitario = fd.getPrezzo();
            if(o.getScontoArticolo() != null) {
                valoreUnitario -= valoreUnitario*(o.getScontoArticolo()/100);
            }
            if(o.getScontoC1() != null) {
                valoreUnitario -= valoreUnitario*(o.getScontoC1()/100);
            }
            if(o.getScontoC2() != null) {
                valoreUnitario -= valoreUnitario*(o.getScontoC2()/100);
            }
            if(o.getScontoP() != null) {
                valoreUnitario -= valoreUnitario*(o.getScontoP()/100);
            }
            m.setValore(valoreUnitario*fd.getQuantita());
            m.setValoreUnitario(valoreUnitario);
            m.setPrezzo(fd.getPrezzo());
            m.setPrezzoextra(fd.getPrezzoextra());
            m.setMvaluta(" ");
            m.setMcambio(0D);
            m.setScontoarticolo(o.getScontoArticolo());
            m.setScontoc1(o.getScontoC1());
            m.setScontoc2(o.getScontoC2());
            m.setScontop(o.getScontoP());
            m.setMprovvarticolo(fd.getFprovvarticolo());
            m.setMprovvcliente(fd.getFprovvcliente());
            m.setLottomag(fd.getLottomagf());
            m.setCondpagmag(ordine.getCodicePagamento());
            m.setIvamag(fd.getIva());
            m.setMagente(ordine.getAgente());
            m.setDataprimamag(null);
            m.setMcentrocosto(fd.getFcentrocostor());
            m.setMvocespesa(fd.getFvocespesa());
            m.setMcommessa(fd.getFcommessa());
            m.setMcig(" ");
            m.setMcup(" ");
            m.setMcolli(fd.getFcolli());
            m.setMpallet(fd.getFpallet());
            m.setMmodoconsegna(f.getModoconsegna());
            m.setMvettore(f.getVettore());
            m.setValoreuser(0D);
            m.setDescruser1(" ");
            m.setDescruser2(" ");
            m.setDescruser3(" ");
            m.setDescruser4(" ");
            m.setDescruser5(" ");
            m.setQuantitauser01(0D);
            m.setQuantitauser02(0D);
            m.setQuantitauser03(0D);
            m.setQuantitauser04(0D);
            m.setQuantitauser05(0D);
            m.setDatauser1(null);
            m.setDatauser2(null);
            m.setDatauser3(null);
            m.setDatauser4(null);
            m.setDatauser5(null);
            m.setFlagtrasferito(f.getFlagtrasferito());
            m.setNotemag(" ");
            m.setProvenienza("F");
            m.setPid(fd.getProgrGenerale());
            m.setRifRigaCommessa(0);
            m.setPartitacdeposito(0);
            m.setControllobf(" ");
            m.setBfrigo(0);
            m.setBfverifica(" ");
            m.setSettore(" ");
            m.setOggetto(" ");
            m.setRigogiornale(0);
            m.setDatainserimento(null);
            m.setUsername(" ");
            m.setDatamodifica(null);
            m.setCostomedio(0D);
            m.setSysCreatedate(f.getSysCreatedate());
            m.setSysCreateuser(f.getSysCreateuser());
            m.setSysUpdatedate(f.getSysUpdatedate());
            m.setSysUpdateuser(f.getSysUpdateuser());
            m.setDatascontrino(null);
            m.setPidPrimanota(0);
            return m;
        }catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Magazzino buildMagazzino(MagazzinoId id, Integer progrGen, OrdineDettaglio o,
                                    Integer gruppoConto, String sottoConto, String numDoc,
                                    String causale, String vettore, LocalDate dataOperazione, String user, LocalDate dataDocumento){
        Magazzino m = new Magazzino();
        Date dOp = Date.from(dataOperazione.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
        Date dDoc = Date.from(dataDocumento.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
            m.setMagazzinoId(id);
            m.setProgrgenerale(progrGen);
            m.setDataMagazzino(dOp);
            m.setNumdocmagazzino(numDoc);
            m.setDatadocmag(dDoc);
            m.setNumfatturamag(" ");
            m.setMcausale(causale);
            m.setMmagazzino("B");
            m.setGruppomag(gruppoConto);
            m.setContomag(sottoConto);
            m.setGruppoprop(0);
            m.setContoprop(" ");
            m.setGruppofattura(0);
            m.setContofattura("");
            m.setContromag(" ");
            m.setTiporigomag(" ");
            m.setMArticolo(o.getFArticolo());
            m.setCodiceean(" ");
            m.setVariante1(" ");
            m.setVariante2(" ");
            m.setVariante3(" ");
            m.setVariante4(" ");
            m.setVariante5(" ");
            m.setMdescrarticolo(o.getFDescrArticolo());
            m.setMunita(o.getFUnitaMisura());
            m.setMcoefficiente(0D);
            m.setMquantita(o.getQuantita() == null ? 0D : o.getQuantita());
            m.setMquantitav(o.getQuantita() == null ? 0D : o.getQuantita());
            m.setMquantita2(o.getQuantita() == null ? 0D : o.getQuantita());
            m.setValore(0D);
            m.setValoreUnitario(0D);
            m.setPrezzo(0D);
            m.setPrezzoextra(0D);
            m.setMvaluta(" ");
            m.setMcambio(0D);
            m.setScontoarticolo(o.getScontoArticolo() == null ? 0D : o.getScontoArticolo());
            m.setScontoc1(o.getScontoC1() == null ? 0D : o.getScontoC1());
            m.setScontoc2(o.getScontoC2() == null ? 0D : o.getScontoC2());
            m.setScontop(o.getScontoP() == null ? 0D : o.getScontoP());
            m.setMprovvarticolo(0D);
            m.setMprovvcliente(0D);
            m.setLottomag("");
            m.setCondpagmag("");
            m.setIvamag("");
            m.setMagente("");
            m.setDataprimamag(null);
            m.setMcentrocosto("");
            m.setMvocespesa("");
            m.setMcommessa("");
            m.setMcig(" ");
            m.setMcup(" ");
            m.setMcolli(o.getFColli() == null ? 0 : o.getFColli());
            m.setMpallet(0D);
            m.setMmodoconsegna("");
            m.setMvettore(vettore);
            m.setValoreuser(0D);
            m.setDescruser1(" ");
            m.setDescruser2(" ");
            m.setDescruser3(" ");
            m.setDescruser4(" ");
            m.setDescruser5(" ");
            m.setQuantitauser01(0D);
            m.setQuantitauser02(0D);
            m.setQuantitauser03(0D);
            m.setQuantitauser04(0D);
            m.setQuantitauser05(0D);
            m.setDatauser1(null);
            m.setDatauser2(null);
            m.setDatauser3(null);
            m.setDatauser4(null);
            m.setDatauser5(null);
            m.setFlagtrasferito("");
            m.setNotemag(" ");
            m.setProvenienza("");
            m.setPid(0);
            m.setRifRigaCommessa(0);
            m.setPartitacdeposito(0);
            m.setControllobf(" ");
            m.setBfrigo(0);
            m.setBfverifica(" ");
            m.setSettore(" ");
            m.setOggetto(" ");
            m.setRigogiornale(0);
            m.setDatainserimento(new Date());
            m.setUsername(" ");
            m.setDatamodifica(null);
            m.setCostomedio(0D);
            m.setSysCreatedate(new Date());
            m.setSysCreateuser(user);
            m.setSysUpdatedate(new Date());
            m.setSysUpdateuser(user);
            m.setDatascontrino(null);
            m.setPidPrimanota(0);
            return m;
    }
}
