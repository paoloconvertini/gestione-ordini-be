-- SQL Server: eseguire nel database aziendale della API principale prima del backend aggiornato.
-- Rieseguibile. Campo nullable per le consegne già esistenti. Nessuna modifica agli indici.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH(N'dbo.GO_CONSEGNA_LIBERA', N'motivazione') IS NULL
BEGIN
    ALTER TABLE dbo.GO_CONSEGNA_LIBERA ADD motivazione varchar(2000) NULL;
END;
COMMIT;
