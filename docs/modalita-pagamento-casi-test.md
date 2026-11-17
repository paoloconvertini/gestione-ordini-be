# Casi manuali modalità pagamento — DB sviluppo

Creati il 17 novembre 2026 su richiesta: cliente dimostrativo 1231/990901
TEST MODALITA PAGAMENTO, 12 ordini, 45 righe ordine (33 pagamento),
12 dettagli GO commerciali e 7 fatture acconto in serie A (sei numerate, una non validata)
(21 righe FATTURE2). Nessun movimento o saldo di magazzino modificato.

Serie 01 e 11 verificate su ordini reali già nel DB. Anagrafica cliente
separata, senza recapiti reali; articolo commerciale già censito, prezzo
dimostrativo 1000 euro imponibili per ogni ordine, sconti zero e IVA/unità
riprese dall'articolo di riferimento. Tutti gli ordini marcati TEST PAGAMENTI.
Non sovrascritto alcun ordine o cliente esistente.

## Risultati verificati con il servizio reale

| Ordine | Caso | Risultato |
|---|---|---|
| 2026/01/910101 | Acconto 30%, saldo 70%, incompleto | Acconto da fatturare, saldo al pronto |
| 2026/01/910102 | Acconto dicembre 2026 | Programmato, descrizione TEST ACCONTO DICEMBRE |
| 2026/01/910103 | Acconto gennaio 2027 | Programmato, descrizione TEST ACCONTO GENNAIO 2027 |
| 2026/01/910104 | Tre acconti con data predefinita | Tre righe con anomalie date, saldo al pronto |
| 2026/11/910105 | Quattro tranche 25%, fatturato 35% | Primo emesso senza warning, secondo/terzo da verificare |
| 2026/11/910106 | Quattro tranche 25%, fatturato 60% | Primi due emessi senza warning, terzo da verificare |
| 2026/11/910107 | Quattro tranche 25%, fatturato 100% | Tutto emesso, nessun warning |
| 2026/11/910108 | Acconto 30% fatto, ordine completo | Solo saldo 70% da fatturare |
| 2026/01/910109 | Acconto 30% fatto, ordine incompleto | Nessuna azione, saldo al pronto |
| 2026/01/910110 | Acconti 20% ottobre, 30% dicembre; saldo 50% | Primo da fatturare, secondo programmato, nessuna anomalia |
| 2026/11/910111 | Acconto 100% fatto, saldo zero | Acconto emesso e saldo non dovuto |
| 2026/11/910112 | Acconto fatto, saldo al 15/12/2026 | Saldo programmato dal 01/12/2026 |

Fatture esistenti: 2026/A/910105 (350), 910106 (600), 910107 (1000),
910108 (300), 910109 (300), 910111 (1000), 910112 (300).
Dopo la prova utente sono stati completati numero/data dei documenti
910105, 910106, 910107, 910108, 910109 e 910111, per renderli visibili anche
nel riepilogo Mostra acconti. Il solo 910112 resta senza numero/data e prova
che una fattura già creata non deve essere duplicata prima della validazione.
Mostra acconti usa FatturaService.getAcconti e filtra espressamente i documenti
senza numero o data. Non è stato cambiato quel criterio applicativo.
Il riepilogo è del cliente: mostra sei acconti, fra cui i 300 euro dell'ordine
2026/11/910108, indipendentemente dall'ordine da cui si apre il pannello.

I 33 snapshot sono stati generati chiamando riconciliaOrdine esclusivamente
per queste chiavi, con commit tramite runner Quarkus temporaneo poi rimosso.
Non sono stati inseriti counter o stati di pagamento artificialmente.
Verifica SQL finale:
- 3 righe DA_FATTURARE;
- 6 righe ANOMALIA appartenenti a 3 ordini distinti;
- 4 righe PROGRAMMATO;
- 8 righe AL_PRONTO;
- 11 righe EMESSO;
- 1 riga NON_DOVUTO.

## Prova nella pagina

1. Entrare come Admin, aprire dashboard e premere Aggiorna.
2. La tile per i soli demo contribuisce 3 fatture da emettere e 3 ordini con anomalie.
3. Aprire la tile. Il filtro iniziale mostra esclusivamente le tranche DA_FATTURARE; anomalie e altri stati sono consultabili scegliendo il filtro.
4. Selezionare Mostra: Tutte le scadenze per vedere tutti e 12 gli ordini.
5. Provare i filtri venditore 01 e 11 e Apri ordine / Torna alle scadenze.
6. Nella lista ordini cercare 9101 per trovare i demo.

Le modifiche ai dati fatte successivamente non sono ricalcolate dalla lettura
pagina: il job notturno resta off in attesa della finestra concordata.

Script dati: scripts/demo_modalita_pagamento_sviluppo.sql, protezione
ambiente inizialmente disattivata e rifiuto dei riferimenti già presenti.
Non rieseguirlo sui dati già creati: non è una procedura di reset.

## Correzione emersa durante la preparazione

Una chiamata reale senza scritture preliminari ha evidenziato che SQL Server
non aveva ancora iniziato la transazione JDBC implicita prima di sp_getapplock.
Aggiunta lettura TOP(1) della sola tabella cache prima del lock. Nessun
BEGIN/COMMIT manuale sovrapposto al transaction manager. Test regressione
senza @TestTransaction e senza INSERT iniziali: superato.
Verificati 8 test integrazione più il runner di preparazione, tutti superati.

Verificato il riepilogo chiamando il servizio reale getAcconti(990901):
sei documenti restituiti, 910108 presente con 300 euro, numero e data,
910112 escluso come previsto. Runner di verifica temporaneo rimosso.

## Confronto con acconti reali — 17 novembre 2026

Analisi su dati preesistenti nel DB sviluppo (non accesso alla produzione),
confronto mapper FattureMapper.buildFatturaAccontoDettaglio e SQL estratto.
Due verifiche reali temporanee, in sola lettura, superate; runner poi rimosso.

- Documento 2026/A/572, fattura 3775/A: *ACC tipo V, quantità 0,
  PREZZO 9426.23, IVA 22, riferimento 2026/13/255.
- Documento 2025/A/711: descrizione Saldo, quantità 0, PREZZO 1311.943,
  riferimento in forma storica ns ord. n° 2025/13/404.
  Il riepilogo riconosce correttamente entrambi.
- Documento 2026/A/1: un'unica fattura con quattro righe *ACC, due per
  ordine (IVA 10 e 22). Ricostruiti separatamente 2026/01/1 con imponibile
  2482.165 (2482.17 ai centesimi) e 2026/01/2 con imponibile 8140.247
  (8140.25 ai centesimi). Questi riferimenti appartengono a ordini storici
  diversi dai demo. Verificata l'associazione dei blocchi, non inventati
  collegamenti agli ordini 3/4 di descrizione simile.
- In tutti i casi le righe *ACC hanno PROGRORDCLI=0: il riferimento è nelle
  righe C successive, coerente con la logica riusata dal nuovo servizio.
- Le fatture reali usano importi anche a tre o più decimali; PREZZO è
  l'imponibile della riga, non va moltiplicato per QUANTITA.
- Nei demo era stata inserita erroneamente quantità 1 sulle fatture *ACC.
  Allineate le sette righe dimostrative a quantità 0, come nel mapper e nei
  dati reali. Nessun importo o percentuale ORDCLI2 cambiato; nessun dato
  reale modificato. Anche lo script di creazione è stato corretto.
- FLAGFATTURA/FLAGBOLLA sono S in alcuni documenti reali ed empty in altri
  già numerati. Non si deduce la validazione da questi soli flag. Nei demo
  sono ancora NULL: differenza esplicita nei campi non usati dal riscontro.
- Rimane una lacuna nei demo manuali: non c'è ancora un caso multiordine
  e multiIVA nello stesso documento, sebbene il relativo riconoscimento sia
  stato verificato sul documento reale 2026/A/1.
- I CSV caso1/2/3 hanno *MODCONS tipo Q e condizioni libere in righe C;
  non contengono un campione Plurima del nuovo standard *ACCONTO/*SALDO.
  Il nuovo codice non deduce automaticamente percentuali da quei testi.

## Evoluzione dopo la prova dell'utente sul saldo 910108

L'utente ha creato il saldo 700 euro tramite applicazione: fattura
2026/A/910113, non ancora numerata/datata definitivamente.
Il 910108 ora è interamente coperto (acconto 300 + saldo 700):
snapshot aggiornato col servizio reale sul solo ordine, entrambe le righe
EMESSO. I numeri iniziali di questo documento descrivono il seed prima di
questa azione: il counter demo corrente DA_FATTURARE scende da 3 a 2.
Nel filtro iniziale l'ordine non appare più; usare Tutte le scadenze/Già emessi.
Il nuovo hook dopo creazione aggiorna automaticamente solo le chiavi fatturate.
