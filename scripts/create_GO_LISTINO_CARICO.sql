CREATE TABLE GO_LISTA_CARICO (
                                   ID          int          not null,
                                   AZIENDA VARCHAR(500) NOT NULL,
                                   NUMERO_ORDINE VARCHAR(50) NOT NULL,
                                   DEPOSITO VARCHAR(500) NULL,
                                   DATA_DISPONIBILE datetime,
                                   PESO float null,
                                   TRASPORTATORE VARCHAR(500) NULL,
                                   DATA_CONVALIDA date,
                                   NUMERO_CONVALIDA int)
;
ALTER TABLE GO_LISTINO_CARICO ADD CONSTRAINT GO_LISTINO_CARICO_PK PRIMARY KEY ( ID );

create unique index GO_LISTINO_CARICO_idx_1
    on GO_LISTINO_CARICO (ID);


create index GO_LISTINO_CARICO_idx_AZIENDA ON GO_LISTINO_CARICO (AZIENDA);
create index GO_LISTINO_CARICO_idx_TRASPORTATORE ON GO_LISTINO_CARICO (TRASPORTATORE);
create UNIQUE index GO_LISTINO_CARICO_idx_NUMERO_ORDINE ON GO_LISTINO_CARICO (NUMERO_ORDINE);