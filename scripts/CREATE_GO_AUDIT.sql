CREATE TABLE GO_AUDIT (
                          ID               BIGINT IDENTITY PRIMARY KEY,
                          ENTITY_NAME      VARCHAR(50) NOT NULL,      -- GoOrdine, GoOrdineDettaglio, OrdineDettaglio, ecc.
                          ANNO             INT NOT NULL,
                          SERIE            VARCHAR(3) NOT NULL,
                          PROGRESSIVO      INT NOT NULL,
                          RIGO             INT NULL,                 -- rigo in ORDCLI2 (OrdineDettaglio)
                          PROGR_GENERALE   INT NULL,                 -- chiave GoOrdineDettaglio
                          FIELD_NAME       VARCHAR(50) NOT NULL,     -- es: qtaDaConsegnare, flBolla, status, ecc.
                          OLD_VALUE        VARCHAR(2000) NULL,
                          NEW_VALUE        VARCHAR(2000) NULL,

                          ACTION_TYPE      VARCHAR(30) NOT NULL,     -- UPDATE / INSERT / DELETE / SCHEDULER / AUTO
                          OPERATION_SOURCE VARCHAR(100) NOT NULL,    -- nome metodo: es. 'scheduler:updateArticoliBolle'

                          CREATE_DATE      DATETIME NOT NULL DEFAULT GETDATE(),
                          NOTE             VARCHAR(500) NULL
);
