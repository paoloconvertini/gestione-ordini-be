alter table GO_CESPITE
    add
        DATA_VEND datetime,
        NUM_DOC_VEND varchar(20),
        INTESTATARIO_VEND varchar(250),
        IMPORTO_VEND float,
        SUPER_AMMORTAMENTO int null
    go