CREATE TABLE GO_NOTA_CONSEGNA (
                                     id varchar(36) NOT NULL,
                                     dataNota datetime not null,
                                     nota varchar(2000) )
;
ALTER TABLE GO_NOTA_CONSEGNA ADD CONSTRAINT GO_NOTA_CONSEGNA_PK PRIMARY KEY
    (id)

create unique index GO_NOTA_CONSEGNA_id_uindex
    on GO_NOTA_CONSEGNA (id);

create unique index GO_NOTA_CONSEGNA_data_uindex
    on GO_NOTA_CONSEGNA (dataNota);