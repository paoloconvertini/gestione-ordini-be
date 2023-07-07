CREATE TABLE GO_ORDINE_FORNITORE (
                                     ANNOOAF int NOT NULL,
                                     SERIEOAF varchar(3) NOT NULL,
                                     PROGRESSIVOOAF int NOT NULL,
    FLINVIATO char(1) default 0,
    NOTE varchar(2000),
    DATAINVIO datetime)
;
ALTER TABLE GO_ORDINE_FORNITORE ADD CONSTRAINT GO_ORDINEFOR01 PRIMARY KEY
    (
     ANNOOAF ASC,
     SERIEOAF ASC,
     PROGRESSIVOOAF ASC
        )