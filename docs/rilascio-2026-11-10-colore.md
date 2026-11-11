# Rilascio 10 novembre 2026 — Colore consegne senza ordine

Frontend **5.2.17**, branch `master`, commit sorgenti `12bfa71b828cf794faa298a427a0343b8797344f`.
Versione frontend precedente 5.2.16. API principale invariata alla 6.2.15.

## Contenuto

Le schede delle consegne senza ordine usano sfondo e bordo `#cd6767`, con
precedenza sui colori di mezzo e fascia Mattina/Pomeriggio. Testi e icone
restano bianchi. In assenza di coordinate il bordo sinistro rosso e l'icona
di avviso restano presenti, mantenendo il colore della scheda.
Nessuna modifica alle consegne collegate a un ordine.

## Installazione

Aggiornare soltanto il container frontend a 5.2.17 o al nuovo latest verificato.
API principale 6.2.15, Authentication e API Ceglie/Ostuni non cambiano.
La modifica è soltanto HTML/CSS: nessun nuovo script SQL, indice o permesso.
Restano i prerequisiti del precedente rilascio per il campo Motivazione.
Deploy di produzione non eseguito durante la preparazione.

## Verifiche

- 15 test frontend superati tra modale consegna libera, planner e mappa,
  con ChromeHeadless e tsconfig temporaneo per la suite pertinente.
- Build production completata. Warning preesistenti: bundle 3,04 MB oltre 2 MB,
  CSS prezzi 6,25 kB oltre 6 kB, CommonJS e due selettori CSS non ottimizzati.
- Stage runtime Nginx del Dockerfile del progetto, artefatti completi
  `dist/gestione-ordini` e `nginx.conf` originale.
- Piattaforma linux/amd64; builder `admiring_zhukovsky`.
- 108 file con checksum in [rilascio-2026-11-10-colore.sha256](rilascio-2026-11-10-colore.sha256),
  inclusi artefatti, configurazione runtime e sorgenti modificati.

## Pubblicazione Docker Hub

Pubblicato `pconvertini/gestione-ordini:5.2.17`, quindi aggiornato latest dallo
stesso digest verificato:
`sha256:3ba31e5f479e92d74a7707d7eed370c0dac4d7d491a8d63a36303120c545aa10`.
Lettura remota conferma uguaglianza versione/latest e piattaforma linux/amd64.
Checksum ricontrollati e contesto Docker temporaneo rimosso.

## Rollback

Ripristinare soltanto il frontend 5.2.16, senza cambiare API o database.
Digest del precedente latest:
`sha256:cadb8f7122532d4cb70876ae70d54dbf57a7f78d2a8a33bbdaf842a18b788487`.

Sorgenti e note del rilascio versionati sui branch principali.
Il lavoro promemoria acquisti resta negli stash.
