CREATE TABLE GO_ATTIVITA_MONTAGGIO (

                                       ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,

    -- Collegamento ordine cliente
                                       ORDINE_ANNO INT NULL,
                                       ORDINE_SERIE VARCHAR(10) NULL,
                                       ORDINE_PROGRESSIVO INT NULL,

    -- Snapshot numero ordine
                                       NUMERO_ORDINE VARCHAR(100) NULL,

    -- Titolo rapido visualizzazione calendario
                                       TITOLO VARCHAR(255) NOT NULL,

    -- Date / orari attività
                                       DATA_ORA_DA DATETIME NOT NULL,
                                       DATA_ORA_A DATETIME NOT NULL,

    -- Snapshot cliente
                                       NOME_CLIENTE VARCHAR(255) NOT NULL,
                                       TELEFONO VARCHAR(100) NULL,
                                       EMAIL VARCHAR(255) NULL,

    -- Indirizzo intervento
                                       VIA VARCHAR(255) NULL,
                                       CIVICO VARCHAR(50) NULL,
                                       CAP VARCHAR(20) NULL,
                                       PAESE VARCHAR(150) NULL,
                                       PROVINCIA VARCHAR(10) NULL,

    -- Operatività
                                       SCALA_MOBILE BIT NOT NULL DEFAULT 0,

    -- Stato attività
                                       STATO VARCHAR(30) NOT NULL,

    -- Reminder appuntamento
                                       PROMEMORIA_INVIATO BIT NOT NULL DEFAULT 0,
                                       DATA_INVIO_PROMEMORIA DATETIME NULL,

    -- Completamento attività
                                       DATA_COMPLETAMENTO DATETIME NULL,

    -- Note operative
                                       NOTE VARCHAR(MAX) NULL,

    -- Audit
    CREATED_AT DATETIME NOT NULL DEFAULT GETDATE(),
    UPDATED_AT DATETIME NULL,

    CREATED_BY VARCHAR(100) NULL,
    UPDATED_BY VARCHAR(100) NULL
);
GO

CREATE INDEX IDX_ATTIVITA_MONTAGGIO_DATE
    ON GO_ATTIVITA_MONTAGGIO (
                              DATA_ORA_DA,
                              DATA_ORA_A
        );
GO

CREATE INDEX IDX_ATTIVITA_MONTAGGIO_STATO
    ON GO_ATTIVITA_MONTAGGIO (
                              STATO
        );
GO

CREATE INDEX IDX_ATTIVITA_MONTAGGIO_ORDINE
    ON GO_ATTIVITA_MONTAGGIO (
                              ORDINE_ANNO,
                              ORDINE_SERIE,
                              ORDINE_PROGRESSIVO
        );
GO

ALTER TABLE GO_ATTIVITA_MONTAGGIO
    ADD TIPO_APPUNTAMENTO VARCHAR(20);