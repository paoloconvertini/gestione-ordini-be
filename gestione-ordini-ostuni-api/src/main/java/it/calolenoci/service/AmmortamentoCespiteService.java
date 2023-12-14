package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import it.calolenoci.mapper.QuadCespiteMapper;
import it.calolenoci.runner.CalcoloCespiteRun;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AmmortamentoCespiteService {

    @Inject
    AmmortamentoCespiteMapper mapper;

    @Inject
    JasperService jasperService;

    @Inject
    QuadCespiteMapper quadCespiteMapper;

    @ConfigProperty(name = "storico")
    boolean storico;

    @Inject
    EntityManager em;

    @Transactional
    @TransactionConfiguration(timeout = 5000000)
    public void calcola(FiltroCespite filtroCespite) {
        try {
            long inizio = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDate date = LocalDate.parse(filtroCespite.getData(), formatter);
            List<CespiteDBDto> cespitiAttivi = Cespite.find("SELECT c, cat " +
                    "FROM Cespite c " +
                    "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite " +
                    "WHERE c.attivo = 'T' AND c.tipoCespite = :t",
                    Parameters.with("t", filtroCespite.getTipoCespite())).project(CespiteDBDto.class).list();
            LocalDate dataCorrente = LocalDate.now();
            if (date != null) {
                dataCorrente = date;
            }
            if (storico) {
                AmmortamentoCespite.delete("idAmmortamento in (:list)", Parameters.with("list", cespitiAttivi.stream().map(c -> c.getCespite().getId()).collect(Collectors.toList())));
                for (CespiteDBDto cespite : cespitiAttivi) {
                    List<AmmortamentoCespite> ammortamentoCespites;
                    if (cespite.getCespite().getImportoRivalutazione() != null) {
                        ammortamentoCespites = calcoloSingoloCespiteRivalutato(cespite, dataCorrente);
                    } else {
                        ammortamentoCespites = calcoloSingoloCespite(cespite, dataCorrente);
                    }

                    if (ammortamentoCespites.isEmpty()) {
                        continue;
                    }
                    AmmortamentoCespite.persist(ammortamentoCespites);
                }
            } else {
                AmmortamentoCespite.delete("idAmmortamento in (:list) and anno >=:a", Parameters.with("list", cespitiAttivi.stream().map(c -> c.getCespite().getId()).collect(Collectors.toList())).and("a", dataCorrente.getYear()));
                for (CespiteDBDto cespite : cespitiAttivi) {
                    List<AmmortamentoCespite> ammortamentoCespites = calcoloAnnoCorrente(cespite, dataCorrente);
                    if (ammortamentoCespites.isEmpty()) {
                        continue;
                    }
                    AmmortamentoCespite.persist(ammortamentoCespites);
                }
            }
            long fine = System.currentTimeMillis();
            Log.info("Metodo calcola ammortamenti: " + (fine - inizio) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore calcolo ammortamento", e);
        }
    }

    private List<AmmortamentoCespite> calcoloAnnoCorrente(CespiteDBDto dto, LocalDate dataCorrente) {
        Cespite cespite = dto.getCespite();
        CategoriaCespite categoriaCespite = dto.getCategoria();
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
//        boolean eliminato = cespite.getDataVendita() != null && StringUtils.isBlank(cespite.getIntestatarioVendita());
//        boolean venduto = cespite.getDataVendita() != null && StringUtils.isNotBlank(cespite.getIntestatarioVendita());
//        if (eliminato || venduto) {
//            dataCorrente = cespite.getDataVendita();
//        }
        int anno = dataCorrente.getYear();
        Optional<AmmortamentoCespite> optAmmAnnoPrec = AmmortamentoCespite.find("idAmmortamento =:id and anno=:a", Parameters.with("id", cespite.getId()).and("a", anno - 1)).singleResultOptional();
        List<AmmortamentoCespite> listAmmortamenti = AmmortamentoCespite.find("idAmmortamento =:id", Parameters.with("id", cespite.getId())).list();
        if (categoriaCespite.getPercAmmortamento() != null && categoriaCespite.getPercAmmortamento() != 0) {
            if (listAmmortamenti.isEmpty()) {
                // cespite nuovo
                if (dto.getCespite().getImportoRivalutazione() != null) {
                    ammortamentoCespiteList = calcoloSingoloCespiteRivalutato(dto, dataCorrente);
                } else {
                    ammortamentoCespiteList = calcoloSingoloCespite(dto, dataCorrente);
                }
            } else if (optAmmAnnoPrec.isPresent()) {
                // cespite presente, calcolo anni a partire da data selezionata
                AmmortamentoCespite ammPrecedente = optAmmAnnoPrec.get();
                if (ammPrecedente.getResiduo() != 0) {
                    int annoPrecedente = ammPrecedente.getAnno();
                    LocalDate dataAmmortamento = LocalDate.of(annoPrecedente + 1, Month.DECEMBER, 31);
                    while (ammPrecedente.getResiduo() != 0 && annoPrecedente < anno) {
                        double quota = cespite.getImporto() * (ammPrecedente.getPercAmm() / 100);
                        double quotaRiv = 0;
                        double fondo = ammPrecedente.getFondo();
                        double fondoRiv = ammPrecedente.getFondoRivalutazione();
                        double residuoDaSalvare = ammPrecedente.getResiduo();
                        if (cespite.getImportoRivalutazione() != null) {
                            quotaRiv = cespite.getImportoRivalutazione() * (ammPrecedente.getPercAmm() / 100);
                            if (cespite.getFondoRivalutazione() != null) {
                                fondoRiv = cespite.getFondoRivalutazione();
                            }
                        }
                        double quotaDaSalvare;
                        double quotaRivDaSalvare;
                        Optional<QuadraturaCespite> optQuad = QuadraturaCespite.find("idCespite =:id AND anno=:a",
                                Parameters.with("id", cespite.getId()).and("a", dataCorrente.getYear())).singleResultOptional();
                        if (optQuad.isPresent()) {
                            QuadraturaCespite q = optQuad.get();
                            quota = cespite.getImporto() * (q.getAmmortamento() / 100);
                        }
                        quotaDaSalvare = quota * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
                        quotaRivDaSalvare = quotaRiv * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
                        fondo += quotaDaSalvare;
                        fondoRiv += quotaRivDaSalvare;
                        double perc = quotaDaSalvare / cespite.getImporto() * 100;
                        residuoDaSalvare -= quotaDaSalvare;
                        residuoDaSalvare -= quotaRivDaSalvare;
                        AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, quotaRivDaSalvare, fondo, fondoRiv, residuoDaSalvare, dataAmmortamento);
                        ammortamentoCespiteList.add(a);
                        dataAmmortamento = dataAmmortamento.plusYears(1);
                        annoPrecedente++;
                        ammPrecedente = a;
                    }
                }
            } else {
                //cespite presente ma già consolidato
                return ammortamentoCespiteList;
            }
        }
        return ammortamentoCespiteList;
    }


    public List<AmmortamentoCespite> calcoloSingoloCespite(CespiteDBDto dto, LocalDate dataCorrente) {
        Cespite cespite = dto.getCespite();
        CategoriaCespite categoriaCespite = dto.getCategoria();
        Log.info("Inizio calcolo cespite: " + cespite.getCespite());
        long inizio = System.currentTimeMillis();
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
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
            double residuo = cespite.getImporto();
            double quotaPrimoAnno = cespite.getImporto() * (percAmmPrimoAnno / 100);
            double fondo = 0;
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
                if (optQuad.isPresent()) {
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

                if (cespite.getImporto() == 0) {
                    perc = 0;
                } else {
                    perc = quotaDaSalvare / cespite.getImporto() * 100;
                }
                AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, 0, fondo, 0, residuo, dataAmmortamento);
                calcolaSuperAmm(cespite, perc, a);
                ammortamentoCespiteList.add(a);
                dataAmmortamento = dataAmmortamento.plusYears(1);
                counter++;
            }

            return buildEliminatoVenduto(cespite, inizio, ammortamentoCespiteList, eliminato, venduto, residuo);
        }
        return ammortamentoCespiteList;
    }

    public List<AmmortamentoCespite> calcoloSingoloCespiteRivalutato(CespiteDBDto dto, LocalDate dataCorrente) {
        Cespite cespite = dto.getCespite();
        CategoriaCespite categoriaCespite = dto.getCategoria();
        Log.info("Inizio calcolo cespite: " + cespite.getCespite());
        long inizio = System.currentTimeMillis();
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
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
            double quotaRiv = 0;
            double residuo = cespite.getImporto();
            double fondoRiv = 0;
            if (cespite.getImportoRivalutazione() != null) {
                quotaRiv = cespite.getImportoRivalutazione() * (percAmmortamento / 100);
                residuo += cespite.getImportoRivalutazione();
                if (cespite.getFondoRivalutazione() != null) {
                    fondoRiv = cespite.getFondoRivalutazione();
                }
            }
            double quotaPrimoAnno = cespite.getImporto() * (percAmmPrimoAnno / 100);
            double fondo = 0;
            double residuoNoRiv = cespite.getImporto();
            double quotaRivDaSalvare = quotaRiv;
            if (cespite.getImportoRivalutazione() != null) {
                residuo += cespite.getImportoRivalutazione();
            }
            LocalDate dataInizio = cespite.getDataInizioCalcoloAmm();

            double residuoDaSalvare;
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
                if (optQuad.isPresent()) {
                    QuadraturaCespite q = optQuad.get();
                    quota = cespite.getImporto() * (q.getAmmortamento() / 100);
                    quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                }

                if (cespite.getImportoRivalutazione() != null) {
                    if (dataAmmortamento.getYear() < 2021) {
                        if (residuoNoRiv < quotaDaSalvare) {
                            quotaDaSalvare = residuoNoRiv;
                            residuoNoRiv = 0;
                            fondo = cespite.getImporto();
                        } else {
                            fondo += quotaDaSalvare;
                            residuoNoRiv = cespite.getImporto() - fondo;
                        }
                        residuoDaSalvare = residuoNoRiv;
                        quotaRivDaSalvare = 0;
                    } else {
                        if (residuo < quotaRiv) {
                            quotaRiv = residuo;
                            residuo = 0;
                            fondoRiv = cespite.getImporto() + cespite.getImportoRivalutazione();
                        } else {
                            fondoRiv += quotaRiv;
                            residuo = cespite.getImportoRivalutazione() - fondoRiv;
                        }

                        if (residuoNoRiv < quotaDaSalvare) {
                            quotaDaSalvare = residuoNoRiv;
                            residuoNoRiv = 0;
                            fondo = cespite.getImporto();
                        } else {
                            fondo += quotaDaSalvare;
                            residuoNoRiv = cespite.getImporto() - fondo;
                        }
                        residuoDaSalvare = residuo;
                        quotaRivDaSalvare = quotaRiv;
                    }
                } else {
                    if (residuoNoRiv < quotaDaSalvare) {
                        quotaDaSalvare = residuoNoRiv;
                        residuoNoRiv = 0;
                        fondo = cespite.getImporto();
                    } else {
                        fondo += quotaDaSalvare;
                        residuoNoRiv = cespite.getImporto() - fondo;
                    }
                    residuoDaSalvare = residuoNoRiv;
                }
                if (cespite.getImporto() == 0) {
                    perc = 0;
                } else {
                    perc = quotaDaSalvare / cespite.getImporto() * 100;
                }
                AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, quotaRivDaSalvare, fondo, fondoRiv, residuoDaSalvare, dataAmmortamento);
                calcolaSuperAmm(cespite, perc, a);
                ammortamentoCespiteList.add(a);
                dataAmmortamento = dataAmmortamento.plusYears(1);
                counter++;
            }

            return buildEliminatoVenduto(cespite, inizio, ammortamentoCespiteList, eliminato, venduto, residuo);
        }
        return ammortamentoCespiteList;
    }

    private List<AmmortamentoCespite> buildEliminatoVenduto(Cespite cespite, long inizio, List<AmmortamentoCespite> ammortamentoCespiteList, boolean eliminato, boolean venduto, double residuo) {
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
        long fine = System.currentTimeMillis();
        Log.info("Fine calcolo cespite: " + cespite.getCespite() + " ### tempo: " + (fine - inizio) / 1000 + " sec");
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

    public RegistroCespitiDto getAll(FiltroCespite filtroCespite) {
        LocalDate localDate = LocalDate.now();
        if (StringUtils.isNotBlank(filtroCespite.getData())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            localDate = LocalDate.parse(filtroCespite.getData(), formatter);
        }
        int anno = localDate.getYear();
        long inizioTempo = System.currentTimeMillis();
        final RegistroCespitiDto result = new RegistroCespitiDto();
        List<CespiteCategoriaDto> cespiteCategoriaDtoList = new ArrayList<>();
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
            List<CespiteDto> cespiteDtoList = new ArrayList<>();
            for (String tipoCespite : mapTipoCespite.keySet()) {
                List<CespiteDBDto> dtoList = mapTipoCespite.get(tipoCespite);
                CespiteCategoriaDto cespiteCategoriaDto = new CespiteCategoriaDto();
                cespiteCategoriaDto.setTipoCespite(tipoCespite);
                CespiteDBDto dbDto = dtoList.get(0);
                cespiteCategoriaDto.setCategoria(dbDto.getCategoria().getDescrizione());
                cespiteCategoriaDto.setPerc(dbDto.getCategoria().getPercAmmortamento());
                for (CespiteDBDto cespiteDBDto : dtoList) {
                    if(cespiteDtoList.stream().anyMatch(c ->
                            Objects.equals(c.getProgressivo1(), cespiteDBDto.getCespite().getProgressivo1())
                            && Objects.equals(c.getProgressivo2(), cespiteDBDto.getCespite().getProgressivo2())
                    )) {
                        continue;
                    }
                    CespiteDto v = new CespiteDto();
                    Cespite cespite = cespiteDBDto.getCespite();
                    v.setId(cespite.getId());
                    v.setCodice(dbDto.getCategoria().getCodice());
                    v.setProgressivo1(cespite.getProgressivo1());
                    v.setProgressivo2(cespite.getProgressivo2());
                    v.setCespite(cespite.getCespite());
                    v.setDataAcq(cespite.getDataAcq());
                    v.setAmmortamento(dbDto.getCategoria().getPercAmmortamento());
                    v.setImporto(cespite.getImporto());
                    v.setImportoRivalutazione(cespite.getImportoRivalutazione());
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
                    List<CespiteDBDto> dtosSameProgressivi = dtoList.stream().filter(
                            c ->
                                    Objects.equals(c.getCespite().getProgressivo1(), cespiteDBDto.getCespite().getProgressivo1())
                                            && Objects.equals(c.getCespite().getProgressivo2(), cespiteDBDto.getCespite().getProgressivo2())
                    ).toList();
                    dtosSameProgressivi.forEach(d -> list.add(d.getAmmortamentoCespite()));
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
                    cespiteDtoList.add(v);
                }
                cespiteDtoList.sort(Comparator.comparing(CespiteDto::getDataAcq));

                List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
                List<AmmortamentoCespite> ammortamentoCespiteList2 = new ArrayList<>();

                CespiteSommaDto sommaDto = new CespiteSommaDto();
                FiscaleRiepilogo inizioEsercizio = new FiscaleRiepilogo();
                inizioEsercizio.setValoreAggiornato(cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).mapToDouble(CespiteDto::getImporto).sum());
                double sum1 = cespiteDtoList.stream().filter(c -> (c.getDataVend() == null || c.getDataVend().getYear() == anno) && c.getAnno() < anno).filter(c -> c.getImportoRivalutazione() != null).mapToDouble(CespiteDto::getImportoRivalutazione).sum();
                inizioEsercizio.setValoreAggiornato(inizioEsercizio.getValoreAggiornato() + sum1);
                cespiteDtoList.forEach(c -> {
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
                double sum2 = ammortamentoCespiteList.stream().filter(a -> a.getFondoRivalutazione() != null).mapToDouble(AmmortamentoCespite::getFondoRivalutazione).sum();
                inizioEsercizio.setFondoAmmortamenti(inizioEsercizio.getFondoAmmortamenti() + sum2);
                inizioEsercizio.setResiduo(inizioEsercizio.getValoreAggiornato() - inizioEsercizio.getFondoAmmortamenti());
                sommaDto.setInizioEsercizio(inizioEsercizio);
                Map<String, List<CespiteDto>> superAmmMap = cespiteDtoList.stream().filter(c -> StringUtils.isNotBlank(c.getSuperAmmDesc())).collect(Collectors.groupingBy(CespiteDto::getSuperAmmDesc));
                for (String superAmmDesc : superAmmMap.keySet()) {
                    List<CespiteDto> viewDtoList = superAmmMap.get(superAmmDesc);
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
                acquisti.setValoreAggiornato(cespiteDtoList.stream()
                        .filter(c -> c.getAnno() == anno).mapToDouble(CespiteDto::getImporto).sum());
                vendite.setValoreAggiornato(-(cespiteDtoList.stream()
                        .filter(c -> c.getImportoVendita() != null && c.getDataVend().getYear() == anno)
                        .mapToDouble(CespiteDto::getImporto)
                        .sum()));


                cespiteDtoList.forEach(c -> ammortamentoCespiteList2.addAll(c.getAmmortamentoCespiteList()));
                List<AmmortamentoCespite> ammortamentoCespitesVend = new ArrayList<>();
                cespiteDtoList.stream()
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
                ammortamentiDeducibili.setAmmortamentoAnticipato(ammortamentoCespiteList2.stream().filter(a -> a.getAnno() == anno && a.getQuotaRivalutazione() != null && StringUtils.startsWith(a.getDescrizione(), "Ammortamento")).mapToDouble(AmmortamentoCespite::getQuotaRivalutazione).sum());
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
                cespiteCategoriaDto.setSomma(sommaDto);
                cespiteDtoList.sort(Comparator.comparing(CespiteDto::getProgressivo1).thenComparing(CespiteDto::getProgressivo2));
                cespiteCategoriaDto.setCespiteDtoList(cespiteDtoList);
                cespiteCategoriaDtoList.add(cespiteCategoriaDto);
            }
            cespiteCategoriaDtoList.sort(Comparator.comparing(CespiteCategoriaDto::getTipoCespite));
            result.setCespiteCategoriaDtoList(cespiteCategoriaDtoList);
            CespiteSommaDto sommaDto = new CespiteSommaDto();

            FiscaleRiepilogo inizio = new FiscaleRiepilogo();
            inizio.setValoreAggiornato(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getValoreAggiornato()).sum());
            inizio.setFondoAmmortamenti(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getFondoAmmortamenti()).sum());
            inizio.setResiduo(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getResiduo()).sum());

            FiscaleRiepilogo fine = new FiscaleRiepilogo();
            fine.setValoreAggiornato(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getValoreAggiornato()).sum());
            fine.setFondoAmmortamenti(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getFondoAmmortamenti()).sum());
            fine.setResiduo(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getResiduo()).sum());

            FiscaleRiepilogo acquisti = new FiscaleRiepilogo();
            FiscaleRiepilogo vendite = new FiscaleRiepilogo();
            acquisti.setValoreAggiornato(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getAcquisti().getValoreAggiornato()).sum());
            vendite.setValoreAggiornato(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getVendite().getValoreAggiornato()).sum());

            FiscaleRiepilogo ammortamentiDeducibili = new FiscaleRiepilogo();
            ammortamentiDeducibili.setAmmortamentoOrdinario(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getAmmortamentoOrdinario()).sum());
            ammortamentiDeducibili.setAmmortamentoAnticipato(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getAmmortamentoAnticipato()).sum());
            ammortamentiDeducibili.setTotaleAmmortamento(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getTotaleAmmortamento()).sum());
            ammortamentiDeducibili.setFondoAmmortamenti(cespiteCategoriaDtoList.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getFondoAmmortamenti()).sum());

            if (cespiteCategoriaDtoList.stream().anyMatch(c -> c.getSomma().getSuperAmm1() != null && c.getSomma().getSuperAmm1().getTotale() != null)) {
                TipoSuperAmm tipo1 = TipoSuperAmm.findById(1L);
                sommaDto.setSuperAmm1(new SuperAmmDto(tipo1.getDescrizione(), cespiteCategoriaDtoList
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm1() != null && c.getSomma().getSuperAmm1().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm1().getTotale())
                        .sum()));
            }
            if (cespiteCategoriaDtoList.stream().anyMatch(c -> c.getSomma().getSuperAmm2() != null && c.getSomma().getSuperAmm2().getTotale() != null)) {
                TipoSuperAmm tipo2 = TipoSuperAmm.findById(2L);
                sommaDto.setSuperAmm2(new SuperAmmDto(tipo2.getDescrizione(), cespiteCategoriaDtoList
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm2() != null && c.getSomma().getSuperAmm2().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm2().getTotale())
                        .sum()));
            }
            if (cespiteCategoriaDtoList.stream().anyMatch(c -> c.getSomma().getSuperAmm3() != null && c.getSomma().getSuperAmm3().getTotale() != null)) {
                sommaDto.setSuperAmm3(new SuperAmmDto(((TipoSuperAmm) TipoSuperAmm.findById(3L)).getDescrizione(), cespiteCategoriaDtoList
                        .stream()
                        .filter(c -> c.getSomma().getSuperAmm3() != null && c.getSomma().getSuperAmm3().getTotale() != null)
                        .mapToDouble(c -> c.getSomma().getSuperAmm3().getTotale())
                        .sum()));
            }
            if (cespiteCategoriaDtoList.stream().anyMatch(c -> c.getSomma().getPlusMinus() != null && c.getSomma().getPlusMinus().getTotale() != null)) {
                sommaDto.setPlusMinus(new SuperAmmDto("Plus", cespiteCategoriaDtoList
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
            result.setCespiteSommaDto(sommaDto);
            long fineTempo = System.currentTimeMillis();
            Log.info("Metodo get ammortamenti: " + (fineTempo - inizioTempo) / 1000 + " sec");
        } catch (Exception e) {
            Log.error("Errore get Registro cespiti", e);
            throw e;
        }
        return result;
    }

    @Transactional
    public void updateCespiti(CespiteRequest c) {
        String query = "SELECT c, cat " +
                "FROM Cespite c " +
                "JOIN CategoriaCespite cat ON cat.tipoCespite = c.tipoCespite " +
                "WHERE c.id =:id ";
        Map<String, Object> params = new HashMap<>();
        params.put("id", c.getId());
        Optional<CespiteDBDto> optionalCespite = Cespite.find(query, params).project(CespiteDBDto.class).singleResultOptional();

        int update = 0;
        if (optionalCespite.isPresent()) {
            CespiteDBDto cespiteDBDto = optionalCespite.get();
            Cespite cespite = cespiteDBDto.getCespite();
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
                List<AmmortamentoCespite> ammortamentoCespites;
                if (cespiteDBDto.getCespite().getImportoRivalutazione() != null) {
                    ammortamentoCespites = calcoloSingoloCespiteRivalutato(cespiteDBDto, LocalDate.now());
                } else {
                    ammortamentoCespites = calcoloSingoloCespite(cespiteDBDto, LocalDate.now());
                }
                if(!ammortamentoCespites.isEmpty()){
                    AmmortamentoCespite.persist(ammortamentoCespites);
                }
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
        if (opt.isEmpty()) {
            q = quadCespiteMapper.fromDtoToEntity(quad);
        } else {
            q = opt.get();
            q.setAmmortamento(quad.getAmmortamento());
        }
        q.persist();
    }

}