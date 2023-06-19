CREATE TABLE GO_ORDINE (
    ANNO int NOT NULL,
    SERIE varchar(3) NOT NULL,
    PROGRESSIVO int NOT NULL,
    STATUS varchar(50) NULL,
    WARN_NO_BOLLA char(1) default 0,
    LOCKED char(1) default 0,
    USER_LOCK varchar(100),
    NOTE varchar(2000),
    HAS_FIRMA char(1) default 0)
;
ALTER TABLE GO_ORDINE ADD CONSTRAINT GO_ORDINE01 PRIMARY KEY
    (
     ANNO ASC,
     SERIE ASC,
     PROGRESSIVO ASC
        )


create index GO_ORDINE_status_index
    on GO_ORDINE (STATUS);