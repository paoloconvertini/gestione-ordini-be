package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.GoOrdineFornitoreDettaglioBK;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.time.Year;
import java.util.Date;

@ApplicationScoped
public class OafArticoloMapper {

    public void viewToEntity(OrdineFornitoreDettaglio entity, OrdineFornitoreDettaglioDto dto) {
        entity.setODescrArticolo(dto.getODescrArticolo());
        entity.setOQuantita(dto.getOQuantita());
        entity.setOQuantitaV(dto.getOQuantita());
       entity.setOPrezzo(dto.getOPrezzo());
       entity.setFScontoArticolo(dto.getFScontoArticolo());
       entity.setScontoF1(dto.getScontoF1());
       entity.setScontoF2(dto.getScontoF2());
       entity.setFScontoP(dto.getFScontoP());
        if(dto.getOQuantita() != null && dto.getOPrezzo() != null){
            entity.setValoreTotale(dto.getOQuantita()*dto.getOPrezzo());
        }
    }

    public OrdineFornitoreDettaglio fromDtoToEntity(ArticoloDto dto, String user) {
        OrdineFornitoreDettaglio entity = new OrdineFornitoreDettaglio();
        entity.setAnno(dto.getAnno());
        entity.setSerie(dto.getSerie());
        entity.setProgressivo(dto.getProgressivo());
        entity.setRigo(dto.getRigo());
        entity.setTipoRigo(dto.getTipoRigo());
        entity.setOArticolo(StringUtils.isNotBlank(dto.getArticolo())?dto.getArticolo():" ");
        entity.setODescrArticolo(dto.getDescrArticolo());
        entity.setMagazzino(" ");
        entity.setProvenienza(" ");
        entity.setNota(" ");
        entity.setPid(0);
        entity.setOCodiceIva(" ");
        entity.setOQuantita(dto.getQuantita()!=null?dto.getQuantita():0D);
        entity.setSaldo(" ");
        entity.setOColli(0);
        entity.setCampoUser5(" ");
        entity.setOvocespesa(" ");
        entity.setOQuantitaV(dto.getQuantita()!=null?dto.getQuantita():0D);
        entity.setOUnitaMisura(StringUtils.isNotBlank(dto.getUnitaMisura())?dto.getUnitaMisura():" ");
        entity.setOPrezzo(dto.getPrezzoBase()!=null?dto.getPrezzoBase():0D);
        entity.setFScontoArticolo(dto.getFScontoArticolo() != null?dto.getFScontoArticolo():0D);
        entity.setScontoF1(dto.getScontoF1() != null?dto.getScontoF1():0D);
        entity.setScontoF2(dto.getScontoF2() != null?dto.getScontoF2():0D);
        entity.setFScontoP(dto.getFScontoP() != null?dto.getFScontoP():0D);
        Integer progrGenerale = OrdineFornitoreDettaglio.find("SELECT MAX(progrGenerale) FROM OrdineFornitoreDettaglio").project(Integer.class).singleResult();
        entity.setProgrGenerale(progrGenerale + 1);
        if(dto.getQuantita() != null && dto.getPrezzoBase() != null){
            entity.setValoreTotale(dto.getQuantita()*dto.getPrezzoBase());
        } else {
            entity.setValoreTotale(0D);
        }
        entity.setOquantita2(0D);
        entity.setSaldo(" ");
        entity.setSinonimo1(0);
        entity.setVariante1(" ");
        entity.setVariante2(" ");
        entity.setVariante3(" ");
        entity.setVariante4(" ");
        entity.setVariante5(" ");
        entity.setCodiceean(" ");
        entity.setOcoefficiente(0D);
        entity.setPrezzoextra(0D);
        entity.setDataconfconseg(new Date());
        entity.setDatarichconseg(new Date());
        entity.setOlottomagf(" ");
        entity.setOpallet(0D);
        entity.setOcommessa(" ");
        entity.setOcentrocostor(" ");
        entity.setImpprovvfisso(0D);
        entity.setOprovvarticolo(0D);
        entity.setOprovvfornitore(0D);
        entity.setQtyuser1(0D);
        entity.setQtyuser2(0D);
        entity.setQtyuser3(0D);
        entity.setQtyuser4(0D);
        entity.setQtyuser5(0D);
        entity.setCampouser1(" ");
        entity.setCampouser2(" ");
        entity.setCampouser3(" ");
        entity.setCampouser4(" ");
        entity.setPidPrimanota(0);
        entity.setUsername(user);
        entity.setSysCreatedate(new Date());
        entity.setSysUpdatedate(new Date());
        entity.setSysCreateuser(user);
        entity.setSysUpdateuser(user);
        return entity;
    }

    public GoOrdineFornitoreDettaglioBK copyOAFDettaglio(OrdineFornitoreDettaglio dto) {
        GoOrdineFornitoreDettaglioBK entity = new GoOrdineFornitoreDettaglioBK();
        entity.setAnno(dto.getAnno());
        entity.setSerie(dto.getSerie());
        entity.setProgressivo(dto.getProgressivo());
        entity.setRigo(dto.getRigo());
        entity.setTipoRigo(dto.getTipoRigo());
        entity.setOArticolo(dto.getOArticolo());
        entity.setODescrArticolo(dto.getODescrArticolo());
        entity.setMagazzino(dto.getMagazzino());
        entity.setProvenienza(dto.getProvenienza());
        entity.setNota(dto.getNota());
        entity.setPid(dto.getPid());
        entity.setOCodiceIva(dto.getOCodiceIva());
        entity.setOQuantita(dto.getOQuantita());
        entity.setSaldo(dto.getSaldo());
        entity.setOColli(dto.getOColli());
        entity.setCampoUser5(dto.getCampoUser5());
        entity.setOvocespesa(dto.getOvocespesa());
        entity.setOQuantitaV(dto.getOQuantitaV());
        entity.setOUnitaMisura(dto.getOUnitaMisura());
        entity.setOPrezzo(dto.getOPrezzo());
        entity.setFScontoArticolo(dto.getFScontoArticolo());
        entity.setScontoF1(dto.getScontoF1());
        entity.setScontoF2(dto.getScontoF2());
        entity.setFScontoP(dto.getFScontoP());
        entity.setProgrGenerale(dto.getProgrGenerale());
        entity.setValoreTotale(dto.getValoreTotale());
        entity.setOquantita2(dto.getOquantita2());
        entity.setSaldo(dto.getSaldo());
        entity.setSinonimo1(dto.getSinonimo1());
        entity.setVariante1(dto.getVariante1());
        entity.setVariante2(dto.getVariante2());
        entity.setVariante3(dto.getVariante3());
        entity.setVariante4(dto.getVariante4());
        entity.setVariante5(dto.getVariante5());
        entity.setCodiceean(dto.getCodiceean());
        entity.setOcoefficiente(dto.getOcoefficiente());
        entity.setPrezzoextra(dto.getPrezzoextra());
        entity.setDataconfconseg(dto.getDataconfconseg());
        entity.setDatarichconseg(dto.getDatarichconseg());
        entity.setOlottomagf(dto.getOlottomagf());
        entity.setOpallet(dto.getOpallet());
        entity.setOcommessa(dto.getOcommessa());
        entity.setOcentrocostor(dto.getOcentrocostor());
        entity.setImpprovvfisso(dto.getImpprovvfisso());
        entity.setOprovvarticolo(dto.getOprovvarticolo());
        entity.setOprovvfornitore(dto.getOprovvfornitore());
        entity.setQtyuser1(dto.getQtyuser1());
        entity.setQtyuser2(dto.getQtyuser2());
        entity.setQtyuser3(dto.getQtyuser3());
        entity.setQtyuser4(dto.getQtyuser4());
        entity.setQtyuser5(dto.getQtyuser5());
        entity.setCampouser1(dto.getCampouser1());
        entity.setCampouser2(dto.getCampouser2());
        entity.setCampouser3(dto.getCampouser3());
        entity.setCampouser4(dto.getCampouser4());
        entity.setPidPrimanota(dto.getPidPrimanota());
        entity.setUsername(dto.getUsername());
        entity.setSysCreatedate(dto.getSysCreatedate());
        entity.setSysUpdatedate(dto.getSysUpdatedate());
        entity.setSysCreateuser(dto.getSysCreateuser());
        entity.setSysUpdateuser(dto.getSysUpdateuser());
        return entity;
    }

   public OrdineFornitoreDettaglio copyEntity(OrdineFornitoreDettaglio o, Integer progrGenerale, Integer rigo,
                                              Integer progressivoFornDettaglio, OrdineDettaglio ordineDettaglio){
       OrdineFornitoreDettaglio fornitoreDettaglio = new OrdineFornitoreDettaglio();
       fornitoreDettaglio.setProgressivo(o.getProgressivo());
       fornitoreDettaglio.setProgrGenerale(progressivoFornDettaglio+1);
       fornitoreDettaglio.setAnno(o.getAnno());
       fornitoreDettaglio.setSerie(o.getSerie());
       fornitoreDettaglio.setRigo(rigo + 1);
       fornitoreDettaglio.setTipoRigo(o.getTipoRigo());
       fornitoreDettaglio.setPid(progrGenerale);
       fornitoreDettaglio.setOArticolo(o.getOArticolo());
       fornitoreDettaglio.setODescrArticolo(o.getODescrArticolo());
       fornitoreDettaglio.setOQuantita(ordineDettaglio.getQuantita());
       fornitoreDettaglio.setOQuantitaV(ordineDettaglio.getQuantita());
       fornitoreDettaglio.setOquantita2(0D);
       fornitoreDettaglio.setOUnitaMisura(o.getOUnitaMisura());
       fornitoreDettaglio.setOColli(o.getOColli());
       fornitoreDettaglio.setOCodiceIva(o.getOCodiceIva());
       fornitoreDettaglio.setProvenienza("C");
       fornitoreDettaglio.setMagazzino("B");
       if (fornitoreDettaglio.getOQuantita() != null && o.getOPrezzo() != null) {
           fornitoreDettaglio.setValoreTotale(fornitoreDettaglio.getOQuantita() * o.getOPrezzo());
       }
       String campoUser5 = "VS.ART." + ordineDettaglio.getCodArtFornitore();
       fornitoreDettaglio.setCampoUser5(StringUtils.truncate(campoUser5, 25));
       String nota = "Riferimento n. " + ordineDettaglio.getAnno() + "/" + ordineDettaglio.getSerie() + "/" + ordineDettaglio.getProgressivo() + "-" + ordineDettaglio.getRigo();
       fornitoreDettaglio.setNota(nota);
       fornitoreDettaglio.setSaldo(o.getSaldo());
       fornitoreDettaglio.setSinonimo1(o.getSinonimo1());
       fornitoreDettaglio.setVariante1(o.getVariante1());
       fornitoreDettaglio.setVariante2(o.getVariante2());
       fornitoreDettaglio.setVariante3(o.getVariante3());
       fornitoreDettaglio.setVariante4(o.getVariante4());
       fornitoreDettaglio.setVariante5(o.getVariante5());
       fornitoreDettaglio.setCodiceean(o.getCodiceean());
       fornitoreDettaglio.setOcoefficiente(o.getOcoefficiente());
       fornitoreDettaglio.setPrezzoextra(o.getPrezzoextra());
       fornitoreDettaglio.setDataconfconseg(new Date());
       fornitoreDettaglio.setDatarichconseg(new Date());
       fornitoreDettaglio.setOlottomagf(o.getOlottomagf());
       fornitoreDettaglio.setOpallet(o.getOpallet());
       fornitoreDettaglio.setOcommessa(o.getOcommessa());
       fornitoreDettaglio.setOcentrocostor(o.getOcentrocostor());
       fornitoreDettaglio.setImpprovvfisso(o.getImpprovvfisso());
       fornitoreDettaglio.setOprovvarticolo(o.getOprovvarticolo());
       fornitoreDettaglio.setOprovvfornitore(o.getOprovvfornitore());
       fornitoreDettaglio.setQtyuser1(o.getQtyuser1());
       fornitoreDettaglio.setQtyuser2(o.getQtyuser2());
       fornitoreDettaglio.setQtyuser3(o.getQtyuser3());
       fornitoreDettaglio.setQtyuser4(o.getQtyuser4());
       fornitoreDettaglio.setQtyuser5(o.getQtyuser5());
       fornitoreDettaglio.setCampouser1(o.getCampouser1());
       fornitoreDettaglio.setCampouser2(o.getCampouser2());
       fornitoreDettaglio.setCampouser3(o.getCampouser3());
       fornitoreDettaglio.setCampouser4(o.getCampouser4());
       fornitoreDettaglio.setPidPrimanota(o.getPidPrimanota());
       fornitoreDettaglio.setUsername(o.getUsername());
       fornitoreDettaglio.setSysCreatedate(new Date());
       fornitoreDettaglio.setSysUpdatedate(new Date());
       fornitoreDettaglio.setSysCreateuser(o.getSysCreateuser());
       fornitoreDettaglio.setSysUpdateuser(o.getSysUpdateuser());
       fornitoreDettaglio.setOvocespesa(o.getOvocespesa());
       fornitoreDettaglio.setOPrezzo(o.getOPrezzo());
       fornitoreDettaglio.setFScontoArticolo(o.getFScontoArticolo());
       fornitoreDettaglio.setScontoF1(o.getScontoF1());
       fornitoreDettaglio.setScontoF2(o.getScontoF2());
       fornitoreDettaglio.setFScontoP(o.getFScontoP());
       return fornitoreDettaglio;
    }

    public OrdineFornitoreDettaglio createRigoRiferimento(String serie, Integer progressivo, String intestazione, Integer rigo, Integer progrGenerale, String user) {
        OrdineFornitoreDettaglio ordineFornitoreDettaglio = new OrdineFornitoreDettaglio();
        ordineFornitoreDettaglio.setTipoRigo("C");
        ordineFornitoreDettaglio.setRigo(rigo + 1);
        ordineFornitoreDettaglio.setAnno(Year.now().getValue());
        ordineFornitoreDettaglio.setSerie(serie);
        ordineFornitoreDettaglio.setProgressivo(progressivo);
        ordineFornitoreDettaglio.setODescrArticolo("Rif. " + intestazione);
        ordineFornitoreDettaglio.setProgrGenerale(progrGenerale + 1);
        ordineFornitoreDettaglio.setNota(" ");
        ordineFornitoreDettaglio.setOQuantita(0D);
        ordineFornitoreDettaglio.setOQuantitaV(0D);
        ordineFornitoreDettaglio.setOquantita2(0D);
        ordineFornitoreDettaglio.setOUnitaMisura(" ");
        ordineFornitoreDettaglio.setOColli(0);
        ordineFornitoreDettaglio.setOCodiceIva(" ");
        ordineFornitoreDettaglio.setProvenienza(" ");
        ordineFornitoreDettaglio.setMagazzino(" ");
        ordineFornitoreDettaglio.setPid(0);
        ordineFornitoreDettaglio.setOArticolo(" ");
        ordineFornitoreDettaglio.setCampoUser5(" ");
        settaCampi(user, ordineFornitoreDettaglio);

        return ordineFornitoreDettaglio;
    }

    public void settaCampi(String user, OrdineFornitoreDettaglio ordineFornitoreDettaglio) {
        ordineFornitoreDettaglio.setSaldo(" ");
        ordineFornitoreDettaglio.setSinonimo1(0);
        ordineFornitoreDettaglio.setVariante1(" ");
        ordineFornitoreDettaglio.setVariante2(" ");
        ordineFornitoreDettaglio.setVariante3(" ");
        ordineFornitoreDettaglio.setVariante4(" ");
        ordineFornitoreDettaglio.setVariante5(" ");
        ordineFornitoreDettaglio.setCodiceean(" ");
        ordineFornitoreDettaglio.setOcoefficiente(0D);
        ordineFornitoreDettaglio.setPrezzoextra(0D);
        ordineFornitoreDettaglio.setDataconfconseg(new Date());
        ordineFornitoreDettaglio.setDatarichconseg(new Date());
        ordineFornitoreDettaglio.setOlottomagf(" ");
        ordineFornitoreDettaglio.setOpallet(0D);
        ordineFornitoreDettaglio.setOcommessa(" ");
        ordineFornitoreDettaglio.setOcentrocostor(" ");
        ordineFornitoreDettaglio.setImpprovvfisso(0D);
        ordineFornitoreDettaglio.setOprovvarticolo(0D);
        ordineFornitoreDettaglio.setOprovvfornitore(0D);
        ordineFornitoreDettaglio.setQtyuser1(0D);
        ordineFornitoreDettaglio.setQtyuser2(0D);
        ordineFornitoreDettaglio.setQtyuser3(0D);
        ordineFornitoreDettaglio.setQtyuser4(0D);
        ordineFornitoreDettaglio.setQtyuser5(0D);
        ordineFornitoreDettaglio.setCampouser1(" ");
        ordineFornitoreDettaglio.setCampouser2(" ");
        ordineFornitoreDettaglio.setCampouser3(" ");
        ordineFornitoreDettaglio.setCampouser4(" ");
        ordineFornitoreDettaglio.setPidPrimanota(0);
        ordineFornitoreDettaglio.setUsername(user);
        ordineFornitoreDettaglio.setSysCreatedate(new Date());
        ordineFornitoreDettaglio.setSysUpdatedate(new Date());
        ordineFornitoreDettaglio.setSysCreateuser(user);
        ordineFornitoreDettaglio.setSysUpdateuser(user);
        ordineFornitoreDettaglio.setOvocespesa(" ");
        ordineFornitoreDettaglio.setOPrezzo(0D);
        ordineFornitoreDettaglio.setValoreTotale(0D);
        ordineFornitoreDettaglio.setFScontoArticolo(0D);
        ordineFornitoreDettaglio.setScontoF1(0D);
        ordineFornitoreDettaglio.setScontoF2(0D);
        ordineFornitoreDettaglio.setFScontoP(0D);
    }
}
