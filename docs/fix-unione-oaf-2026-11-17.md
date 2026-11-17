# Fix unione ordini fornitori — 17 novembre 2026

## Requisiti confermati

Errore riconoscibile HTTP/UI, rollback completo, pulizia selezioni residue,
warning e conferma esplicita per unioni tra fornitori diversi. A differenza
della prima proposta nell'analisi, l'utente ha richiesto di consentire i
fornitori diversi dopo conferma, non di vietarli in assoluto.
Nessuna riparazione automatica dei dati dell'incidente Tubes/Atlas/Mirage.

## Backend

POST /api/oaf/unisciOrdini/anteprima accetta le chiavi degli ordini e restituisce
la lista riletta dal DB, i fornitori effettivi, la destinazione (prima riga),
un flag fornitoriDiversi e un'impronta versione. Ordine mantenuto: anno più
recente, primo selezionato a parità di anno (regola esistente ora esplicita).
Occorrono almeno due chiavi distinte, ordini esistenti e sospesi (F), fornitore
presente. Gruppo/conto e nomi del client non vengono usati come fonte autorevole.

POST /api/oaf/unisciOrdini mantiene il corpo lista per compatibilità e riceve
confermaFornitoriDiversi/versione come query param. Per fornitori diversi
richiede conferma esplicita e versione corrispondente. La versione rileva cambi
ai riferimenti, al fornitore, alla data aggiornamento e ai principali dati delle
righe (progrGenerale/rigo/articolo/quantità/prezzo). Headers e righe sono bloccati
in scrittura durante la validazione finale. La versione non è una firma né
una verifica autonoma del gesto umano: lega la richiesta ai dati dell'anteprima.

Unione in una transazione con rollback su eccezioni: nessun catch che inghiotte
errori nel servizio. Errori di validazione -> HTTP 400 con error=true/messaggio;
errori tecnici -> HTTP 500 con error=true/messaggio generico e stack nel log.
HTTP 201 soltanto dopo il completamento della transazione.
Le righe aggiornano ANNOOAF, SERIEOAF, PROGRESSIVOOAF e RIGO; conservano
PROGRGENERALE e collegamenti alle righe cliente. Gestita destinazione senza righe.
Backup testata e tutte le righe sorgenti nelle tabelle BK già esistenti, prima
del trasferimento. Rimossa la cancellazione automatica delle presunte righe
orfane: ogni riga deve essere trasferita esattamente una volta o l'unione fallisce.
Backup precedenti con la stessa chiave non vengono sovrascritti.

Gli altri endpoint di creazione, approvazione, invio e cancellazione non cambiano.
Permessi e ruoli dell'unione conservati (ADMIN/AMMINISTRATIVO). Nessuna tabella,
colonna, indice o migrazione introdotta.

## Frontend

Selezione azzerata su ricerca/reset ricerca, cambio pagina/stato/filtri di invio,
ricaricamento della lista e termine di un tentativo di unione. In questo flusso
non si conservano selezioni nascoste su pagine o ricerche precedenti.
Pulsante disabilitato con meno di due ordini, loader o operazione in corso.
Anteprima server e dialog con tutti i riferimenti/fornitori e indicazione della
testata mantenuta; se fornitori diversi, warning e checkbox aggiuntiva obbligatoria.
Se la selezione cambia durante l'attesa dell'anteprima o del dialog, la vecchia
operazione non parte. Errori HTTP ed eventuali risposte error=true vengono mostrati;
loader sempre rilasciato, nessun messaggio di successo su errore. Blocco doppio invio.

## Test e verifiche

- 21 test backend superati: 14 OrdineFornitoreServiceTest, 5
  UnioneOrdiniFornitoriTest, 2 UnioneOafResourceTest. Zero skipped/fallimenti/errori.
- Fornitori diversi senza conferma rifiutati, nomi/gruppi client falsificati ignorati,
  conferma valida accettata, versione obsoleta, duplicati, ordini non sospesi.
- Caso serie diverse con ordine terzo avente lo stesso numero: terzo invariato.
- Rollback effettivo: fixture isolate, errore mapper provocato dopo il primo
  trasferimento, verifica in nuova transazione di righe/testate originali e
  assenza del backup parziale. Fixture eliminate in finally. Altri test con rollback.
- 8 test frontend superati: selezioni nascoste, annullamento, conferma/versione,
  errori di anteprima e salvataggio, anteprime obsolete, doppio invio, dialog
  fornitori diversi con checkbox obbligatoria.
- Compilazione backend e build Angular development verificate.

Implementazione su feature/ordine-fornitore-infissi. Commit e push richiesti
dall'utente; non pubblicata in immagini Docker. Nessun dato di produzione modificato.


## Dati per prova manuale creati su richiesta

Creati nel solo DB di sviluppo il 17 novembre: 7 testate sospese F e 14 righe
marcate TEST UNIONE, con fornitori già censiti, nessun legame alle righe cliente
(PID=0) e nessun movimento/giacenza modificato. Codici delle righe dimostrativi.
Script riproducibile `scripts/demo_unione_oaf_sviluppo.sql` con blocco di sicurezza
iniziale: richiede conferma ambiente sviluppo e rifiuta riferimenti già esistenti.

- 2026/B/900001 e 900002: Tubes, due righe ciascuno, unione ordinaria.
- 2026/B/900003: Atlas Concorde, per warning fornitori diversi.
- 2026/B/900004: Mirage, per warning fornitori diversi.
- 2026/B/900010 e 900011: Tubes, ulteriore coppia per unione.
- 2026/B/900012: Atlas, ordine estraneo da lasciare non selezionato.

Correzione su richiesta utente: inizialmente erano state usate serie sintetiche
TST/TSA/TSB, compatibili col varchar(3) ma non rappresentative del flusso reale.
Verificato OrdineFornitoreService: la creazione usa serie B per tutti i fornitori.
Aggiornati in transazione testate, righe e GO_ORDINE_FORNITORE; Atlas TSB/900010
rinumerato B/900012 per evitare collisione con Tubes B/900010.
Verificati sette ordini, due righe ciascuno. Nessuna unione eseguita.
Cercare 900 nella lista Sospesi; il precedente filtro TS non vale più.
Il caso serie diverse rimane una verifica tecnica automatizzata, non la
riproduzione del normale flusso di creazione.

## Correzione selezione tra pagine

Su richiesta utente mantenuta la selezione al cambio pagina/dimensione pagina:
la paginazione è locale sulla medesima datasource. Filtri, stato, ricaricamento,
successo/errore unione continuano a svuotarla. Il dialog elenca anche gli ordini
selezionati nelle altre pagine. Aggiunto test di selezione su due pagine,
ritorno alla prima e invio di entrambe le chiavi all'anteprima.

## Grafica lista e conferma

Barra contestuale con totale selezionato su tutte le pagine, Deseleziona tutto
e Unisci ordini; comandi disabilitati durante il salvataggio.
Righe selezionate con fondo leggero e bordo laterale nel colore applicativo.
Dialog con ordine mantenuto in riquadro dedicato (numero, nome e codice
fornitore), elenco degli ordini di origine e avviso ambra per fornitori diversi.
Conferma aggiuntiva invariata. Stili adattivi su schermi stretti.
Build Angular development e nove test frontend superati.

### Ambra sulle righe di fornitori diversi

Evidenziate in ambra le righe selezionate con gruppo/conto diverso dalla
destinazione (anno più recente, prima selezione a parità), ricalcolate alla
deselezione. Nel dialog stessa evidenziazione e testo Fornitore diverso.
La query lista restituisce ora gruppo e conto nei due casi sospesi/approvati;
aggiunti i corrispondenti costruttori DTO senza alterare quelli preesistenti.
Verificati 10 test frontend e 21 test backend, inclusa findAllByStatus.
Test API su porta dinamica per non interferire con i servizi avviati.
