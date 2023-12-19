package it.calolenoci.runner;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.CespiteDBDto;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class CalcoloCespiteRun implements Runnable{

    private EntityManager em;
    private AmmortamentoCespiteMapper mapper;

    private CespiteDBDto dto;

    private LocalDate dataCorrente;

    private boolean rivalutato;

    private List<AmmortamentoCespite> ammortamentoCespites;

    public CalcoloCespiteRun(EntityManager em, AmmortamentoCespiteMapper mapper, boolean rivalutato, CespiteDBDto dto, LocalDate dataCorrente, List<AmmortamentoCespite> ammortamentoCespites) {
        this.em = em;
        this.mapper = mapper;
        this.rivalutato = rivalutato;
        this.dto = dto;
        this.dataCorrente = dataCorrente;
        this.ammortamentoCespites = ammortamentoCespites;
    }

    @Override
    public void run() {
        System.out.println("Doing heavy processing - START "+Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
            if(this.rivalutato) {
                this.ammortamentoCespites = calcoloSingoloCespiteRivalutato(this.dto, this.dataCorrente);
            } else {
                this.ammortamentoCespites = calcoloSingoloCespite(this.dto, this.dataCorrente);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Doing heavy processing - END "+Thread.currentThread().getName());

    }

    @Transactional
    private List<AmmortamentoCespite> calcoloSingoloCespite(CespiteDBDto dto, LocalDate dataCorrente) {
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
/*                QuadraturaCespite q = em.createQuery("select q FROM QuadraturaCespite q WHERE q.idCespite=:id AND q.anno=:a", QuadraturaCespite.class)
                                .setParameter("id", cespite.getId()).setParameter("a", dataAmmortamento.getYear()).getSingleResult();
                if (q != null) {
                    quota = cespite.getImporto() * (q.getAmmortamento() / 100);
                    quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                }*/
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

    @Transactional
    private List<AmmortamentoCespite> calcoloSingoloCespiteRivalutato(CespiteDBDto dto, LocalDate dataCorrente) {
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
               /* QuadraturaCespite q = em.createQuery("select q FROM QuadraturaCespite q WHERE q.idCespite=:id AND q.anno=:a", QuadraturaCespite.class)
                        .setParameter("id", cespite.getId()).setParameter("a", dataAmmortamento.getYear()).getSingleResult();
                if (q != null) {
                    quota = cespite.getImporto() * (q.getAmmortamento() / 100);
                    quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                }*/

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
}
