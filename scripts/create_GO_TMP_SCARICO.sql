CREATE TABLE GO_TMP_SCARICO (
    MARTICOLO varchar(13) NOT NULL,
    MMAGAZZINO varchar(3) NOT NULL,
    ID_BOLLA int NOT NULL,
    ATTIVO char(1)
                            )
;
ALTER TABLE GO_TMP_SCARICO ADD CONSTRAINT GO_TMP_SCARICO_PK PRIMARY KEY
    (
     MARTICOLO ASC,
     MMAGAZZINO ASC
        )


create index GO_TMP_SCARICO_index
    on GO_TMP_SCARICO (MARTICOLO, MMAGAZZINO);