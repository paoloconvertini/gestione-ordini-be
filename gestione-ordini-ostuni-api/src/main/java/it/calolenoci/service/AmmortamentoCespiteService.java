package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import it.calolenoci.mapper.QuadCespiteMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.time.LocalDate;
import java.time.Month;
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

    @ConfigProperty(name = "storico")
    boolean storico;

    @Transactional
    @TransactionConfiguration(timeout = 5000000)
    public void calcola(LocalDate dataCorrente) {
        try {
            long inizio = System.currentTimeMillis();
            Log.debug("### Inizio calcolo registro cespiti");
            List<Cespite> cespitiAttivi = Cespite.find("attivo = 'T'").list();
            if (storico) {
                AmmortamentoCespite.delete("idAmmortamento in (:list)", Parameters.with("list", cespitiAttivi.stream().map(Cespite::getId).collect(Collectors.toList())));
                for (Cespite cespite : cespitiAttivi) {
                    CategoriaCespite categoriaCespite = CategoriaCespite.find("tipoCespite=:t", Parameters.with("t", cespite.getTipoCespite())).firstResult();
                    List<AmmortamentoCespite> ammortamentoCespites;
                    if (cespite.getImportoRivalutazione() != null) {
                        ammortamentoCespites = calcoloSingoloCespiteRivalutato(categoriaCespite.getPercAmmortamento(), cespite, dataCorrente);
                    } else {
                        ammortamentoCespites = calcoloSingoloCespite(categoriaCespite.getPercAmmortamento(), cespite, dataCorrente);
                    }

                    if (ammortamentoCespites.isEmpty()) {
                        continue;
                    }
                    AmmortamentoCespite.persist(ammortamentoCespites);
                }
            } else {
                Log.debug("--- flag storico disattivato");
                AmmortamentoCespite.delete("idAmmortamento in (:list) and anno >=:a", Parameters.with("list", cespitiAttivi.stream().map(Cespite::getId).collect(Collectors.toList())).and("a", dataCorrente.getYear()));
                for (Cespite cespite : cespitiAttivi) {
                    CategoriaCespite categoriaCespite = CategoriaCespite.find("tipoCespite=:t", Parameters.with("t", cespite.getTipoCespite())).firstResult();
                    List<AmmortamentoCespite> ammortamentoCespites = calcoloAnnoCorrente(categoriaCespite.getPercAmmortamento(), cespite, dataCorrente);
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

    private List<AmmortamentoCespite> calcoloAnnoCorrente(Double percAmm, Cespite cespite, LocalDate dataCorrente) {
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        int anno = dataCorrente.getYear();
        //Optional<AmmortamentoCespite> optAmmAnnoPrec = AmmortamentoCespite.find("idAmmortamento =:id and anno=:a", Parameters.with("id", cespite.getId()).and("a", anno - 1)).singleResultOptional();
        List<AmmortamentoCespite> listAmmortamenti = AmmortamentoCespite.find("idAmmortamento =:id", Parameters.with("id", cespite.getId())).list();
        Optional<AmmortamentoCespite> optAmmAnnoPrec = listAmmortamenti.stream().filter(a -> a.getAnno() == anno - 1).findFirst();
        if (percAmm != null && percAmm != 0) {
            if (listAmmortamenti.isEmpty()) {
                // cespite nuovo
                if (cespite.getImportoRivalutazione() != null) {
                    ammortamentoCespiteList = calcoloSingoloCespiteRivalutato(percAmm, cespite, dataCorrente);
                } else {
                    ammortamentoCespiteList = calcoloSingoloCespite(percAmm, cespite, dataCorrente);
                }
            } else if (optAmmAnnoPrec.isPresent()) {
                // cespite presente, calcolo anni a partire da data selezionata
                AmmortamentoCespite ammPrecedente = optAmmAnnoPrec.get();
                if (cespite.getImportoRivalutazione() != null) {
                    calcoloAnnoCorrenteRivalutato(percAmm, ammortamentoCespiteList, ammPrecedente, cespite, dataCorrente);
                } else if (ammPrecedente.getFondo() < cespite.getImporto()) {
                    calcoloAnnoCorrenteAmmortamento(dataCorrente, cespite, ammortamentoCespiteList, ammPrecedente);
                }
            } else {
                //cespite presente ma già consolidato
                return ammortamentoCespiteList;
            }
        }
        return ammortamentoCespiteList;
    }

    private void calcoloAnnoCorrenteAmmortamento(LocalDate dataCorrente, Cespite cespite, List<AmmortamentoCespite> ammortamentoCespiteList, AmmortamentoCespite ammPrecedente) {
        double residuo = ammPrecedente.getResiduo();
        int annoPrecedente = ammPrecedente.getAnno();
        LocalDate dataAmmortamento = LocalDate.of(annoPrecedente + 1, Month.DECEMBER, 31);
        while (residuo != 0 && dataAmmortamento.getYear() <= dataCorrente.getYear()) {
            if (dataAmmortamento.getYear() == dataCorrente.getYear()) {
                dataAmmortamento = dataCorrente;
            }
            double quota = cespite.getImporto() * (ammPrecedente.getPercAmm() / 100);
            Optional<QuadraturaCespite> optQuad = QuadraturaCespite.find("idCespite =:id AND anno=:a",
                    Parameters.with("id", cespite.getId()).and("a", dataCorrente.getYear())).singleResultOptional();
            if (optQuad.isPresent()) {
                QuadraturaCespite q = optQuad.get();
                quota = cespite.getImporto() * (q.getAmmortamento() / 100);
            }
            double quotaDaSalvare = quota * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
            double fondo = ammPrecedente.getFondo() + quotaDaSalvare;

            if (fondo >= cespite.getImporto()) {
                quotaDaSalvare = residuo;
                residuo = 0;
                fondo = cespite.getImporto();
            } else {
                residuo = cespite.getImporto() - fondo;
            }

            double perc = quotaDaSalvare / cespite.getImporto() * 100;
            AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, 0, fondo, 0, residuo, dataAmmortamento);
            ammortamentoCespiteList.add(a);
            dataAmmortamento = dataAmmortamento.plusYears(1);
        }
    }

    private void calcoloAnnoCorrenteRivalutato(Double percAmm, List<AmmortamentoCespite> ammortamentoCespiteList, AmmortamentoCespite ammPrecedente, Cespite cespite, LocalDate dataCorrente) {
        double residuo = ammPrecedente.getResiduo();
        double fondo = ammPrecedente.getFondo();
        boolean ammOrdConsolidato = (fondo == cespite.getImporto());
        int annoPrecedente = ammPrecedente.getAnno();
        if(residuo == 0 && annoPrecedente == 2020) {
            residuo = cespite.getImportoRivalutazione();
        }
        LocalDate dataAmmortamento = LocalDate.of(annoPrecedente + 1, Month.DECEMBER, 31);
        while (residuo != 0 && dataAmmortamento.getYear() <= dataCorrente.getYear()) {
            if (dataAmmortamento.getYear() == dataCorrente.getYear()) {
                dataAmmortamento = dataCorrente;
            }
            double quota = cespite.getImporto() * (percAmm / 100);
            double quotaRiv = cespite.getImportoRivalutazione() * (percAmm / 100);
            double fondoRiv = ammPrecedente.getFondoRivalutazione();
            double residuoDaSalvare;
            double quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
            double quotaRivDaSalvare = quotaRiv * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
            double residuoNoRiv = cespite.getImporto();
            Optional<QuadraturaCespite> optQuad = QuadraturaCespite.find("idCespite =:id AND anno=:a",
                    Parameters.with("id", cespite.getId()).and("a", dataCorrente.getYear())).singleResultOptional();
            if (optQuad.isPresent()) {
                QuadraturaCespite q = optQuad.get();
                quota = cespite.getImporto() * (q.getAmmortamento() / 100);
                quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
            }

            fondoRiv += quotaRivDaSalvare;
            if (fondoRiv >= (cespite.getImporto() + cespite.getImportoRivalutazione())) {
                quotaRivDaSalvare = residuo;
                residuo = 0;
                fondoRiv = cespite.getImporto() + cespite.getImportoRivalutazione();
            } else {
                residuo = cespite.getImportoRivalutazione() - fondoRiv;
            }

            if (!ammOrdConsolidato) {
                fondo += quotaDaSalvare;
                if (fondo >= cespite.getImporto()) {
                    quotaDaSalvare = residuoNoRiv;
                    fondo = cespite.getImporto();
                }
            } else {
                quotaDaSalvare = 0;
            }
            residuoDaSalvare = residuo;
            double perc;
            if (cespite.getImporto() == 0) {
                perc = 0;
            } else {
                perc = quotaDaSalvare / cespite.getImporto() * 100;
            }
            ammortamentoCespiteList.add(mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, quotaRivDaSalvare, fondo, fondoRiv, residuoDaSalvare, dataAmmortamento));
            dataAmmortamento = dataAmmortamento.plusYears(1);
        }
    }

    public List<AmmortamentoCespite> calcoloSingoloCespite(Double percAmm, Cespite cespite, LocalDate dataCorrente) {
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        boolean eliminato = cespite.getDataVendita() != null && StringUtils.isBlank(cespite.getIntestatarioVendita());
        boolean venduto = cespite.getDataVendita() != null && StringUtils.isNotBlank(cespite.getIntestatarioVendita());
        if (eliminato || venduto) {
            dataCorrente = cespite.getDataVendita();
        }
        if (percAmm != null && percAmm != 0) {
            double percAmmortamento = percAmm;
            double percAmmPrimoAnno = percAmm / 2;
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

                fondo += quotaDaSalvare;
                if (fondo >= cespite.getImporto()) {
                    quotaDaSalvare = residuo;
                    residuo = 0;
                    fondo = cespite.getImporto();
                } else {
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

            return buildEliminatoVenduto(cespite, ammortamentoCespiteList, eliminato, venduto, residuo);
        }
        return ammortamentoCespiteList;
    }

    public List<AmmortamentoCespite> calcoloSingoloCespiteRivalutato(Double percAmm, Cespite cespite, LocalDate dataCorrente) {
        List<AmmortamentoCespite> ammortamentoCespiteList = new ArrayList<>();
        boolean eliminato = cespite.getDataVendita() != null && StringUtils.isBlank(cespite.getIntestatarioVendita());
        boolean venduto = cespite.getDataVendita() != null && StringUtils.isNotBlank(cespite.getIntestatarioVendita());
        if (eliminato || venduto) {
            dataCorrente = cespite.getDataVendita();
        }
        if (percAmm != null && percAmm != 0) {
            double percAmmortamento = percAmm;
            double percAmmPrimoAnno = percAmm / 2;
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
                        fondo += quotaDaSalvare;
                        if (fondo >= cespite.getImporto()) {
                            quotaDaSalvare = residuoNoRiv;
                            residuoNoRiv = 0;
                            fondo = cespite.getImporto();
                        } else {
                            residuoNoRiv = cespite.getImporto() - fondo;
                        }
                        residuoDaSalvare = residuoNoRiv;
                        quotaRivDaSalvare = 0;
                    } else {
                        fondoRiv += quotaRiv;
                        if (fondoRiv >= cespite.getImporto() + cespite.getImportoRivalutazione()) {
                            quotaRiv = residuo;
                            residuo = 0;
                            fondoRiv = cespite.getImporto() + cespite.getImportoRivalutazione();
                        } else {
                            residuo = cespite.getImportoRivalutazione() - fondoRiv;
                        }

                        fondo += quotaDaSalvare;
                        if (fondo >= cespite.getImporto()) {
                            quotaDaSalvare = residuoNoRiv;
                            residuoNoRiv = 0;
                            fondo = cespite.getImporto();
                        } else {
                            residuoNoRiv = cespite.getImporto() - fondo;
                        }
                        residuoDaSalvare = residuo;
                        quotaRivDaSalvare = quotaRiv;
                    }
                } else {
                    fondo += quotaDaSalvare;
                    if (fondo >= cespite.getImporto()) {
                        quotaDaSalvare = residuoNoRiv;
                        residuoNoRiv = 0;
                        fondo = cespite.getImporto();
                    } else {
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

            return buildEliminatoVenduto(cespite, ammortamentoCespiteList, eliminato, venduto, residuo);
        }
        return ammortamentoCespiteList;
    }

    private List<AmmortamentoCespite> buildEliminatoVenduto(Cespite cespite, List<AmmortamentoCespite> ammortamentoCespiteList, boolean eliminato, boolean venduto, double residuo) {
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

    public RegistroCespitiDto getRegistroCespiti(FiltroCespite filtroCespite) {
        Log.debug("### INIZIO get Registro cespiti");
        LocalDate localDate = LocalDate.now();
        if (StringUtils.isNotBlank(filtroCespite.getData())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            localDate = LocalDate.parse(filtroCespite.getData(), formatter);
        }
        int anno = localDate.getYear();
        long inizioTempo = System.currentTimeMillis();
        final RegistroCespitiDto view = new RegistroCespitiDto();
        List<CategoriaCespitiDto> result = new ArrayList<>();
        try {
            String query = "SELECT c.id, c.tipoCespite, c.progressivo1, c.progressivo2, c.cespite, c.dataAcq, c.numDocAcq, c.fornitore, " +
                    "c.importo, c.importoRivalutazione, c.attivo, c.dataVendita, c.numDocVendita, c.intestatarioVendita, c.importoVendita," +
                    " c.superAmm, c.protocollo, c.giornale, c.anno, c.dataInizioCalcoloAmm, c.flPrimoAnno, c.fondoRivalutazione, a.dataAmm, " +
                    "a.descrizione, a.percAmm, a.quota, a.fondo, a.residuo, a.anno, a.superPercentuale, a.superQuota, a.quotaRivalutazione, a.fondoRivalutazione," +
                    " t.id, t.descrizione, t.percAmmortamento, t.costoGruppo, t.costoConto, t.ammGruppo, t.ammConto, " +
                    "t.fondoGruppo, t.fondoConto, t.plusGruppo, t.plusConto, t.minusGruppo, t.minusConto " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite t ON t.tipoCespite = c.tipoCespite ";
            Map<String, Object> params = new HashMap<>();
            if (StringUtils.isNotBlank(filtroCespite.getTipoCespite())) {
                query += "WHERE c.tipoCespite = :q";
                params.put("q", filtroCespite.getTipoCespite());
            }
            List<RegistroCespiteDto> cespiteDtos = Cespite.find(query, params).project(RegistroCespiteDto.class).list();
            String queryNoAmm = "SELECT c.id, c.tipoCespite, c.progressivo1, c.progressivo2, c.cespite, c.dataAcq, c.numDocAcq, c.fornitore, " +
                    "c.importo, c.importoRivalutazione, c.attivo, c.dataVendita, c.numDocVendita, c.intestatarioVendita, c.importoVendita," +
                    " c.superAmm, c.protocollo, c.giornale, c.anno, c.dataInizioCalcoloAmm, c.flPrimoAnno, c.fondoRivalutazione, " +
                    " t.id, t.descrizione, t.percAmmortamento, t.costoGruppo, t.costoConto, t.ammGruppo, t.ammConto, " +
                    "t.fondoGruppo, t.fondoConto, t.plusGruppo, t.plusConto, t.minusGruppo, t.minusConto " +
                    "FROM Cespite c " +
                    "JOIN CategoriaCespite t ON t.tipoCespite = c.tipoCespite " +
                    "WHERE NOT EXISTS (SELECT 1 FROM AmmortamentoCespite a WHERE a.idAmmortamento = c.id) ";
            Map<String, Object> paramsNoAmm = new HashMap<>();
            if (filtroCespite != null && StringUtils.isNotBlank(filtroCespite.getTipoCespite())) {
                queryNoAmm += "AND c.tipoCespite = :q";
                paramsNoAmm.put("q", filtroCespite.getTipoCespite());
            }
            List<RegistroCespiteDto> listNoAmm = Cespite.find(queryNoAmm, paramsNoAmm).project(RegistroCespiteDto.class).list();
            if (!listNoAmm.isEmpty()) {
                cespiteDtos.addAll(listNoAmm);
            }
            Map<String, List<RegistroCespiteDto>> mapTipoCespite = cespiteDtos.stream().collect(Collectors.groupingBy(RegistroCespiteDto::getTipoCespite));

            for (String tipoCespite : mapTipoCespite.keySet()) {
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
                            TipoSuperAmm tipoSuperAmm = TipoSuperAmm.findById(dbDto1.getSuperAmm());
                            v.setSuperAmmDesc(tipoSuperAmm.getDescrizione());
                        }
                        List<AmmortamentoCespite> list = new ArrayList<>();
                        progr2List.forEach(d -> list.add(mapper.buildAmmortamento(d)));
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
                    }
                    CespiteProgressivoDto cespiteProgressivoDto = new CespiteProgressivoDto();
                    cespiteProgressivoDto.setProgressivo(progressivo);
                    cespiteDtoList.sort(Comparator.comparing(CespiteDto::getDataAcq));
                    cespiteProgressivoDto.setCespiteDtoList(cespiteDtoList);
                    cespiteProgressivoDtoList.add(cespiteProgressivoDto);
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
                cespiteDtoList.forEach(c -> {
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
                    List<AmmortamentoCespite> list = new ArrayList<>();
                    viewDtoList.forEach(c -> list.addAll(c.getAmmortamentoCespiteList()));
                    double sum = list.stream()
                            .filter(a -> a.getAnno() == anno - 1 && a.getSuperQuota() != null)
                            .mapToDouble(AmmortamentoCespite::getSuperQuota)
                            .sum();
                    if (sum == 0) {
                        continue;
                    }
                    TipoSuperAmm t = TipoSuperAmm.find("descrizione = :desc", Parameters.with("desc", superAmmDesc)).firstResult();
                    SuperAmmDto dto = new SuperAmmDto();
                    dto.setDesc(superAmmDesc);
                    dto.setTotale(sum);
                    if (t.id == 1) {
                        sommaDto.setSuperAmm1(dto);
                    } else if (t.id == 2) {
                        sommaDto.setSuperAmm2(dto);
                    } else if (t.id == 3) {
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
            result.sort(Comparator.comparing(CategoriaCespitiDto::getTipoCespite));
            view.setCespiteList(result);
            CespiteSommaDto sommaDto = new CespiteSommaDto();

            FiscaleRiepilogoDto inizio = new FiscaleRiepilogoDto();
            inizio.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getValoreAggiornato()).sum());
            inizio.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getFondoAmmortamenti()).sum());
            inizio.setFondoAmmortamentiRiv(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getFondoAmmortamentiRiv()).sum());
            inizio.setFondoAmmortamentiTot(inizio.getFondoAmmortamenti() + inizio.getFondoAmmortamentiRiv());
            inizio.setResiduo(result.stream().mapToDouble(c -> c.getSomma().getInizioEsercizio().getResiduo()).sum());

            FiscaleRiepilogoDto fine = new FiscaleRiepilogoDto();
            fine.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getValoreAggiornato()).sum());
            fine.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getFondoAmmortamenti()).sum());
            fine.setFondoAmmortamentiRiv(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getFondoAmmortamentiRiv()).sum());
            fine.setFondoAmmortamentiTot(fine.getFondoAmmortamenti() + fine.getFondoAmmortamentiRiv());
            fine.setResiduo(result.stream().mapToDouble(c -> c.getSomma().getFineEsercizio().getResiduo()).sum());

            FiscaleRiepilogoDto acquisti = new FiscaleRiepilogoDto();
            FiscaleRiepilogoDto vendite = new FiscaleRiepilogoDto();
            acquisti.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getAcquisti().getValoreAggiornato()).sum());
            vendite.setValoreAggiornato(result.stream().mapToDouble(c -> c.getSomma().getVendite().getValoreAggiornato()).sum());
            vendite.setTotaleAmmortamento(result.stream().mapToDouble(c -> c.getSomma().getVendite().getTotaleAmmortamento()).sum());
            vendite.setFondoAmmortamenti(result.stream().mapToDouble(c -> c.getSomma().getVendite().getFondoAmmortamenti()).sum());
            vendite.setFondoAmmortamentiRiv(result.stream().mapToDouble(c -> c.getSomma().getVendite().getFondoAmmortamentiRiv()).sum());
            vendite.setFondoAmmortamentiTot(result.stream().mapToDouble(c -> c.getSomma().getVendite().getFondoAmmortamentiTot()).sum());


            FiscaleRiepilogoDto ammortamentiDeducibili = new FiscaleRiepilogoDto();
            ammortamentiDeducibili.setAmmortamentoOrdinario(result.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getAmmortamentoOrdinario()).sum());
            ammortamentiDeducibili.setAmmortamentoAnticipato(result.stream().mapToDouble(c -> c.getSomma().getAmmortamentiDeducibili().getAmmortamentoAnticipato()).sum());
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
          /*  if(!report){
                view.getCespiteList().forEach(c -> c.setCespiteProgressivoDtoList(new ArrayList<>()));
            }*/
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
                    ammortamentoCespites = calcoloSingoloCespiteRivalutato(cespiteDBDto.getCategoria().getPercAmmortamento(), cespite, LocalDate.now());
                } else {
                    ammortamentoCespites = calcoloSingoloCespite(cespiteDBDto.getCategoria().getPercAmmortamento(), cespite, LocalDate.now());
                }
                if (!ammortamentoCespites.isEmpty()) {
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


    public File scaricaRegistroCespiti(RegistroCespitiDto view) {
        Log.debug(" ### INIZIO generazione report Registro cespiti ###");
        File report;
        try {
            report = jasperService.createReport(view);
            Log.debug(" ### FINE generazione report Registro cespiti ###");
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
