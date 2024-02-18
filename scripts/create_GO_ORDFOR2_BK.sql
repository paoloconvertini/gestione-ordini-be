-- auto-generated definition
create table GO_ORDFOR2
(
    ANNOOAF         int        not null,
    SERIEOAF        varchar(3) not null,
    PROGRESSIVOOAF  int        not null,
    RIGO            int        not null,
    PROGRGENERALE   int,
    TIPORIGOOAF     varchar(2),
    OARTICOLO       varchar(13),
    SINONIMO1       int,
    VARIANTE1       varchar(3),
    VARIANTE2       varchar(3),
    VARIANTE3       varchar(3),
    VARIANTE4       varchar(3),
    VARIANTE5       varchar(3),
    ODESCRARTICOLO  varchar(50),
    CODICEEAN       varchar(60),
    OSALDOACCONTO   varchar,
    OQUANTITA       float,
    OQUANTITAV      float,
    OQUANTITA2      float,
    OPREZZO         float,
    OVALORE         float,
    OUNITAMISURA    varchar(2),
    OCOEFFICIENTE   float,
    OCODICEIVA      varchar(3),
    FSCONTOARTICOLO float,
    SCONTOF1        float,
    SCONTOF2        float,
    FSCONTOP        float,
    PREZZOEXTRA     float,
    OMAGAZZ         varchar(3),
    DATARICHCONSEG  datetime,
    DATACONFCONSEG  datetime,
    OLOTTOMAGF      varchar(10),
    OCOLLI          int,
    OPALLET         float,
    OCOMMESSA       varchar(10),
    OCENTROCOSTOR   varchar(10),
    OVOCESPESA      varchar(10),
    IMPPROVVFISSO   float,
    OPROVVARTICOLO  float,
    OPROVVFORNITORE float,
    QTYUSER1        float,
    QTYUSER2        float,
    QTYUSER3        float,
    QTYUSER4        float,
    QTYUSER5        float,
    CAMPOUSER1      varchar(25),
    CAMPOUSER2      varchar(25),
    CAMPOUSER3      varchar(25),
    CAMPOUSER4      varchar(25),
    CAMPOUSER5      varchar(25),
    DATAUSER1       datetime,
    DATAUSER2       datetime,
    DATAUSER3       datetime,
    DATAUSER4       datetime,
    DATAUSER5       datetime,
    PROVENIENZA     varchar,
    PID             int,
    PID_PRIMANOTA   int,
    NOTEORDFOR2     text,
    USERNAME        varchar(20),
    DATAMODIFICA    datetime,
    SYS_CREATEDATE  datetime,
    SYS_CREATEUSER  varchar(20),
    SYS_UPDATEDATE  datetime,
    SYS_UPDATEUSER  varchar(20),
    constraint GO_ORDFOR200
        primary key (ANNOOAF, SERIEOAF, PROGRESSIVOOAF, RIGO)
)
go

create unique index GO_ORDFOR2001
    on GO_ORDFOR2 (PROGRGENERALE)
go

