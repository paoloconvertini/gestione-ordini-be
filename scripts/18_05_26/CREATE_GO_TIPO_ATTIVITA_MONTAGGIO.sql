CREATE TABLE GO_TIPO_ATTIVITA_MONTAGGIO
(

    ID                     BIGINT IDENTITY (1,1) NOT NULL PRIMARY KEY,

    DESCRIZIONE            VARCHAR(100)          NOT NULL,

    ORDINE_VISUALIZZAZIONE INT                   NOT NULL,

    ATTIVO                 BIT                   NOT NULL DEFAULT 1
);
GO
INSERT INTO GO_TIPO_ATTIVITA_MONTAGGIO (
    DESCRIZIONE,
    ORDINE_VISUALIZZAZIONE
)
VALUES
    ('Profili', 1),
    ('Cassonetti', 2),
    ('Zanzariere', 3),
    ('Infissi', 4),
    ('Porte', 5),
    ('Tende', 6),
    ('Vetrate', 7),
    ('Persiane', 8);
GO

CREATE TABLE GO_ATTIVITA_MONTAGGIO_DETT (

                                            ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,

                                            ID_ATTIVITA_MONTAGGIO BIGINT NOT NULL,

                                            ID_TIPO_ATTIVITA BIGINT NOT NULL,

                                            QUANTITA DECIMAL(10,2) NULL,

                                            COMPLETATO BIT NOT NULL DEFAULT 0,

                                            NOTE VARCHAR(500) NULL
);
GO

CREATE TABLE GO_OPERAIO (

                            ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,

                            NOME VARCHAR(150) NOT NULL,

                            ATTIVO BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE GO_ATTIVITA_MONTAGGIO_OPERAIO (

                                               ID BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,

                                               ID_ATTIVITA_MONTAGGIO BIGINT NOT NULL,

                                               ID_OPERAIO BIGINT NOT NULL,

                                               PRINCIPALE BIT NOT NULL DEFAULT 0
);
GO