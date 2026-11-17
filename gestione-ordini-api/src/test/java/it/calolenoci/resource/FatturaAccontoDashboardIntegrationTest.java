package it.calolenoci.resource;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.FatturaAccontoDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.entity.*;
import it.calolenoci.service.ModalitaPagamentoService;
import it.calolenoci.service.OrdineService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FatturaAccontoDashboardIntegrationTest {
    @Inject OrdineService ordini;
    @Inject ModalitaPagamentoService pagamenti;

    @Test void trentaPiuSettantaAggiornaDashboardDopoIlCommitSenzaJobCompleto() {
        int numero=700_000_000+Math.floorMod(UUID.randomUUID().hashCode(),100_000_000);
        String conto="Y"+String.format("%05d",numero%100000);
        int anno=2038;
        boolean creato=false;
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                assertEquals(0,Fatture.count("contoCliente",conto));
                assertEquals(0,Ordine.count("anno=?1 AND serie='11' AND progressivo=?2",anno,numero));
                Ordine o=new Ordine(); o.setAnno(anno); o.setSerie("11"); o.setProgressivo(numero);
                o.setGruppoCliente(1231); o.setGruppoFattura(1231); o.setContoCliente(conto); o.setContoFattura(conto);
                o.setProvvisorio(" "); o.setDataOrdine(LocalDate.now()); o.setDataConferma(LocalDate.now()); o.persist();
                GoOrdine go=new GoOrdine(); go.setAnno(anno); go.setSerie("11"); go.setProgressivo(numero);
                go.setStatus("COMPLETO"); go.persist();
                for(int r=1;r<=3;r++) {
                    OrdineDettaglio d=new OrdineDettaglio(); d.setAnno(anno); d.setSerie("11");
                    d.setProgressivo(numero); d.setRigo(r); d.setProgrGenerale(-numero-r);
                    d.setTipoRigo(r==1 ? " " : "C"); d.setFArticolo(r==1 ? "TEST" : r==2 ? "*ACCONTO" : "*SALDO");
                    d.setFDescrArticolo(r==1 ? "Test materiale" : r==2 ? "Acconto" : "Saldo");
                    d.setQuantita(r==1 ? 1D : r==2 ? 30D : 70D); d.setPrezzo(r==1 ? 1000D : 0D);
                    d.setFCodiceIva("10"); d.setDataConfConsegna(LocalDate.now()); d.persist();
                }
            });
            creato=true;
            OrdineResource r=new OrdineResource();
            r.ordineService=ordini; r.modalitaPagamentoService=pagamenti; r.user="test-pagamenti";
            var primo=r.creaFatturaAcconto(List.of(dto(anno,numero,conto,330,false)));
            assertEquals(200,primo.getStatus());
            assertFalse(((ResponseDto)primo.getEntity()).getError());
            QuarkusTransaction.requiringNew().run(() -> {
                assertEquals("EMESSO",riga(anno,numero,2).getStato());
                assertEquals("DA_FATTURARE",riga(anno,numero,3).getStato());
            });
            var saldo=r.creaFatturaAcconto(List.of(dto(anno,numero,conto,770,true)));
            assertEquals(200,saldo.getStatus());
            assertFalse(((ResponseDto)saldo.getEntity()).getError());
            QuarkusTransaction.requiringNew().run(() -> {
                assertEquals("EMESSO",riga(anno,numero,2).getStato());
                assertEquals("EMESSO",riga(anno,numero,3).getStato());
                assertEquals(0,GoPagamentoOrdine.count("anno=?1 AND serie='11' AND progressivo=?2 AND stato='DA_FATTURARE'",anno,numero));
                assertEquals(2,Fatture.count("contoCliente",conto));
            });
        } finally {
            if (creato) QuarkusTransaction.requiringNew().run(() -> {
                List<Fatture> documenti=Fatture.find("contoCliente",conto).list();
                for(Fatture f:documenti) {
                    FattureDettaglio.delete("anno=?1 AND serie=?2 AND progressivo=?3",f.getAnno(),f.getSerie(),f.getProgressivo());
                    f.delete();
                }
                GoPagamentoOrdine.delete("anno=?1 AND serie='11' AND progressivo=?2",anno,numero);
                OrdineDettaglio.delete("anno=?1 AND serie='11' AND progressivo=?2",anno,numero);
                GoOrdine.delete("anno=?1 AND serie='11' AND progressivo=?2",anno,numero);
                Ordine.delete("anno=?1 AND serie='11' AND progressivo=?2",anno,numero);
            });
        }
    }
    private GoPagamentoOrdine riga(int anno,int numero,int rigo) {
        return GoPagamentoOrdine.find("anno=?1 AND serie='11' AND progressivo=?2 AND rigo=?3",anno,numero,rigo).firstResult();
    }
    private FatturaAccontoDto dto(int anno,int numero,String conto,double ivato,boolean saldo) {
        var d=new FatturaAccontoDto(); d.setAnno(anno); d.setSerie("11"); d.setProgressivo(numero);
        d.setContoCliente(conto); d.setIva("10"); d.setNuovoAccontoIvato(ivato); d.setASaldo(saldo); return d;
    }
}
