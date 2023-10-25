package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.CespiteDBDto;
import it.calolenoci.dto.CespiteDto;
import it.calolenoci.dto.CespiteViewDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import org.jfree.util.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AmmortamentoCespiteService {

    @Inject
    AmmortamentoCespiteMapper mapper;

    @Transactional
    public void calcola() {
        try {
            List<Cespite> cespitiAttivi = Cespite.find("attivo = 'T'").list();
            //LocalDate dataCorrente = LocalDate.now();
            LocalDate dataCorrente = LocalDate.of(2023, Month.OCTOBER, 23);
            for (Cespite cespite : cespitiAttivi) {
                List<AmmortamentoCespite> ammList = AmmortamentoCespite.find("idAmmortamento =:id",
                        Sort.descending("dataAmm"),
                        Parameters.with("id", cespite.getId())).list();
                double percAmmortamento = cespite.getAmmortamento();
                double percAmmPrimoAnno = cespite.getAmmortamento()/2;
                double perc;
                double quotaDaSalvare;
                double quota = cespite.getImporto() * (percAmmortamento/100);
                double quotaPrimoAnno = cespite.getImporto() * (percAmmPrimoAnno/100);
                double fondo = 0;
                double residuo = cespite.getImporto();
                //Nuovo cespite da calcolare
                if (ammList.isEmpty()) {
                    LocalDate dataInizio = new java.sql.Date(cespite.getDataAcq().getTime()).toLocalDate();
                    int annoCompleto;
                    boolean calcoloParziale = true;
                    if (!dataCorrente.getMonth().equals(Month.DECEMBER) || dataCorrente.getDayOfMonth() != 31) {
                        LocalDate lastYear = dataCorrente.minusYears(1);
                        annoCompleto = lastYear.getYear();
                    } else {
                        annoCompleto = dataCorrente.getYear();
                        calcoloParziale = false;
                    }
                    int anno = dataInizio.getYear();
                    List<AmmortamentoCespite> cespiteList = new ArrayList<>();
                    int counter = 1;
                    while (residuo > 0 && anno <= annoCompleto) {
                        if(counter == 1) {
                            perc = percAmmPrimoAnno;
                            quotaDaSalvare = quotaPrimoAnno;
                        } else {
                            perc = percAmmortamento;
                            quotaDaSalvare = quota;
                        }
                        fondo += quotaDaSalvare;
                        residuo = cespite.getImporto() - fondo;
                        if(residuo < 0) {
                            residuo = 0;
                            fondo = cespite.getImporto();
                        }
                        AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, fondo, residuo, anno, null);
                        cespiteList.add(a);
                        anno++;
                        counter++;
                    }
                    if (calcoloParziale && residuo != 0) {
                        if(counter == 1) {
                            perc = percAmmPrimoAnno;
                            quotaDaSalvare = quotaPrimoAnno * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
                        } else {
                            perc = percAmmortamento;
                            quotaDaSalvare = quota * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
                        }
                        fondo += quotaDaSalvare;
                        residuo = cespite.getImporto() - fondo;
                        if(residuo < 0) {
                            residuo = 0;
                            fondo = cespite.getImporto();
                            if(counter == 1){
                                quotaDaSalvare = quotaPrimoAnno;
                            } else {
                                quotaDaSalvare = quota;
                            }

                        }
                        AmmortamentoCespite a = mapper.buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, fondo, residuo, anno, dataCorrente);
                        cespiteList.add(a);
                    }
                    if (residuo == 0) {
                        cespite.setAttivo(Boolean.FALSE);
                        Cespite.persist(cespite);
                    }
                    AmmortamentoCespite.persist(cespiteList);
                } else {
                    AmmortamentoCespite a = ammList.get(0);
                    if (dataCorrente.getMonth().equals(Month.JANUARY) && dataCorrente.getDayOfMonth() == 1) {
                        //Creo un nuovo record
                        quota = quota * dataCorrente.getDayOfYear() / (dataCorrente.isLeapYear() ? 366 : 365);
                        perc = percAmmortamento;
                        fondo = quota + a.getFondo();
                        residuo = cespite.getImporto() - fondo;
                        if(residuo < 0) {
                            residuo = 0;
                            fondo = cespite.getImporto();
                            quota = a.getQuota();
                        }
                        AmmortamentoCespite.persist(mapper.buildAmmortamento(cespite.getId(), perc, quota, fondo, residuo, dataCorrente.getYear(), dataCorrente));
                    } else {
                        double fondoPrec = a.getFondo() - a.getQuota();
                        double quotaAnnuale = cespite.getImporto() * a.getPercAmm()/100;
                        quota = (quotaAnnuale * dataCorrente.getDayOfYear()) / (dataCorrente.isLeapYear() ? 366 : 365);
                        fondo = quota + fondoPrec;
                        residuo = cespite.getImporto() - fondo;
                        if(residuo < 0) {
                            residuo = 0;
                            fondo = cespite.getImporto();
                            quota = quotaAnnuale;
                        }
                        a.setDataAmm(java.sql.Date.valueOf(dataCorrente));
                        a.setFondo(fondo);
                        a.setResiduo(residuo);
                        a.setQuota(quota);
                        AmmortamentoCespite.persist(a);
                    }
                    if (residuo == 0) {
                        cespite.setAttivo(Boolean.FALSE);
                        Cespite.persist(cespite);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("Errore calcolo ammortamento", e);
        }
    }

    public List<CespiteDto> getAll() {
        List<CespiteDto> result = new ArrayList<>();
        try {
            List<CespiteDBDto> cespiteDtos = Cespite.find("SELECT c, a, cat.descrtipocesp " +
                    "FROM Cespite c " +
                    "JOIN AmmortamentoCespite a ON c.id = a.idAmmortamento " +
                    "JOIN CategoriaCespite cat ON cat.tipocespite = c.tipoCespite").project(CespiteDBDto.class).list();
            Map<Integer, List<CespiteDBDto>> map = cespiteDtos.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getProgressivo1()));
            for (Integer progressivo : map.keySet()) {
                List<CespiteDBDto> dtoList = map.get(progressivo);
                Map<Integer, List<CespiteDBDto>> progrList = dtoList.stream().collect(Collectors.groupingBy(dto -> dto.getCespite().getProgressivo2()));
                List<CespiteViewDto> cespiteViewDtoList = new ArrayList<>();
                for (Integer progr2 : progrList.keySet()) {
                    List<CespiteDBDto> progr2List = progrList.get(progr2);
                    CespiteDBDto dbDto = progr2List.get(0);
                    CespiteViewDto v = new CespiteViewDto();
                    Cespite cespite = dbDto.getCespite();
                    v.setCodice(cespite.getCodice());
                    v.setProgressivo1(progressivo);
                    v.setProgressivo2(progr2);
                    v.setCespite(cespite.getCespite());
                    v.setDataAcq(cespite.getDataAcq());
                    v.setAmmortamento(cespite.getAmmortamento());
                    v.setImporto(cespite.getImporto());
                    v.setFornitore(cespite.getFornitore());
                    v.setNumDocAcq(cespite.getNumDocAcq());
                    List<AmmortamentoCespite> list = new ArrayList<>();
                    progr2List.forEach(d -> list.add(d.getAmmortamentoCespite()));
                    list.sort(Comparator.comparing(AmmortamentoCespite::getDataAmm));
                    v.setAmmortamentoCespiteList(list);
                    cespiteViewDtoList.add(v);
                }
                CespiteDBDto dbDto = dtoList.get(0);
                CespiteDto cespiteDto = new CespiteDto();
                cespiteDto.setTipoCespite(dbDto.getCespite().getTipoCespite());
                cespiteDto.setCategoria(dbDto.getCategoria());
                cespiteDto.setCespiteViewDtoList(cespiteViewDtoList);
                result.add(cespiteDto);
            }
        } catch (Exception e) {
            Log.error("Errore get all cespiti", e);
            return result;
        }
        result.sort(Comparator.comparing(CespiteDto::getTipoCespite));
        return result;
    }



}
