CREATE TABLE GO_DEPOSITO (
    ID          int          not null,
    NOME varchar(500) NOT NULL);

ALTER TABLE GO_DEPOSITO ADD CONSTRAINT GO_DEPOSITO_PK PRIMARY KEY ( ID );

create unique index GO_DEPOSITO_idx_1
    on GO_DEPOSITO (ID);


create index GO_CESPITE_idx_nome ON GO_DEPOSITO (NOME);