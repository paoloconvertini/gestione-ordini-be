# Modalità pagamento: analisi iniziale

## Verifica campi di modifica ORDCLI2 — 17 novembre 2026

Consultati .ai/database.md e .ai/data-relationships.md, entity OrdineDettaglio,
i tre export utente Downloads/caso1.csv, caso2.csv, caso3.csv e SQL estratto
Downloads/sql_inizi.txt. La fonte documentata possibili_query_sql.txt non è
attualmente presente in Downloads.

Schema DB sviluppo verificato in sola lettura:
- SYS_UPDATEDATE datetime nullable, SYS_UPDATEUSER varchar nullable;
- DATAMODIFICA datetime nullable (mappato LocalDate nell'entity);
- il campo consegna effettivo è DATACONFCONSEGNA, non
  DATACONFERMACONSEGNA;
- nessun trigger su dbo.ORDCLI2 nel DB sviluppo.

Il mapping già espone sysUpdatedate come Date con TemporalType.TIMESTAMP.
DATAMODIFICA è trattato come data, meno adatto a distinguere più modifiche
nella stessa giornata.

Dati disponibili:
- sviluppo: 1645 righe ORDCLI2, di cui 942 con SYS_UPDATEDATE nullo;
- 16 righe *MODCONS, di cui 8 senza timestamp; nessuna *ACCONTO/*SALDO;
- export caso1: *MODCONS PROGRGENERALE 814194 ha SYS_UPDATEDATE
  2026-08-31 16:57:27;
- export caso2 e caso3: *MODCONS con SYS_UPDATEDATE NULL, pur avendo
  DATAMODIFICA valorizzata.

SQL estratto identificatore 1796211 legge dataConfConsegna da ORDCLI2 per
progrGenerale, ma non dimostra l'aggiornamento del timestamp al cambio data.
Non individuata una query che provi che la modifica data eseguita da Plurima
aggiorni sempre SYS_UPDATEDATE. La presenza del campo non basta a garantirlo.

Conclusione: campo candidato SYS_UPDATEDATE; prima di usarlo come unico
filtro incrementale serve confronto prima/dopo di una modifica da Plurima.
Le cancellazioni non sono rilevabili dal solo timestamp delle righe superstiti.
Alternativa robusta senza lavoro sul caricamento articoli: riconciliazione
periodica in background dei campi delle sole righe pagamento degli ordini
interessati, comprendendo null e rimozioni. Nessuna implementazione o modifica
dati ERP effettuata in questa analisi; nessun indice aggiunto.

## Decisione sul riscontro fatture

Regola finale richiesta dall'utente: il totale realmente fatturato copre le
soglie cumulative delle tranche previste, anche se le percentuali delle fatture
non coincidono con quelle pianificate. Quattro tranche 25/25/25/25:
35% fatturato copre solo la prima, 60% copre le prime due, 100% tutte.
Non genera più anomalie per il solo scostamento di percentuale o per tranche
di uguale importo. Counter e warning ignorano le tranche interamente coperte.
Una tranche solo parzialmente coperta resta da gestire.
Ordine delle soglie: acconti per data di scadenza e poi rigo, saldo per ultimo.
Le soglie sono calcolate sulla percentuale cumulata dell'imponibile totale,
arrotondata ai centesimi, evitando la somma di arrotondamenti indipendenti.
Se percentuali mancanti/non valide o somma diversa da 100 impediscono di
ricostruire le soglie, rimane la segnalazione da verificare.
L'importo mostrato come quota prevista coperta non è l'importo totale del
singolo documento: riferimenti e importi effettivi delle fatture restano visibili.

## Implementazione

Branch backend e frontend: feature/modalita-pagamento.

- RegolePagamento: fino a tre *ACCONTO e una *SALDO; percentuali valide,
  somma 100, saldo zero consentito con acconto 100%; più acconti richiedono
  date distinte e diverse dalla conferma. Data nulla equivale al default.
- Acconto con data predefinita: dovuto dalla conferma; saldo con data
  predefinita: dovuto quando GoOrdine.status è COMPLETO.
- Data modificata: primo giorno del mese indicato, per acconti e saldo.
- Normalizzazione descrizione delle sole righe *ACCONTO: mese italiano,
  anno se diverso dall'anno corrente. Salva descrizione base e ultimo testo
  scritto per preservare le modifiche dell'utente e ripristinare la base
  quando la data torna al default. Rileva mesi già presenti senza duplicarli.
  Non tronca descrizioni: se eccedono la capacità effettiva di FDESCRARTICOLO
  segnala anomalia e conserva il testo.
- Le uniche scritture ERP della nuova funzione sono FDESCRARTICOLO, con
  confronto ottimistico di chiave, PROGRGENERALE, vecchia descrizione, data
  e quantità. Un conflitto annulla la transazione e viene ritentato al ciclo
  seguente. Non modifica date, percentuali, magazzino o fatture.
- Tabella GO_PAGAMENTO_ORDINE: snapshot per PROGRGENERALE, scadenza,
  percentuale, stato, anomalie, imponibile, riferimenti e data ultimo controllo.
- Riconciliazione completa predisposta esclusivamente via cron giornaliero
  notturno (pagamenti.controllo.cron / PAGAMENTI_CONTROLLO_CRON), default off.
  Nessuna scansione ogni cinque minuti né all'avvio dell'applicazione.
- L'acquisizione restituisce le chiavi dei soli ordini effettivamente nuovi;
  vengono controllati puntualmente dopo il commit dell'acquisizione, senza
  ripercorrere gli ordini precedenti. Nessun controllo se non arrivano ordini.
  Filtro sulla chiave applicato a righe, snapshot, totali, anagrafiche e ricerca
  fatture del cliente interessato; la pulizia riguarda solo l'ordine richiesto.
- L'acquisizione mantiene la propria transazione, separata dai controlli.
- Lock transazionale SQL Server sp_getapplock evita riconciliazioni
  contemporanee. Un lock non disponibile viene segnalato, non trattato
  silenziosamente come controllo riuscito.
- Confronto dei valori senza dipendere da SYS_UPDATEDATE: rileva timestamp
  NULL e cancellazioni. Le modifiche degli ordini esistenti vengono rilevate
  dalla riconciliazione notturna, una volta attivata nella finestra concordata.
- Legge solo ordini confermati gestiti e non archiviati. Ripulisce gli snapshot
  di righe eliminate, cambiate di codice, ordini eliminati/non più pertinenti.
- Nessun controllo aggiunto al caricamento lista articoli. La dashboard
  legge lo snapshot, non rilegge fatture/righe ERP per calcolare le scadenze.
- Riconoscimento fatture riusa FatturaService.settaRifOrdCliente: blocchi *ACC,
  eventuali righe per IVA e commento riferimento ordine. Include le fatture
  già create ma non ancora validate, per evitare di proporne duplicati.
  Aggregazione per documento e ordine, verifica anche conto cliente.
  Se il cliente ha *ACC senza riferimento riconoscibile, segnala verifica
  manuale anziché affermare che manca la fattura. Non usa MT_RELFATORD.
- Lo stato EMESSO indica solo che esiste la fattura corrispondente; non
  certifica l'incasso. Lo stato NON_DOVUTO copre saldo con percentuale zero.
- Date e anno corrente del controllo riferiti a Europe/Rome.
- Non vengono rigenerati automaticamente i PDF ordine già creati.

## Dashboard e accesso

Tile Da fatturare oggi visibile solo al ruolo Admin: counter degli ordini
clienti distinti con almeno una tranche dovuta, arretrati compresi.
Le anomalie sono riportate separatamente dal counter degli ordini da fatturare.
Aprendo la tile, filtro DA_FATTURARE predefinito e sole tranche dovute;
nessuna tranche futura, al pronto o già emessa nella vista iniziale.
Gli altri stati e Tutte le scadenze sono consultabili solo scegliendo i filtri.
GET dashboard/pagamenti protetto RolesAllowed(Admin).
Il precedente POST di riconciliazione completa è stato rimosso. Rotta frontend protetta da AuthGuard con role Admin.
Vista per venditore e ordine, filtro nome e codice venditore e stato,
riferimenti fatture, anomalie e ultima verifica.
Aggiorna lista legge soltanto il risultato salvato: nessuna scansione completa
può essere avviata dalla pagina durante il giorno.
Apri ordine e ritorno alle scadenze conservano filtri e normale sblocco ordine.
Non emette fatture automaticamente.

## Migrazione e pregresso

scripts/create_GO_PAGAMENTO_ORDINE.sql: nuova tabella GO, eseguito nel solo
DB sviluppo. Da eseguire prima del backend in produzione. Nessun nuovo indice
o alterazione sulle tabelle ERP. Nessun rilascio/deploy eseguito.

scripts/verifica_pregresso_modalita_pagamento.sql è di sola lettura.
Esclude ordini archiviati/interamente consegnati, quelli già nel nuovo standard
e quelli con fatture acconto collegate per riferimento slash/trattino
(con controllo confini per evitare 123 contro 1234) o PROGRORDCLI.
Segnala comunque se esistono acconti del cliente da verificare manualmente.
Sul dataset sviluppo ha restituito tre candidati, tutti da verificare:
2025/11/735, 2026/01/3, 2026/01/4. Non rappresentano una conferma di assenza
di acconti. Nessuna conversione automatica o UPDATE del pregresso applicato.

## Verifiche

- Test puri: date/default, primo del mese, mesi/anno e idempotenza,
  date mancanti/duplicate, percentuali e saldo zero.
- Integrazione su SQL Server sviluppo con rollback delle fixture: acconto
  immediato, saldo al COMPLETO, cambio data senza timestamp, rimozione righe,
  fattura già creata non validata, archivio e saldo zero.
- Test contatori: Admin vede dovuti e anomalie per ordine; venditore non riceve
  dati pagamento nella dashboard.
- Suite generale API: 313 test superati prima dell'ultima aggiunta saldo zero;
  successivo controllo mirato sulla versione finale: 5 test integrazione
  superati, incluso saldo zero NON_DOVUTO. In totale 314 casi backend distinti
  verificati tra suite generale e controllo finale.
- Frontend: 28 test superati, inclusi filtri, accesso Admin, caricamento/errori
  e ritorno dal dettaglio dopo sblocco.
- Build Angular development verificata.

Limiti espliciti: il riconoscimento storico resta legato ai riferimenti già
gestiti dall'applicativo. Fatture prive di riferimenti riconoscibili restano
da verificare sulle tranche non ancora coperte dal fatturato noto.
Nessuna logica di riconciliazione nella lettura della pagina articoli.

## Finestra notturna: in attesa dei dati di produzione

Configurazione sorgente dev: cespiti 01:30, magazzino 21:30, bolle
06:55/19:55/20:55, mail 20:30 e 20:45; acquisizione/carichi a intervallo.
Gli orari prod sono forniti tramite configurazione esterna CRON_EXPR*:
non sono presenti nei file env locali consultati e non sono stati verificati
sul server. Il docker-compose di esempio non dimostra gli orari effettivi.
Non si può dedurre una finestra senza sovrapposizioni dai soli default dev.
PAGAMENTI_CONTROLLO_CRON resta off finché non sono noti orari e durate degli
altri job, inclusi quelli esterni a questa API. Nessun cron produttivo scelto
arbitrariamente e nessun job esterno modificato.

## Verifiche dopo le ultime correzioni

48 test backend mirati superati: regole cumulative 35/60/100%, warning esclusi
sulle tranche coperte, controllo puntuale isolato, nessun controllo senza
nuovi ordini e regressioni OrdineService.
28 test frontend superati dopo rimozione dei controlli completi manuali.
Build Angular development finale superata.


## Dati manuali e avvio lock

Creati e verificati 12 casi nel DB sviluppo, con calcolo reale dei 33 snapshot.
Dettagli: [Casi manuali](modalita-pagamento-casi-test.md).
Corretta inizializzazione transazione SQL Server prima di sp_getapplock con
una lettura limitata della cache; aggiunto test senza scritture preliminari.
Runner temporaneo di preparazione rimosso dopo commit dei soli dati demo.

## Leggibilità condizioni nel dettaglio — primo approccio poi superato

Il filtro stato nella vista pagamenti seleziona gli ordini che hanno almeno
una tranche corrispondente, poi mostra tutte le condizioni di quegli ordini,
comprese le tranche già coperte. Evita di nascondere il primo acconto quando
il solo saldo richiede un'azione. Nessun nuovo ricalcolo/query ERP.
Nel dettaglio articoli le righe *ACCONTO e *SALDO mostrano QUANTITA come
percentuale, anche per TIPORIGO C/AC, senza unità materiali o quantità da
consegnare e senza renderla modificabile con l'editor quantità articoli.
Gli altri commenti e le quantità materiali mantengono il comportamento.
Esempio demo 2026/11/910108: acconto 30% già coperto e saldo 70% da fatturare.

## Aggiornamento dopo creazione fattura dall'applicazione

Problema riprodotto con 2026/11/910108: fattura 2026/A/910113 A SALDO,
700 euro imponibili e riferimento corretto, ma snapshot ancora del precedente
controllo e saldo DA_FATTURARE. La lettura dashboard non riconcilia (volutamente).

OrdineResource.creaFatturaAcconto ora, dopo ritorno positivo dal servizio
transazionale (quindi commit riuscito), aggiorna esclusivamente gli ordini
contenuti nella richiesta. Deduplica le chiavi: più IVA per uno stesso ordine
non generano più controlli. Nessuna chiamata alla riconciliazione generale,
nessuna modifica alla frequenza del job notturno. Le fatture create in Plurima
restano di competenza della riconciliazione notturna.

Un errore di creazione non avvia il controllo e restituisce HTTP 500/error=true.
Se la fattura è stata salvata ma fallisce il successivo aggiornamento delle
segnalazioni, non si ripete né si annulla la fattura: risposta esplicita sul
salvataggio riuscito e warning aggiornamento, log con ordine ed eccezione.

39 test mirati superati: unit test sequenza salvataggio/aggiornamento,
deduplica ordini e isolamento errori, integrazione reale 300+700 dopo commit
senza @TestTransaction e pulizia fixture in finally; regressioni servizio ordini
e pagamento. Verifica finale con runner temporaneo solo per il 910108,
poi rimosso: entrambe le tranche EMESSO con riferimenti 910108 (300) e
910113 (700). Filtro iniziale azioni non mostra più quest'ordine; TUTTI o
EMESSO consentono di consultarlo.

## Verifica filtri con interfaccia Material — comportamento precedente superato

Verificati i menu venditore/stato su Chrome Headless con i 33 snapshot
effettivi dei 12 demo dopo il saldo dell'utente. Tutti i casi corretti:
AZIONI 5 ordini, DA_FATTURARE 2, ANOMALIA 3, PROGRAMMATO 4,
AL_PRONTO 8, EMESSO 7, TUTTI 12. Venditore 11 + DA_FATTURARE: zero;
venditore 01 + DA_FATTURARE: 910101 e 910110.
Provata conservazione del contesto: selezionando DA_FATTURARE, l'ordine
910101 mostra sia acconto 30% sia saldo 70% al pronto.
Il filtro è per ordini con almeno una tranche corrispondente, non per sole
righe. Perciò EMESSO mostra anche le altre tranche dei medesimi ordini.
Non riscontrato difetto nei filtri; nessuna modifica alla logica applicativa.
Conservato test UI Material riproducibile con fixture compatte indipendenti
dal DB, dopo aver verificato il comportamento sugli effettivi dati estratti.

## Decisione finale UI: subito cosa fatturare oggi

Richiesta esplicita utente: aprire la tile e vedere esclusivamente quello
da fatturare oggi, senza scadenze future o tranche già coperte.
Sostituisce il precedente filtro iniziale AZIONI e la visualizzazione di
tutte le tranche degli ordini compatibili.

- Default DA_FATTURARE, pagina Da fatturare oggi con data corrente italiana.
- Filtro applicato alle singole tranche; raggruppamento delle sole righe
  corrispondenti per venditore e ordine.
- PROGRAMMATO, AL_PRONTO, EMESSO, ANOMALIA esclusi dalla vista iniziale.
- TUTTI mostra l'intero piano solo su scelta esplicita; gli altri filtri
  mostrano esclusivamente le tranche nello stato selezionato.
- Rimossa opzione combinata AZIONI; vecchio parametro non riconosciuto
  ricade sul default DA_FATTURARE.
- Tile conta ordini distinti (più rate dovute dello stesso ordine contano 1).
- Con dati demo attuali, 17 novembre 2026: 2026/01/910101, acconto 30%
  300 euro; 2026/01/910110, acconto 20% 200 euro arretrato.
  Nessun saldo al pronto né acconto futuro all'apertura.
- Per contesto completo: Apri ordine o filtro Tutte le scadenze.
- Nessun nuovo ricalcolo/scansione ERP: lettura cache come prima.
- 27 test frontend superati, inclusa prova dei menu Material e assert sulle
  sole due tranche iniziali e sulle 33 righe solo dopo scelta TUTTI;
  11 test backend superati, con due tranche dovute nello stesso ordine che
  incrementano il counter di uno solo; build Angular development superata.

## Punto di ripresa — fine lavoro 17 novembre 2026

Commit e push richiesti dall'utente su feature/modalita-pagamento in entrambi
i repository. Nessun merge sui principali, rilascio Docker o deploy richiesto.

Stato da mantenere:
- apertura tile solo DA_FATTURARE, counter per ordini distinti, altre tranche
  esclusivamente tramite scelta dei filtri;
- riscontro cumulativo del fatturato (35/60/100 su tranche 25%);
- aggiornamenti puntuali alla nuova acquisizione e dopo creazione fatture
  nell'app, senza scansioni complete durante il giorno;
- job completo notturno OFF: manca verifica orari/durate effettivi prod;
- tabella GO già creata nel solo sviluppo, script da applicare prima del
  futuro rilascio in produzione;
- demo disponibili e stato aggiornato dopo saldo utente 910108, documento
  2026/A/910113 da 700 euro; adesso i demo dovuti sono 910101 e 910110.

Punti aperti confermati:
1. Consultare configurazione/durate job produzione prima di scegliere una
   finestra giornaliera notturna senza sovrapposizioni.
2. Verificare un ordine realmente creato in Plurima nel nuovo standard:
   nei tre CSV e nel DB sviluppo non dimostrativo manca ancora il campione.
3. Completare la prova manuale di fattura multiordine/multiIVA; verificato
   il riconoscimento sul documento reale 2026/A/1, non ancora riprodotto
   in un nuovo demo manuale.
4. Revisione pregresso: query sola lettura pronta, nessuna conversione
   automatica delle descrizioni libere applicata.

Ultime verifiche valide: 27 test frontend e 11 backend sul focus odierno,
build Angular development; 39 test su aggiornamento post-fattura e servizi
correlati, più verifica finale di riallineamento saldo e integrazione
post-commit. La suite generale di 313 test fu eseguita prima delle ultime
correzioni: non dichiararla come suite generale rieseguita sul commit finale.
