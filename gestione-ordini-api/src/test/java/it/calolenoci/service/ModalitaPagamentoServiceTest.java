package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.entity.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ModalitaPagamentoServiceTest {
    @Inject ModalitaPagamentoService service;
    @Inject EntityManager em;

    private int numero() { return 700_000_000 + Math.floorMod(UUID.randomUUID().hashCode(),100_000_000); }
    private Ordine ordine(String stato) {
        Ordine o=new Ordine(); o.setAnno(2038); o.setSerie("MP1"); o.setProgressivo(numero());
        o.setGruppoCliente(2351); o.setContoCliente("MPTEST"); o.setProvvisorio("N"); o.setDataConferma(LocalDate.now()); o.persist();
        GoOrdine go=new GoOrdine(); go.setAnno(o.getAnno()); go.setSerie(o.getSerie()); go.setProgressivo(o.getProgressivo()); go.setStatus(stato); go.persist();
        riga(o,1,"TEST",1,1000,LocalDate.now()); return o;
    }
    private OrdineDettaglio riga(Ordine o,int rigo,String codice,double quantita,double prezzo,LocalDate data) {
        OrdineDettaglio d=new OrdineDettaglio();
        d.setAnno(o.getAnno()); d.setSerie(o.getSerie()); d.setProgressivo(o.getProgressivo()); d.setRigo(rigo);
        d.setProgrGenerale(-numero()); d.setFArticolo(codice); d.setTipoRigo(codice.startsWith("*") ? "C" : " ");
        d.setFDescrArticolo(codice.equals("*ACCONTO") ? "Acconto" : "Saldo");
        d.setQuantita(quantita); d.setPrezzo(prezzo); d.setDataConfConsegna(data); d.persist(); return d;
    }
    private GoPagamentoOrdine snapshot(int id) { em.flush(); em.clear(); return GoPagamentoOrdine.findById(id); }
    private void sincronizza() { em.flush(); em.clear(); service.riconcilia(); em.flush(); em.clear(); }

    @Test void avviaIlLockAncheSenzaScritturePrecedentiNellaTransazione() {
        assertDoesNotThrow(() -> service.riconciliaOrdine(1900,"00",Integer.MIN_VALUE));
    }

    @Test @TestTransaction void immediatoESaldoSoloQuandoCompleto() {
        Ordine o=ordine("INCOMPLETO");
        int acc=riga(o,2,"*ACCONTO",30,0,o.getDataConferma()).getProgrGenerale();
        int saldo=riga(o,3,"*SALDO",70,0,o.getDataConferma()).getProgrGenerale();
        sincronizza();
        assertEquals("DA_FATTURARE",snapshot(acc).getStato());
        assertEquals("AL_PRONTO",snapshot(saldo).getStato());
        GoOrdine.update("status='COMPLETO' WHERE anno=?1 AND serie=?2 AND progressivo=?3",o.getAnno(),o.getSerie(),o.getProgressivo());
        sincronizza();
        assertEquals("DA_FATTURARE",snapshot(saldo).getStato());
    }
    @Test @TestTransaction void cambiaMeseDaPlurimaAncheSenzaTimestampERimuoveRigheCancellate() {
        Ordine o=ordine("INCOMPLETO");
        LocalDate futura=LocalDate.now().plusMonths(2).withDayOfMonth(17);
        int id=riga(o,2,"*ACCONTO",30,0,futura).getProgrGenerale();
        riga(o,3,"*SALDO",70,0,o.getDataConferma());
        sincronizza();
        String descr=snapshot(id).getDescrizioneScritta();
        assertTrue(descr.startsWith("Acconto "));
        assertEquals(futura.withDayOfMonth(1),snapshot(id).getDataScadenza());
        sincronizza();
        assertEquals(descr,snapshot(id).getDescrizioneScritta());
        OrdineDettaglio.update("dataConfConsegna=?1 WHERE progrGenerale=?2",o.getDataConferma(),id);
        sincronizza();
        assertEquals("Acconto",snapshot(id).getDescrizioneScritta());
        assertEquals("DA_FATTURARE",snapshot(id).getStato());
        OrdineDettaglio.delete("progrGenerale",id);
        sincronizza();
        assertNull(snapshot(id));
    }
    @Test @TestTransaction void piuAccontiSenzaCambioDataSonoAnomalie() {
        Ordine o=ordine("INCOMPLETO");
        int a=riga(o,2,"*ACCONTO",30,0,o.getDataConferma()).getProgrGenerale();
        int b=riga(o,3,"*ACCONTO",30,0,o.getDataConferma()).getProgrGenerale();
        riga(o,4,"*SALDO",40,0,o.getDataConferma());
        sincronizza();
        assertEquals("ANOMALIA",snapshot(a).getStato());
        assertEquals("ANOMALIA",snapshot(b).getStato());
        assertTrue(snapshot(a).getAnomalia().contains("diversa"));
    }
    @Test @TestTransaction void controlloPuntualeNonRileggeNeModificaAltriOrdini() {
        Ordine primo=ordine("INCOMPLETO");
        int a=riga(primo,2,"*ACCONTO",30,0,primo.getDataConferma()).getProgrGenerale();
        riga(primo,3,"*SALDO",70,0,primo.getDataConferma());
        Ordine altro=ordine("INCOMPLETO");
        int b=riga(altro,2,"*ACCONTO",30,0,altro.getDataConferma()).getProgrGenerale();
        riga(altro,3,"*SALDO",70,0,altro.getDataConferma());
        sincronizza();
        var controlloAltro=snapshot(b).getControllatoIl();
        OrdineDettaglio.update("dataConfConsegna=?1 WHERE progrGenerale=?2",LocalDate.now().plusMonths(2),b);
        em.flush(); em.clear();
        service.riconciliaOrdine(primo.getAnno(),primo.getSerie(),primo.getProgressivo());
        assertEquals("DA_FATTURARE",snapshot(a).getStato());
        assertEquals(controlloAltro,snapshot(b).getControllatoIl());
        assertEquals("DA_FATTURARE",snapshot(b).getStato());
        service.riconciliaOrdine(2038,"MP1",1);
        assertNotNull(snapshot(a));
        assertNotNull(snapshot(b));
        OrdineDettaglio.delete("progrGenerale",a);
        service.riconciliaOrdine(primo.getAnno(),primo.getSerie(),primo.getProgressivo());
        assertNull(snapshot(a));
        assertNotNull(snapshot(b));
    }
    @Test @TestTransaction void saldoZeroNonRichiedeFattura() {
        Ordine o=ordine("COMPLETO");
        riga(o,2,"*ACCONTO",100,0,o.getDataConferma());
        int saldo=riga(o,3,"*SALDO",0,0,o.getDataConferma()).getProgrGenerale();
        sincronizza();
        assertEquals("NON_DOVUTO",snapshot(saldo).getStato());
    }
    @Test @TestTransaction void fatturaGiaCreataEscludeAccontoAnchePrimaDiValidazione() {
        Ordine o=ordine("COMPLETO");
        int acc=riga(o,2,"*ACCONTO",30,0,o.getDataConferma()).getProgrGenerale();
        int saldo=riga(o,3,"*SALDO",70,0,o.getDataConferma()).getProgrGenerale();
        int fattura=fattura(o,300D);
        sincronizza();
        assertEquals("EMESSO",snapshot(acc).getStato());
        assertEquals("DA_FATTURARE",snapshot(saldo).getStato());
        assertTrue(snapshot(acc).getFatture().contains(Integer.toString(fattura)));
        GoOrdine.update("status='ARCHIVIATO' WHERE anno=?1 AND serie=?2 AND progressivo=?3",o.getAnno(),o.getSerie(),o.getProgressivo());
        sincronizza();
        assertNull(snapshot(acc));
    }
    private int fattura(Ordine o, double importo) {
        Fatture f=new Fatture(); f.setAnno(2038); f.setSerie("A"); f.setProgressivo(numero());
        f.setGruppoCliente(2351); f.setContoCliente("MPTEST"); f.persist();
        for (int i=1;i<=3;i++) {
            FattureDettaglio d=new FattureDettaglio(); d.setAnno(f.getAnno()); d.setSerie(f.getSerie());
            d.setProgressivo(f.getProgressivo()); d.setRigo(i); d.setProgrGenerale(-numero());
            d.setFArticolo(i==1 ? "*ACC" : ""); d.setTipoRigo(i==1 ? "V" : "C");
            d.setPrezzo(i==1 ? importo : 0D); d.setIva(i==1 ? "22" : "");
            d.setFDescrArticolo(i==1 ? "Acconto" : i==2 ? "Su prossima fornitura" : "ordine n."+o.getAnno()+"/"+o.getSerie()+"/"+o.getProgressivo());
            d.persist();
        }
        return f.getProgressivo();
    }

    @Test @TestTransaction void trentacinquePercentoEscludeCounterEWarningDellaPrimaDiQuattroTrancheUguali() {
        Ordine o=ordine("COMPLETO");
        int primo=riga(o,2,"*ACCONTO",25,0,o.getDataConferma()).getProgrGenerale();
        int secondo=riga(o,3,"*ACCONTO",25,0,o.getDataConferma()).getProgrGenerale();
        int terzo=riga(o,4,"*ACCONTO",25,0,o.getDataConferma()).getProgrGenerale();
        int saldo=riga(o,5,"*SALDO",25,0,o.getDataConferma()).getProgrGenerale();
        fattura(o,350D);
        sincronizza();
        assertEquals("EMESSO",snapshot(primo).getStato());
        assertEquals("",snapshot(primo).getAnomalia());
        assertEquals("ANOMALIA",snapshot(secondo).getStato());
        assertTrue(snapshot(secondo).getAnomalia().contains("diversa"));
        assertEquals("ANOMALIA",snapshot(terzo).getStato());
        assertEquals("DA_FATTURARE",snapshot(saldo).getStato());
        fattura(o,250D);
        sincronizza();
        assertEquals("EMESSO",snapshot(secondo).getStato());
        assertEquals("",snapshot(secondo).getAnomalia());
        assertEquals("ANOMALIA",snapshot(terzo).getStato());
        fattura(o,400D);
        sincronizza();
        for (int id : new int[]{primo,secondo,terzo,saldo}) {
            assertEquals("EMESSO",snapshot(id).getStato());
            assertEquals("",snapshot(id).getAnomalia());
        }
    }

}
