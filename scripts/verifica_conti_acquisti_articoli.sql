-- Sola lettura. Fonti: query estratte dall'eseguibile Plurima in Downloads.
-- TMDD: default acquisti; TGCT: associazione classe fornitore/classe articolo.
-- Non scegliere il conto dalla frequenza osservata negli articoli esistenti.
SET NOCOUNT ON;
SET LOCK_TIMEOUT 5000;

-- Fonte dei conti selezionabili nell'importazione articoli Plurima:
-- strings_ascii.txt:463135 e :463156, ricerca con tipoConto='C'.
-- Non confondere TIPOCONTO='C' (costo) con CLIFOR='C' (cliente).
SELECT p.GRUPPOCONTO AS GRUPPOACQUISTI,
       p.SOTTOCONTO AS CONTOACQUISTI,
       p.INTESTAZIONE, p.CONTINUAINTEST, p.TIPOCONTO, p.FLBLOCCATO
FROM dbo.PIANOCONTI p
WHERE p.TIPOCONTO = 'C'
ORDER BY p.GRUPPOCONTO, p.SOTTOCONTO;

IF OBJECT_ID(N'dbo.TMDD') IS NOT NULL
BEGIN
    EXEC(N'SELECT d.GRUPPOACQUISTI, d.CONTOACQUISTI, p.INTESTAZIONE
           FROM dbo.TMDD d
           LEFT JOIN dbo.PIANOCONTI p
             ON p.GRUPPOCONTO=d.GRUPPOACQUISTI AND p.SOTTOCONTO=d.CONTOACQUISTI');
END
ELSE
    SELECT N'TMDD assente: default acquisti non disponibile in questo database' AS ESITO;

IF OBJECT_ID(N'dbo.TGCT') IS NOT NULL
BEGIN
    EXEC(N'SELECT t.CLASSE1, t.CLASSEA1, a.DESCRIZIONE AS CLASSE_ARTICOLO,
                  t.GRUPPOACQUISTI, t.CONTOACQUISTI, p.INTESTAZIONE
           FROM dbo.TGCT t
           LEFT JOIN dbo.TCA1 a ON a.CODICE=t.CLASSEA1
           LEFT JOIN dbo.PIANOCONTI p
             ON p.GRUPPOCONTO=t.GRUPPOACQUISTI AND p.SOTTOCONTO=t.CONTOACQUISTI
           ORDER BY t.CLASSE1, t.CLASSEA1');
END
ELSE
    SELECT N'TGCT assente: associazioni contabili non disponibili in questo database' AS ESITO;
