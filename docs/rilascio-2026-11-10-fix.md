# Rilascio 10 novembre 2026 — Fix motivazione consegne e reset prezzi

Versioni API principale **6.2.15** e frontend **5.2.16**.
Branch backend `prod`, frontend `master`.
Commit sorgenti BE `87097e9`, FE `8de13d6`, pubblicati su origin.
Precedenti: API 6.2.14, frontend 5.2.15.

## Contenuto

- Campo Motivazione facoltativo (massimo 2000 caratteri) nelle consegne senza
  ordine: inserimento, modifica, persistenza e visualizzazione nella scheda
  del planner; testo su due righe con tooltip completo.
- Pagina prezzi: pulsante gomma nel campo fornitore. Azzera filtro, lista,
  fornitore caricato, riepilogo soglia, anteprima, selezioni ed errori;
  riporta il focus sull'input. La precedente anteprima non resta applicabile.
- Sviluppo promemoria acquisti escluso: conservato negli stash dedicati.

## Aggiornamento database e installazione

La tabella GO_CONSEGNA_LIBERA è già stata creata nell'installazione esistente:
**eseguire soltanto**
[alter_GO_CONSEGNA_LIBERA_motivazione.sql](../scripts/alter_GO_CONSEGNA_LIBERA_motivazione.sql)
nel database aziendale della API principale **prima di avviare l'API 6.2.15**.
Lo script aggiunge `motivazione varchar(2000) NULL`, è transazionale e
rieseguibile. Non modifica indici né i dati esistenti. Le consegne pregresse
mantengono motivazione nulla e restano valide.

```sql
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH(N'dbo.GO_CONSEGNA_LIBERA', N'motivazione') IS NULL
BEGIN
    ALTER TABLE dbo.GO_CONSEGNA_LIBERA ADD motivazione varchar(2000) NULL;
END;
COMMIT;
```

Il CREATE aggiornato serve solo alle nuove installazioni. La migrazione ALTER
è stata verificata due volte nel solo Docker locale; nessuno script è stato
eseguito in produzione. Conservare i riferimenti delle immagini installate e
seguire la procedura di backup prevista in produzione.

Aggiornare insieme API e frontend: il frontend richiede il nuovo backend per
salvare la motivazione. Nessun nuovo permesso richiesto. Authentication e API
Ceglie/Ostuni non cambiano. Il deploy di produzione resta separato.

## Verifiche e artefatti

- API: `clean package` con build/push implicite Quarkus disabilitate; 39 test
  superati, zero errori/fallimenti/skipped (ConsegnaLiberaServiceTest,
  GiroConsegneServiceTest, OrdineServiceTest e JasperServiceTest).
- Frontend: 56 test superati tra consegne, modale, mappa e pagina prezzi,
  con ChromeHeadless e tsconfig temporaneo per isolare la suite pertinente.
- Build frontend production completata. Warning preesistenti: bundle iniziale
  3,04 MB oltre 2 MB, CSS prezzi 6,25 kB oltre 6 kB, CommonJS e due selettori
  CSS saltati dall'ottimizzatore.
- Warning backend preesistenti su converter/unità di persistenza,
  costruttore privato di OperaioMapper, API deprecate e unchecked.
- API ricostruita da pulito; assenza delle classi PromemoriaAcquisto nel JAR
  verificata prima della pubblicazione.
- Artefatti runtime: intero `target/quarkus-app` dell'API; frontend
  `dist/gestione-ordini` e `nginx.conf` originale. Dockerfile JVM e stage
  runtime Nginx del progetto, Java 21 e font JasperReports conservati.
- Piattaforma `linux/amd64`; builder `admiring_zhukovsky`.
- Checksum di 343 file in [rilascio-2026-11-10-fix.sha256](rilascio-2026-11-10-fix.sha256),
  percorsi relativi alla root `gestione_ordini`.

Comando API dalla root backend:

```sh
scripts/run-with-env.sh mvn -pl gestione-ordini-api -Dtest=ConsegnaLiberaServiceTest,GiroConsegneServiceTest,OrdineServiceTest,JasperServiceTest -Dquarkus.http.test-port=0 -Dquarkus.container-image.build=false -Dquarkus.container-image.push=false clean package
```

Frontend: `npm run build -- --configuration=production`.

## Pubblicazione Docker Hub

Entrambi i tag con versione sono stati pubblicati prima dell'aggiornamento dei
latest. Lettura remota conferma coincidenza dei digest versione/latest e
piattaforma runtime linux/amd64:

- `pconvertini/gestione-ordini-api:6.2.15` e `:latest`:
  `sha256:6087507cce54cd8eba23dfebbe1015765574215f83a5e0f9c201ed8d635fc618`
- `pconvertini/gestione-ordini:5.2.16` e `:latest`:
  `sha256:cadb8f7122532d4cb70876ae70d54dbf57a7f78d2a8a33bbdaf842a18b788487`

Checksum ricontrollati dopo la pubblicazione. Contesti Docker temporanei rimossi;
artefatti originali e stash conservati. Deploy di produzione non eseguito.

## Rollback

Ripristinare insieme API 6.2.14 e frontend 5.2.15, oppure i digest effettivamente
installati prima dell'aggiornamento. La colonna motivazione può restare presente:
la precedente API non la usa. Conservare i valori, non eliminare la colonna.

Digest latest prima di questo rilascio:

- API: `sha256:f774ca12b034e53ff515d0d42c50ea5ac8d47df050451428bb72559358ea3233`
- Frontend: `sha256:b961ea585f6a1a49f7b566f36245368f2e542dd969dbd67a793197b116ef3d3f`
