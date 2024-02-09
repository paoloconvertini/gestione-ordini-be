package it.calolenoci.runner;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.RegistroCespiteDto;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.QuadraturaCespite;
import it.calolenoci.entity.TipoSuperAmm;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class CalcolaAmmortamentoRunner implements Runnable {

    private List<AmmortamentoCespite> list;

    private RegistroCespiteDto cespite;

    private LocalDate dataCorrente;

    public CalcolaAmmortamentoRunner(List<AmmortamentoCespite> list, RegistroCespiteDto cespite, LocalDate dataCorrente) {
        this.list = list;
        this.cespite = cespite;
        this.dataCorrente = dataCorrente;
    }

    @Override
    public void run() {
        if(this.cespite.getImportoRivalutazione() != null && this.cespite.getImportoRivalutazione() != 0) {
            calcolaAmmortamentoSingoloRivalutato();
        } else {
            calcolaAmmortamentoSingolo();
        }
    }

    private void calcolaAmmortamentoSingoloRivalutato() {
        Double percAmm = cespite.getPercAmmortamento();
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
            LocalDate dataInizio = cespite.getDtInizioCalcoloAmm();

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
                if (cespite.getQuadraturaCespiteList() != null && !cespite.getQuadraturaCespiteList().isEmpty()) {
                    for (QuadraturaCespite quadraturaCespite : cespite.getQuadraturaCespiteList()) {
                        if (quadraturaCespite.getAnno() == dataAmmortamento.getYear()) {
                            quota = cespite.getImporto() * (quadraturaCespite.getAmmortamento() / 100);
                            quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                        }
                    }
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
                AmmortamentoCespite a = buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, quotaRivDaSalvare, fondo, fondoRiv, residuoDaSalvare, dataAmmortamento);
                if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
                    calcolaSuperAmm(cespite, perc, a);
                }
                list.add(a);
                dataAmmortamento = dataAmmortamento.plusYears(1);
                counter++;
            }
            buildEliminatoVenduto(cespite, list, eliminato, venduto, residuo);
        }
    }

    private void calcolaAmmortamentoSingolo(){
        Double percAmm = cespite.getPercAmmortamento();
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
            LocalDate dataInizio = cespite.getDtInizioCalcoloAmm();
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
                if (cespite.getQuadraturaCespiteList() != null && !cespite.getQuadraturaCespiteList().isEmpty()) {
                    for (QuadraturaCespite quadraturaCespite : cespite.getQuadraturaCespiteList()) {
                        if(quadraturaCespite.getAnno() == dataAmmortamento.getYear()){
                            quota = cespite.getImporto() * (quadraturaCespite.getAmmortamento() / 100);
                            quotaDaSalvare = quota * dataAmmortamento.getDayOfYear() / (dataAmmortamento.isLeapYear() ? 366 : 365);
                        }
                    }
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
                AmmortamentoCespite a = buildAmmortamento(cespite.getId(), perc, quotaDaSalvare, 0, fondo, 0, residuo, dataAmmortamento);
                if (cespite.getSuperAmm() != null && cespite.getSuperAmm() != 0L) {
                    calcolaSuperAmm(cespite, perc, a);
                }
                list.add(a);
                dataAmmortamento = dataAmmortamento.plusYears(1);
                counter++;
            }

            buildEliminatoVenduto(cespite, list, eliminato, venduto, residuo);
        }
    }

    private void buildEliminatoVenduto(RegistroCespiteDto cespite, List<AmmortamentoCespite> ammortamentoCespiteList, boolean eliminato, boolean venduto, double residuo) {
        if (eliminato) {
            ammortamentoCespiteList.add(buildEliminato(cespite.getId(), cespite.getDataVendita()));
        }
        if (venduto) {
            ammortamentoCespiteList.addAll(buildVenduto(cespite, residuo));
        }
        if (eliminato || venduto) {
            cespite.setAttivo(Boolean.FALSE);
            Cespite.persist(cespite);
        }
    }

    private void calcolaSuperAmm(RegistroCespiteDto cespite, double perc, AmmortamentoCespite a) {
            double importSuperAmm = (cespite.getPercSuperAmm() * cespite.getImporto()) / 100;
            double quotaSuperAmm = (perc * importSuperAmm) / 100;
            if (a.getResiduo() < quotaSuperAmm) {
                quotaSuperAmm = a.getResiduo();
            }
            a.setSuperPercentuale(Math.round(perc * 100.0) / 100.0);
            a.setSuperQuota(Math.round(quotaSuperAmm * 100.0) / 100.0);
    }

    private AmmortamentoCespite buildAmmortamento(String id, double perc, double quota, double quotaRivalutazione, double fondo, double fondoRivalutazione, double valoreResiduo, LocalDate dataCorrente) {
        AmmortamentoCespite a = new AmmortamentoCespite();
        a.setIdAmmortamento(id);
        a.setDataAmm(dataCorrente);
        a.setDescrizione("Ammortamento ordinario deducibile");
        a.setPercAmm(Math.round(perc * 100.0) / 100.0);
        a.setFondo(Math.round(fondo * 100.0) / 100.0);
        a.setFondoRivalutazione(Math.round(fondoRivalutazione * 100.0) / 100.0);
        a.setQuota(Math.round(quota* 100.0) / 100.0);
        a.setQuotaRivalutazione(Math.round(quotaRivalutazione * 100.0) / 100.0);
        a.setResiduo(Math.round(valoreResiduo * 100.0) / 100.0);
        a.setAnno(dataCorrente.getYear());
        return a;
    }

    private AmmortamentoCespite buildEliminato(String id, LocalDate dataVendita){
        AmmortamentoCespite ammEliminato = new AmmortamentoCespite();
        ammEliminato.setIdAmmortamento(id);
        ammEliminato.setDescrizione("ELIMINAZIONE CESPITE");
        ammEliminato.setDataAmm(dataVendita);
        int anno = dataVendita.getYear();
        ammEliminato.setAnno(anno);
        return ammEliminato;
    }

    private List<AmmortamentoCespite> buildVenduto(RegistroCespiteDto cespite, Double residuo){
        int anno = cespite.getDataVendita().getYear();
        List<AmmortamentoCespite> list = new ArrayList<>();
        AmmortamentoCespite ammVenduto = new AmmortamentoCespite();
        ammVenduto.setIdAmmortamento(cespite.getId());
        ammVenduto.setDescrizione("VENDITA CESPITE");
        ammVenduto.setDataAmm(cespite.getDataVendita());
        ammVenduto.setAnno(anno);
        AmmortamentoCespite ammVenduto1 = new AmmortamentoCespite();
        ammVenduto1.setIdAmmortamento(cespite.getId());
        ammVenduto1.setDescrizione("venduto a " + cespite.getIntestatarioVendita() + " n. fatt.: " + cespite.getNumDocVend());
        ammVenduto1.setQuota(Math.round(cespite.getImportoVendita() * 100.0) / 100.0);
        ammVenduto1.setAnno(anno);
        AmmortamentoCespite ammVenduto2 = new AmmortamentoCespite();
        ammVenduto2.setIdAmmortamento(cespite.getId());
        ammVenduto2.setDescrizione("Plus/Minus valenza");
        ammVenduto2.setQuota(Math.round((cespite.getImportoVendita() - residuo) * 100.0) / 100.0);
        ammVenduto2.setAnno(anno);
        list.add(ammVenduto);
        list.add(ammVenduto1);
        list.add(ammVenduto2);
        return list;
    }

}
