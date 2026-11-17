-- SOLA LETTURA. Nessuna conversione automatica dei commenti liberi.
-- Candidati: ordini gestiti, ancora aperti/non interamente consegnati, senza
-- nuovo standard e senza fatture acconto riconoscibili collegate all'ordine.
-- Le fatture cliente prive di riferimento riconoscibile richiedono controllo manuale.
SET NOCOUNT ON;
WITH OrdiniAperti AS (
    SELECT o.ANNO,o.SERIE,o.PROGRESSIVO,o.DATAConferma,o.GRUPPOCLIENTE,o.CONTOCLIENTE,
           go.STATUS,p.INTESTAZIONE
    FROM ORDCLI o
    JOIN GO_ORDINE go ON go.ANNO=o.ANNO AND go.SERIE=o.SERIE AND go.PROGRESSIVO=o.PROGRESSIVO
    LEFT JOIN PIANOCONTI p ON p.GRUPPOCONTO=o.GRUPPOCLIENTE AND p.SOTTOCONTO=o.CONTOCLIENTE
    WHERE go.STATUS <> 'ARCHIVIATO' AND o.PROVVISORIO <> 'S'
      AND EXISTS (SELECT 1 FROM ORDCLI2 d WHERE d.ANNO=o.ANNO AND d.SERIE=o.SERIE
          AND d.PROGRESSIVO=o.PROGRESSIVO AND d.TIPORIGO=' '
          AND d.FARTICOLO NOT IN ('*ACCONTO','*SALDO','*MODCONS')
          AND ISNULL(d.SALDOACCONTO,'') <> 'S')
      AND NOT EXISTS (SELECT 1 FROM ORDCLI2 d WHERE d.ANNO=o.ANNO AND d.SERIE=o.SERIE
          AND d.PROGRESSIVO=o.PROGRESSIVO AND d.FARTICOLO IN ('*ACCONTO','*SALDO'))
), FattureAcconto AS (
    SELECT f.ANNO,f.SERIE,f.PROGRESSIVO,f.GRUPPOCLIENTE,f.CONTOCLIENTE
    FROM FATTURE f
    WHERE f.SERIE='A' AND f.PROGRESSIVO>0
      AND EXISTS (SELECT 1 FROM FATTURE2 a WHERE a.ANNO=f.ANNO AND a.SERIE=f.SERIE
          AND a.PROGRESSIVO=f.PROGRESSIVO AND a.FARTICOLO='*ACC')
)
SELECT o.*,
    CASE WHEN EXISTS (SELECT 1 FROM FattureAcconto f
              WHERE f.GRUPPOCLIENTE=o.GRUPPOCLIENTE AND f.CONTOCLIENTE=o.CONTOCLIENTE)
         THEN 'VERIFICARE: cliente con fatture acconto, controllare riferimenti e dati storici'
         ELSE 'CANDIDATO: nessuna fattura acconto del cliente trovata' END VERIFICA,
    (SELECT COUNT(*) FROM ORDCLI2 d WHERE d.ANNO=o.ANNO AND d.SERIE=o.SERIE
        AND d.PROGRESSIVO=o.PROGRESSIVO AND d.FARTICOLO='*MODCONS') RIGHE_MODCONS
FROM OrdiniAperti o
WHERE NOT EXISTS (
    SELECT 1 FROM FattureAcconto f JOIN FATTURE2 r
      ON r.ANNO=f.ANNO AND r.SERIE=f.SERIE AND r.PROGRESSIVO=f.PROGRESSIVO
    WHERE f.GRUPPOCLIENTE=o.GRUPPOCLIENTE AND f.CONTOCLIENTE=o.CONTOCLIENTE
      AND (
        PATINDEX('%[^0-9]'+CONVERT(varchar(4),o.ANNO)+'/'+RTRIM(o.SERIE)+'/'+CONVERT(varchar(12),o.PROGRESSIVO)+'[^0-9]%',
                 ' '+ISNULL(r.FDESCRARTICOLO,'')+' ')>0
        OR PATINDEX('%[^0-9]'+CONVERT(varchar(4),o.ANNO)+'-'+RTRIM(o.SERIE)+'-'+CONVERT(varchar(12),o.PROGRESSIVO)+'[^0-9]%',
                 ' '+ISNULL(r.FDESCRARTICOLO,'')+' ')>0
        OR EXISTS (SELECT 1 FROM ORDCLI2 d WHERE d.PROGRGENERALE=r.PROGRORDCLI
            AND d.ANNO=o.ANNO AND d.SERIE=o.SERIE AND d.PROGRESSIVO=o.PROGRESSIVO)
      )
)
ORDER BY o.ANNO,o.SERIE,o.PROGRESSIVO;
