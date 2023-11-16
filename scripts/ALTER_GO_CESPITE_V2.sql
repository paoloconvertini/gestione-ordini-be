alter table GO_CESPITE
    drop column AMMORTAMENTO
go

alter table GO_CESPITE
    drop column COSTO_GRUPPO
go

alter table GO_CESPITE
    drop column COSTO_CONTO
go

alter table GO_CESPITE
    drop column FONDO_GRUPPO
go

alter table GO_CESPITE
    drop column FONTO_CONTO
go

alter table GO_CESPITE
    drop column PLUS_GRUPPO
go

alter table GO_CESPITE
    drop column PLUS_CONTO
go

alter table GO_CESPITE
    drop column MINUS_GRUPPO
go

alter table GO_CESPITE
    drop column MINUS_CONTO
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


