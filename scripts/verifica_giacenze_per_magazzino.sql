-- Sola lettura: eseguire nel database aziendale SQL Server usato dalla API.
-- Somma le varianti per articolo e mantiene separate le tipologie QTY.
WITH Giacenze AS (
    SELECT MMAGAZZINO, QTY, MARTICOLO,
           SUM(COALESCE(QGIACENZA, 0)) AS Giacenza
    FROM dbo.SALDIMAGAZZINO
    GROUP BY MMAGAZZINO, QTY, MARTICOLO
)
SELECT MMAGAZZINO AS Magazzino,
       QTY,
       COUNT_BIG(*) AS Articoli,
       SUM(CONVERT(bigint, CASE WHEN Giacenza > 0 THEN 1 ELSE 0 END)) AS ConGiacenzaPositiva,
       SUM(CONVERT(bigint, CASE WHEN Giacenza = 0 THEN 1 ELSE 0 END)) AS ConGiacenzaZero,
       SUM(CONVERT(bigint, CASE WHEN Giacenza < 0 THEN 1 ELSE 0 END)) AS ConGiacenzaNegativa
FROM Giacenze
GROUP BY MMAGAZZINO, QTY
ORDER BY MMAGAZZINO, QTY;
