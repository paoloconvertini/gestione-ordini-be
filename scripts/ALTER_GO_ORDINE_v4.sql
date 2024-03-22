alter table GO_ORDINE
    add
        DATA_NOTE datetime,
    USER_NOTE varchar(100),
    DATA_NOTE_LOGISTICA datetime,
    USER_NOTE_LOGISTICA varchar(100)
go