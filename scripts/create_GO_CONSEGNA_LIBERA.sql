-- SQL Server: database aziendale dell'API principale. Eseguire prima del backend aggiornato.
-- Rieseguibile. Non crea ordini né modifica l'anagrafica clienti.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.GO_CONSEGNA_LIBERA', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.GO_CONSEGNA_LIBERA (
        id bigint IDENTITY(1,1) NOT NULL CONSTRAINT PK_GO_CONSEGNA_LIBERA PRIMARY KEY,
        intestazione varchar(255) NOT NULL,
        indirizzo varchar(255) NOT NULL,
        localita varchar(255) NOT NULL,
        telefono varchar(50) NULL,
        latitudine float NULL,
        longitudine float NULL,
        dataConsegna date NOT NULL,
        oraConsegna char(1) NOT NULL,
        idVeicolo int NOT NULL,
        ordine bigint NOT NULL,
        CONSTRAINT CK_GO_CONSEGNA_LIBERA_FASCIA CHECK (oraConsegna IN ('M', 'P')),
        CONSTRAINT CK_GO_CONSEGNA_LIBERA_ORDINE CHECK (ordine > 0)
    );
    CREATE INDEX IX_GO_CONSEGNA_LIBERA_GIRO ON dbo.GO_CONSEGNA_LIBERA (dataConsegna, idVeicolo, oraConsegna, ordine);
END;
COMMIT;
