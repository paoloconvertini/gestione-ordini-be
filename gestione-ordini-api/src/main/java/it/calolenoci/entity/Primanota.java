package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@IdClass(PrimanotaPK.class)
@Table(name = "PRIMANOTA")
public class Primanota extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ANNO", nullable = false)
    private int anno;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "GIORNALE", nullable = false, length = 1)
    private String giornale;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PROTOCOLLO", nullable = false)
    private int protocollo;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "PROGRPRIMANOTA", nullable = false)
    private int progrprimanota;
    
    @Column(name = "PROGRGENERALE", nullable = true)
    private Integer progrgenerale;
    
    @Column(name = "DIVISIONE", nullable = true, length = 5)
    private String divisione;
    
    @Column(name = "ANNOPARTITA", nullable = true)
    private Integer annopartita;
    
    @Column(name = "NUMPARTITA", nullable = true)
    private Integer numpartita;
    
    @Column(name = "DATAOPERAZIONE", nullable = true)
    private LocalDate dataoperazione;
    
    @Column(name = "DATAMOVIMENTO", nullable = true)
    private LocalDate datamovimento;
    
    @Column(name = "NUMERODOCUMENTO", nullable = true, length = 15)
    private String numerodocumento;
    
    @Column(name = "CAUSALE", nullable = true, length = 3)
    private String causale;
    
    @Column(name = "GRUPPOCONTO", nullable = true)
    private Integer gruppoconto;
    
    @Column(name = "SOTTOCONTO", nullable = true, length = 6)
    private String sottoconto;
    
    @Column(name = "DESCRSUPPL", nullable = true, length = 25)
    private String descrsuppl;
    
    @Column(name = "IMPORTO", nullable = true, precision = 0)
    private Double importo;
    
    @Column(name = "RIGOGIORNALE", nullable = true)
    private Integer rigogiornale;
    
    @Column(name = "CODICEPAGAMENTO", nullable = true, length = 3)
    private String codicepagamento;
    
    @Column(name = "CODDIFFPAG", nullable = true, length = 3)
    private String coddiffpag;
    
    @Column(name = "IVAPRIMASCAD", nullable = true, length = 1)
    private String ivaprimascad;
    
    @Column(name = "DATAPRIMASCAD", nullable = true)
    private LocalDate dataprimascad;
    
    @Column(name = "IMPORTOVE", nullable = true, precision = 0)
    private Double importove;
    
    @Column(name = "DATAPRENOTATO", nullable = true)
    private LocalDate dataprenotato;
    
    @Column(name = "IMPPRENOTATO", nullable = true, precision = 0)
    private Double impprenotato;
    
    @Column(name = "DATAAUTORIZZA", nullable = true)
    private LocalDate dataautorizza;
    
    @Column(name = "USERAUTORIZZA", nullable = true, length = 20)
    private String userautorizza;
    
    @Column(name = "INIZIOPERIODO", nullable = true)
    private LocalDate inizioperiodo;
    
    @Column(name = "FINEPERIODO", nullable = true)
    private LocalDate fineperiodo;
    
    @Column(name = "ONORARIO", nullable = true, precision = 0)
    private Double onorario;
    
    @Column(name = "SPESE", nullable = true, precision = 0)
    private Double spese;
    
    @Column(name = "SPESENO770", nullable = true, precision = 0)
    private Double speseno770;
    
    @Column(name = "TIPOCOMPENSO", nullable = true, length = 3)
    private String tipocompenso;
    
    @Column(name = "RITENUTA", nullable = true, precision = 0)
    private Double ritenuta;
    
    @Column(name = "RITINPS", nullable = true, precision = 0)
    private Double ritinps;
    
    @Column(name = "RITINAIL", nullable = true, precision = 0)
    private Double ritinail;
    
    @Column(name = "RITENASARCO", nullable = true, precision = 0)
    private Double ritenasarco;
    
    @Column(name = "ANNOENASARCO", nullable = true)
    private Integer annoenasarco;
    
    @Column(name = "TRIMENASARCO", nullable = true)
    private Integer trimenasarco;
    
    @Column(name = "FLAGTRASFERITO", nullable = true, length = 1)
    private String flagtrasferito;
    
    @Column(name = "NUMERORATA", nullable = true)
    private Integer numerorata;
    
    @Column(name = "NUMCONCILIAZIONE", nullable = true)
    private Integer numconciliazione;
    
    @Column(name = "FLAGPROVVISORIA", nullable = true, length = 1)
    private String flagprovvisoria;
    
    @Column(name = "FLAGFTBLOCCATA", nullable = true, length = 1)
    private String flagftbloccata;
    
    @Column(name = "VALUTA", nullable = true, length = 3)
    private String valuta;
    
    @Column(name = "CAMBIO", nullable = true, precision = 0)
    private Double cambio;
    
    @Column(name = "FLAGGENERICO", nullable = true, length = 1)
    private String flaggenerico;
    
    @Column(name = "CONTROLLOBF", nullable = true, length = 1)
    private String controllobf;
    
    @Column(name = "AGENTE", nullable = true, length = 3)
    private String agente;
    
    @Column(name = "BANCAPAG", nullable = true, length = 3)
    private String bancapag;
    
    @Column(name = "DATAVALUTA", nullable = true)
    private LocalDate datavaluta;
    
    @Column(name = "CIG", nullable = true, length = 15)
    private String cig;
    
    @Column(name = "CUP", nullable = true, length = 15)
    private String cup;
    
    @Column(name = "VALUSER1", nullable = true, precision = 0)
    private Double valuser1;
    
    @Column(name = "VALUSER2", nullable = true, precision = 0)
    private Double valuser2;
    
    @Column(name = "VALUSER3", nullable = true, precision = 0)
    private Double valuser3;
    
    @Column(name = "VALUSER4", nullable = true, precision = 0)
    private Double valuser4;
    
    @Column(name = "VALUSER5", nullable = true, precision = 0)
    private Double valuser5;
    
    @Column(name = "CAMPOUSER1", nullable = true, length = 30)
    private String campouser1;
    
    @Column(name = "CAMPOUSER2", nullable = true, length = 30)
    private String campouser2;
    
    @Column(name = "CAMPOUSER3", nullable = true, length = 30)
    private String campouser3;
    
    @Column(name = "CAMPOUSER4", nullable = true, length = 30)
    private String campouser4;
    
    @Column(name = "CAMPOUSER5", nullable = true, length = 30)
    private String campouser5;
    
    @Column(name = "USERNAME", nullable = true, length = 20)
    private String username;
    
    @Column(name = "DATAMODIFICA", nullable = true)
    private LocalDate datamodifica;
    
    @Column(name = "NOTEPRIMANOTA", nullable = true, length = -1)
    private String noteprimanota;
    
    @Column(name = "PROVENIENZA", nullable = true, length = 1)
    private String provenienza;
    
    @Column(name = "SYS_CREATEDATE", nullable = true)
    private LocalDate sysCreatedate;
    
    @Column(name = "SYS_CREATEUSER", nullable = true, length = 20)
    private String sysCreateuser;
    
    @Column(name = "SYS_UPDATEDATE", nullable = true)
    private LocalDate sysUpdatedate;
    
    @Column(name = "SYS_UPDATEUSER", nullable = true, length = 20)
    private String sysUpdateuser;
    
    @Column(name = "SPESEENASARCO", nullable = true, precision = 0)
    private Double speseenasarco;
    
    @Column(name = "IBAN", nullable = true, length = 40)
    private String iban;
    
    @Column(name = "ANNODIRITTO", nullable = true, precision = 0)
    private Double annodiritto;
    
    @Column(name = "PID", nullable = true)
    private Integer pid;
    
    @Column(name = "IVASP", nullable = true, precision = 0)
    private Double ivasp;
    
    @Column(name = "SPESECOD8", nullable = true, precision = 0)
    private Double spesecod8;

    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public String getGiornale() {
        return giornale;
    }

    public void setGiornale(String giornale) {
        this.giornale = giornale;
    }

    public int getProtocollo() {
        return protocollo;
    }

    public void setProtocollo(int protocollo) {
        this.protocollo = protocollo;
    }

    public int getProgrprimanota() {
        return progrprimanota;
    }

    public void setProgrprimanota(int progrprimanota) {
        this.progrprimanota = progrprimanota;
    }

    public Integer getProgrgenerale() {
        return progrgenerale;
    }

    public void setProgrgenerale(Integer progrgenerale) {
        this.progrgenerale = progrgenerale;
    }

    public String getDivisione() {
        return divisione;
    }

    public void setDivisione(String divisione) {
        this.divisione = divisione;
    }

    public Integer getAnnopartita() {
        return annopartita;
    }

    public void setAnnopartita(Integer annopartita) {
        this.annopartita = annopartita;
    }

    public Integer getNumpartita() {
        return numpartita;
    }

    public void setNumpartita(Integer numpartita) {
        this.numpartita = numpartita;
    }

    public LocalDate getDataoperazione() {
        return dataoperazione;
    }

    public void setDataoperazione(LocalDate dataoperazione) {
        this.dataoperazione = dataoperazione;
    }

    public LocalDate getDatamovimento() {
        return datamovimento;
    }

    public void setDatamovimento(LocalDate datamovimento) {
        this.datamovimento = datamovimento;
    }

    public String getNumerodocumento() {
        return numerodocumento;
    }

    public void setNumerodocumento(String numerodocumento) {
        this.numerodocumento = numerodocumento;
    }

    public String getCausale() {
        return causale;
    }

    public void setCausale(String causale) {
        this.causale = causale;
    }

    public Integer getGruppoconto() {
        return gruppoconto;
    }

    public void setGruppoconto(Integer gruppoconto) {
        this.gruppoconto = gruppoconto;
    }

    public String getSottoconto() {
        return sottoconto;
    }

    public void setSottoconto(String sottoconto) {
        this.sottoconto = sottoconto;
    }

    public String getDescrsuppl() {
        return descrsuppl;
    }

    public void setDescrsuppl(String descrsuppl) {
        this.descrsuppl = descrsuppl;
    }

    public Double getImporto() {
        return importo;
    }

    public void setImporto(Double importo) {
        this.importo = importo;
    }

    public Integer getRigogiornale() {
        return rigogiornale;
    }

    public void setRigogiornale(Integer rigogiornale) {
        this.rigogiornale = rigogiornale;
    }

    public String getCodicepagamento() {
        return codicepagamento;
    }

    public void setCodicepagamento(String codicepagamento) {
        this.codicepagamento = codicepagamento;
    }

    public String getCoddiffpag() {
        return coddiffpag;
    }

    public void setCoddiffpag(String coddiffpag) {
        this.coddiffpag = coddiffpag;
    }

    public String getIvaprimascad() {
        return ivaprimascad;
    }

    public void setIvaprimascad(String ivaprimascad) {
        this.ivaprimascad = ivaprimascad;
    }

    public LocalDate getDataprimascad() {
        return dataprimascad;
    }

    public void setDataprimascad(LocalDate dataprimascad) {
        this.dataprimascad = dataprimascad;
    }

    public Double getImportove() {
        return importove;
    }

    public void setImportove(Double importove) {
        this.importove = importove;
    }

    public LocalDate getDataprenotato() {
        return dataprenotato;
    }

    public void setDataprenotato(LocalDate dataprenotato) {
        this.dataprenotato = dataprenotato;
    }

    public Double getImpprenotato() {
        return impprenotato;
    }

    public void setImpprenotato(Double impprenotato) {
        this.impprenotato = impprenotato;
    }

    public LocalDate getDataautorizza() {
        return dataautorizza;
    }

    public void setDataautorizza(LocalDate dataautorizza) {
        this.dataautorizza = dataautorizza;
    }

    public String getUserautorizza() {
        return userautorizza;
    }

    public void setUserautorizza(String userautorizza) {
        this.userautorizza = userautorizza;
    }

    public LocalDate getInizioperiodo() {
        return inizioperiodo;
    }

    public void setInizioperiodo(LocalDate inizioperiodo) {
        this.inizioperiodo = inizioperiodo;
    }

    public LocalDate getFineperiodo() {
        return fineperiodo;
    }

    public void setFineperiodo(LocalDate fineperiodo) {
        this.fineperiodo = fineperiodo;
    }

    public Double getOnorario() {
        return onorario;
    }

    public void setOnorario(Double onorario) {
        this.onorario = onorario;
    }

    public Double getSpese() {
        return spese;
    }

    public void setSpese(Double spese) {
        this.spese = spese;
    }

    public Double getSpeseno770() {
        return speseno770;
    }

    public void setSpeseno770(Double speseno770) {
        this.speseno770 = speseno770;
    }

    public String getTipocompenso() {
        return tipocompenso;
    }

    public void setTipocompenso(String tipocompenso) {
        this.tipocompenso = tipocompenso;
    }

    public Double getRitenuta() {
        return ritenuta;
    }

    public void setRitenuta(Double ritenuta) {
        this.ritenuta = ritenuta;
    }

    public Double getRitinps() {
        return ritinps;
    }

    public void setRitinps(Double ritinps) {
        this.ritinps = ritinps;
    }

    public Double getRitinail() {
        return ritinail;
    }

    public void setRitinail(Double ritinail) {
        this.ritinail = ritinail;
    }

    public Double getRitenasarco() {
        return ritenasarco;
    }

    public void setRitenasarco(Double ritenasarco) {
        this.ritenasarco = ritenasarco;
    }

    public Integer getAnnoenasarco() {
        return annoenasarco;
    }

    public void setAnnoenasarco(Integer annoenasarco) {
        this.annoenasarco = annoenasarco;
    }

    public Integer getTrimenasarco() {
        return trimenasarco;
    }

    public void setTrimenasarco(Integer trimenasarco) {
        this.trimenasarco = trimenasarco;
    }

    public String getFlagtrasferito() {
        return flagtrasferito;
    }

    public void setFlagtrasferito(String flagtrasferito) {
        this.flagtrasferito = flagtrasferito;
    }

    public Integer getNumerorata() {
        return numerorata;
    }

    public void setNumerorata(Integer numerorata) {
        this.numerorata = numerorata;
    }

    public Integer getNumconciliazione() {
        return numconciliazione;
    }

    public void setNumconciliazione(Integer numconciliazione) {
        this.numconciliazione = numconciliazione;
    }

    public String getFlagprovvisoria() {
        return flagprovvisoria;
    }

    public void setFlagprovvisoria(String flagprovvisoria) {
        this.flagprovvisoria = flagprovvisoria;
    }

    public String getFlagftbloccata() {
        return flagftbloccata;
    }

    public void setFlagftbloccata(String flagftbloccata) {
        this.flagftbloccata = flagftbloccata;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    public Double getCambio() {
        return cambio;
    }

    public void setCambio(Double cambio) {
        this.cambio = cambio;
    }

    public String getFlaggenerico() {
        return flaggenerico;
    }

    public void setFlaggenerico(String flaggenerico) {
        this.flaggenerico = flaggenerico;
    }

    public String getControllobf() {
        return controllobf;
    }

    public void setControllobf(String controllobf) {
        this.controllobf = controllobf;
    }

    public String getAgente() {
        return agente;
    }

    public void setAgente(String agente) {
        this.agente = agente;
    }

    public String getBancapag() {
        return bancapag;
    }

    public void setBancapag(String bancapag) {
        this.bancapag = bancapag;
    }

    public LocalDate getDatavaluta() {
        return datavaluta;
    }

    public void setDatavaluta(LocalDate datavaluta) {
        this.datavaluta = datavaluta;
    }

    public String getCig() {
        return cig;
    }

    public void setCig(String cig) {
        this.cig = cig;
    }

    public String getCup() {
        return cup;
    }

    public void setCup(String cup) {
        this.cup = cup;
    }

    public Double getValuser1() {
        return valuser1;
    }

    public void setValuser1(Double valuser1) {
        this.valuser1 = valuser1;
    }

    public Double getValuser2() {
        return valuser2;
    }

    public void setValuser2(Double valuser2) {
        this.valuser2 = valuser2;
    }

    public Double getValuser3() {
        return valuser3;
    }

    public void setValuser3(Double valuser3) {
        this.valuser3 = valuser3;
    }

    public Double getValuser4() {
        return valuser4;
    }

    public void setValuser4(Double valuser4) {
        this.valuser4 = valuser4;
    }

    public Double getValuser5() {
        return valuser5;
    }

    public void setValuser5(Double valuser5) {
        this.valuser5 = valuser5;
    }

    public String getCampouser1() {
        return campouser1;
    }

    public void setCampouser1(String campouser1) {
        this.campouser1 = campouser1;
    }

    public String getCampouser2() {
        return campouser2;
    }

    public void setCampouser2(String campouser2) {
        this.campouser2 = campouser2;
    }

    public String getCampouser3() {
        return campouser3;
    }

    public void setCampouser3(String campouser3) {
        this.campouser3 = campouser3;
    }

    public String getCampouser4() {
        return campouser4;
    }

    public void setCampouser4(String campouser4) {
        this.campouser4 = campouser4;
    }

    public String getCampouser5() {
        return campouser5;
    }

    public void setCampouser5(String campouser5) {
        this.campouser5 = campouser5;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDatamodifica() {
        return datamodifica;
    }

    public void setDatamodifica(LocalDate datamodifica) {
        this.datamodifica = datamodifica;
    }

    public String getNoteprimanota() {
        return noteprimanota;
    }

    public void setNoteprimanota(String noteprimanota) {
        this.noteprimanota = noteprimanota;
    }

    public String getProvenienza() {
        return provenienza;
    }

    public void setProvenienza(String provenienza) {
        this.provenienza = provenienza;
    }

    public LocalDate getSysCreatedate() {
        return sysCreatedate;
    }

    public void setSysCreatedate(LocalDate sysCreatedate) {
        this.sysCreatedate = sysCreatedate;
    }

    public String getSysCreateuser() {
        return sysCreateuser;
    }

    public void setSysCreateuser(String sysCreateuser) {
        this.sysCreateuser = sysCreateuser;
    }

    public LocalDate getSysUpdatedate() {
        return sysUpdatedate;
    }

    public void setSysUpdatedate(LocalDate sysUpdatedate) {
        this.sysUpdatedate = sysUpdatedate;
    }

    public String getSysUpdateuser() {
        return sysUpdateuser;
    }

    public void setSysUpdateuser(String sysUpdateuser) {
        this.sysUpdateuser = sysUpdateuser;
    }

    public Double getSpeseenasarco() {
        return speseenasarco;
    }

    public void setSpeseenasarco(Double speseenasarco) {
        this.speseenasarco = speseenasarco;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Double getAnnodiritto() {
        return annodiritto;
    }

    public void setAnnodiritto(Double annodiritto) {
        this.annodiritto = annodiritto;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Double getIvasp() {
        return ivasp;
    }

    public void setIvasp(Double ivasp) {
        this.ivasp = ivasp;
    }

    public Double getSpesecod8() {
        return spesecod8;
    }

    public void setSpesecod8(Double spesecod8) {
        this.spesecod8 = spesecod8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Primanota primanota = (Primanota) o;

        if (anno != primanota.anno) return false;
        if (protocollo != primanota.protocollo) return false;
        if (progrprimanota != primanota.progrprimanota) return false;
        if (giornale != null ? !giornale.equals(primanota.giornale) : primanota.giornale != null) return false;
        if (progrgenerale != null ? !progrgenerale.equals(primanota.progrgenerale) : primanota.progrgenerale != null)
            return false;
        if (divisione != null ? !divisione.equals(primanota.divisione) : primanota.divisione != null) return false;
        if (annopartita != null ? !annopartita.equals(primanota.annopartita) : primanota.annopartita != null)
            return false;
        if (numpartita != null ? !numpartita.equals(primanota.numpartita) : primanota.numpartita != null) return false;
        if (dataoperazione != null ? !dataoperazione.equals(primanota.dataoperazione) : primanota.dataoperazione != null)
            return false;
        if (datamovimento != null ? !datamovimento.equals(primanota.datamovimento) : primanota.datamovimento != null)
            return false;
        if (numerodocumento != null ? !numerodocumento.equals(primanota.numerodocumento) : primanota.numerodocumento != null)
            return false;
        if (causale != null ? !causale.equals(primanota.causale) : primanota.causale != null) return false;
        if (gruppoconto != null ? !gruppoconto.equals(primanota.gruppoconto) : primanota.gruppoconto != null)
            return false;
        if (sottoconto != null ? !sottoconto.equals(primanota.sottoconto) : primanota.sottoconto != null) return false;
        if (descrsuppl != null ? !descrsuppl.equals(primanota.descrsuppl) : primanota.descrsuppl != null) return false;
        if (importo != null ? !importo.equals(primanota.importo) : primanota.importo != null) return false;
        if (rigogiornale != null ? !rigogiornale.equals(primanota.rigogiornale) : primanota.rigogiornale != null)
            return false;
        if (codicepagamento != null ? !codicepagamento.equals(primanota.codicepagamento) : primanota.codicepagamento != null)
            return false;
        if (coddiffpag != null ? !coddiffpag.equals(primanota.coddiffpag) : primanota.coddiffpag != null) return false;
        if (ivaprimascad != null ? !ivaprimascad.equals(primanota.ivaprimascad) : primanota.ivaprimascad != null)
            return false;
        if (dataprimascad != null ? !dataprimascad.equals(primanota.dataprimascad) : primanota.dataprimascad != null)
            return false;
        if (importove != null ? !importove.equals(primanota.importove) : primanota.importove != null) return false;
        if (dataprenotato != null ? !dataprenotato.equals(primanota.dataprenotato) : primanota.dataprenotato != null)
            return false;
        if (impprenotato != null ? !impprenotato.equals(primanota.impprenotato) : primanota.impprenotato != null)
            return false;
        if (dataautorizza != null ? !dataautorizza.equals(primanota.dataautorizza) : primanota.dataautorizza != null)
            return false;
        if (userautorizza != null ? !userautorizza.equals(primanota.userautorizza) : primanota.userautorizza != null)
            return false;
        if (inizioperiodo != null ? !inizioperiodo.equals(primanota.inizioperiodo) : primanota.inizioperiodo != null)
            return false;
        if (fineperiodo != null ? !fineperiodo.equals(primanota.fineperiodo) : primanota.fineperiodo != null)
            return false;
        if (onorario != null ? !onorario.equals(primanota.onorario) : primanota.onorario != null) return false;
        if (spese != null ? !spese.equals(primanota.spese) : primanota.spese != null) return false;
        if (speseno770 != null ? !speseno770.equals(primanota.speseno770) : primanota.speseno770 != null) return false;
        if (tipocompenso != null ? !tipocompenso.equals(primanota.tipocompenso) : primanota.tipocompenso != null)
            return false;
        if (ritenuta != null ? !ritenuta.equals(primanota.ritenuta) : primanota.ritenuta != null) return false;
        if (ritinps != null ? !ritinps.equals(primanota.ritinps) : primanota.ritinps != null) return false;
        if (ritinail != null ? !ritinail.equals(primanota.ritinail) : primanota.ritinail != null) return false;
        if (ritenasarco != null ? !ritenasarco.equals(primanota.ritenasarco) : primanota.ritenasarco != null)
            return false;
        if (annoenasarco != null ? !annoenasarco.equals(primanota.annoenasarco) : primanota.annoenasarco != null)
            return false;
        if (trimenasarco != null ? !trimenasarco.equals(primanota.trimenasarco) : primanota.trimenasarco != null)
            return false;
        if (flagtrasferito != null ? !flagtrasferito.equals(primanota.flagtrasferito) : primanota.flagtrasferito != null)
            return false;
        if (numerorata != null ? !numerorata.equals(primanota.numerorata) : primanota.numerorata != null) return false;
        if (numconciliazione != null ? !numconciliazione.equals(primanota.numconciliazione) : primanota.numconciliazione != null)
            return false;
        if (flagprovvisoria != null ? !flagprovvisoria.equals(primanota.flagprovvisoria) : primanota.flagprovvisoria != null)
            return false;
        if (flagftbloccata != null ? !flagftbloccata.equals(primanota.flagftbloccata) : primanota.flagftbloccata != null)
            return false;
        if (valuta != null ? !valuta.equals(primanota.valuta) : primanota.valuta != null) return false;
        if (cambio != null ? !cambio.equals(primanota.cambio) : primanota.cambio != null) return false;
        if (flaggenerico != null ? !flaggenerico.equals(primanota.flaggenerico) : primanota.flaggenerico != null)
            return false;
        if (controllobf != null ? !controllobf.equals(primanota.controllobf) : primanota.controllobf != null)
            return false;
        if (agente != null ? !agente.equals(primanota.agente) : primanota.agente != null) return false;
        if (bancapag != null ? !bancapag.equals(primanota.bancapag) : primanota.bancapag != null) return false;
        if (datavaluta != null ? !datavaluta.equals(primanota.datavaluta) : primanota.datavaluta != null) return false;
        if (cig != null ? !cig.equals(primanota.cig) : primanota.cig != null) return false;
        if (cup != null ? !cup.equals(primanota.cup) : primanota.cup != null) return false;
        if (valuser1 != null ? !valuser1.equals(primanota.valuser1) : primanota.valuser1 != null) return false;
        if (valuser2 != null ? !valuser2.equals(primanota.valuser2) : primanota.valuser2 != null) return false;
        if (valuser3 != null ? !valuser3.equals(primanota.valuser3) : primanota.valuser3 != null) return false;
        if (valuser4 != null ? !valuser4.equals(primanota.valuser4) : primanota.valuser4 != null) return false;
        if (valuser5 != null ? !valuser5.equals(primanota.valuser5) : primanota.valuser5 != null) return false;
        if (campouser1 != null ? !campouser1.equals(primanota.campouser1) : primanota.campouser1 != null) return false;
        if (campouser2 != null ? !campouser2.equals(primanota.campouser2) : primanota.campouser2 != null) return false;
        if (campouser3 != null ? !campouser3.equals(primanota.campouser3) : primanota.campouser3 != null) return false;
        if (campouser4 != null ? !campouser4.equals(primanota.campouser4) : primanota.campouser4 != null) return false;
        if (campouser5 != null ? !campouser5.equals(primanota.campouser5) : primanota.campouser5 != null) return false;
        if (username != null ? !username.equals(primanota.username) : primanota.username != null) return false;
        if (datamodifica != null ? !datamodifica.equals(primanota.datamodifica) : primanota.datamodifica != null)
            return false;
        if (noteprimanota != null ? !noteprimanota.equals(primanota.noteprimanota) : primanota.noteprimanota != null)
            return false;
        if (provenienza != null ? !provenienza.equals(primanota.provenienza) : primanota.provenienza != null)
            return false;
        if (sysCreatedate != null ? !sysCreatedate.equals(primanota.sysCreatedate) : primanota.sysCreatedate != null)
            return false;
        if (sysCreateuser != null ? !sysCreateuser.equals(primanota.sysCreateuser) : primanota.sysCreateuser != null)
            return false;
        if (sysUpdatedate != null ? !sysUpdatedate.equals(primanota.sysUpdatedate) : primanota.sysUpdatedate != null)
            return false;
        if (sysUpdateuser != null ? !sysUpdateuser.equals(primanota.sysUpdateuser) : primanota.sysUpdateuser != null)
            return false;
        if (speseenasarco != null ? !speseenasarco.equals(primanota.speseenasarco) : primanota.speseenasarco != null)
            return false;
        if (iban != null ? !iban.equals(primanota.iban) : primanota.iban != null) return false;
        if (annodiritto != null ? !annodiritto.equals(primanota.annodiritto) : primanota.annodiritto != null)
            return false;
        if (pid != null ? !pid.equals(primanota.pid) : primanota.pid != null) return false;
        if (ivasp != null ? !ivasp.equals(primanota.ivasp) : primanota.ivasp != null) return false;
        if (spesecod8 != null ? !spesecod8.equals(primanota.spesecod8) : primanota.spesecod8 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = anno;
        result = 31 * result + (giornale != null ? giornale.hashCode() : 0);
        result = 31 * result + protocollo;
        result = 31 * result + progrprimanota;
        result = 31 * result + (progrgenerale != null ? progrgenerale.hashCode() : 0);
        result = 31 * result + (divisione != null ? divisione.hashCode() : 0);
        result = 31 * result + (annopartita != null ? annopartita.hashCode() : 0);
        result = 31 * result + (numpartita != null ? numpartita.hashCode() : 0);
        result = 31 * result + (dataoperazione != null ? dataoperazione.hashCode() : 0);
        result = 31 * result + (datamovimento != null ? datamovimento.hashCode() : 0);
        result = 31 * result + (numerodocumento != null ? numerodocumento.hashCode() : 0);
        result = 31 * result + (causale != null ? causale.hashCode() : 0);
        result = 31 * result + (gruppoconto != null ? gruppoconto.hashCode() : 0);
        result = 31 * result + (sottoconto != null ? sottoconto.hashCode() : 0);
        result = 31 * result + (descrsuppl != null ? descrsuppl.hashCode() : 0);
        result = 31 * result + (importo != null ? importo.hashCode() : 0);
        result = 31 * result + (rigogiornale != null ? rigogiornale.hashCode() : 0);
        result = 31 * result + (codicepagamento != null ? codicepagamento.hashCode() : 0);
        result = 31 * result + (coddiffpag != null ? coddiffpag.hashCode() : 0);
        result = 31 * result + (ivaprimascad != null ? ivaprimascad.hashCode() : 0);
        result = 31 * result + (dataprimascad != null ? dataprimascad.hashCode() : 0);
        result = 31 * result + (importove != null ? importove.hashCode() : 0);
        result = 31 * result + (dataprenotato != null ? dataprenotato.hashCode() : 0);
        result = 31 * result + (impprenotato != null ? impprenotato.hashCode() : 0);
        result = 31 * result + (dataautorizza != null ? dataautorizza.hashCode() : 0);
        result = 31 * result + (userautorizza != null ? userautorizza.hashCode() : 0);
        result = 31 * result + (inizioperiodo != null ? inizioperiodo.hashCode() : 0);
        result = 31 * result + (fineperiodo != null ? fineperiodo.hashCode() : 0);
        result = 31 * result + (onorario != null ? onorario.hashCode() : 0);
        result = 31 * result + (spese != null ? spese.hashCode() : 0);
        result = 31 * result + (speseno770 != null ? speseno770.hashCode() : 0);
        result = 31 * result + (tipocompenso != null ? tipocompenso.hashCode() : 0);
        result = 31 * result + (ritenuta != null ? ritenuta.hashCode() : 0);
        result = 31 * result + (ritinps != null ? ritinps.hashCode() : 0);
        result = 31 * result + (ritinail != null ? ritinail.hashCode() : 0);
        result = 31 * result + (ritenasarco != null ? ritenasarco.hashCode() : 0);
        result = 31 * result + (annoenasarco != null ? annoenasarco.hashCode() : 0);
        result = 31 * result + (trimenasarco != null ? trimenasarco.hashCode() : 0);
        result = 31 * result + (flagtrasferito != null ? flagtrasferito.hashCode() : 0);
        result = 31 * result + (numerorata != null ? numerorata.hashCode() : 0);
        result = 31 * result + (numconciliazione != null ? numconciliazione.hashCode() : 0);
        result = 31 * result + (flagprovvisoria != null ? flagprovvisoria.hashCode() : 0);
        result = 31 * result + (flagftbloccata != null ? flagftbloccata.hashCode() : 0);
        result = 31 * result + (valuta != null ? valuta.hashCode() : 0);
        result = 31 * result + (cambio != null ? cambio.hashCode() : 0);
        result = 31 * result + (flaggenerico != null ? flaggenerico.hashCode() : 0);
        result = 31 * result + (controllobf != null ? controllobf.hashCode() : 0);
        result = 31 * result + (agente != null ? agente.hashCode() : 0);
        result = 31 * result + (bancapag != null ? bancapag.hashCode() : 0);
        result = 31 * result + (datavaluta != null ? datavaluta.hashCode() : 0);
        result = 31 * result + (cig != null ? cig.hashCode() : 0);
        result = 31 * result + (cup != null ? cup.hashCode() : 0);
        result = 31 * result + (valuser1 != null ? valuser1.hashCode() : 0);
        result = 31 * result + (valuser2 != null ? valuser2.hashCode() : 0);
        result = 31 * result + (valuser3 != null ? valuser3.hashCode() : 0);
        result = 31 * result + (valuser4 != null ? valuser4.hashCode() : 0);
        result = 31 * result + (valuser5 != null ? valuser5.hashCode() : 0);
        result = 31 * result + (campouser1 != null ? campouser1.hashCode() : 0);
        result = 31 * result + (campouser2 != null ? campouser2.hashCode() : 0);
        result = 31 * result + (campouser3 != null ? campouser3.hashCode() : 0);
        result = 31 * result + (campouser4 != null ? campouser4.hashCode() : 0);
        result = 31 * result + (campouser5 != null ? campouser5.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (datamodifica != null ? datamodifica.hashCode() : 0);
        result = 31 * result + (noteprimanota != null ? noteprimanota.hashCode() : 0);
        result = 31 * result + (provenienza != null ? provenienza.hashCode() : 0);
        result = 31 * result + (sysCreatedate != null ? sysCreatedate.hashCode() : 0);
        result = 31 * result + (sysCreateuser != null ? sysCreateuser.hashCode() : 0);
        result = 31 * result + (sysUpdatedate != null ? sysUpdatedate.hashCode() : 0);
        result = 31 * result + (sysUpdateuser != null ? sysUpdateuser.hashCode() : 0);
        result = 31 * result + (speseenasarco != null ? speseenasarco.hashCode() : 0);
        result = 31 * result + (iban != null ? iban.hashCode() : 0);
        result = 31 * result + (annodiritto != null ? annodiritto.hashCode() : 0);
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        result = 31 * result + (ivasp != null ? ivasp.hashCode() : 0);
        result = 31 * result + (spesecod8 != null ? spesecod8.hashCode() : 0);
        return result;
    }
}
