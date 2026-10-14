-- MySQL: eseguire nel database di autenticazione auth, NON nel SQL Server aziendale.
-- Stessa struttura delle altre voci: PERMISSION -> ROLE_PERMISSION -> JWT permissions.
-- Il permesso consente consultazione, anteprima e aggiornamento dei prezzi base.
-- Assegnazione iniziale ad Admin e Amministrativo, modificabile dalla gestione permessi.
-- Script rieseguibile: non duplica permessi o associazioni e non modifica quelli esistenti.
-- Dopo l'esecuzione gli utenti devono rifare il login per aggiornare il JWT.
USE auth;
START TRANSACTION;

INSERT INTO PERMISSION (NAME, DESCRIPTION)
SELECT 'prezzi_articoli.view', 'Consente accesso e gestione dei prezzi base degli articoli per classificazione fornitore A1'
WHERE NOT EXISTS (
    SELECT 1 FROM PERMISSION WHERE NAME = 'prezzi_articoli.view'
);

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT r.ID, p.ID
FROM ROLE r
JOIN PERMISSION p ON p.NAME = 'prezzi_articoli.view'
WHERE r.NAME IN ('Admin', 'Amministrativo')
  AND NOT EXISTS (
      SELECT 1 FROM ROLE_PERMISSION rp
      WHERE rp.ROLE_ID = r.ID AND rp.PERMISSION_ID = p.ID
  );

COMMIT;

SELECT p.ID, p.NAME, p.DESCRIPTION, r.NAME AS RUOLO
FROM PERMISSION p
LEFT JOIN ROLE_PERMISSION rp ON rp.PERMISSION_ID = p.ID
LEFT JOIN ROLE r ON r.ID = rp.ROLE_ID
WHERE p.NAME = 'prezzi_articoli.view'
ORDER BY r.NAME;
