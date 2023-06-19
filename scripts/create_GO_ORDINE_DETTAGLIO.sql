CREATE TABLE GO_ORDINE_DETTAGLIO (

    ANNO int NOT NULL,
    SERIE varchar(3) NOT NULL,
    PROGRESSIVO int NOT NULL,
    RIGO int NOT NULL,
    STATUS varchar(50) NULL,
    FLAG_RISERVATO       char(1) default 0,
    FLAG_NON_DISPONIBILE char(1) default 0,
    FLAG_ORDINATO char(1) default 0,
    FLAG_CONSEGNATO char(1) default 0,
    TONO varchar(20) NULL,
    QUANTITA_DA_CONSEGNARE float NULL,
    QTA_CONS_NO_BOLLA float NULL,
    HAS_BOLLA char(1) default 0,
    NOTE varchar(2000),
    QTA_RISERVATA float NULL,
    FLAG_PRONTO_CONSEGNA char(1) default 0,
    QTA_PRONTO_CONSEGNA float NULL
                                 )
ALTER TABLE GO_ORDINE_DETTAGLIO ADD CONSTRAINT GO_ORDINEDETTAGLIO01 PRIMARY KEY CLUSTERED (
                                                                    ANNO ASC,
                                                                    SERIE ASC,
                                                                    PROGRESSIVO ASC,
                                                                    RIGO ASC
    );
;