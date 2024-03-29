-- auto-generated definition
create table GO_ORDFOR
(
    ANNOOAF         int        not null,
    SERIEOAF        varchar(3) not null,
    PROGRESSIVOOAF  int        not null,
    GRUPPOFOAF      int,
    CONTOFOAF       varchar(6),
    DATAORDINE      datetime,
    DATACONFORDINE  datetime,
    NUMCONFORDINE   varchar(7),
    DATAREVISIONE   datetime,
    NUMREVISIONE    int,
    OTIPOCOMPENSO   varchar(3),
    OCODICEPAGAMENT varchar(3),
    TCOMMESSA       varchar(10),
    OIVAPRIMASCAD   varchar,
    ODATAPRIMASCAD  datetime,
    OBANCAPAGAMENTO varchar(3),
    IBAN            varchar(50),
    DESCRBANCA      varchar(50),
    SWIFT           varchar(11),
    OMODOCONSEGNA   varchar(3),
    TCODICEIVA      varchar(3),
    SCONTOFORNITOR1 float,
    SCONTOFORNITOR2 float,
    FSCONTOPAGAMENT float,
    TPROVVARTICOLO  float,
    TPROVVFORNITORE float,
    MAGAZZINO       varchar(3),
    PROVVISORIO     varchar,
    REFINTERNO      varchar(6),
    OAGENTE         varchar(3),
    OLINGUA         varchar(3),
    OVALUTA         varchar(3),
    OCAMBIO         float,
    OOGGETTO        varchar(5),
    CIG             varchar(15),
    CUP             varchar(15),
    VETTORE         varchar(3),
    TARGA           varchar(20),
    TARGARIMORCHIO  varchar(20),
    NOTEINTERNE     text,
    PROGRESSIVOGEN  int,
    REFERENTE       varchar(50),
    OGGETTO         text,
    GRUPPOCARICO    int,
    CONTOCARICO     varchar(6),
    PREFAZIONE      text,
    PIEDEPAGINA     text,
    GRUPPOMEDIATORE int,
    CONTOMEDIATORE  varchar(6),
    FLAGTRASFERITO  varchar,
    DATAUSER1       datetime,
    DATAUSER2       datetime,
    VALOREUSER      float,
    FLPALMARI       varchar,
    UTENTEPALMARE   varchar(20),
    USERNAME        varchar(20),
    DATAMODIFICA    datetime,
    SYS_CREATEDATE  datetime,
    SYS_CREATEUSER  varchar(20),
    SYS_UPDATEDATE  datetime,
    SYS_UPDATEUSER  varchar(20),
    constraint GO_ORDFOR00
        primary key (ANNOOAF, SERIEOAF, PROGRESSIVOOAF)
)
    go

