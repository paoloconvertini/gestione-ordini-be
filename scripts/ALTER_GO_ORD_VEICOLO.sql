alter table GO_ORD_VEICOLO
    add DATA_CONSEGNA datetime
    go

alter table GO_ORD_VEICOLO
    drop constraint GO_ORD_VEICOLO_PK
go

alter table GO_ORD_VEICOLO
    add constraint GO_ORD_VEICOLO_pk
        primary key (anno, serie, progressivo)
go