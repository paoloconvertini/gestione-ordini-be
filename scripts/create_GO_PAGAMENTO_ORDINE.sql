-- Eseguire prima del backend con modalità pagamento abilitate.
-- Nessuna modifica o indice sulle tabelle ERP.
IF OBJECT_ID('dbo.GO_PAGAMENTO_ORDINE', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.GO_PAGAMENTO_ORDINE (
        PROGRGENERALE int NOT NULL PRIMARY KEY,
        ANNO int NOT NULL,
        SERIE varchar(3) NOT NULL,
        PROGRESSIVO int NOT NULL,
        RIGO int NOT NULL,
        ARTICOLO varchar(13) NOT NULL,
        PERCENTUALE decimal(9,4) NULL,
        DATA_PREVISTA date NULL,
        DATA_SCADENZA date NULL,
        DESCRIZIONE_BASE varchar(2000) NULL,
        DESCRIZIONE_SCRITTA varchar(2000) NULL,
        ANOMALIA varchar(2000) NULL,
        CLIENTE varchar(250) NULL,
        STATO_ORDINE varchar(30) NULL,
        STATO varchar(30) NOT NULL,
        IMPORTO_PREVISTO decimal(18,2) NULL,
        IMPORTO_FATTURATO decimal(18,2) NULL,
        FATTURE varchar(2000) NULL,
        CONTROLLATO_IL datetime2 NOT NULL
    );
END;
