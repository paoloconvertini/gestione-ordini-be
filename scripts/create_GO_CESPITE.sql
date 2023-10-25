CREATE TABLE GO_CESPITE (
    ID          varchar(36)          not null,
    TIPO_CESPITE varchar(3) NOT NULL,
    CODICE varchar(20) NULL,
    PROGRESSIVO1 int,
    PROGRESSIVO2 int,
    CESPITE varchar(500),
    DATA_ACQ datetime,
    NUM_DOC_ACQ varchar(20),
    FORNITORE varchar(100),
    IMPORTO float,
    AMMORTAMENTO float,
    ATTIVO char(1) default 'T' not null);

ALTER TABLE GO_CESPITE ADD CONSTRAINT GO_CESPITE_PK PRIMARY KEY ( ID );

create unique index GO_CESPITE_idx_1
    on GO_CESPITE (TIPO_CESPITE, PROGRESSIVO1, PROGRESSIVO2);


create index GO_CESPITE_idx_forn ON GO_CESPITE (FORNITORE);
create index GO_CESPITE_idx_cod ON GO_CESPITE (CODICE);
create index GO_CESPITE_idx_cesp ON GO_CESPITE (CESPITE);