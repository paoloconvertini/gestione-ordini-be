CREATE TABLE GO_SHOWROOM_MOTIVO (
                                    ID BIGINT IDENTITY(1,1) PRIMARY KEY,
                                    DESCRIZIONE VARCHAR(150) NOT NULL,
                                    ATTIVO BIT NOT NULL DEFAULT 1
);

CREATE TABLE GO_SHOWROOM_VISIT (
                                   ID BIGINT IDENTITY(1,1) PRIMARY KEY,
                                   NOME_CLIENTE VARCHAR(250) NOT NULL,
                                   PROVENIENZA VARCHAR(100) NULL,
                                   TELEFONO VARCHAR(50) NULL,
                                   MOTIVO_ID BIGINT NOT NULL,
                                   VENDITORE_ID BIGINT NOT NULL,
                                   DATA_VISITA DATETIME2 NOT NULL DEFAULT GETDATE(),
                                   CREATED_AT DATETIME2 NOT NULL DEFAULT GETDATE(),
                                   UPDATED_AT DATETIME2 NULL,
                                   CONSTRAINT FK_GO_SHOWROOM_MOTIVO
                                       FOREIGN KEY (MOTIVO_ID)
                                           REFERENCES GO_SHOWROOM_MOTIVO(ID)
);