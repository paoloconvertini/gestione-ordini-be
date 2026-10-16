# Articoli Plurima: classificazioni, marchi e aggiornamento prezzi

Ultimo aggiornamento: 2026-10-15.

## Scopo e fonti

Conservare le verifiche effettuate durante lo sviluppo della pagina Prezzi
articoli, evitando di ricostruire il modello dati in futuro.

Fonti: codice backend/frontend del progetto, query eseguite sul database di
sviluppo, risultati di query di produzione eseguite e forniti dall'utente,
schermate Plurima e documento del cliente. I conteggi sono fotografie del
momento, non vincoli del modello. Il database di produzione non è stato
interrogato direttamente dall'agente.

Sono stati consultati anche `/Users/pconvertini/Downloads/Bas02u_unpacked.exe`
e le estrazioni già presenti in Downloads (`sql_inizi.txt`,
`strings_complete.txt`, `strings_ascii.txt`, `strings_unicode.txt`,
`sqlcom (1).csv`). Le stringhe del programma possono riferirsi a moduli o
versioni differenti: verificarne sempre l'applicabilità allo schema installato.

## Distinguere codice, marchio, categoria e fornitore d'acquisto

- `dbo.ARTICOLI_TAB` è la tabella anagrafica; `dbo.Articoli` in produzione è
  una vista. Gli aggiornamenti della funzionalità usano `ARTICOLI_TAB`.
- `ARTICOLO` è il codice articolo. Le prime tre cifre spesso richiamano un
  marchio, ma il prefisso è una convenzione e non una relazione obbligatoria.
- `CLASSEA1` risolve il codice in `TCA1.CODICE`, con descrizione in
  `TCA1.DESCRIZIONE`. Nei dati osservati questa classificazione identifica
  marchi/ditte, ad esempio 710 = RUBINETTERIA PAFFONI SPA. Non certifica il
  fornitore d'acquisto e non è equivalente al prefisso.
- `CLASSEA3` risolve il codice in `TCA3` e contiene categorie/settori
  merceologici, ad esempio 020 = Materiale Termo-Idrico, 070 = Rubinetteria.
- Esistono `TCA1` ... `TCA10`, con `CODICE` e `DESCRIZIONE`. Per A2, A4 e A5
  non è ancora stato confermato il significato funzionale completo: non
  chiamarle automaticamente categoria, famiglia o marchio.
- `FORNALTERNATIVI` collega gli articoli ai fornitori d'acquisto. Un articolo
  può essere acquistato da più fornitori, come precisato dal cliente: tale
  relazione non è il filtro desiderato per trovare un marchio.

Join fornitore: `FORNALTERNATIVI.ARTICOLO = ARTICOLI_TAB.ARTICOLO`;
`GRUPPOF = PIANOCONTI.GRUPPOCONTO`, `CONTOF = PIANOCONTI.SOTTOCONTO`.
Nel dataset locale i conti possono avere zeri iniziali solo su un lato
(esempio 7008 contro 007008). Per diagnosi si è usato un confronto numerico
con TRY_CONVERT oltre a quello testuale; non normalizzare i codici
alfanumerici indiscriminatamente. Una join ai fornitori può moltiplicare le
righe: contare gli articoli distinti. Il significato dei valori di FDEFAULT
non è stato confermato; non usarli come regola senza ulteriore verifica.

## Esigenza confermata del cliente

Recuperare gli articoli Paffoni indipendentemente dal prefisso 710 e dal
fornitore da cui vengono ordinati. Il filtro concordato è la classificazione
A1 = 710. Comprende tutti gli articoli **classificati** Paffoni; non garantisce
il recupero di articoli con classificazione errata o mancante.

Esempi locali: `PAF000001` ha classe 710, `LUX000001` classe 414,
`FAR000001` classe 255. Alcuni codici numerici hanno prefisso diverso dalla
classe. Non unire automaticamente filtro per prefisso e filtro per classe.

Il filtro A1 è implementato: la pagina propone "Fornitore (classe A1)" con
codice e descrizione da TCA1, usando il servizio ArticoloClasseFornitoreService
esistente. Non usa FORNALTERNATIVI né il prefisso del codice.
GET /api/prezzi-articoli/classi-fornitore carica la lista con lo stesso permesso
della pagina. GET di ricerca e PUT di aggiornamento usano il campo `classeA1`,
che sostituisce `prefisso`. Il confronto SQL è sull'intero codice A1 (trim).
Anche il salvataggio rilegge la classe: se cambia dopo l'anteprima, l'articolo
non è più ammesso e l'operazione viene rifiutata senza aggiornamenti parziali.

## Risultati di produzione: A1 e prefisso

Conteggi forniti dall'utente il 2026-10-07. Fuori prefisso significa A1 uguale
al codice indicato ma prefisso diverso. L'ultima colonna è il caso inverso.
La discordanza non implica necessariamente un errore.

| A1 | Descrizione TCA1 | Totale A1 | Fuori prefisso | Prefisso uguale, A1 diversa/vuota |
|---|---|---:|---:|---:|
| 205 | GEORG FISCHER SPA | 223 | 0 | 1 |
| 230 | VALVOSANITARIA BUGATTI SPA | 219 | 0 | 0 |
| 252 | EUROGAS SNC | 45 | 0 | 0 |
| 254 | MAGIT SRL | 180 | 17 | 13 |
| 255 | FAR RUBINETTERIE SPA | 5459 | 5158 | 4 |
| 257 | GENERAL FITTINGS SRL | 818 | 9 | 8 |
| 258 | EMIFLEX SPA | 110 | 0 | 3 |
| 310 | WAVIN SPA | 2855 | 2411 | 6 |
| 414 | LUXOR SPA | 4975 | 4878 | 2 |
| 445 | ATLAS FILTRI SRL | 68 | 0 | 4 |
| 710 | RUBINETTERIA PAFFONI SPA | 7453 | 6795 | 37 |

Correzione rispetto alla foto iniziale: Paffoni è 710, mentre 740 descrive
RUBINETTERIE DITTE VARIE. La lista cliente indicava ricarico 48% per Paffoni
e 45% per gli altri marchi elencati; le percentuali operative restano
inserite dall'utente e non devono essere codificate come default obbligatori.

## Copertura delle classificazioni in produzione

Totale anagrafica: 640549 articoli, senza filtri su blocco, movimentazione o
costo. NULL, stringa vuota e soli spazi sono stati conteggiati come mancanti.

| Campo | Senza classificazione | Codici distinti non vuoti |
|---|---:|---:|
| CLASSEA1 | 176441 | 501 |
| CLASSEA2 | 572156 | 474 |
| CLASSEA3 | 535441 | 118 |
| CLASSEA4 | 639232 | 18 |
| CLASSEA5 | 640120 | 2 |
| CLASSEA6 | 640549 | 0 |
| CLASSEA7 | 640549 | 0 |
| CLASSEA8 | 640549 | 0 |
| CLASSEA9 | 640549 | 0 |
| CLASSEA10 | 640549 | 0 |

A3 contiene 070 Rubinetteria (26729), 020 Materiale Termo-Idrico (15419),
085 Ceramica (13938), 095 Arredo Bagno (5715), 060 Vasche e Docce (5124),
080 Porcellana (4665), 075 Arredo Cucine (4333), 010 Materiale Costruzioni
(3817), 030 Materiale per Scarichi (835), 120 INFISSI/PORTE/AVVOLGIBILI/PERSIANE
(540), 035 Utensileria varia (204).

Sono presenti anche codici senza lookup: 60 (4974), 95 (4813), 70 (2626),
10 (742), nonché pas, chr, ino, bia e altri. Non equiparare automaticamente
70 a 070, 60 a 060 ecc.; non è stata confermata la perdita dello zero.
L'ipotesi che alcuni valori siano finiture è ancora da verificare.
Alcune descrizioni nel lookup sono semplicemente ".".

Distribuzione A3 dei 7453 articoli con A1 = 710:

| A3 | Categoria | Articoli |
|---|---|---:|
| NULL/vuota | Senza categoria | 5975 |
| 070 | Rubinetteria | 1463 |
| 020 | Materiale Termo-Idrico | 9 |
| 075 | Arredo Cucine | 3 |
| 030 | Materiale per Scarichi | 3 |

Quindi il filtro A3 = 070 escluderebbe gran parte degli articoli classificati
Paffoni. A3 non sostituisce A1 per l'esigenza concordata.

## Costo ultimo, IVA e precisione

La funzionalità legge il costo ultimo dal movimento MAGAZZINO più recente
che aggiorna l'ultimo costo: join TMMC su MCAUSALE, AGGIORNAULTIMO = 'S',
MQUANTITA > 0, VALORE > 0, VALOREUNITARIO > 0, data <= GETDATE().
Ordinamento DATAOPMAGAZZINO DESC, PROGRGENERALE DESC. Costo = VALORE / MQUANTITA.
Se l'unità del movimento differisce da quella anagrafica, l'articolo è escluso.
Non sostituire questo valore con il PREZZO nominale del movimento: sconti e
altre componenti possono produrre differenze.

IVA separata: MAGAZZINO.IVAMAG -> TGCI.CODICEIVA, aliquota TGCI.ALIQUOTA.
Produzione: 56725 movimenti confrontabili, 32910 coincidono col netto scontato,
268 con l'ivato. Esempio: 749 meno 45% meno 5% = 391.3525, costo 391.35,
aliquota 22. Evidenza a favore del netto, non prova universale per ogni riga.
Per decisione dell'utente, la funzionalità non aggiunge né scorpora IVA.
Diagnostica salvata in `scripts/verifica_iva_costo_ultimo.sql`.

Nel DB locale PREZZOBASE è float (precisione binaria 53), senza scala decimale
fissa. Su 508285 prezzi positivi: 197798 oltre due decimali, 9867 oltre tre
(tolleranza confronto 0.0000001). Esempi 14.755, 1.558, 1.031008, 2.041600.
Questo non significa che il campo abbia una scala obbligatoria di tre cifre.

Comportamento applicativo concordato e implementato:

- Nuovo prezzo = costo ultimo * (1 + percentuale / 100), calcolo BigDecimal
  lato server e arrotondamento HALF_UP a **3 decimali** anche in anteprima.
- FE visualizza prezzi/costi con **2 decimali**, senza unità PZ/MQ.
- Ricarico attuale = (prezzo base / costo ultimo - 1) * 100; il backend lo
  restituisce a due decimali, il FE lo mostra arrotondato all'intero.
- La percentuale effettiva può differire da quella impostata per arrotondamento
  del prezzo; quella impostata non è memorizzata nell'anagrafica Plurima; dal 2026-10-08
  la soglia di riferimento viene salvata separatamente per classe A1 in GO_RICARICO_FORNITORE.
- In lettura i valori monetari sono normalizzati a 6 decimali per confrontare
  gli snapshot; il costo non è ridotto a due decimali prima del calcolo.
- Una versione iniziale salvava a due decimali: correggere eventuali prezzi
  applicati allora richiede ripetere l'operazione con le stesse percentuali e
  gruppi. La correzione del codice non ha riscritto automaticamente quei dati.

## Funzionalità Prezzi articoli implementata

Route FE `/prezzi-articoli`, API `/api/prezzi-articoli`.
Service BE `PrezziArticoliService`, resource `PrezziArticoliResource`;
componenti FE in `src/app/components/prezzi-articoli/`.

Anteprima con tutti gli articoli aggiornabili selezionati; selezione valida
anche tra pagine della tabella. Mancanza di costo, unità diversa e blocco
escludono l'aggiornamento (blocchi S/N nel codice attuale), salvo inclusione
esplicita dei bloccati. Modifiche ai filtri richiedono una nuova anteprima.

Se alcuni aggiornabili sono deselezionati, Applica apre una popup che li elenca:
No aggiorna solo i selezionati; sì permette un ricarico diverso sui deselezionati;
Annulla tutto non aggiorna nulla. Il server valida entrambi i gruppi prima delle
scritture e aggiorna prima il secondo, poi il primo nella stessa transazione.
Conflitti su prezzo/costo/movimento o articoli duplicati impediscono il salvataggio.
Il nuovo prezzo è ricalcolato dal server, non fidandosi di quello del client.
Aggiornati anche SYS_UPDATEDATE e SYS_UPDATEUSER.

Permesso unico `prezzi_articoli.view` usato per menu, route, pagina e API,
compresa l'applicazione dei prezzi, secondo la struttura concordata. Nessun
controllo frontend diretto sul ruolo. Script MySQL idempotente per produzione:
`scripts/permesso_prezzi_articoli.sql`; associa inizialmente Admin e
Amministrativo al permesso. Applicato solo in sviluppo; richiede nuovo login
per aggiornare i permessi nel token.

Test di regressione: `PrezziArticoliServiceTest`, `PrezziArticoliResourceTest`
e `prezzi-articoli.component.spec.ts`. Nessuna modifica a prezzi o classificazioni
in produzione è stata eseguita dall'agente.

## Query di riferimento (sola lettura)

### Articoli classificati Paffoni, indipendentemente da prefisso e acquisti

```sql
SELECT a.ARTICOLO, a.DESCRARTICOLO, a.CLASSEA1, a.CLASSEA3, a.PREZZOBASE
FROM dbo.ARTICOLI_TAB a
WHERE LTRIM(RTRIM(a.CLASSEA1)) = '710'
ORDER BY a.ARTICOLO;
```

### Copertura di ogni classificazione

```sql
SELECT v.Campo, COUNT_BIG(*) AS TotaleArticoli,
       SUM(CONVERT(bigint, CASE
           WHEN NULLIF(LTRIM(RTRIM(v.Codice)), '') IS NULL THEN 1 ELSE 0
       END)) AS SenzaClassificazione,
       COUNT(DISTINCT NULLIF(LTRIM(RTRIM(v.Codice)), '')) AS CodiciDistinti
FROM dbo.ARTICOLI_TAB a
CROSS APPLY (VALUES
    ('CLASSEA1', a.CLASSEA1), ('CLASSEA2', a.CLASSEA2),
    ('CLASSEA3', a.CLASSEA3), ('CLASSEA4', a.CLASSEA4),
    ('CLASSEA5', a.CLASSEA5), ('CLASSEA6', a.CLASSEA6),
    ('CLASSEA7', a.CLASSEA7), ('CLASSEA8', a.CLASSEA8),
    ('CLASSEA9', a.CLASSEA9), ('CLASSEA10', a.CLASSEA10)
) v(Campo, Codice)
GROUP BY v.Campo ORDER BY v.Campo;
```

### Categoria degli articoli classificati Paffoni

```sql
SELECT NULLIF(LTRIM(RTRIM(a.CLASSEA3)), '') AS CodiceCategoria,
       t.DESCRIZIONE, COUNT_BIG(*) AS NumeroArticoli
FROM dbo.ARTICOLI_TAB a
LEFT JOIN dbo.TCA3 t ON LTRIM(RTRIM(t.CODICE)) = LTRIM(RTRIM(a.CLASSEA3))
WHERE LTRIM(RTRIM(a.CLASSEA1)) = '710'
GROUP BY NULLIF(LTRIM(RTRIM(a.CLASSEA3)), ''), t.DESCRIZIONE
ORDER BY NumeroArticoli DESC;
```

## Verifiche ancora aperte

- Completezza/correttezza di A1: articoli del marchio con classe vuota o errata
  non sono recuperabili con certezza attraverso A1 da sola.
- Significato completo di A2/A4/A5 e dei codici A3 senza lookup.
- Eventuale bonifica delle classificazioni: nessuna bonifica autorizzata o fatta.
- IVA/costi accessori delle righe che non coincidono con il netto scontato.
- Il filtro categoria A3 è distinto da quello A1 e non è implementato.

## Rettifica costi importati dalla precedente azienda (2026-10-08)

Regola richiesta dal cliente: se la data del movimento che determina il costo
ultimo è **esattamente 31/12/2021**, il costo di riferimento per i prezzi va
maggiorato del **40%**. Il confronto usa la data civile del LocalDateTime del
movimento, ignorando l'orario. Non vale per date precedenti o successive e
non si applica se un acquisto valido più recente ha sostituito quel movimento.

Il DTO mantiene `costoUltimo` originale per i controlli dello snapshot e
aggiunge `costoRiferimento` e `costoRettificato`. Il primo è costoUltimo * 1.40
per quella data, altrimenti costoUltimo; costo assente resta null.
La tabella principale e la popup mostrano il costoRiferimento a due decimali
con indicazione "Costo rettificato +40%" quando applicabile. Anche il ricarico
attuale è calcolato sul costo mostrato, per coerenza.

Anteprima e applicazione dei due gruppi calcolano il nuovo prezzo a partire
dal costoRiferimento, con arrotondamento finale a tre decimali. Il costo
rettificato non è arrotondato a due decimali prima del calcolo. Il server
ricalcola sempre la rettifica dai dati riletti, ignorando i valori derivati
mandati dal frontend. Un cambiamento della data del movimento rispetto
all'anteprima produce conflitto, anche a parità di costo e identificativo.

**Non si scrive mai il costo rettificato in MAGAZZINO né in altri campi di
costo dell'anagrafica.** L'unica scrittura di prezzo rimane PREZZOBASE più
metadati di aggiornamento. Non sono necessarie migrazioni di schema.

Esempio: costo originale 100, data 31/12/2021 -> costo mostrato 140;
ricarico 60% -> nuovo prezzo 224.000; ricarico 70% -> 238.000.
Rileggendo dopo il salvataggio il costo originale resta 100 e il costo di
riferimento resta 140: la maggiorazione non si accumula tra aggiornamenti.

Test aggiunti: data con orario, date adiacenti, movimento successivo,
assenza di costo, due gruppi di percentuali, dati derivati client alterati,
invarianza di VALORE/MQUANTITA/VALOREUNITARIO e conflitto su cambio data.

### Ricerca della classificazione fornitore (2026-10-08)

Il filtro A1 nella pagina prezzi usa un autocomplete Angular Material, con
ricerca locale per parte del codice o del nome senza distinzione tra maiuscole
e minuscole. La scelta mostra codice e descrizione; alle API viene inviato
solo il codice A1. Un testo che non corrisponde a un codice della lista non
abilita la ricerca o il salvataggio.

## Soglia di ricarico unica per fornitore A1 (2026-10-08)

Decisione esplicita dell'utente: una **percentuale unica per fornitore A1**,
non per articolo e non per fornitore d'acquisto. La soglia è un riferimento
modificabile e salvabile **indipendentemente dai prezzi** con il pulsante
"Salva soglia". Il salvataggio non modifica PREZZOBASE né MAGAZZINO.
Applicare i ricarichi ai prezzi, incluso il secondo gruppo della popup,
**non modifica la soglia**. Consultazione e anteprima non salvano la soglia.
Non dedurre la soglia dalla percentuale principale applicata ai prezzi.

Persistenza: `dbo.GO_RICARICO_FORNITORE`, chiave primaria CLASSEA1 varchar(3),
PERCENTUALE decimal(9,4), CREATED_AT/UPDATED_AT datetime2,
CREATED_BY/UPDATED_BY varchar(255). Nessuna riga prima del primo salvataggio
significa soglia null; 0% è invece un valore valido. Una riga per A1, aggiornata
senza duplicarla. Non è uno storico degli aggiornamenti.

Script SQL Server idempotente `scripts/create_GO_RICARICO_FORNITORE.sql`,
applicato in sviluppo. **In produzione eseguirlo prima di avviare il backend
aggiornato**, nel database aziendale della API, non in MySQL auth.
Nessuna soglia iniziale viene dedotta da vecchi prezzi o ricarichi già applicati.
I campi QUANTITAUSER01...05 e CAMPOUSER1...5 esistono in ARTICOLI_TAB ma non
sono stati riutilizzati: non ne è garantita l'assenza di altri utilizzi Plurima.

La GET ricerca prezzi restituisce ora `{articoli, ricaricoImpostato}` invece
dell'array semplice, anche senza articoli. La PUT prezzi include ricaricoImpostato come
snapshot della soglia vista in anteprima, senza scriverla.
La PUT `/api/prezzi-articoli/soglia-fornitore` riceve classeA1, percentuale
(nuova soglia) e ricaricoImpostato (soglia precedente/null). Stesso permesso
prezzi_articoli.view. Valida percentuale 0...10000, massimo quattro decimali. Lock UPDLOCK/HOLDLOCK sulla chiave A1
anche assente serializza i salvataggi dello stesso fornitore; una soglia cambiata
rispetto allo snapshot restituisce 409 prima delle modifiche. Un conflitto
articolo non deve salvare la soglia. Deploy coordinato backend/frontend.

FE: dopo Cerca, il campo "Soglia ricarico fornitore %" è modificabile;
"Salva soglia" salva solo il riferimento. La dicitura "Soglia salvata" mostra
il valore effettivamente persistito, inizialmente "—". La bozza digitata non
influenza il delta finché il salvataggio non riesce. È possibile salvare 0%.
Il controllo è indipendente da "Nuovo ricarico %" e non richiede anteprima
prezzi o articoli aggiornabili. Cambiando fornitore serve una nuova ricerca
prima di poter modificare e salvare la sua soglia.
Nuova colonna "Delta soglia": ricarico runtime meno soglia, in punti percentuali,
arrotondato all’intero solo per la visualizzazione; positivo verde (Sopra soglia), negativo rosso
(Sotto soglia), zero neutro (In soglia). Il testo e il segno accompagnano i
colori. Se manca la soglia o il ricarico runtime, il delta è "—". Cambiando A1
il riferimento precedente viene nascosto finché non si esegue la nuova ricerca.
Il delta parte dal ricarico runtime a due decimali e viene arrotondato
all’intero nel FE (anche per colore e stato), e considera il costo rettificato +40% dove previsto.

Esempio: soglia 60%, runtime 58% -> -2 p.p.; runtime 70% -> +10 p.p.
Resta possibile una piccola differenza dovuta all'arrotondamento del prezzo a
tre decimali; il confronto non applica una tolleranza commerciale aggiuntiva.

La pagina mostra tre badge sopra la tabella: Sotto soglia, Sopra soglia e
In soglia. Contano tutti gli articoli caricati, anche oltre la pagina corrente,
con lo stesso delta arrotondato della tabella. I ricarichi runtime assenti
sono conteggiati separatamente come non confrontabili. Senza soglia salvata
i badge non vengono mostrati.

## Azione rapida Porta a soglia (2026-10-08)

Il pulsante "Porta a soglia" è accanto al badge rosso Sotto soglia. È disponibile
solo con soglia salvata, ricerca coerente con il fornitore e almeno un articolo
sotto soglia aggiornabile. Usa il delta arrotondato della colonna/badge: una
piccola differenza che appare In soglia non è candidata all'azione rapida.

Il click effettua solo una GET di anteprima alla percentuale della soglia
salvata, non una scrittura. La tabella mostra solo i candidati aggiornabili,
selezionati inizialmente tutti; l'utente può deselezionarne alcuni. I badge
continuano a contare l'intero risultato del fornitore. "Mostra tutti" torna
alla ricerca normale. Il ricarico runtime assente, i bloccati non inclusi e le
unità incompatibili non sono candidati, anche se il badge rosso può includere
articoli non aggiornabili con ricarico runtime calcolabile.

Applica apre la normale conferma, senza popup di ricarico alternativo:
i deselezionati sono semplicemente esclusi. La PUT porta `portaASoglia=true`,
soglia salvata come percentuale e solo gli articoli selezionati. Il backend
rilegge i dati e verifica soglia presente, percentuale identica alla soglia,
assenza di secondo gruppo e delta arrotondato negativo per ogni articolo.
Restano tutti i controlli di snapshot e transazione. Prezzi ricalcolati sul
costo di riferimento (incluso +40% sui costi del 31/12/2021) e arrotondati a
3 decimali. La soglia non è modificata. Dopo successo si ricarica la ricerca
normale, aggiornando ricarichi, delta e badge.

Se la soglia cambia durante la richiesta di anteprima, Applica resta disabilitato
e viene richiesto di ripremere Porta a soglia. Modifiche manuali ai filtri o
alla percentuale invalidano la possibilità di applicare l'anteprima; una
normale ricerca/anteprima esce dalla modalità rapida.


### Presentazione della pagina (8 ottobre 2026)

Ricerca compatta con autocomplete, contesto del fornitore caricato e due
pannelli per soglia e anteprima. I riepiloghi riportano i conteggi; l'azione
Porta a soglia è nel menu ⋮ del riquadro Sotto soglia, con icona di spunta.
Il pulsante menu appare al passaggio del mouse o al focus da tastiera;
sui dispositivi touch resta visibile. Le condizioni di abilitazione restano
quelle dell'azione rapida descritte sopra.

Descrizione e codice articolo condividono la stessa colonna. La tabella segue lo scorrimento verticale della pagina, senza una seconda
barra verticale interna; mantiene lo scorrimento orizzontale su schermi stretti,
righe selezionate evidenziate e
colonna del prezzo proposto evidenziata durante l'anteprima. La barra di
conferma in basso mostra il numero di selezionati su tutte le pagine.
Annulla anteprima e Mostra tutti ripristinano l'elenco già caricato e
azzerano la selezione, senza chiamate di aggiornamento o modifiche ai dati.

La colonna Stato è sostituita da un'icona accanto alla descrizione: spunta
per gli aggiornabili, avviso per gli esclusi. Il tooltip e l'etichetta
accessibile riportano il motivo di esclusione; l'icona riceve focus da tastiera.

## Giacenza positiva e dettaglio magazzini (2026-10-15)

La pagina prezzi mostra solo gli articoli del fornitore A1 con giacenza
**totale strettamente maggiore di zero**, considerando tutti i magazzini.
Si usa SALDIMAGAZZINO.QGIACENZA per QTY='1' (quantità principale, coerente con
SaldiMagazzinoMapper), sommando varianti e magazzini. QTY='2' resta separata
e non viene sommata alla quantità principale. I saldi mancanti o nulli non
rendono positivo il totale. I saldi negativi concorrono alla somma.

Verifica di produzione fornita dall'utente il 15 ottobre, ottenuta con
`scripts/verifica_giacenze_per_magazzino.sql`: B/QTY1 42869 articoli, 8578
positivi e 1338 negativi; B/QTY2 118 articoli, 1 positivo e 109 negativi;
C/QTY1 2158 articoli, 16 positivi e 11 negativi; OST/QTY1 461 articoli, tutti
positivi. Questi numeri non sono default né vincoli. La richiesta è stata
quindi estesa a tutti i magazzini, con esposizione dei saldi.

Una sola riga e una sola selezione per articolo: il DTO aggiunge
`giacenzaTotale` e `giacenze` (magazzino/giacenza). Le colonne Magazzino e
Giacenza mostrano i saldi affiancati nello stesso ordine, esclusivamente per
magazzini con giacenza positiva. I magazzini con saldo zero o negativo non
sono visualizzati. Il filtro di inclusione dell'articolo continua a usare
il totale complessivo, comprensivo anche degli eventuali saldi negativi. L'anagrafica
prezzi resta unica e non viene aggiornata una volta per magazzino.

Il filtro è nel metodo leggi condiviso da ricerca, anteprima, Porta a soglia
e applicazione dei prezzi (compreso il secondo gruppo). Durante il salvataggio
anche i saldi sono letti con HOLDLOCK: se il totale non è più positivo rispetto
all'anteprima, l'articolo non risulta ammesso e si restituisce 409 senza
aggiornamenti parziali. Una variazione di quantità che mantiene il totale
positivo non modifica il calcolo del prezzo né costituisce da sola un conflitto.

Non servono migrazioni SQL. Frontend e backend vanno aggiornati insieme
per esporre i nuovi campi. Verifiche: suite prezzi backend completa, regressioni
su varianti/magazzini, QTY2 separata, assenza saldi, totale zero/negativo e
giacenza esaurita dopo l'anteprima; suite frontend e build production.

Il restyling dashboard è stato messo da parte nei due repository con stash
`feature/restyling-dashboard: pausa prima filtro giacenza prezzi`; questa
modifica viene sviluppata su BE prod e FE master.

La tabella presenta prima gli articoli aggiornabili, poi gli esclusi per
altri motivi (blocco/unità), infine tutti quelli senza costo valido. Dentro
ciascun gruppo mantiene l’ordine per codice ricevuto dal backend. Lo stesso
ordine è conservato in ricerca, anteprima e annullamento dell’anteprima.
