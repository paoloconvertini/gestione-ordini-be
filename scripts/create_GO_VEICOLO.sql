CREATE TABLE GO_VEICOLO (
                                     id int NOT NULL,
                                     descrizione varchar(250) NOT NULL)
;
ALTER TABLE GO_VEICOLO ADD CONSTRAINT GO_VEICOLO_PK PRIMARY KEY
    (id)

create unique index GO_VEICOLO_id_uindex
    on GO_VEICOLO (id);