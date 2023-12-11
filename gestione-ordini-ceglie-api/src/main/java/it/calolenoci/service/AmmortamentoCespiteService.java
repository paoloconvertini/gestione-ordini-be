package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import it.calolenoci.mapper.QuadCespiteMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AmmortamentoCespiteService {

    @Inject
    AmmortamentoCespiteMapper mapper;

    @Inject
    JasperService jasperService;

    @Inject
    QuadCespiteMapper quadCespiteMapper;

    @Transactional
    public void calcola(LocalDate date) {
        try {
            long inizio = System.currentTimeMillis();
            List<Cespite> cespitiAttivi = Cespite.find("attivo = 'T'").list();
            LocalDate dataCorrente = LocalDate.now();
            if (date != null) {
                dataCorrente = date;
            }
            AmmortamentoCespite.delete("idAmmortamento in (:list)", Parameters.with("list", cespitiAttivi.stream().map(Cespite::getId).collect(Collectors.toList())));
            for (Cespite cespite : cespitiAttivi) {
                List<AmmortamentoCespite> ammortamentoCespites = calcoloSingoloCespite(cespite, dataCorrente);
                if(ammortamentoCespites.isEmpty()) {
                    continue;
                }
                AmmortamentoCespite.persist(ammortamentoCespites);
            }
            long fine = System.currentTimeMillis();
            Log.info("Metodo calcola ammortamenti: " + (fine - inizio) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore calcolo ammortamento", e);
        }
    }


    public List<AmmortamentoCespite> calcoloSingoloCespite(Cespite cespite, LocalDate dataCorrente){
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        CategoriaCespite categoriaCespite = CategoriaCespite.find("tipoCespite=:t", Parameters.with("t", cespite.getTipoCespite())).firstResult();
        boolean eliminato = cespite.getDataVendita() != null && StringUtils.isBlank(cespite.getIntestatarioVendita());
        boolean venduto = cespite.getDataVendita() != null && StringUtils.isNotBlank(cespite.getIntestatarioVendita());
        if (eliminato || venduto) {
            dataCorrente = cespite.getDataVendita();
        }
        if (categoriaCespite.getPercAmmortamento() != null && categoriaCespite.getPercAmmortamento() != 0) {
            double percAmmortamento = categoriaCespite.getPercAmmortamento();
            double percAmmPrimoAnno = categoriaCespite.getPercAmmortamento() / 2;
            double perc;
            double quotaDaSalvare;
            double quota = cespite.getImporto() * (percAmmortamento / 100);
            double quotaPrimoAnno = cespite.getImporto() * (percAmmPrimoAnno / 100);
            double fondo = 0;
            double residuo = cespite.getImporto();
            LocalDate dataInizio = cespite.getDataInizioCalcoloAmm();

            int counter = 1;
            LocalDate dataAmmortamento = LocalDate.of(dataInizio.getYear(), Month.DECEMBER, 31);
            while (residuo > 0 && dataAmmortamento.getYear() <= dataCorrente.getYear()) {
                if (dataAmmortamento.getYear() == dataCorrente.getYear()) {
                    dataAmmortamento = dataCorrente;
                }
                if (counter == 1 && cespite.getFlPrimoAnno()) {
                    quotaDaSalvare = quotaPrimoAnno * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                } else {
                    quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                }
                Optional<QuadraturaCespite> optQuad = QuadraturaCespite.find("idCespite =:id AND anno=:a",
                        Parameters.with("id", cespite.getId()).and("a", dataAmmortamento.getYear())).singleResultOptional();
                if(optQuad.isPresent()){
                    QuadraturaCespite q = optQuad.get();
                    quota = cespite.getImporto() * (q.getAmmortamento() / 100);
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
                ammortamentoCespiteList.add(a);
                dataAmmortamento = dataAmmortamento.plusYears(1);
                counter++;
            }
            if (eliminato) {
                ammortamentoCespiteList.add(mapper.buildEliminato(cespite.getId(), cespite.getDataVendita()));
            }
            if (venduto) {
                ammortamentoCespiteList.addAll(mapper.buildVenduto(cespite, residuo));
            }
            if (eliminato || venduto) {
                cespite.setAttivo(Boolean.FALSE);
                Cespite.persist(cespite);
            }
            return ammortamentoCespiteList;
        }
        return ammortamentoCespiteList;
    }

    private void calcolaSuperAmm(Cespite cespite, double perc, AmmortamentoCespite a) {
        if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
            TipoSuperAmm tipoSuperAmm = TipoSuperAmm.findById(cespite.getSuperAmm());
            double importSuperAmm = (tipoSuperAmm.getPerc() * cespite.getImporto()) / 100;
            double quotaSuperAmm = (perc * importSuperAmm) / 100;
            if (a.getResiduo() < quotaSuperAmm) {
                quotaSuperAmm = a.getResiduo();
            }
            a.setSuperPercentuale(perc);
            a.setSuperQuota(quotaSuperAmm);
        }
    }

    public CespiteView getAll(FiltroCespite filtroCespite) {
        LocalDate localDate = LocalDate.now();
        if(StringUtils.isNotBlank(filtroCespite.getData())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            localDate = LocalDate.parse(filtroCespite.getData(), formatter);
        }
        int anno = localDate.getYear();
        long inizioTempo = System.currentTimeMillis();
        final CespiteView view = new CespiteView();
        List<CespiteDto> result = new ArrayList<>();
        try {
            String query = "SELECT c, a, cat " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite ";
            Map<String, Object> params = new HashMap<>();
            if (StringUtils.isNotBlank(filtroCespite.getTipoCespite())) {
                query += "WHERE cat.tipoCespite = :q";
                params.put("q", filtroCespite.getTipoCespite());
            }
            List<CespiteDBDto> cespiteDtos = Cespite.find(query, params).project(CespiteDBDto.class).list();
            String queryNoAmm = "SELECT c, cat " +
                    "FROM Cespite c " +
                    "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite " +
                    "WHERE NOT EXISTS (SELECT 1 FROM AmmortamentoCespite a WHERE a.idAmmortamento = c.id) ";
            Map<String, Object> paramsNoAmm = new HashMap<>();
            if (filtroCespite != null && StringUtils.isNotBlank(filtroCespite.getTipoCespite())) {
                queryNoAmm += "AND cat.tipoCespite = :q";
                paramsNoAmm.put("q", filtroCespite.getTipoCespite());
            }
            List<CespiteDBDto> listNoAmm = Cespite.find(queryNoAmm, paramsNoAmm).project(CespiteDBDto.class).list();
            if (!listNoAmm.isEmpty()) {
                cespiteDtos.addAll(listNoAmm);
            }
            Map<String, List<CespiteDBDto>> mapTipoCespite = cespiteDtos.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getTipoCespite()));
            for (String tipoCespite : mapTipoCespite.keySet()) {
                List<CespiteDBDto> dtoList = mapTipoCespite.get(tipoCespite);
                CespiteDto cespiteDto = new CespiteDto();
                cespiteDto.setTipoCespite(tipoCespite);
                CespiteDBDto dbDto = dtoList.get(0);
                cespiteDto.setCategoria(dbDto.getCategoria().getDescrizione());
                cespiteDto.setPerc(dbDto.getCategoria().getPercAmmortamento());
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
                        v.setId(cespite.getId());
                        v.setCodice(dbDto.getCategoria().getCodice());
                        v.setProgressivo1(progressivo);
                        v.setProgressivo2(progr2);
                        v.setCespite(cespite.getCespite());
                        v.setDataAcq(cespite.getDataAcq());
                        v.setAmmortamento(dbDto.getCategoria().getPercAmmortamento());
                        v.setImporto(cespite.getImporto());
                        v.setFornitore(cespite.getFornitore());
                        v.setNumDocAcq(cespite.getNumDocAcq());
                        v.setAnno(cespite.getDataAcq().getYear());
                        v.setDataVend(cespite.getDataVendita());
                        v.setImportoVendita(cespite.getImportoVendita());
                        v.setProtocollo(cespite.getProtocollo());
                        v.setAnnoProtocollo(cespite.getAnno());
                        v.setGiornale(cespite.getGiornale());
                        if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
                            TipoSuperAmm tipoSuperAmm = TipoSuperAmm.findById(cespite.getSuperAmm());
                            v.setSuperAmmDesc(tipoSuperAmm.getDescrizione());
                        }
                        List<AmmortamentoCespite> list = new ArrayList<>();
                        progr2List.forEach(d -> list.add(d.getAmmortamentoCespite()));
                        List<AmmortamentoCespite> collect = list.stream().filter(a -> a != null && a.getDataAmm() != null && !StringUtils.startsWith(a.getDescrizione(), "VENDITA")).sorted(Comparator.comparing(AmmortamentoCespite::getDataAmm)).collect(Collectors.toList());
                        if (!cespite.getAttivo()) {
                            if (StringUtils.isNotBlank(cespite.getIntestatarioVendita())) {
                                collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "VENDITA")).toList());
                                collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "venduto")).toList());
                                collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "Plus")).toList());
                            } else {
                                collect.addAll(collect.size(), list.stream().filter(a -> a != null && StringUtils.startsWith(a.getDescrizione(), "ELIMINAZIONE")).toList());
                            }
                        }
                        v.setAmmortamentoCespiteList(collect);
                        cespiteViewDtoList.add(v);
                    }
                    CespiteProgressivoDto cespiteProgressivoDto = new CespiteProgressivoDto();
                    cespiteProgressivoDto.setProgressivo(progressivo);
                    cespiteViewDtoList.sort(Comparator.comparing(CespiteViewDto::getDataAcq));
                    cespiteProgressivoDto.setCespiteViewDtoList(cespiteViewDtoList);
                    cespiteProgressivoDtoList.add(cespiteProgressivoDto);
                }
                List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
                List<AmmortamentoCespite> ammortamentoCespiteList2 = new ArrayList<>();
                List<CespiteViewDto> cespiteViewDtoList = new ArrayList<>();
                cespiteProgressivoDtoList.forEach(p -> cespiteViewDtoList.addAll(p.getCespiteViewDtoList()));
                CespiteSommaDto sommaDto = new CespiteSommaDto();
                FiscaleRiepilogo inizioEsercizio = new FiscaleRiepilogo();
                inizioEsercizio.setValoreAggiornato(cespiteViewDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).mapToDouble(CespiteViewDto::getImporto).sum());
                cespiteViewDtoList.forEach(c -> {
                    AmmortamentoCespite a;
                    if (!c.getAmmortamentoCespiteList().isEmpty()) {
                        if (c.getAmmortamentoCespiteList().stream().anyMatch(m -> m.getAnno().equals(anno - 1))) {
                            a = c.getAmmortamentoCespiteList().stream().filter(m -> m.getAnno().equals(anno - 1)).findFirst().get();
                            ammortamentoCespiteList.add(a);
                        } else if (c.getAmmortamentoCespiteList().stream().noneMatch(m -> m.getAnno().equals(anno))) {
                            a = c.getAmmortamentoCespiteList().get(c.getAmmortamentoCespiteList().size() - 1);
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
                inizioEsercizio.setResiduo(inizioEsercizio.getValoreAggiornato() - inizioEsercizio.getFondoAmmortamenti());
                sommaDto.setInizioEsercizio(inizioEsercizio);
                Map<String, List<CespiteViewDto>> superAmmMap = cespiteViewDtoList.stream().filter(c -> StringUtils.isNotBlank(c.getSuperAmmDesc())).collect(Collectors.groupingBy(CespiteViewDto::getSuperAmmDesc));
                for (String superAmmDesc : superAmmMap.keySet()) {
                    List<CespiteViewDto> viewDtoList = superAmmMap.get(superAmmDesc);
                    List<AmmortamentoCespite> list = new ArrayList<>();
                    viewDtoList.forEach(c -> list.addAll(c.getAmmortamentoCespiteList()));
                    double sum = list.stream()
                            .filter(a -> a.getAnno() == anno - 1 && a.getSuperQuota() != null)
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
                        .filter(c -> c.getAnno() == anno).mapToDouble(CespiteViewDto::getImporto).sum());
                vendite.setValoreAggiornato(-(cespiteViewDtoList.stream()
                        .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                        .mapToDouble(CespiteViewDto::getImporto)
                        .sum()));


                cespiteViewDtoList.forEach(c -> ammortamentoCespiteList2.addAll(c.getAmmortamentoCespiteList()));
                List<AmmortamentoCespite> ammortamentoCespitesVend = new ArrayList<>();
                cespiteViewDtoList.stream()
                        .filter(c -> c.getImportoVendita() != null)
                        .toList()
                        .forEach(c -> ammortamentoCespitesVend.add(c.getAmmortamentoCespiteList()
                                .stream()
                                .filter(a -> StringUtils.startsWith(a.getDescrizione(), "Ammortamento"))
                                .max(Comparator.comparing(AmmortamentoCespite::getAnno))
                                .get()));
                vendite.setTotaleAmmortamento(-(ammortamentoCespitesVend.stream().filter(a -> a.getAnno() == anno).mapToDouble(AmmortamentoCespite::getFondo).sum()));
                vendite.setFondoAmmortamenti(inizioEsercizio.getFondoAmmortamenti() + vendite.getTotaleAmmortamento());
                FiscaleRiepilogo ammortamentiDeducibili = new FiscaleRiepilogo();
                ammortamentiDeducibili.setAmmortamentoOrdinario(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuota() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuota).sum());
                ammortamentiDeducibili.setTotaleAmmortamento(ammortamentiDeducibili.getAmmortamentoOrdinario() + ammortamentiDeducibili.getAmmortamentoAnticipato());
                ammortamentiDeducibili.setFondoAmmortamenti(ammortamentiDeducibili.getTotaleAmmortamento() + vendite.getFondoAmmortamenti());

                FiscaleRiepilogo fineEsercizio = new FiscaleRiepilogo();
                fineEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + acquisti.getValoreAggiornato() + vendite.getValoreAggiornato());
                fineEsercizio.setFondoAmmortamenti(ammortamentiDeducibili.getFondoAmmortamenti());
                fineEsercizio.setTotaleAmmortamento(ammortamentiDeducibili.getTotaleAmmortamento());
                fineEsercizio.setResiduo(fineEsercizio.getValoreAggiornato() - fineEsercizio.getFondoAmmortamenti());

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
                cespiteDto.setSomma(sommaDto);
                cespiteProgressivoDtoList.sort(Comparator.comparing(CespiteProgressivoDto::getProgressivo));
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
            long fineTempo = System.currentTimeMillis();
            Log.info("Metodo get ammortamenti: " + (fineTempo - inizioTempo) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore get Registro cespiti", e);
            throw e;
        }
        return view;
    }

    @Transactional
    public void updateCespiti(CespiteRequest c) {
        Cespite cespite = Cespite.findById(c.getId());
        int update = 0;
        if (cespite != null) {
            if (c.getPerc() != null) {
                update = Cespite.update("ammortamento = :p WHERE id =:id", Parameters.with("p", c.getPerc()).and("id", c.getId()));
            }
            if (StringUtils.isNotBlank(c.getTipoCespite())) {
                Optional<Cespite> optCat = Cespite.find("tipoCespite = :t", Sort.descending("progressivo1"), Parameters.with("t", c.getTipoCespite())).firstResultOptional();
                int progr1 = 1;
                if (optCat.isPresent()) {
                    progr1 = optCat.get().getProgressivo1() + 1;
                }
                update = Cespite.update("tipoCespite = :t, progressivo1 =:p, progressivo2 = 1 WHERE id =:id",
                        Parameters.with("t", c.getTipoCespite()).and("p", progr1).and("id", c.getId()));
                if (update > 0) {
                    CategoriaCespite categoriaCespite = CategoriaCespite.find("tipoCespite = :t", Parameters.with("t", c.getTipoCespite())).firstResult();
                    Optional<Primanota> optional = Primanota.find("anno =:a AND giornale =:g AND protocollo =:p AND importo =:i",
                            Parameters.with("a", cespite.getAnno()).and("g", cespite.getGiornale())
                                    .and("p", cespite.getProtocollo()).and("i", cespite.getImporto())).firstResultOptional();
                    if (optional.isPresent()) {
                        Primanota primanota = optional.get();
                        primanota.setGruppoconto(categoriaCespite.getCostoGruppo());
                        primanota.setSottoconto(categoriaCespite.getCostoConto());
                        primanota.persist();
                    }
                }
            }
            if (update > 0) {
                AmmortamentoCespite.deleteById(c.getId());
            }
        }
    }

    public void createCespite(PrimanotaDto dto) {
        try {
            Optional<CategoriaCespite> categoriaCespiteOptional = CategoriaCespite.find("costoGruppo=:g AND costoConto=:c",
                    Parameters.with("g", dto.getGruppoconto()).and("c", dto.getSottoconto())).firstResultOptional();
            if (categoriaCespiteOptional.isPresent()) {
                Cespite c = new Cespite();
                c.setCespite(dto.getDescrsuppl());
                c.setAttivo(Boolean.TRUE);
                c.setImporto(dto.getImporto());
                c.setDataAcq(dto.getDatamovimento());
                c.setNumDocAcq(dto.getNumerodocumento());
                CategoriaCespite categoriaCespite = CategoriaCespite.find("costoGruppo=:g AND costoConto=:c",
                        Parameters.with("g", dto.getGruppoconto()).and("c", dto.getSottoconto())).firstResult();
                c.setTipoCespite(categoriaCespite.getTipoCespite());
                Optional<Cespite> optionalCespite = Cespite.find("tipoCespite=:t", Sort.descending("progressivo1"),
                        Parameters.with("t", categoriaCespite.getTipoCespite())).firstResultOptional();
                Primanota primanota = Primanota.find("anno = :a AND giornale=:g AND protocollo=:p AND progrprimanota=1",
                        Parameters.with("a", dto.getAnno()).and("g", dto.getGiornale())
                                .and("p", dto.getProtocollo())).firstResult();
                int progr1 = 1;
                Integer progr2 = 1;
                if (optionalCespite.isPresent()) {
                    progr1 = optionalCespite.get().getProgressivo1() + 1;
                    progr2 = optionalCespite.get().getProgressivo2();
                }
                FatturepaixIn fattura = FatturepaixIn.findById(primanota.getPid());
                if (fattura != null) {
                    c.setFornitore(fattura.getFornitoreDenom() + StringUtils.SPACE + primanota.getGruppoconto() + " - " + primanota.getSottoconto());
                    //TODO salvare nomefile fattura
                } else {
                    PianoConti pianoConti = PianoConti.find("gruppoConto=:g AND sottoConto =:c", Parameters.with("g", primanota.getGruppoconto())
                            .and("c", primanota.getSottoconto())).firstResult();
                    c.setFornitore(pianoConti.getIntestazione() + StringUtils.SPACE + primanota.getGruppoconto() + " - " + primanota.getSottoconto());
                }
                c.setProgressivo1(progr1);
                c.setProgressivo2(progr2);
                c.setProtocollo(dto.getProtocollo());
                c.setGiornale(dto.getGiornale());
                c.setAnno(dto.getAnno());
                c.setFlPrimoAnno(Boolean.TRUE);
                c.setDataInizioCalcoloAmm(c.getDataAcq());
                c.persist();
            } else {
                Log.debug("Cespite non creato");
            }
        } catch (Exception e) {
            Log.error("Error creating new cespite", e);
            throw e;
        }
    }


    public File scaricaRegistroCespiti(FiltroCespite filtroCespite) {
        File report;
        try {
            report = jasperService.createReport(filtroCespite);
        } catch (Exception e) {
            report = null;
            Log.error("error scarica regisro");
        }
        return report;
    }

    @Transactional
    public void saveQuadratura(QuadraturaCespiteRequest quad) {
        Optional<QuadraturaCespite> opt = QuadraturaCespite.find("idCespite =:id AND anno=:a",
                Parameters.with("id", quad.getIdCespite()).and("a", quad.getAnno())).singleResultOptional();
        QuadraturaCespite q;
        if(opt.isEmpty()) {
            q = quadCespiteMapper.fromDtoToEntity(quad);
        } else {
            q = opt.get();
            q.setAmmortamento(quad.getAmmortamento());
        }
        q.persist();
    }

}
