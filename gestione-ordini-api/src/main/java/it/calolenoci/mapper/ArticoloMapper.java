package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdineDettaglio;
import it.calolenoci.entity.OrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Year;
import java.util.Date;


@ApplicationScoped
public class ArticoloMapper {

    public void fromDtoToEntity (GoOrdineDettaglio o, OrdineDettaglioDto dto) {
        if(dto.getFlagNonDisponibile() == null) {
            o.setFlagNonDisponibile(Boolean.FALSE);
        } else {
            o.setFlagNonDisponibile(dto.getFlagNonDisponibile());
        }
        if(dto.getFlagOrdinato() == null) {
            o.setFlagOrdinato(Boolean.FALSE);
        } else {
            o.setFlagOrdinato(dto.getFlagOrdinato());
        }
        if(dto.getFlagRiservato() == null) {
            o.setFlagRiservato(Boolean.FALSE);
        } else {
            o.setFlagRiservato(dto.getFlagRiservato());
        }
        if(dto.getFlagConsegnato() == null) {
            o.setFlagConsegnato(Boolean.FALSE);
        } else {
            o.setFlagConsegnato(dto.getFlagConsegnato());
        }
        if(dto.getFlProntoConsegna() == null) {
            o.setFlProntoConsegna(Boolean.FALSE);
        } else {
            o.setFlProntoConsegna(dto.getFlProntoConsegna());
        }
        o.setQtaDaConsegnare(dto.getQtaDaConsegnare());
        if(dto.getQtaConsegnatoSenzaBolla() != null) {
            o.setQtaConsegnatoSenzaBolla(dto.getQtaConsegnatoSenzaBolla());
        }
        if(dto.getQtaProntoConsegna() != null) {
            o.setQtaProntoConsegna(dto.getQtaProntoConsegna());
        }
        if(dto.getQtaRiservata() != null){
            o.setQtaRiservata(dto.getQtaRiservata());
        }
        if(StringUtils.isNotBlank(dto.getNote())){
            o.setNote(dto.getNote());
        }
       // o.setFArticolo(dto.getFArticolo());
    }

    public OrdineDettaglio copia(Integer anno, OrdineDettaglio src,
                                 Integer nuovoProgressivo,
                                 Integer nuovoRigo,
                                 Integer nuovoProgrGenerale,
                                 String user){
        OrdineDettaglio dst = new OrdineDettaglio();

        // ======================
        // 🔹 CHIAVI
        // ======================
        dst.setAnno(anno);
        dst.setSerie(src.getSerie());
        dst.setProgressivo(nuovoProgressivo);
        dst.setRigo(nuovoRigo);

        // ======================
        // 🔹 DATI BASE
        // ======================
        dst.setTipoRigo(src.getTipoRigo());
        dst.setFArticolo(src.getFArticolo());
        dst.setCodArtFornitore(src.getCodArtFornitore());
        dst.setFDescrArticolo(src.getFDescrArticolo());

        dst.setDataConfConsegna(src.getDataConfConsegna());
        dst.setDataRichConsegna(src.getDataRichConsegna());

        dst.setQuantita(src.getQuantita());
        dst.setQuantitaV(src.getQuantitaV());
        dst.setQuantita2(src.getQuantita2());

        dst.setPrezzo(src.getPrezzo());
        dst.setFColli(src.getFColli());
        dst.setTono(src.getTono());
        dst.setFUnitaMisura(src.getFUnitaMisura());

        dst.setScontoArticolo(src.getScontoArticolo());
        dst.setScontoC1(src.getScontoC1());
        dst.setScontoC2(src.getScontoC2());
        dst.setScontoP(src.getScontoP());

        dst.setFCodiceIva(src.getFCodiceIva());
        dst.setNoteOrdCli(src.getNoteOrdCli());

        // ======================
        // 🔹 VARIANTI
        // ======================
        dst.setVariante1(src.getVariante1());
        dst.setVariante2(src.getVariante2());
        dst.setVariante3(src.getVariante3());
        dst.setVariante4(src.getVariante4());
        dst.setVariante5(src.getVariante5());

        dst.setCodiceean(src.getCodiceean());
        dst.setQtaomaggio(src.getQtaomaggio());
        dst.setFcoefficiente(src.getFcoefficiente());
        dst.setPrezzoextra(src.getPrezzoextra());

        dst.setMagazz(src.getMagazz());

        // ======================
        // 🔹 PROVVIGIONI
        // ======================
        dst.setImpprovvfisso(src.getImpprovvfisso());
        dst.setFprovvarticolo(src.getFprovvarticolo());
        dst.setFprovvcliente(src.getFprovvcliente());

        dst.setFpallet(src.getFpallet());
        dst.setCoefprezzo(src.getCoefprezzo());

        dst.setFcentrocostor(src.getFcentrocostor());
        dst.setFcommessa(src.getFcommessa());

        dst.setFgrupporicavo(src.getFgrupporicavo());
        dst.setFcontoricavo(src.getFcontoricavo());

        dst.setFprovenienza(src.getFprovenienza());
        dst.setFpid(src.getFpid());

        // ======================
        // 🔹 USER FIELDS
        // ======================
        dst.setQtyuser1(src.getQtyuser1());
        dst.setQtyuser2(src.getQtyuser2());
        dst.setQtyuser3(src.getQtyuser3());
        dst.setQtyuser4(src.getQtyuser4());
        dst.setQtyuser5(src.getQtyuser5());
        dst.setQtyuser6(src.getQtyuser6());
        dst.setQtyuser7(src.getQtyuser7());

        dst.setDescruser1(src.getDescruser1());
        dst.setDescruser2(src.getDescruser2());
        dst.setDescruser3(src.getDescruser3());
        dst.setDescruser4(src.getDescruser4());
        dst.setDescruser5(src.getDescruser5());
        dst.setDescruser6(src.getDescruser6());
        dst.setDescruser7(src.getDescruser7());

        dst.setDatauser1(src.getDatauser1());
        dst.setDatauser2(src.getDatauser2());
        dst.setDatauser3(src.getDatauser3());
        dst.setDatauser4(src.getDatauser4());
        dst.setDatauser5(src.getDatauser5());
        dst.setDatauser6(src.getDatauser6());
        dst.setDatauser7(src.getDatauser7());

        dst.setContromarca(src.getContromarca());

        // ======================
        // 🔥 RESET LOGICO
        // ======================
        dst.setSaldoAcconto("");

        dst.setProgrGenerale(nuovoProgrGenerale);

        // ======================
        // 🔹 AUDIT
        // ======================
        dst.setSysCreatedate(new Date());
        dst.setSysUpdatedate(new Date());
        dst.setSysCreateuser(user);
        dst.setSysUpdateuser(user);

        return dst;
    }
}
