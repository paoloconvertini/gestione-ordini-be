package it.calolenoci.runner;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.QuadraturaCespite;
import it.calolenoci.entity.TipoSuperAmm;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class RegistroCespiteRunner implements Runnable {


    private Map<String, List<RegistroCespiteDto>> mapTipoCespite;

    private String tipoCespite;

    private int anno;

    private List<CategoriaCespitiDto> result;

    public RegistroCespiteRunner(Map<String, List<RegistroCespiteDto>> mapTipoCespite, String tipoCespite, int anno, List<CategoriaCespitiDto> result) {
        this.mapTipoCespite = mapTipoCespite;
        this.tipoCespite = tipoCespite;
        this.anno = anno;
        this.result = result;
    }

    @Override
    public void run() {
        getRegistroCespiteList();
    }

    private void getRegistroCespiteList () {
        List<RegistroCespiteDto> dtoList = mapTipoCespite.get(tipoCespite);
        CategoriaCespitiDto categoriaCespitiDto = new CategoriaCespitiDto();
        categoriaCespitiDto.setTipoCespite(tipoCespite);
        RegistroCespiteDto dbDto = dtoList.get(0);
        categoriaCespitiDto.setCategoria(dbDto.getDescrTipoCesp());
        categoriaCespitiDto.setPerc(dbDto.getPercAmmortamento());
        Map<Integer, List<RegistroCespiteDto>> progr1Map = dtoList.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getProgressivo1));
        List<CespiteProgressivoDto> cespiteProgressivoDtoList = new ArrayList<>();
        for (Integer progressivo : progr1Map.keySet()) {
            List<RegistroCespiteDto> progressivo1List = progr1Map.get(progressivo);
            Map<Integer, List<RegistroCespiteDto>> progr2Map = progressivo1List.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getProgressivo2));
            List<CespiteDto> cespiteDtoList = new ArrayList<>();
            for (Integer progr2 : progr2Map.keySet()) {
                List<RegistroCespiteDto> progr2List = progr2Map.get(progr2);
                RegistroCespiteDto dbDto1 = progr2List.get(0);
                CespiteDto v = new CespiteDto();
                v.setId(dbDto1.getId());
                v.setProgressivo1(progressivo);
                v.setProgressivo2(progr2);
                v.setCespite(dbDto1.getCespite());
                v.setDataAcq(dbDto1.getDataAcq());
                v.setAmmortamento(dbDto1.getPercAmmortamento());
                v.setImporto(dbDto1.getImporto());
                v.setImportoRivalutazione(dbDto1.getImportoRivalutazione());
                v.setFornitore(dbDto1.getFornitore());
                v.setNumDocAcq(dbDto1.getNumDocAcq());
                v.setAnno(dbDto1.getDataAcq().getYear());
                v.setDataVend(dbDto1.getDataVendita());
                v.setImportoVendita(dbDto1.getImportoVendita());
                v.setProtocollo(dbDto1.getProtocollo());
                v.setAnnoProtocollo(dbDto1.getAnno());
                v.setGiornale(dbDto1.getGiornale());
                if (dbDto1.getSuperAmm() != null && dbDto1.getSuperAmm() != 0L) {
                    v.setSuperAmmDesc(dbDto1.getDescrizioneSuperAmm());
                    v.setIdSuperAmm(dbDto1.getIdSuperAmm());
                    v.setPercSuperAmm(dbDto1.getPercSuperAmm());
                }
                List<AmmortamentoCespite> list = new ArrayList<>();
                progr2List.forEach(d -> list.add(buildAmmortamento(d)));
                List<AmmortamentoCespite> collect = list.stream().filter(a -> a != null && a.getDataAmm() != null && !StringUtils.startsWith(a.getDescrizione(), "VENDITA")).sorted(Comparator.comparing(AmmortamentoCespite::getDataAmm)).collect(Collectors.toList());
                if (!dbDto1.getAttivo()) {
                    if (StringUtils.isNotBlank(dbDto1.getIntestatarioVendita())) {
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "VENDITA")).toList());
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "venduto")).toList());
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "Plus")).toList());
                    } else {
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "ELIMINAZIONE")).toList());
                    }
                }
                v.setAmmortamentoCespiteList(collect);
                cespiteDtoList.add(v);

                CespiteProgressivoDto cespiteProgressivoDto = new CespiteProgressivoDto();
                cespiteProgressivoDto.setProgressivo(progressivo);
                cespiteDtoList.sort(Comparator.comparing(CespiteDto::getDataAcq));
                cespiteProgressivoDto.setCespiteDtoList(cespiteDtoList);
                cespiteProgressivoDtoList.add(cespiteProgressivoDto);
            }
        }
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        List<AmmortamentoCespite> ammortamentoCespiteList2 = new ArrayList<>();
        List<CespiteDto> cespiteDtoList = new ArrayList<>();
        cespiteProgressivoDtoList.forEach(p -> cespiteDtoList.addAll(p.getCespiteDtoList()));
        CespiteSommaDto sommaDto = new CespiteSommaDto();
        FiscaleRiepilogoDto inizioEsercizio = new FiscaleRiepilogoDto();
        inizioEsercizio.setValoreAggiornato(cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).mapToDouble(CespiteDto::getImporto).sum());
        double sum1 = cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).filter(c -> c.getImportoRivalutazione() != null).mapToDouble(CespiteDto::getImportoRivalutazione).sum();
        inizioEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + sum1);
        cespiteDtoList.stream()
                .filter(c -> ((c.getDataVend() == null || (c.getDataVend().getYear() == anno)) && c.getAnno() < anno))
                .forEach(c -> {
                    AmmortamentoCespite a;
                    if (!c.getAmmortamentoCespiteList().isEmpty()) {
                        if (c.getAmmortamentoCespiteList().stream().anyMatch(m -> m.getAnno().equals(anno - 1))) {
                            a = c.getAmmortamentoCespiteList().stream().filter(m -> m.getAnno().equals(anno - 1)).findFirst().get();
                            ammortamentoCespiteList.add(a);
                        } else if (c.getAmmortamentoCespiteList().stream()
                                .filter(amm -> StringUtils.startsWith(amm.getDescrizione(), "Ammortamento"))
                                .noneMatch(m -> m.getAnno().equals(anno))) {
                            a = c.getAmmortamentoCespiteList()
                                    .stream()
                                    .filter(amm -> StringUtils.startsWith(amm.getDescrizione(), "Ammortamento"))
                                    .max(Comparator.comparing(AmmortamentoCespite::getAnno))
                                    .get();
                            ammortamentoCespiteList.add(a);
                        }
                    }

                });

        inizioEsercizio.setFondoAmmortamenti(ammortamentoCespiteList
                .stream()
                .filter(a -> a.getFondo() != null)
                .mapToDouble(AmmortamentoCespite::getFondo)
                .sum()
        );
        double sum2 = ammortamentoCespiteList.stream().filter(a -> a.getFondoRivalutazione() != null).mapToDouble(AmmortamentoCespite::getFondoRivalutazione).sum();
        if (sum2 != 0) {
            inizioEsercizio.setFondoAmmortamentiRiv(sum2);
        }
        inizioEsercizio.setFondoAmmortamentiTot((inizioEsercizio.getFondoAmmortamenti() + inizioEsercizio.getFondoAmmortamentiRiv()));
        inizioEsercizio.setResiduo(inizioEsercizio.getValoreAggiornato() - inizioEsercizio.getFondoAmmortamentiTot());
        sommaDto.setInizioEsercizio(inizioEsercizio);
        Map<String, List<CespiteDto>> superAmmMap = cespiteDtoList.stream().filter(c -> StringUtils.isNotBlank(c.getSuperAmmDesc())).collect(Collectors.groupingBy(CespiteDto::getSuperAmmDesc));
        for (String superAmmDesc : superAmmMap.keySet()) {
            List<CespiteDto> viewDtoList = superAmmMap.get(superAmmDesc);
            CespiteDto cespiteDto = viewDtoList.get(0);
            List<AmmortamentoCespite> list = new ArrayList<>();
            viewDtoList.forEach(c -> list.addAll(c.getAmmortamentoCespiteList()));
            double sum = list.stream()
                    .filter(a -> a.getAnno() == anno - 1 && a.getSuperQuota() != null)
                    .mapToDouble(AmmortamentoCespite::getSuperQuota)
                    .sum();
            if (sum == 0) {
                continue;
            }
            SuperAmmDto dto = new SuperAmmDto();
            dto.setDesc(superAmmDesc);
            dto.setTotale(sum);
            if (cespiteDto.getIdSuperAmm() == 1) {
                sommaDto.setSuperAmm1(dto);
            } else if (cespiteDto.getIdSuperAmm() == 2) {
                sommaDto.setSuperAmm2(dto);
            } else if (cespiteDto.getIdSuperAmm() == 3) {
                sommaDto.setSuperAmm3(dto);
            } else {
                sommaDto.setSuperAmm4(dto);
            }
        }

        FiscaleRiepilogoDto acquisti = new FiscaleRiepilogoDto();
        FiscaleRiepilogoDto vendite = new FiscaleRiepilogoDto();
        acquisti.setValoreAggiornato(cespiteDtoList.stream()
                .filter(c -> c.getAnno() == anno).mapToDouble(CespiteDto::getImporto).sum());
        vendite.setValoreAggiornato(-(cespiteDtoList.stream()
                .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                .mapToDouble(CespiteDto::getImporto)
                .sum()));


        cespiteDtoList.forEach(c -> ammortamentoCespiteList2.addAll(c.getAmmortamentoCespiteList()));
        List<AmmortamentoCespite> ammortamentoCespitesVend = new ArrayList<>();
        cespiteDtoList.stream()
                .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                .toList()
                .forEach(c -> ammortamentoCespitesVend.add(c.getAmmortamentoCespiteList()
                        .stream()
                        .filter(a -> StringUtils.startsWith(a.getDescrizione(), "Ammortamento"))
                        .max(Comparator.comparing(AmmortamentoCespite::getAnno))
                        .get()));

        vendite.setTotaleAmmortamento(-(ammortamentoCespitesVend.stream().mapToDouble(a -> a.getFondo() + a.getFondoRivalutazione()).sum()));
        vendite.setFondoAmmortamenti(inizioEsercizio.getFondoAmmortamenti() + vendite.getAmmortamentoOrdinario());
        vendite.setFondoAmmortamentiRiv(inizioEsercizio.getFondoAmmortamentiRiv() + vendite.getAmmortamentoAnticipato());
        vendite.setFondoAmmortamentiTot(inizioEsercizio.getFondoAmmortamentiTot() + vendite.getTotaleAmmortamento());
        FiscaleRiepilogoDto ammortamentiDeducibili = new FiscaleRiepilogoDto();
        ammortamentiDeducibili.setAmmortamentoOrdinario(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuota() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuota).sum());
        ammortamentiDeducibili.setAmmortamentoAnticipato(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuotaRivalutazione() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuotaRivalutazione).sum());
        ammortamentiDeducibili.setTotaleAmmortamento(ammortamentiDeducibili.getAmmortamentoOrdinario() + ammortamentiDeducibili.getAmmortamentoAnticipato());
        ammortamentiDeducibili.setFondoAmmortamenti(ammortamentiDeducibili.getAmmortamentoOrdinario() + vendite.getFondoAmmortamenti());
        ammortamentiDeducibili.setFondoAmmortamentiRiv(ammortamentiDeducibili.getAmmortamentoAnticipato() + vendite.getFondoAmmortamentiRiv());
        ammortamentiDeducibili.setFondoAmmortamentiTot(ammortamentiDeducibili.getTotaleAmmortamento() + vendite.getFondoAmmortamentiTot());

        FiscaleRiepilogoDto fineEsercizio = new FiscaleRiepilogoDto();
        fineEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + acquisti.getValoreAggiornato() + vendite.getValoreAggiornato());
        fineEsercizio.setFondoAmmortamenti(ammortamentiDeducibili.getFondoAmmortamenti());
        fineEsercizio.setFondoAmmortamentiRiv(ammortamentiDeducibili.getFondoAmmortamentiRiv());
        fineEsercizio.setFondoAmmortamentiTot(fineEsercizio.getFondoAmmortamenti() + fineEsercizio.getFondoAmmortamentiRiv());
        fineEsercizio.setTotaleAmmortamento(ammortamentiDeducibili.getTotaleAmmortamento());
        fineEsercizio.setResiduo(fineEsercizio.getValoreAggiornato() - fineEsercizio.getFondoAmmortamentiTot());

        double plus = ammortamentoCespiteList2
                .stream()
                .filter(a -> a.getAnno() == anno
                        && StringUtils.startsWith(a.getDescrizione(), "Plus"))
                .mapToDouble(AmmortamentoCespite::getQuota).sum();
        if (plus != 0) {
            SuperAmmDto plusDto = new SuperAmmDto();
            plusDto.setDesc("Plus");
            plusDto.setTotale(plus);
            sommaDto.setPlusMinus(plusDto);
        }

        sommaDto.setAcquisti(acquisti);
        sommaDto.setVendite(vendite);
        sommaDto.setAmmortamentiDeducibili(ammortamentiDeducibili);
        sommaDto.setFineEsercizio(fineEsercizio);
        categoriaCespitiDto.setSomma(sommaDto);
        cespiteProgressivoDtoList.sort(Comparator.comparing(CespiteProgressivoDto::getProgressivo));
        categoriaCespitiDto.setCespiteProgressivoDtoList(cespiteProgressivoDtoList);
        result.add(categoriaCespitiDto);
    }
    
    
/*    private void x(){
        List<RegistroCespiteDto> dtoList = mapTipoCespite.get(tipoCespite);
        CategoriaCespitiDto categoriaCespitiDto = new CategoriaCespitiDto();
        categoriaCespitiDto.setTipoCespite(tipoCespite);
        RegistroCespiteDto dbDto = dtoList.get(0);
        categoriaCespitiDto.setCategoria(dbDto.getDescrTipoCesp());
        categoriaCespitiDto.setPerc(dbDto.getPercAmmortamento());
        Map<Integer, List<RegistroCespiteDto>> progr1Map = dtoList.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getProgressivo1));
        List<CespiteProgressivoDto> cespiteProgressivoDtoList = new ArrayList<>();
        for (Integer progressivo : progr1Map.keySet()) {
            List<RegistroCespiteDto> progressivo1List = progr1Map.get(progressivo);
            Map<Integer, List<RegistroCespiteDto>> progr2Map = progressivo1List.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getProgressivo2));
            List<CespiteDto> cespiteDtoList = new ArrayList<>();
            for (Integer progr2 : progr2Map.keySet()) {
                List<RegistroCespiteDto> progr2List = progr2Map.get(progr2);
                RegistroCespiteDto dbDto1 = progr2List.get(0);
                CespiteDto v = new CespiteDto();
                v.setId(dbDto1.getId());
                v.setProgressivo1(progressivo);
                v.setProgressivo2(progr2);
                v.setCespite(dbDto1.getCespite());
                v.setDataAcq(dbDto1.getDataAcq());
                v.setAmmortamento(dbDto1.getPercAmmortamento());
                v.setImporto(dbDto1.getImporto());
                v.setImportoRivalutazione(dbDto1.getImportoRivalutazione());
                v.setFornitore(dbDto1.getFornitore());
                v.setNumDocAcq(dbDto1.getNumDocAcq());
                v.setAnno(dbDto1.getDataAcq().getYear());
                v.setDataVend(dbDto1.getDataVendita());
                v.setImportoVendita(dbDto1.getImportoVendita());
                v.setProtocollo(dbDto1.getProtocollo());
                v.setAnnoProtocollo(dbDto1.getAnno());
                v.setGiornale(dbDto1.getGiornale());
                if (dbDto1.getSuperAmm() != null && dbDto1.getSuperAmm() != 0L) {
                    v.setSuperAmmDesc(dbDto1.getDescrizioneSuperAmm());
                    v.setIdSuperAmm(dbDto1.getIdSuperAmm());
                    v.setPercSuperAmm(dbDto1.getPercSuperAmm());
                }
                List<AmmortamentoCespite> list = new ArrayList<>();
                progr2List.forEach(d -> list.add(buildAmmortamento(d)));
                List<AmmortamentoCespite> collect = list.stream().filter(a -> a != null && a.getDataAmm() != null && !StringUtils.startsWith(a.getDescrizione(), "VENDITA")).sorted(Comparator.comparing(AmmortamentoCespite::getDataAmm)).collect(Collectors.toList());
                if (!dbDto1.getAttivo()) {
                    if (StringUtils.isNotBlank(dbDto1.getIntestatarioVendita())) {
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "VENDITA")).toList());
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "venduto")).toList());
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "Plus")).toList());
                    } else {
                        collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "ELIMINAZIONE")).toList());
                    }
                }
                v.setAmmortamentoCespiteList(collect);
                cespiteDtoList.add(v);

                CespiteProgressivoDto cespiteProgressivoDto = new CespiteProgressivoDto();
                cespiteProgressivoDto.setProgressivo(progressivo);
                cespiteDtoList.sort(Comparator.comparing(CespiteDto::getDataAcq));
                cespiteProgressivoDto.setCespiteDtoList(cespiteDtoList);
                cespiteProgressivoDtoList.add(cespiteProgressivoDto);
            }
        }
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        List<AmmortamentoCespite> ammortamentoCespiteList2 = new ArrayList<>();
        List<CespiteDto> cespiteDtoList = new ArrayList<>();
        cespiteProgressivoDtoList.forEach(p -> cespiteDtoList.addAll(p.getCespiteDtoList()));
        CespiteSommaDto sommaDto = new CespiteSommaDto();
        FiscaleRiepilogoDto inizioEsercizio = new FiscaleRiepilogoDto();
        inizioEsercizio.setValoreAggiornato(cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).mapToDouble(CespiteDto::getImporto).sum());
        double sum1 = cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).filter(c -> c.getImportoRivalutazione() != null).mapToDouble(CespiteDto::getImportoRivalutazione).sum();
        inizioEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + sum1);
        cespiteDtoList.stream()
                .filter(c -> ((c.getDataVend() == null || (c.getDataVend().getYear() == anno)) && c.getAnno() < anno))
                .forEach(c -> {
                    AmmortamentoCespite a;
                    if (!c.getAmmortamentoCespiteList().isEmpty()) {
                        if (c.getAmmortamentoCespiteList().stream().anyMatch(m -> m.getAnno().equals(anno - 1))) {
                            a = c.getAmmortamentoCespiteList().stream().filter(m -> m.getAnno().equals(anno - 1)).findFirst().get();
                            ammortamentoCespiteList.add(a);
                        } else if (c.getAmmortamentoCespiteList().stream()
                                .filter(amm -> StringUtils.startsWith(amm.getDescrizione(), "Ammortamento"))
                                .noneMatch(m -> m.getAnno().equals(anno))) {
                            a = c.getAmmortamentoCespiteList()
                                    .stream()
                                    .filter(amm -> StringUtils.startsWith(amm.getDescrizione(), "Ammortamento"))
                                    .max(Comparator.comparing(AmmortamentoCespite::getAnno))
                                    .get();
                            ammortamentoCespiteList.add(a);
                        }
                    }

                });

        inizioEsercizio.setFondoAmmortamenti(ammortamentoCespiteList
                .stream()
                .filter(a -> a.getFondo() != null)
                .mapToDouble(AmmortamentoCespite::getFondo)
                .sum()
        );
        double sum2 = ammortamentoCespiteList.stream().filter(a -> a.getFondoRivalutazione() != null).mapToDouble(AmmortamentoCespite::getFondoRivalutazione).sum();
        if (sum2 != 0) {
            inizioEsercizio.setFondoAmmortamentiRiv(sum2);
        }
        inizioEsercizio.setFondoAmmortamentiTot((inizioEsercizio.getFondoAmmortamenti() + inizioEsercizio.getFondoAmmortamentiRiv()));
        inizioEsercizio.setResiduo(inizioEsercizio.getValoreAggiornato() - inizioEsercizio.getFondoAmmortamentiTot());
        sommaDto.setInizioEsercizio(inizioEsercizio);
        Map<String, List<CespiteDto>> superAmmMap = cespiteDtoList.stream().filter(c -> StringUtils.isNotBlank(c.getSuperAmmDesc())).collect(Collectors.groupingBy(CespiteDto::getSuperAmmDesc));
        for (String superAmmDesc : superAmmMap.keySet()) {
            List<CespiteDto> viewDtoList = superAmmMap.get(superAmmDesc);
            CespiteDto cespiteDto = viewDtoList.get(0);
            List<AmmortamentoCespite> list = new ArrayList<>();
            viewDtoList.forEach(c -> list.addAll(c.getAmmortamentoCespiteList()));
            double sum = list.stream()
                    .filter(a -> a.getAnno() == anno - 1 && a.getSuperQuota() != null)
                    .mapToDouble(AmmortamentoCespite::getSuperQuota)
                    .sum();
            if (sum == 0) {
                continue;
            }
            SuperAmmDto dto = new SuperAmmDto();
            dto.setDesc(superAmmDesc);
            dto.setTotale(sum);
            if (cespiteDto.getIdSuperAmm() == 1) {
                sommaDto.setSuperAmm1(dto);
            } else if (cespiteDto.getIdSuperAmm() == 2) {
                sommaDto.setSuperAmm2(dto);
            } else if (cespiteDto.getIdSuperAmm() == 3) {
                sommaDto.setSuperAmm3(dto);
            } else {
                sommaDto.setSuperAmm4(dto);
            }
        }

        FiscaleRiepilogoDto acquisti = new FiscaleRiepilogoDto();
        FiscaleRiepilogoDto vendite = new FiscaleRiepilogoDto();
        acquisti.setValoreAggiornato(cespiteDtoList.stream()
                .filter(c -> c.getAnno() == anno).mapToDouble(CespiteDto::getImporto).sum());
        vendite.setValoreAggiornato(-(cespiteDtoList.stream()
                .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                .mapToDouble(CespiteDto::getImporto)
                .sum()));


        cespiteDtoList.forEach(c -> ammortamentoCespiteList2.addAll(c.getAmmortamentoCespiteList()));
        List<AmmortamentoCespite> ammortamentoCespitesVend = new ArrayList<>();
        cespiteDtoList.stream()
                .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                .toList()
                .forEach(c -> ammortamentoCespitesVend.add(c.getAmmortamentoCespiteList()
                        .stream()
                        .filter(a -> StringUtils.startsWith(a.getDescrizione(), "Ammortamento"))
                        .max(Comparator.comparing(AmmortamentoCespite::getAnno))
                        .get()));

        vendite.setTotaleAmmortamento(-(ammortamentoCespitesVend.stream().mapToDouble(a -> a.getFondo() + a.getFondoRivalutazione()).sum()));
        vendite.setFondoAmmortamenti(inizioEsercizio.getFondoAmmortamenti() + vendite.getAmmortamentoOrdinario());
        vendite.setFondoAmmortamentiRiv(inizioEsercizio.getFondoAmmortamentiRiv() + vendite.getAmmortamentoAnticipato());
        vendite.setFondoAmmortamentiTot(inizioEsercizio.getFondoAmmortamentiTot() + vendite.getTotaleAmmortamento());
        FiscaleRiepilogoDto ammortamentiDeducibili = new FiscaleRiepilogoDto();
        ammortamentiDeducibili.setAmmortamentoOrdinario(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuota() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuota).sum());
        ammortamentiDeducibili.setAmmortamentoAnticipato(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuotaRivalutazione() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuotaRivalutazione).sum());
        ammortamentiDeducibili.setTotaleAmmortamento(ammortamentiDeducibili.getAmmortamentoOrdinario() + ammortamentiDeducibili.getAmmortamentoAnticipato());
        ammortamentiDeducibili.setFondoAmmortamenti(ammortamentiDeducibili.getAmmortamentoOrdinario() + vendite.getFondoAmmortamenti());
        ammortamentiDeducibili.setFondoAmmortamentiRiv(ammortamentiDeducibili.getAmmortamentoAnticipato() + vendite.getFondoAmmortamentiRiv());
        ammortamentiDeducibili.setFondoAmmortamentiTot(ammortamentiDeducibili.getTotaleAmmortamento() + vendite.getFondoAmmortamentiTot());

        FiscaleRiepilogoDto fineEsercizio = new FiscaleRiepilogoDto();
        fineEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + acquisti.getValoreAggiornato() + vendite.getValoreAggiornato());
        fineEsercizio.setFondoAmmortamenti(ammortamentiDeducibili.getFondoAmmortamenti());
        fineEsercizio.setFondoAmmortamentiRiv(ammortamentiDeducibili.getFondoAmmortamentiRiv());
        fineEsercizio.setFondoAmmortamentiTot(fineEsercizio.getFondoAmmortamenti() + fineEsercizio.getFondoAmmortamentiRiv());
        fineEsercizio.setTotaleAmmortamento(ammortamentiDeducibili.getTotaleAmmortamento());
        fineEsercizio.setResiduo(fineEsercizio.getValoreAggiornato() - fineEsercizio.getFondoAmmortamentiTot());

        double plus = ammortamentoCespiteList2
                .stream()
                .filter(a -> a.getAnno() == anno
                        && StringUtils.startsWith(a.getDescrizione(), "Plus"))
                .mapToDouble(AmmortamentoCespite::getQuota).sum();
        if (plus != 0) {
            SuperAmmDto plusDto = new SuperAmmDto();
            plusDto.setDesc("Plus");
            plusDto.setTotale(plus);
            sommaDto.setPlusMinus(plusDto);
        }

        sommaDto.setAcquisti(acquisti);
        sommaDto.setVendite(vendite);
        sommaDto.setAmmortamentiDeducibili(ammortamentiDeducibili);
        sommaDto.setFineEsercizio(fineEsercizio);
        categoriaCespitiDto.setSomma(sommaDto);
        cespiteProgressivoDtoList.sort(Comparator.comparing(CespiteProgressivoDto::getProgressivo));
        categoriaCespitiDto.setCespiteProgressivoDtoList(cespiteProgressivoDtoList);
        result.add(categoriaCespitiDto);
    } */


    private AmmortamentoCespite buildAmmortamento(RegistroCespiteDto d) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        //a.setIdAmmortamento(id);
        a.setDataAmm(d.getDataAmm());
        a.setDescrizione(d.getDescrAmm());
        a.setPercAmm(d.getPercAmm());
        a.setFondo(d.getFondo());
        a.setFondoRivalutazione(d.getFondoRivAmm());
        a.setQuota(d.getQuota());
        a.setQuotaRivalutazione(d.getQuotaRivalutazione());
        a.setResiduo(d.getResiduo());
        a.setAnno(d.getAnnoAmm());
        a.setSuperQuota(d.getQuotaSuper());
        a.setSuperPercentuale(d.getPercSuper());
        return a;
    }
}
