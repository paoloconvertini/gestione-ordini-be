# Carico magazzino infissi

Branch: feature/ordine-fornitore-infissi. Implementazione del 14 novembre 2026.

## Ambito concordato

Solo carico di magazzino, senza collegamento a ordini fornitori e senza scansioni.
Dal dettaglio di un ordine cliente si selezionano gli articoli e si apre Carico
magazzino infissi. Il flusso ordinario Carica Mag. resta invariato.
La nuova modale raccoglie numero DDT (anche alfanumerico, massimo 15 caratteri),
data documento, data operazione, vettore, causale e totale imponibile esclusa IVA.
Le quantità sono quelle intere delle righe selezionate, non quantità ricavate
dalle righe del DDT e non quantità modificabili in questa modale.

## Ripartizione e arrotondamenti

Costo medio comune = imponibile / somma quantità. Ogni riga riceve un importo
proporzionale alla quantità. Il backend usa BigDecimal: calcola le quote in
centesimi interi per difetto, poi assegna i centesimi residui alle frazioni
maggiori. A parità di frazione prevale l'ordine stabile rigo/progrGenerale.
Nessuna quota negativa e nessuna differenza accumulata sull'ultima riga.
Esempi: 1000 euro con quantità 2/3 -> 400/600; 100 euro su tre righe da un
pezzo -> 33,34/33,33/33,33. Il costo medio visualizzato è approssimato a 8
decimali; il totale riga è il riferimento per i centesimi. In scrittura,
VALORE è la quota riga, VALOREUNITARIO e PREZZO sono quota/quantità a 8 decimali.
Gli sconti cliente non vengono riapplicati al totale netto.

Anteprima e salvataggio usano lo stesso calcolo backend. Modificare il form
invalida l'anteprima; risposte di anteprime obsolete sono scartate. Per salvare
è obbligatoria una nuova anteprima valida. Durante l'invio form e chiusura della
modale sono bloccati; un errore richiede una nuova anteprima.

## Scritture e controlli

POST /api/saldi-magazzino/infissi/anteprima: sola lettura.
POST /api/saldi-magazzino/infissi: registrazione transazionale.
Stessi ruoli ADMIN/AMMINISTRATIVO e stesso permesso UI del carico ordinario.
Le righe vengono rilette dal DB tramite PROGRGENERALE: selezioni duplicate,
righe C/AC, codici generici *PZ/*MQ/*ML, quantità non positive o diverse da
quelle visualizzate e selezioni tra ordini clienti diversi sono rifiutati.
Il totale deve essere positivo e non avere frazioni inferiori al centesimo.

Riutilizzati MagazzinoMapper e SaldiMagazzinoMapper del carico esistente:
movimento CF, deposito B, quantità caricate e aggiornamento QCARICHI/QGIACENZA,
compreso il caso di più righe con lo stesso codice. La selezione deve avere
fornitori coerenti: unica associazione o unico fornitore predefinito, e stesso
fornitore per il DDT. Se il fornitore manca o è ambiguo viene fermato l'intero
carico invece di saltare articoli. Progressivi ERP assegnati con lock SQL
UPDLOCK/HOLDLOCK nella transazione. Nessuna riga ordine cliente viene alterata.
Come nel carico ordinario, PID=0 e provenienza vuota: nessun collegamento OAF.

Nessuna tabella, colonna o indice aggiunto. Schema locale verificato: VALORE,
VALOREUNITARIO e PREZZO sono float SQL (precisione 53), mappati Double nelle
entity. Ripartizione e anteprima sono decimali; conversione solo in persistenza,
senza modificare lo schema ERP. La quadratura contabile va letta ai centesimi.
Restano gli aggiornamenti dei saldi previsti dal carico ordinario: nessuna nuova
regola automatica per costo ultimo, VCARICHI o stato operativo degli articoli.
Non introdotta idempotenza persistente: il blocco UI impedisce invii ripetuti
nella stessa conferma; in caso di risposta di rete incerta verificare il carico
prima di ripetere una registrazione.

## Verifiche

- 12 test backend: 4 sulla ripartizione, 3 integrazione nuovo carico e 5 di
  regressione del carico ordinario; tutti superati senza skipped.
- Test aritmetico su centinaia di importi, quantità frazionarie, residui,
  importi di pochi centesimi e parità di frazioni.
- Persistenza verificata su DB di sviluppo con rollback: somma delle quote,
  quantità, saldi ripetuti dello stesso articolo, prezzo unitario, esclusione
  degli sconti cliente e assenza di collegamenti OAF.
- 22 test frontend su modale e dettaglio: validazione, anteprima obbligatoria,
  richieste obsolete, errore salvataggio e blocco doppio invio.
- Build Angular development e compilazione backend completate.

Incluso nella preparazione API 6.2.18 / frontend 5.2.20 del 14 novembre 2026.
Stato pubblicazione e digest nelle note del rilascio. Nessun deploy incluso.
