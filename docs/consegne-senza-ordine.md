# Consegne senza ordine cliente

Il planner Consegne settimanali permette di creare una «Nuova consegna senza
ordine». Intestazione con autocomplete sull'anagrafica clienti esistente,
oppure testo libero senza selezione. La selezione copia indirizzo, località,
telefono e coordinate; il luogo resta modificabile e una sua modifica azzera
le coordinate precedenti. Non vengono creati clienti né ordini nell'ERP.

La località propone i comuni della tabella `TTCO` dopo tre caratteri, riusando
la ricerca già presente negli altri moduli; resta possibile inserire testo libero.

Campi obbligatori: intestazione, indirizzo, località, data, fascia
Mattina/Pomeriggio e mezzo. Telefono facoltativo. Motivazione facoltativa, massimo 2000 caratteri,
modificabile nella modale e visibile nella scheda del planner. L'ora usa la fascia già
prevista per le consegne; non è stato introdotto un orario puntuale.

Le consegne indipendenti compaiono nel planner con icona `link_off` e tooltip
«Consegna senza ordine».
Sono modificabili, eliminabili e riordinabili con gli ordini nello stesso giro
(data, fascia, mezzo). Con coordinate valide partecipano anche alla mappa e
al riordino del percorso; il comando coordinate aggiorna la consegna stessa.
Il dettaglio apre il modulo della consegna, senza cercare articoli o bolle.
La condivisione WhatsApp indica che la consegna è senza ordine.

## Database e API

Prima del backend aggiornato, eseguire nel database aziendale dell'API
principale `scripts/create_GO_CONSEGNA_LIBERA.sql`. Script SQL Server
rieseguibile, già applicato al solo Docker locale durante lo sviluppo.
Nessuna modifica alle chiavi della tabella GO_ORD_VEICOLO.

GO_CONSEGNA_LIBERA ha identità bigint autonoma e conserva i dati del
destinatario e della destinazione al momento del salvataggio. Non dipende
dall'esistenza di ORDCLI, GO_ORDINE o di un cliente PIANOCONTI.

API `api/consegne-libere`: POST crea, PUT /{id} modifica, DELETE /{id}
elimina. Stessi ruoli della programmazione consegne (Admin, Venditore,
Magazziniere, Amministrativo, Logistica). La ricerca clienti esistente
accetta ora anche i ruoli della logistica.

Nel DTO del planner e del riordino `idConsegnaLibera` identifica la consegna
indipendente; anno/serie/progressivo restano riservati agli ordini reali.
L'estrazione settimanale rispetta il filtro mezzo. Se viene richiesto un
codice venditore, le consegne indipendenti non sono incluse poiché non hanno
un ordine o una serie venditore associati. La settimana conserva l'attuale
visualizzazione lunedì-sabato.

La lista operativa degli ordini nella logistica resta una lista di ordini:
le nuove consegne indipendenti sono gestite nel planner settimanale.

## Verifiche

Test backend ConsegnaLiberaServiceTest, OrdineServiceTest e JasperServiceTest:
37 test superati (incluso GiroConsegneServiceTest). Comprendono creazione libera, modifica e spostamento,
giri misti, eliminazione, drag-and-drop, filtri e validazione.
14 test frontend dedicati a modulo, planner e mappa superati. Build production
verificata; restano i warning preesistenti su dimensione bundle, CommonJS e CSS.

Il rilascio richiede script e aggiornamento coordinato di FE e API principale.
Versioni e pubblicazione Docker Hub in [Rilascio 10 novembre 2026](rilascio-2026-11-10.md).

## Aggiornamento: motivazione

Per installazioni già esistenti, prima della nuova API eseguire
[scripts/alter_GO_CONSEGNA_LIBERA_motivazione.sql](../scripts/alter_GO_CONSEGNA_LIBERA_motivazione.sql).
Aggiunge soltanto la colonna nullable `motivazione varchar(2000)`, senza modificare
indici o dati esistenti. Lo script di creazione iniziale comprende già la colonna.
Il campo viene salvato e restituito dal backend anche dopo una modifica o il
ricalcolo delle coordinate. Valori vuoti sono normalizzati a null.

Verifiche della fix: 32 test backend di consegne, riordino e ordini;
15 test frontend di modale, planner e mappa; build frontend production.
Migrazione verificata nel solo SQL Server Docker di sviluppo.
