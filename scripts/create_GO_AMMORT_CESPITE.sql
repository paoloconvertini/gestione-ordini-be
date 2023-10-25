CREATE TABLE GO_AMMORT_CESPITE (
    ID          varchar(36)          not null,
    ID_AMMORTAMENTO varchar(36) not null,
    DATA_AMM datetime,
    DESCRIZIONE varchar(200) NULL,
    PERC_AMM float,
    QUOTA float,
    FONDO float,
    RESIDUO float,
    ANNO int);

ALTER TABLE GO_AMMORT_CESPITE ADD CONSTRAINT GO_AMMORT_CESPITE_PK PRIMARY KEY ( ID );

create index GO_AMMORT_CESPITE_idx_1 on GO_AMMORT_CESPITE (ID_AMMORTAMENTO);