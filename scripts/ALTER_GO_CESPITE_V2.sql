alter table GO_CESPITE
    drop column AMMORTAMENTO
go

drop index GO_CESPITE_idx_cod on GO_CESPITE
go

alter table GO_CESPITE
    drop column CODICE
go

alter table GO_CESPITE
    add
        PROTOCOLLO int,
        GIORNALE varchar(1),
        ANNO int
go


alter table GO_CESPITE
    add
        DT_INIZIO_CALCOLO_AMM datetime,
        FL_PRIMO_ANNO varchar(1)
go


