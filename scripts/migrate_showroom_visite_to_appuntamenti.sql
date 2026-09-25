/*
   Migrazione delle visite storiche nell'agenda appuntamenti.
   Eseguire prima in ambiente di sviluppo e verificare il report finale.
   Il file modifica esclusivamente tabelle GO_* (TTCO e PIANOCONTI sono
   consultate soltanto per arricchire i dati).
*/
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('GO_SHOWROOM_VISIT_MIGRATION', 'U') IS NULL
BEGIN
    CREATE TABLE GO_SHOWROOM_VISIT_MIGRATION
    (
        ID_VISITA BIGINT NOT NULL PRIMARY KEY,
        ID_APPUNTAMENTO BIGINT NOT NULL,
        MIGRATED_AT DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;

/* Il modello appuntamento non memorizza la data di fine separatamente.
   Una visita alle 23:00 o dopo non può quindi essere rappresentata
   correttamente senza cambiare lo schema: la migrazione si interrompe. */
IF EXISTS (
    SELECT 1
    FROM GO_SHOWROOM_VISIT v
    WHERE ISNULL(v.IS_DELETED, 0) = 0
      AND CAST(v.DATA_VISITA AS time) >= '23:00:00'
)
BEGIN
    THROW 51000, 'Migrazione interrotta: esistono visite dalle 23:00 in poi, non rappresentabili con data di fine separata.', 1;
END;

IF EXISTS (
    SELECT 1
    FROM GO_SHOWROOM_VISIT v
    WHERE ISNULL(v.IS_DELETED, 0) = 0
      AND (v.SEDE_ID IS NULL OR v.MOTIVO_ID IS NULL OR v.VENDITORE_COD IS NULL)
)
BEGIN
    THROW 51001, 'Migrazione interrotta: esistono visite attive prive di sede, motivo o venditore.', 1;
END;

DECLARE
    @idVisita BIGINT,
    @idAppuntamento BIGINT,
    @sedeId BIGINT,
    @gruppoConto INT,
    @sottoConto VARCHAR(6),
    @nomeCliente VARCHAR(150),
    @telefono VARCHAR(50),
    @comuneIstat VARCHAR(100),
    @comune VARCHAR(100),
    @provincia VARCHAR(10),
    @dataVisita DATETIME2,
    @motivoId BIGINT,
    @venditore VARCHAR(3),
    @note VARCHAR(1000),
    @createdAt DATETIME2;

DECLARE visite CURSOR LOCAL FAST_FORWARD FOR
    SELECT v.ID, v.SEDE_ID, pc.GRUPPO_CONTO, v.CODICE_CLIENTE,
           v.NOME_CLIENTE, v.TELEFONO, v.COMUNE_ISTAT,
           c.NOMECOMUNE, c.PROVCOMUNE, v.DATA_VISITA,
           v.MOTIVO_ID, v.VENDITORE_COD, v.NOTE, v.CREATED_AT
    FROM GO_SHOWROOM_VISIT v
    OUTER APPLY (
        SELECT TOP (1) p.GRUPPOCONTO AS GRUPPO_CONTO
        FROM PIANOCONTI p
        WHERE p.CLIFOR = 'C'
          AND p.SOTTOCONTO = v.CODICE_CLIENTE
        ORDER BY p.GRUPPOCONTO
    ) pc
    /* TTCO appartiene al database comune, non al database delle tabelle GO_
       dove risiedono le tabelle GO_*. */
    LEFT JOIN Trading_srl22.dbo.TTCO c ON c.CODICECOMUNE = v.COMUNE_ISTAT
    WHERE ISNULL(v.IS_DELETED, 0) = 0
      AND NOT EXISTS (
          SELECT 1 FROM GO_SHOWROOM_VISIT_MIGRATION m WHERE m.ID_VISITA = v.ID
      );

OPEN visite;
FETCH NEXT FROM visite INTO @idVisita, @sedeId, @gruppoConto, @sottoConto,
    @nomeCliente, @telefono, @comuneIstat, @comune, @provincia, @dataVisita,
    @motivoId, @venditore, @note, @createdAt;

WHILE @@FETCH_STATUS = 0
BEGIN
    INSERT INTO GO_APPUNTAMENTO
    (
        SEDE_ID, GRUPPO_CONTO, SOTTO_CONTO, NOME_CLIENTE, TELEFONO,
        COMUNE, PROVINCIA, DATA_APPUNTAMENTO, ORA_DA, ORA_A,
        TIPO_EVENTO, ID_MOTIVO, NOTE, PROMEMORIA_INVIATO,
        OUTLOOK_EVENT_ID, CREATED_AT
    )
    VALUES
    (
        @sedeId, @gruppoConto, @sottoConto, @nomeCliente, @telefono,
        @comune, @provincia, CAST(@dataVisita AS date), CAST(@dataVisita AS time),
        CAST(DATEADD(HOUR, 1, @dataVisita) AS time), 'VISITA', @motivoId, @note,
        0, NULL, COALESCE(@createdAt, SYSDATETIME())
    );

    SET @idAppuntamento = CONVERT(BIGINT, SCOPE_IDENTITY());

    INSERT INTO GO_APPUNTAMENTO_VENDITORE (ID_APPUNTAMENTO, COD_VENDITORE)
    VALUES (@idAppuntamento, @venditore);

    INSERT INTO GO_SHOWROOM_VISIT_MIGRATION (ID_VISITA, ID_APPUNTAMENTO)
    VALUES (@idVisita, @idAppuntamento);

    FETCH NEXT FROM visite INTO @idVisita, @sedeId, @gruppoConto, @sottoConto,
        @nomeCliente, @telefono, @comuneIstat, @comune, @provincia, @dataVisita,
        @motivoId, @venditore, @note, @createdAt;
END;

CLOSE visite;
DEALLOCATE visite;

SELECT COUNT(*) AS VISITE_MIGRATE
FROM GO_SHOWROOM_VISIT_MIGRATION;

COMMIT TRANSACTION;
