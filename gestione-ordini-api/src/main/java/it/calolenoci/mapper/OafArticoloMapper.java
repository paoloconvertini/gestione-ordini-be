package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloDto;
import it.calolenoci.dto.OrdineFornitoreDettaglioDto;
import it.calolenoci.entity.GoOrdineFornitoreDettaglioBK;
import it.calolenoci.entity.OrdineFornitoreDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
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
}
