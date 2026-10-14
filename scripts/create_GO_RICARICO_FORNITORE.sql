-- SQL Server: eseguire nel database aziendale usato da gestione-ordini-api.
-- Eseguire PRIMA di avviare la versione che legge la soglia dei fornitori.
-- Rieseguibile; nessuna inizializzazione dai prezzi storici: soglia assente = NULL.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.GO_RICARICO_FORNITORE', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.GO_RICARICO_FORNITORE (
        CLASSEA1 varchar(3) NOT NULL,
        PERCENTUALE decimal(9,4) NOT NULL,
        CREATED_AT datetime2 NOT NULL CONSTRAINT DF_GO_RICARICO_FORNITORE_CREATED DEFAULT GETDATE(),
        UPDATED_AT datetime2 NOT NULL CONSTRAINT DF_GO_RICARICO_FORNITORE_UPDATED DEFAULT GETDATE(),
        CREATED_BY varchar(255) NULL,
        UPDATED_BY varchar(255) NULL,
        CONSTRAINT PK_GO_RICARICO_FORNITORE PRIMARY KEY (CLASSEA1),
        CONSTRAINT CK_GO_RICARICO_FORNITORE_PERCENTUALE CHECK (PERCENTUALE >= 0 AND PERCENTUALE <= 10000)
    );
END;
COMMIT;
