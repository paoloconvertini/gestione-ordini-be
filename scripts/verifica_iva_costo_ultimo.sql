-- Sola lettura: eseguire nel database aziendale da verificare.
-- IVAMAG e un codice IVA, non un importo. TGCI contiene la relativa aliquota.
-- Il confronto e indicativo: coefficienti, rettifiche e costi accessori
-- possono rendere VALORE/MQUANTITA diverso dal semplice prezzo scontato.
SET NOCOUNT ON;
SELECT TOP (50)
    m.MARTICOLO, m.DATAOPMAGAZZINO, m.PROGRGENERALE,
    m.MUNITA, m.MQUANTITA, m.PREZZO,
    m.SCONTOARTICOLO, m.SCONTOC1, m.SCONTOC2, m.SCONTOP,
    m.IVAMAG AS CodiceIva, i.ALIQUOTA,
    m.VALORE, m.VALOREUNITARIO,
    m.VALORE / NULLIF(m.MQUANTITA, 0) AS CostoUltimo,
    m.PREZZO * (1 - COALESCE(m.SCONTOARTICOLO, 0) / 100.0)
             * (1 - COALESCE(m.SCONTOC1, 0) / 100.0)
             * (1 - COALESCE(m.SCONTOC2, 0) / 100.0)
             * (1 - COALESCE(m.SCONTOP, 0) / 100.0) AS PrezzoScontatoSenzaAggiuntaIva
FROM dbo.MAGAZZINO m
JOIN dbo.TMMC t ON t.MCAUSALE = m.MCAUSALE
LEFT JOIN dbo.TGCI i ON i.CODICEIVA = m.IVAMAG
WHERE t.AGGIORNAULTIMO = 'S'
  AND m.MQUANTITA > 0 AND m.VALORE > 0 AND m.VALOREUNITARIO > 0
ORDER BY m.DATAOPMAGAZZINO DESC, m.PROGRGENERALE DESC;

WITH Confronto AS (
    SELECT m.VALORE / m.MQUANTITA AS Costo, i.ALIQUOTA,
           m.PREZZO * (1 - COALESCE(m.SCONTOARTICOLO, 0) / 100.0)
                    * (1 - COALESCE(m.SCONTOC1, 0) / 100.0)
                    * (1 - COALESCE(m.SCONTOC2, 0) / 100.0)
                    * (1 - COALESCE(m.SCONTOP, 0) / 100.0) AS Netto
    FROM dbo.MAGAZZINO m
    JOIN dbo.TMMC t ON t.MCAUSALE = m.MCAUSALE
    JOIN dbo.TGCI i ON i.CODICEIVA = m.IVAMAG
    WHERE t.AGGIORNAULTIMO = 'S'
      AND m.MQUANTITA > 0 AND m.VALORE > 0 AND m.VALOREUNITARIO > 0
      AND m.PREZZO > 0 AND i.ALIQUOTA > 0
)
SELECT COUNT(*) AS MovimentiConfrontabili,
       SUM(CASE WHEN ABS(Costo - Netto) <= 0.01 THEN 1 ELSE 0 END) AS CoincidonoConNetto,
       SUM(CASE WHEN ABS(Costo - Netto * (1 + ALIQUOTA / 100.0)) <= 0.01 THEN 1 ELSE 0 END) AS CoincidonoConIvato
FROM Confronto;
