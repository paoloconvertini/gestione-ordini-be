alter table GO_ORDINE_DETTAGLIO
    add DATA_CARICO datetime2,
        DATA_DOC datetime2,
        NUM_DOC varchar(15),
        ANNO_MAG int,
        SERIE_MAG varchar(3),
        PROGRESSIVO_MAG int
go
