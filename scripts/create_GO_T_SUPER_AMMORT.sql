CREATE TABLE GO_T_SUPER_AMMORT (
    ID          int          not null,
    DESCRIZIONE varchar (100),
    PERC int
                               );

ALTER TABLE GO_T_SUPER_AMMORT ADD CONSTRAINT GO_T_SUPER_AMMORT_PK PRIMARY KEY ( ID );

create index DESCRIZIONE_idx_1 on GO_T_SUPER_AMMORT (DESCRIZIONE);