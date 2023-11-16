CREATE TABLE GO_TIPO_CESPITE
(
    ID                varchar(36) not null,
    TIPO_CESPITE      varchar(3)  NOT NULL,
    CODICE            varchar(20) NULL,
    DESCRIZIONE       varchar(100) null,
    PERC_AMMORTAMENTO float,
    COSTO_GRUPPO      int,
    COSTO_CONTO       varchar(6),
    AMM_GRUPPO        int,
    AMM_CONTO         varchar(6),
    FONDO_GRUPPO      int,
    FONDO_CONTO       varchar(6),
    PLUS_GRUPPO       int,
    PLUS_CONTO        varchar(6),
    MINUS_GRUPPO      int,
    MINUS_CONTO       varchar(6)
);

ALTER TABLE GO_TIPO_CESPITE ADD CONSTRAINT GO_TIPO_CESPITE_PK PRIMARY KEY ( ID );

create unique index GO_TIPO_CESPITE_idx_1
    on GO_TIPO_CESPITE (TIPO_CESPITE);
