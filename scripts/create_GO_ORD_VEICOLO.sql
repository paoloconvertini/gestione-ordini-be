CREATE TABLE GO_ORD_VEICOLO (
                                     anno int NOT NULL,
                                     serie varchar(3) NOT NULL,
                                     progressivo int  NOT NULL,
                                     idVeicolo int  NOT NULL
                            );

ALTER TABLE GO_ORD_VEICOLO ADD CONSTRAINT GO_ORD_VEICOLO_PK PRIMARY KEY
    (anno, serie, progressivo, idVeicolo)

create unique index GO_ORD_VEICOLO_id_uindex
    on GO_ORD_VEICOLO (anno, serie, progressivo, idVeicolo);