package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.*;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.TipoSuperAmm;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class AmmortamentoCespiteService {

    @Inject
    AmmortamentoCespiteMapper mapper;

    public static final DecimalFormat df = new DecimalFormat("0.00");

    @Transactional
    public void calcola(LocalDate date) {
        try {
            List<Cespite> cespitiAttivi = Cespite.find("attivo = 'T'").list();
            LocalDate dataCorrente = LocalDate.now();
            if (date != null) {
                dataCorrente = date;
            }
            AmmortamentoCespite.delete("idAmmortamento in (:list)", Parameters.with("list", cespitiAttivi.stream().map(Cespite::getId).collect(Collectors.toList())));
            for (Cespite cespite : cespitiAttivi) {
                boolean eliminato = cespite.getDataVendita() != null && StringUtils.isBlank(cespite.getIntestatarioVendita());
                boolean venduto = cespite.getDataVendita() != null && StringUtils.isNotBlank(cespite.getIntestatarioVendita());
                if (eliminato || venduto) {
                    dataCorrente = cespite.getDataVendita();
                }
                double percAmmortamento = cespite.getAmmortamento();
                double percAmmPrimoAnno = cespite.getAmmortamento() / 2;
                double perc;
                double quotaDaSalvare;
                double quota = cespite.getImporto() * (percAmmortamento / 100);
                double quotaPrimoAnno = cespite.getImporto() * (percAmmPrimoAnno / 100);
                double fondo = 0;
                double residuo = cespite.getImporto();
                LocalDate dataInizio = cespite.getDataAcq();
                List<AmmortamentoCespite> cespiteList = new ArrayList<>();
                int counter = 1;
                LocalDate dataAmmortamento = LocalDate.of(dataInizio.getYear(), Month.DECEMBER, 31);
                while (residuo > 0 && dataAmmortamento.getYear() <= dataCorrente.getYear()
                ) {
                    if (dataAmmortamento.getYear() == dataCorrente.getYear()) {
                        dataAmmortamento = dataCorrente;
                    }
                    if (counter == 1) {
                        quotaDaSalvare = quotaPrimoAnno * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                    } else {
                        quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                    }
                    if (residuo < quotaDaSalvare) {
                        quotaDaSalvare = residuo;
                        residuo = 0;
                        fondo = cespite.getImporto();
                    } else {
                        fondo += quotaDaSalvare;
                        residuo = cespite.getImporto() - fondo;
                    }
                    perc = quotaDaSalvare / cespite.getImporto() * 100;
                    AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, fondo, residuo, dataAmmortamento);
                    calcolaSuperAmm(cespite, perc, a);
                    cespiteList.add(a);
                    dataAmmortamento = dataAmmortamento.plusYears(1);
                    counter++;
                }
                if (eliminato) {
                    cespiteList.add(mapper.buildEliminato(cespite.getId(), cespite.getDataVendita()));
                }
                if (venduto) {
                    cespiteList.addAll(mapper.buildVenduto(cespite, residuo));
                }
                if (eliminato || venduto) {
                    cespite.setAttivo(Boolean.FALSE);
                    Cespite.persist(cespite);
                }
                AmmortamentoCespite.persist(cespiteList);
            }
        } catch (Exception e) {
            Log.error("Errore calcolo ammortamento", e);
        }
    }

    private void calcolaSuperAmm(Cespite cespite, double perc, AmmortamentoCespite a) {
        if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
            TipoSuperAmm tipoSuperAmm = TipoSuperAmm.findById(cespite.getSuperAmm());
            double importSuperAmm = (tipoSuperAmm.getPerc() * cespite.getImporto()) / 100;
            double quotaSuperAmm = (perc * importSuperAmm) / 100;
            a.setSuperPercentuale(perc);
            a.setSuperQuota(quotaSuperAmm);
        }
    }

    public CespiteView getAll() {
        final CespiteView view = new CespiteView();
        List<CespiteDto> result = new ArrayList<>();
        try {
            List<CespiteDBDto> cespiteDtos = Cespite.find("SELECT c, a, cat.descrtipocesp " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite cat ON cat.tipocespite = c.tipoCespite").project(CespiteDBDto.class).list();
            Map<String, List<CespiteDBDto>> map = cespiteDtos.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getTipoCespite()));
            for (String tipoCespite : map.keySet()) {
                List<CespiteDBDto> dtoList = map.get(tipoCespite);
                CespiteDto cespiteDto = new CespiteDto();
                cespiteDto.setTipoCespite(tipoCespite);
                CespiteDBDto dbDto = dtoList.get(0);
                cespiteDto.setCategoria(dbDto.getCategoria());
                Map<Integer, List<CespiteDBDto>> progr1Map = dtoList.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getProgressivo1()));
                List<CespiteProgressivoDto> cespiteProgressivoDtoList = new ArrayList<>();
                for (Integer progressivo : progr1Map.keySet()) {
                    List<CespiteDBDto> progressivo1List = progr1Map.get(progressivo);
                    Map<Integer, List<CespiteDBDto>> progr2Map = progressivo1List.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getProgressivo2()));
                    List<CespiteViewDto> cespiteViewDtoList = new ArrayList<>();
                    for (Integer progr2 : progr2Map.keySet()) {
                        List<CespiteDBDto> progr2List = progr2Map.get(progr2);
                        CespiteDBDto dbDto1 = progr2List.get(0);
                        CespiteViewDto v = new CespiteViewDto();
                        Cespite cespite = dbDto1.getCespite();
                        v.setCodice(cespite.getCodice());
                        v.setProgressivo1(progressivo);
                        v.setProgressivo2(progr2);
                        v.setCespite(cespite.getCespite());
                        v.setDataAcq(cespite.getDataAcq());
                        v.setAmmortamento(cespite.getAmmortamento());
                        v.setImporto(cespite.getImporto());
                        v.setFornitore(cespite.getFornitore());
                        v.setNumDocAcq(cespite.getNumDocAcq());
                        v.setAnno(cespite.getDataAcq().getYear());
                        if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
                            TipoSuperAmm tipoSuperAmm = TipoSuperAmm.findById(cespite.getSuperAmm());
                            v.setSuperAmmDesc(tipoSuperAmm.getDescrizione());
                        }
                        List<AmmortamentoCespite> list = new ArrayList<>();
                        progr2List.forEach(d -> list.add(d.getAmmortamentoCespite()));
                        List<AmmortamentoCespite> collect = list.stream().filter(a -> a.getDataAmm() != null && !StringUtils.startsWith(a.getDescrizione(), "VENDITA")).sorted(Comparator.comparing(AmmortamentoCespite::getDataAmm)).collect(Collectors.toList());
                        if (!cespite.getAttivo()) {
                            if (StringUtils.isNotBlank(cespite.getIntestatarioVendita())) {
                                collect.addAll(collect.size(), list.stream().filter(a -> StringUtils.startsWith(a.getDescrizione(), "VENDITA")).toList());
                                collect.addAll(collect.size(), list.stream().filter(a -> StringUtils.startsWith(a.getDescrizione(), "venduto")).toList());
                                collect.addAll(collect.size(), list.stream().filter(a -> StringUtils.startsWith(a.getDescrizione(), "Plus")).toList());
                            } else {
                                collect.addAll(collect.size(), list.stream().filter(a -> StringUtils.startsWith(a.getDescrizione(), "ELIMINAZIONE")).toList());
                            }
                        }
                        v.setAmmortamentoCespiteList(collect);
                        cespiteViewDtoList.add(v);
                    }
                    CespiteProgressivoDto cespiteProgressivoDto = new CespiteProgressivoDto();
                    cespiteProgressivoDto.setProgressivo(progressivo);
                    cespiteProgressivoDto.setCespiteViewDtoList(cespiteViewDtoList);
                    cespiteProgressivoDtoList.add(cespiteProgressivoDto);
                }
                List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
                List<CespiteViewDto> cespiteViewDtoList = new ArrayList<>();
                cespiteProgressivoDtoList.forEach(p -> cespiteViewDtoList.addAll(p.getCespiteViewDtoList()));
                CespiteSommaDto sommaDto = new CespiteSommaDto();
                FiscaleRiepilogo inizioEsercizio = new FiscaleRiepilogo();
                inizioEsercizio.setValoreAggiornato(cespiteViewDtoList.stream().filter(c -> c.getAnno() < Year.now().getValue()).mapToDouble(CespiteViewDto::getImporto).sum());
                cespiteViewDtoList.forEach(c -> ammortamentoCespiteList.addAll(c.getAmmortamentoCespiteList()));
                inizioEsercizio.setFondoAmmortamenti(ammortamentoCespiteList
                        .stream()
                        .filter(a -> a.getAnno() == Year.now().minusYears(1).getValue())
                        .mapToDouble(AmmortamentoCespite::getFondo)
                        .sum()
                );
                inizioEsercizio.setResiduo(inizioEsercizio.getValoreAggiornato() - inizioEsercizio.getFondoAmmortamenti());
                sommaDto.setInizioEsercizio(inizioEsercizio);
                Map<String, List<CespiteViewDto>> superAmmMap = cespiteViewDtoList.stream().filter(c -> StringUtils.isNotBlank(c.getSuperAmmDesc())).collect(Collectors.groupingBy(CespiteViewDto::getSuperAmmDesc));
                for (String superAmmDesc : superAmmMap.keySet()) {
                    List<CespiteViewDto> viewDtoList = superAmmMap.get(superAmmDesc);
                    List<AmmortamentoCespite> list = new ArrayList<>();
                    viewDtoList.forEach(c -> list.addAll(c.getAmmortamentoCespiteList()));
                    double sum = list.stream()
                            .filter(a -> a.getAnno() == Year.now().minusYears(1).getValue() && a.getSuperQuota() != null)
                            .mapToDouble(AmmortamentoCespite::getSuperQuota)
                            .sum();
                    TipoSuperAmm t = TipoSuperAmm.find("descrizione = :desc", Parameters.with("desc", superAmmDesc)).firstResult();
                    SuperAmmDto dto = new SuperAmmDto();
                    dto.setDesc(superAmmDesc);
                    dto.setTotale(sum);
                    if (t.id == 1) {
                        sommaDto.setSuperAmm1(dto);
                    } else if (t.id == 2) {
                        sommaDto.setSuperAmm2(dto);
                    } else {
                        sommaDto.setSuperAmm3(dto);
                    }
                }

                FiscaleRiepilogo acquisti = new FiscaleRiepilogo();
                FiscaleRiepilogo vendite = new FiscaleRiepilogo();
                acquisti.setValoreAggiornato(cespiteViewDtoList.stream()
                        .filter(c -> c.getAnno() == Year.now().getValue()).mapToDouble(CespiteViewDto::getImporto).sum());
                vendite.setValoreAggiornato(cespiteViewDtoList.stream()
                        .filter(c -> c.getAnno() == Year.now().getValue() && c.getImportoVendita() != null).mapToDouble(CespiteViewDto::getImportoVendita).sum());

                FiscaleRiepilogo ammortamentiDeducibili = new FiscaleRiepilogo();
                ammortamentiDeducibili.setAmmortamentoOrdinario(ammortamentoCespiteList.stream().filter(a -> a.getAnno() == Year.now().minusYears(1).getValue() && a.getQuota() != null).mapToDouble(AmmortamentoCespite::getQuota).sum());
                ammortamentiDeducibili.setTotaleAmmortamento(ammortamentiDeducibili.getAmmortamentoOrdinario() + ammortamentiDeducibili.getAmmortamentoAnticipato());
                ammortamentiDeducibili.setFondoAmmortamenti(ammortamentiDeducibili.getTotaleAmmortamento() + inizioEsercizio.getFondoAmmortamenti());

                FiscaleRiepilogo fineEsercizio = new FiscaleRiepilogo();
                fineEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + acquisti.getValoreAggiornato() - vendite.getValoreAggiornato());
                fineEsercizio.setFondoAmmortamenti(ammortamentiDeducibili.getFondoAmmortamenti());
                fineEsercizio.setResiduo(fineEsercizio.getValoreAggiornato() - fineEsercizio.getFondoAmmortamenti());

                double plus = ammortamentoCespiteList
                        .stream()
                        .filter(a -> a.getAnno() == Year.now().getValue()
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
                cespiteDto.setSomma(sommaDto);
                cespiteDto.setCespiteProgressivoDtoList(cespiteProgressivoDtoList);
                result.add(cespiteDto);
            }
            result.sort(Comparator.comparing(CespiteDto::getTipoCespite));
            view.setCespiteList(result);
            CespiteSommaDto sommaDto = new CespiteSommaDto();

            FiscaleRiepilogo inizio = new FiscaleRiepilogo();
            inizio.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getValoreAggiornato()).sum());
            inizio.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getFondoAmmortamenti()).sum());
            inizio.setResiduo(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getResiduo()).sum());

            FiscaleRiepilogo fine = new FiscaleRiepilogo();
            fine.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getValoreAggiornato()).sum());
            fine.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getFondoAmmortamenti()).sum());
            fine.setResiduo(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getResiduo()).sum());

            FiscaleRiepilogo acquisti = new FiscaleRiepilogo();
            FiscaleRiepilogo vendite = new FiscaleRiepilogo();
            acquisti.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getAcquisti().getValoreAggiornato()).sum());
            vendite.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getVendite().getValoreAggiornato()).sum());

            FiscaleRiepilogo ammortamentiDeducibili = new FiscaleRiepilogo();
            ammortamentiDeducibili.setAmmortamentoOrdinario(result.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getAmmortamentoOrdinario()).sum());
            ammortamentiDeducibili.setTotaleAmmortamento(result.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getTotaleAmmortamento()).sum());
            ammortamentiDeducibili.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getFondoAmmortamenti()).sum());

            if (result.stream().anyMatch(c -> c.getSomma().getSuperAmm1() != null && c.getSomma().getSuperAmm1().getTotale() != null)) {
                TipoSuperAmm tipo1 = TipoSuperAmm.findById(1L);
                sommaDto.setSuperAmm1(new SuperAmmDto(tipo1.getDescrizione(), result
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm1() != null && c.getSomma().getSuperAmm1().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm1().getTotale())
                        .sum()));
            }
            if (result.stream().anyMatch(c -> c.getSomma().getSuperAmm2() != null && c.getSomma().getSuperAmm2().getTotale() != null)) {
                TipoSuperAmm tipo2 = TipoSuperAmm.findById(2L);
                sommaDto.setSuperAmm2(new SuperAmmDto(tipo2.getDescrizione(), result
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm2() != null && c.getSomma().getSuperAmm2().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm2().getTotale())
                        .sum()));
            }
            if (result.stream().anyMatch(c -> c.getSomma().getSuperAmm3() != null && c.getSomma().getSuperAmm3().getTotale() != null)) {
                sommaDto.setSuperAmm3(new SuperAmmDto(((TipoSuperAmm) TipoSuperAmm.findById(3L)).getDescrizione(), result
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm3() != null && c.getSomma().getSuperAmm3().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm3().getTotale())
                        .sum()));
            }
            if (result.stream().anyMatch(c -> c.getSomma().getPlusMinus() != null && c.getSomma().getPlusMinus().getTotale() != null)) {
                sommaDto.setPlusMinus(new SuperAmmDto("Plus", result
                        .stream()
                        .filter(c -> c.getSomma().getPlusMinus() != null && c.getSomma().getPlusMinus().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getPlusMinus().getTotale())
                        .sum()));
            }
            sommaDto.setInizioEsercizio(inizio);
            sommaDto.setAcquisti(acquisti);
            sommaDto.setVendite(vendite);
            sommaDto.setAmmortamentiDeducibili(ammortamentiDeducibili);
            sommaDto.setFineEsercizio(fine);
            view.setCespiteSommaDto(sommaDto);
        } catch (Exception e) {
            Log.error("Errore get all cespiti", e);
            return view;
        }
        return view;
    }

    @Transactional
    public void updateCespiti(CespiteRequest c) {
        int update = Cespite.update("ammortamento = :p WHERE id =:id", Parameters.with("p", c.getPerc()).and("id", c.getId()));
        if (update > 0) {
            AmmortamentoCespite.deleteById(c.getId());
        }
    }

    public void cercaCespitiVenduti() {

    }

    public void cercaCespiti() {

    }
}
