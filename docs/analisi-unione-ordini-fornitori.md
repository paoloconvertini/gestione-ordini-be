# Analisi unione ordini fornitori — 16 novembre 2026

Aggiornamento: fix implementata e testata il 17 novembre, descritta in
[fix-unione-oaf-2026-11-17.md](fix-unione-oaf-2026-11-17.md).
La proposta iniziale di vietare fornitori diversi è stata sostituita, su
richiesta utente, con warning e conferma esplicita. La diagnosi sotto descrive
il codice precedente alla fix.

Richiesta: verificare se il flusso attuale impedisce unioni tra fornitori diversi.
Consultata prima la nota in .ai/testing.md sull'unione; poi seguito il flusso
UI -> OrdineFornitoreService FE -> OrdineFornitoreResource -> servizio BE.
Sorgenti del flusso identici ai principali prod/master nel checkout verificato.
Nessuna operazione di unione eseguita, nessuna query o modifica in produzione.

## Risultato

Manca un controllo che tutti gli ordini appartengano allo stesso fornitore.
In UI il checkbox esegue selection.toggle(row), senza confrontare gruppo/conto.
Unisci ordini passa l'intera selezione all'API; la presenza del pulsante dipende
dal permesso e filtroStatus F, non dall'omogeneità del fornitore.
La resource controlla solo lista nulla/vuota; il servizio non valida gruppo/conto
né rilegge tutte le testate per verificarne l'omogeneità prima di scrivere.

Il servizio ordina per anno decrescente e mantiene il primo ordine: a parità
anno non c'è un ordinamento aggiuntivo per data o progressivo. Trasferisce
anno/progressivo/rigo delle righe degli altri ordini e poi archivia le testate
nelle BK prima di eliminarle da ORDFOR. Con serie uguale e progressivi distinti,
righe provenienti da fornitori diversi possono quindi finire sotto la testata
del fornitore dell'ordine mantenuto. Il flusso è privo della protezione richiesta.

## Altri rischi osservati nello stesso metodo

- Non viene aggiornato SERIEOAF durante lo spostamento: con serie diverse
  la chiave destinazione delle righe non coincide necessariamente con quella
  della testata mantenuta. Rischio di righe orfane o riferimenti incoerenti.
- Le eccezioni sono intercettate e soltanto loggate nel servizio: la resource
  può rispondere Ordini unificati! anche a fronte di errori; la gestione non
  garantisce esplicitamente rollback dell'intera unione su ogni fallimento.
- Numero minimo di due ordini distinti non validato; precedente nota su caso
  singolo e controllo duplicati confermati dalla lettura del codice.

## Correzione proposta, non implementata

Prima di ogni scrittura: deduplicare/validare chiavi, richiedere almeno due
ordini distinti, rileggere le testate dal DB e verificare che GRUPPOFOAF/CONTOFOAF
siano uguali. Non fidarsi del fornitore ricevuto dal frontend. Aggiungere anche
feedback preventivo UI. Gestire esplicitamente serie e fallimenti atomici,
con test transazionali che provino il rifiuto dei fornitori diversi senza
spostamenti o cancellazioni. Non procedere a riparazioni di dati di produzione
senza identificare gli ordini concretamente coinvolti.

## Riferimenti

- FE oaf-list.component.html: selezione righe e pulsante Unisci ordini.
- FE oaf-list.component.ts: unisciOrdini(), circa riga 122.
- BE resource/OrdineFornitoreResource.java: unisciOrdini(), circa riga 118.
- BE service/OrdineFornitoreService.java: unisciOrdini(), circa riga 164.
- BE test/service/OrdineFornitoreServiceTest.java: test sui duplicati; non copre
  il rifiuto di ordini con fornitori diversi.


## Segnalazione concreta: Tubes / Atlas / Mirage

L'utente segnala in produzione un'unione intenzionale di due ordini Tubes,
al termine della quale risultano coinvolti anche ordini Atlas e Mirage.
La causa dell'episodio non è ancora dimostrata: mancano chiavi ordini e orario.

Due percorsi da discriminare:
1. Selezione nascosta: selection è indipendente dal datasource; applyFilter,
   clearSearch tramite applyFilter, pageEvent e retrieveFornitoreList non
   cancellano la selezione. Dopo unione la selezione non è svuotata. Ordini
   selezionati in precedenza possono quindi non essere visibili con ricerca
   Tubes attiva, ma restare nel payload this.selection.selected. Il backend
   non li rifiuta per fornitore diverso. Anche il cambio stato/ricaricamento
   mantiene oggetti selezionati appartenenti al precedente datasource.
2. Destinazione delle righe: mantenendo la SERIEOAF sorgente ma cambiando anno
   e progressivo, in caso di serie diverse si può indirizzare una riga sotto
   una chiave già appartenente a un ordine terzo. Non si può quindi assumere
   che ogni fornitore coinvolto fosse necessariamente selezionato dall'utente.

Confrontare payload/log dell'operazione, chiavi originarie e superstiti,
ORDFOR/ORDFOR2 e testate di backup GO_ORDFOR. GO_ORDFOR2 contiene in questo
metodo soltanto righe rimaste orfane prima della cancellazione: non promettere
che costituisca una copia completa delle righe trasferite. Non eseguire una
seconda unione per tentare il ripristino. Nessuna correzione o riparazione
applicata durante questa analisi.
