# Dashboard e navigazione — branch feature/restyling-dashboard

Il frontend apre `/dashboard` dopo il login. Toolbar con nome applicazione e
menu utente, navigazione laterale a sinistra tramite MatSidenav già presente
nel progetto. Chiusura memorizzata su desktop; su mobile il menu è a comparsa
e si chiude dopo la navigazione. Le destinazioni e i controlli permesso
preesistenti sono conservati.

`GET /api/dashboard` richiede autenticazione e restituisce solo contatori
consentiti dai permessi del JWT. Senza `ordini_clienti.view` non interroga gli
ordini. `articoli.codifica_articoli` abilita il contatore da codificare;
`ordini_clienti.pronto_consegna` abilita il pronto consegna. Nessun dato è
inventato quando il servizio non è disponibile: la pagina mostra errore e
consente di riprovare.

Le query riusano la base della lista: Ordine con cliente valido, data conferma
almeno data.inizio, provvisorio diverso da S, stato applicativo non vuoto e
non ARCHIVIATO. Il pronto consegna usa GoOrdine.hasProntoConsegna, come la
lista. La codifica usa EXISTS su ORDCLI2 per almeno un codice *PZ, *MQ o *ML,
come l'azione di codifica esistente. Un ordine vale uno anche se contiene
più righe speciali. Nessuna deduzione dal solo stato DA_ORDINARE.

Il filtro booleano daCodificare viene impostato dai contatori della dashboard.
I click sui contatori ripristinano lo stato filtri prima di impostare il
filtro richiesto: contatore e lista hanno lo stesso perimetro. La selezione
manuale di altri filtri dopo l'apertura può naturalmente cambiarlo.

## Carichi da processare e altre segnalazioni

Il riferimento al "deposito" è stato chiarito: l'utente intende l'alert rosso e
l'evidenziazione del numero ordine. Entrambi usano GoOrdine.hasCarico. Il
contatore Carichi da processare usa esattamente quel flag, senza dedurre la
presenza in un deposito fisico o l'intero ordine completo.

La dashboard mostra fino a cinque ordini in attesa, ordinati per la prima
dataCarico delle righe con flagRiservato=false, flagOrdinato non nullo e
numDoc non nullo, condizioni usate nel salvataggio degli articoli per il
flag hasCarico. dataCarico è popolata da findCarichi dal movimento Magazzino.
Le righe riservate già processate non anticipano la durata. Non si usa la
data dell'ordine come ripiego: gli ordini senza data restano nel contatore e
sono mostrati in fondo alla lista con una dicitura esplicita. Data futura
viene indicata come tale. Il contatore comprende tutti gli ordini segnalati,
indipendentemente dalla durata e dalla pagina della lista ordini.

Aggiunti anche Consegnati senza bolla (go.warnNoBolla=true, ordini aperti) e
Archiviati senza firma (stato ARCHIVIATO, hasFirma=false o null). Il secondo
richiede il permesso ordini_clienti.warn_non_firmato, come la relativa icona.
I filtri carichiDaProcessare, senzaBolla e senzaFirma permettono di aprire la
lista con lo stesso insieme del contatore. Le checkbox aggiuntive non sono
mostrate nel pannello della lista, che conserva i filtri originali; il Reset
esistente rimuove anche i filtri applicati dalla dashboard.

## Verifiche

Test backend: permessi dei contatori, COUNT con più righe da codificare,
coerenza con la lista, pronto consegna, esclusione archiviati e provvisori.
Test frontend: ingresso dashboard, stato filtri, errori di caricamento,
menu desktop/mobile e rendering dei soli contatori autorizzati.
Nessuna migrazione database necessaria per i contatori implementati.

Verifica finale: 10 test backend (7 della risorsa dashboard e 3 query sul
database di sviluppo) e 16 test frontend superati. Build frontend production
completata con i warning già presenti su budget bundle, CommonJS e selettori
CSS. Le suite frontend sono state isolate con un tsconfig temporaneo per
evitare il test preesistente non compilabile has-perm.directive.spec.ts.
Nessuna immagine pubblicata per questo branch.

## Da ordinare e ingresso nel dettaglio da codificare

Aggiunto il contatore Da ordinare: stesso perimetro della lista e stato
esatto DA_ORDINARE; il click apre quel filtro, senza limitarsi agli articoli
speciali. È disponibile con ordini_clienti.view.

Il dettaglio applicava automaticamente Non disponibili per DA_ORDINARE
e Non consegnati per COMPLETO/INCOMPLETO. Questi filtri potevano nascondere
le righe speciali per cui l'ordine era stato conteggiato. Arrivando dalla
lista daCodificare si attiva ora Solo articoli da codificare, con filtri
automatici disattivati e ricerca testuale precedente azzerata. La checkbox
è disattivabile per vedere anche gli altri articoli. Gli ingressi ordinari
nel dettaglio conservano i default precedenti.

Filtro ordini e filtro dettagli usano la stessa lista backend
OrdineDettaglio.CODICI_DA_CODIFICARE. Test SQL verifica che righe speciali
senza flag applicativi siano incluse nel contatore e leggibili nel dettaglio
quando si rimuove il filtro automatico; test frontend coprono gli ingressi
da DA_ORDINARE, COMPLETO e INCOMPLETO. L'ordine specifico segnalato dal cliente
non è stato identificato: resta possibile verificarlo quando viene fornito.

## Consegne da programmare

Il contatore richiede ordini_clienti.view e logistica.update_veicolo, il
permesso usato dall'azione Programma consegna. Comprende ordini aperti con
hasProntoConsegna=true e almeno una riga ORDCLI2 collegata tramite
progrGenerale a GoOrdineDettaglio con flProntoConsegna=true e flBolla=true
sulla stessa riga. Non basta una vecchia bolla su un'altra riga dell'ordine.
L'ordine è escluso se GO_ORD_VEICOLO ha dataConsegna non nulla; una
registrazione priva di data non equivale a una programmazione. Il conteggio
usa EXISTS e non moltiplica gli ordini con più righe pronte e bollate.

Il filtro consegneDaProgrammare è condiviso da contatore e lista, applicato
dalla dashboard e rimovibile tramite il Reset esistente. Dopo un salvataggio riuscito del dialogo Programma consegna
la lista si ricarica, rimuovendo gli ordini ora programmati dal risultato.
Test coprono righe diverse, più righe valide, giro senza data, data presente,
assenza pronto consegna, archivio e coerenza conteggio/lista.

## Da ordinare con note — 11 novembre 2026

Nuovo contatore `daOrdinareConNote`, disponibile con `ordini_clienti.view`.
Conta gli ordini aperti del medesimo perimetro dashboard (cliente valido,
data conferma >= data.inizio, non provvisori, stato non vuoto e non ARCHIVIATO)
con almeno una riga articolo `flagNonDisponibile=true` e nota GO non vuota
sulla stessa riga. È la definizione di Da ordinare già usata nel dettaglio;
non è un filtro sul solo stato testata DA_ORDINARE. Commenti C, acconti AC,
stringhe vuote/spazi e valore sentinella NULL sono esclusi. Le note generali
ordine e le note logistica non vengono lette né condivise. Il COUNT usa EXISTS:
più articoli con nota nello stesso ordine valgono sempre uno.

Il click apre `/ordini-note-da-ordinare`, con API autenticata
`GET /api/dashboard/note-da-ordinare?venditore=...` protetta dallo stesso
permesso. La proiezione restituisce solo riferimenti ordine/cliente, stato,
identità tecnica e descrizione riga, nota e data. Nessuna modifica ai dati.

La vista è divisa per venditore (ORDCLI.SERIE, come il resto dell'applicazione),
poi per ordine cliente. I nomi sono risolti dall'anagrafica utenti esistente
quando il ruolo consente il filtro venditore; in caso di indisponibilità resta
il codice, senza impedire la lettura delle note. Il filtro seleziona il
venditore mantenendo i gruppi per ordine. Ordini e gruppi sono ordinati per
la nota più vecchia presente; dentro ciascun ordine le righe sono cronologiche.
Date assenti restano in fondo al proprio gruppo, senza date di ripiego.
`GO_ORDINE_DETTAGLIO.DATA_NOTE` è la data già registrata dall'applicazione:
addNotes la aggiorna a ogni salvataggio, quindi non esiste una distinta data
di prima creazione da ricostruire.

Ogni riga ha una checkbox, ogni venditore una checkbox Seleziona tutto e un
pulsante WhatsApp con il numero di righe selezionate. Il messaggio comprende
solo quel venditore e le sue righe selezionate, raggruppate per ordine, con
numero ordine, cliente, descrizione/codice articolo e testo della nota.
Si riusa il flusso delle consegne `https://wa.me/?text=` con encodeURIComponent:
si apre WhatsApp e l'utente sceglie destinatario e invio, senza messaggi
automatici né necessità di salvare numeri telefonici. Aggiorna ricarica note
e azzera la selezione; in errore la condivisione non è disponibile.

Non servono migrazioni, nuove tabelle, indici o modifiche ai permessi.
Verifiche: 37 test backend superati (26 OrdineServiceTest,
3 OrdiniConNoteServiceTest, 8 DashboardResourceTest), 24 test frontend
superati tra dashboard, nuova vista e regressioni navigazione/login/dettaglio.
Build production frontend riuscita, con warning già presenti su bundle,
CommonJS e selettori CSS. Condivisioni verificate mediante spy del browser:
nessun messaggio WhatsApp è stato inviato durante i test.

## Da ordinare modificati — evidenziazione rosa

Tile rosa con icona carrello e matita e contatore `daOrdinareModificati`.
Stesso permesso `ordini_clienti.view` e stesso perimetro della lista. La regola
è esattamente quella della cella Cliente con classe isModified: stato
DA_ORDINARE e createDate diversa da updateDate. Il confronto include anche
il caso in cui una sola delle due date sia nulla; con entrambe nulle il
contatore non include l'ordine, come il confronto JavaScript esistente.

Il click imposta `filtroStatus=DA_ORDINARE` e `daOrdinareModificati=true`,
azzerando gli altri filtri. La stessa clausola backend è riutilizzata dal
conteggio e dalla lista. Non vengono aggiunte checkbox al pannello filtri;
Reset rimuove anche questa selezione. Nessun campo database o indice nuovo.

Verifica aggiornata: 39 test backend e 25 frontend superati, build production
riuscita. Il test SQL confronta contatore/lista per date uguali, diverse,
una data nulla, entrambe nulle e stato diverso da DA_ORDINARE.

## Ritorno alla dashboard

Pulsante Dashboard nella lista ordini, destinazione di tutte le tile, accanto
ad Aggiorna lista. Presente anche nel dettaglio articoli: riusa annulla con
destinazione /dashboard e attende lo sblocco quando l'ordine è dell'utente
corrente. Il normale Indietro continua a tornare alla lista ordini. La vista
note conserva il proprio pulsante di ritorno già presente.
Verifica: 28 test frontend superati, compresi attesa sblocco, mancato sblocco
e conservazione del percorso ordinario; build production completata.

## Fix successiva al rilascio 6.2.16: esclusione dei consegnati dalle note

La clausola condivisa da lista e contatore Da ordinare con note include solo
le righe con ORDCLI2.SALDOACCONTO vuoto o nullo. Sono escluse sia A (consegna
parziale) sia S (saldo): anche una consegna parziale indica un acquisto già
avvenuto e non deve più comparire tra gli articoli da ordinare con note.
La colonna è mappata da OrdineDettaglio.saldoAcconto ed è aggiornata da
FatturaService; non vengono ricalcolate quantità o interrogate le bolle per
questa lista. Il flag applicativo di consegna non sostituisce lo stato ERP.
Se non rimangono righe valide l'ordine esce anche dal contatore.
Nessuna modifica allo schema o agli indici. 41 test backend superati, inclusi
valori null/vuoto/spazio/A/S e ordini con righe miste. Inclusa nel rilascio API 6.2.17.
