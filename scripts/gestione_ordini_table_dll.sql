create table ARTICOLI_TAB
(
    ARTICOLO          varchar(13) not null
        constraint ARTICOLI_TAB00
            primary key,
    DESCRARTICOLO     varchar(50),
    DESCRARTSUPPL     varchar(40),
    DESCRESTESA       text,
    ORDINAMENTO       varchar(15),
    UNITAMISURA       varchar(2),
    UNITAMISURASEC    varchar(2),
    COEFFICIENTE      float,
    FLCOEFFTEORICO    varchar,
    UNITAMISURA2      varchar(2),
    UNITAMISURAPRO    varchar(2),
    COEFFICIENTEPRO   float,
    FLUM2PRODUZIONE   varchar,
    FLUM2VENDITA      varchar,
    FLUMSECACQUISTI   varchar,
    FLUMSECVENDITA    varchar,
    COSTOBASE         float,
    COSTOLAVORO       float,
    PREZZOBASE        float,
    PREZZOUMSEC       varchar,
    PREZZOEXTRA       float,
    SCONTOBASE        float,
    CODICEIVA         varchar(3),
    CODDECIMALIPREZZO varchar(3),
    PROVVAGENTE       float,
    PROVVCAPOAREA     float,
    CALCOLAPROVV      varchar,
    GRUPPOVENDITE     int,
    CONTOVENDITE      varchar(6),
    CLASSEA1          varchar(3),
    CLASSEA2          varchar(3),
    CLASSEA3          varchar(3),
    CLASSEA4          varchar(3),
    CLASSEA5          varchar(3),
    CLASSEA6          varchar(3),
    CLASSEA7          varchar(3),
    CLASSEA8          varchar(3),
    CLASSEA9          varchar(3),
    CLASSEA10         varchar(3),
    FLAGLISTINO       varchar,
    GRUPPOACQUISTI    int,
    CONTOACQUISTI     varchar(6),
    QUANTITAUSER01    float,
    QUANTITAUSER02    float,
    QUANTITAUSER03    float,
    QUANTITAUSER04    float,
    QUANTITAUSER05    float,
    CAMPOUSER1        varchar(30),
    CAMPOUSER2        varchar(30),
    CAMPOUSER3        varchar(30),
    CAMPOUSER4        varchar(30),
    CAMPOUSER5        varchar(30),
    ARTICOLORAGGR     varchar(13),
    RIFORIGINALE      varchar(20),
    NOTEARTICOLO      text,
    NOMENCLATURA      varchar(8),
    IVAAGEVOLATA      varchar,
    TIPODOCUMENTOFE   varchar(5),
    PESO              float,
    PESONETTO         float,
    QTAPERCONF        float,
    DIMPERCONF        varchar(15),
    PESOPERCONF       float,
    QTABUSTA          float,
    PESOBUSTA         float,
    PALLET            varchar(3),
    QTAPALLET         float,
    GESTIONESCORTA    varchar,
    UBICAZIONE        varchar(10),
    DEPOSITOREPARTO   varchar(10),
    QTAREPARTO        float,
    LOTTOMINIMO       float,
    QTALOTTO          float,
    PUNTORIORDINO     float,
    SCORTAMINIMA      float,
    GGAPPROVVIG       int,
    SCORTAREPARTO     float,
    QTAMINIMAFATT     float,
    LOTTOMINIMOFATT   float,
    QUALITA           varchar(80),
    MODULOETK         varchar(30),
    TEMPOPROD         float,
    ARTDISTINTABASE   varchar(13),
    ARTICOLOC         varchar(13),
    VARIANTE1         varchar(3),
    VARIANTE2         varchar(3),
    VARIANTE3         varchar(3),
    VARIANTE4         varchar(3),
    VARIANTE5         varchar(3),
    AGGRV1            varchar(13),
    AGGRV2            varchar(13),
    AGGRV3            varchar(13),
    AGGRV4            varchar(13),
    AGGRV5            varchar(13),
    ARTICOLOVUOTO     varchar(13),
    QTYVUOTI          float,
    TIPORIGOGRUPPI    varchar(2),
    CESPITE           varchar(3),
    BLOCCATO          varchar,
    PROGR1            int,
    PROGR2            int,
    QTARIF            float,
    TIPOARTICOLO      varchar(9),
    CAUSALEINEVASO    varchar(3),
    SERVIZIO          float,
    FLGIORNIMESISCAD  varchar,
    PERIODOSCADENZA   int,
    FLNUMEROSERIE     varchar,
    FLASSORTIMENTO    varchar,
    FLCODICEEAN       varchar,
    FLLOTTO           varchar,
    FLPALLET          varchar,
    FLTRATTATO        varchar,
    FLAGTRASFERITO    varchar,
    FLFABBRICAZIONE   varchar,
    FLSCONTI          varchar,
    IMMAGINE          varchar(60),
    LINKSCHEDA        varchar(100),
    LINK1             varchar(250),
    LINK2             varchar(250),
    LINK3             varchar(250),
    PUBBLICAZIONE     int,
    FLB2B             varchar,
    FLB2C             varchar,
    NOTECATALOGO      text,
    APPOGGIO          varchar(150),
    OMAGGIABILE       varchar,
    RENDIBILE         varchar,
    GRUPPOFORNITORE   int,
    CONTOFORNITORE    varchar(6),
    DOCUMENTO         varchar(60),
    DATAMODIFICA      datetime,
    USERNAME          varchar(20),
    SYS_CREATEDATE    datetime,
    SYS_CREATEUSER    varchar(20),
    SYS_UPDATEDATE    datetime,
    SYS_UPDATEUSER    varchar(20),
    FLCONF            varchar,
    DESCRBREVE        varchar(20),
    COLLISTRATO       int
)
    go

create table FATTURE
(
    ANNO              int        not null,
    SERIE             varchar(3) not null,
    PROGRESSIVO       int        not null,
    GRUPPOCLIENTE     int,
    CONTOCLIENTE      varchar(6),
    GRUPPOFATTURA     int,
    CONTOFATTURA      varchar(6),
    TIPOFATTURA       varchar,
    DATABOLLA         datetime,
    NUMEROBOLLA       varchar(7),
    DATAFATTURA       datetime,
    NUMEROFATTURA     varchar(7),
    DATAOPERAZIONE    datetime,
    NUMEROALLEGATO    varchar(7),
    NUMFATFORNITORE   int,
    FCODICEPAGAMENT   varchar(3),
    FCODDIFFPAG       varchar(3),
    FDATAPRIMASCAD    datetime,
    OGGETTO           varchar(5),
    BANCAAPPOGGIO     float,
    NSBANCAINCASSO    varchar(3),
    SPESEBOLLO        varchar,
    IVAPRIMASCAD      varchar,
    AGENTE            varchar(3),
    LISTINO           varchar(3),
    MODOCONSEGNA      varchar(3),
    VETTORE           varchar(3),
    TARGA             varchar(30),
    TARGARIMORCHIO    varchar(30),
    VETTORE2          varchar(3),
    CAUSALETRASP      varchar(3),
    ASPETTOBENE       varchar(40),
    CODICECOLLI       varchar(3),
    NUMEROCOLLI       int,
    DATATRASPORTO     datetime,
    ORATRASPORTO      datetime,
    STATOCONSEGNA     varchar,
    DATACONSEGNAEFF   datetime,
    ORACONSEGNAEFF    datetime,
    DATAORABOLLA      datetime,
    DATAORAALLEST     datetime,
    TEMPOALLESTIMENTO int,
    FLAGVETTORE       varchar,
    PROGRINVIOVETTORE int,
    TOTPESO           float,
    TOTPESONETTO      float,
    TOTPEDANE         int,
    TOTVOLUME         float,
    INTESTDIVERSE     varchar(40),
    INDIRDIVERSE      varchar(30),
    LOCDIVERSE        varchar(35),
    CAPDIVERSE        varchar(6),
    PROVDIVERSE       varchar(2),
    FLAGFATTURA       varchar,
    FLAGBOLLA         varchar,
    BOLLASOLA         varchar,
    SCONTOCLIENTE1    float,
    SCONTOCLIENTE2    float,
    SCONTOPAGAMENTO   float,
    MAGAZZINO         varchar(3),
    FVALUTA           varchar(3),
    FLINGUA           varchar(3),
    FCAMBIO           float,
    FLAGTRASFERITO    varchar,
    FLAGEFFETTI       varchar,
    FCODICEIVAT       varchar(3),
    FPROVVARTICOLO    float,
    FPROVVCLIENTE     float,
    TCOMMESSA         varchar(10),
    TCENTROCOSTO      varchar(10),
    TVOCESPESA        varchar(10),
    TCOMPETENZA       int,
    FDATAPAGPROV      datetime,
    FDATAPAGCAPO      datetime,
    FDATAPAGCAPOAG    datetime,
    FDATAPAGAGENZIA   datetime,
    FLTIPOLIQUIDAPROV varchar(2),
    SETTORE           varchar(3),
    ANNOPARTITA       int,
    NUMPARTITA        int,
    FLBLOCCOPAG       varchar,
    FLVARIAZIONE      varchar,
    GRUPPOCOMPENSA    int,
    CONTOCOMPENSA     varchar(6),
    FLSPEDITO         varchar,
    DATACONSEGNA      datetime,
    FLENTRO           int,
    PRIORITA          int,
    PROGRESSIVOGEN    int,
    VALUSERN1         float,
    VALUSERN2         float,
    VALUSERALFA1      varchar(60),
    VALUSERALFA2      varchar(60),
    FNOTEFATTURA      text,
    FNOTEPIEDE        text,
    DATAPDF           datetime,
    DATAINVIOPDF      datetime,
    PROGRREGBOLLI     int,
    TPROVENIENZA      varchar(3),
    TPID              int,
    CIG               varchar(15),
    CUP               varchar(15),
    DATAMODIFICA      datetime,
    USERNAME          varchar(20),
    SYS_CREATEDATE    datetime,
    SYS_CREATEUSER    varchar(20),
    SYS_UPDATEDATE    datetime,
    SYS_UPDATEUSER    varchar(20),
    FLINVIORIFATT     varchar,
    constraint FATTURE00
        primary key (ANNO, SERIE, PROGRESSIVO)
)
    go

create table FATTURE2
(
    ANNO           int        not null,
    SERIE          varchar(3) not null,
    PROGRESSIVO    int        not null,
    RIGO           int        not null,
    PROGRGENERALE  int,
    PROGRORDCLI    int,
    TIPORIGO       varchar(2),
    FARTICOLO      varchar(13),
    VARIANTE1      varchar(3),
    VARIANTE2      varchar(3),
    VARIANTE3      varchar(3),
    VARIANTE4      varchar(3),
    VARIANTE5      varchar(3),
    FDESCRARTICOLO varchar(50),
    CODICEEAN      varchar(60),
    QUANTITA       float,
    QUANTITA2      float,
    QTAOMAGGIO     float,
    QTAINEVASA     float,
    CAUSALEINEVASO varchar(3),
    PREZZO         float,
    FUNITAMISURA   varchar(2),
    FCOEFFICIENTE  float,
    SCONTOARTICOLO float,
    SCONTOC1       float,
    SCONTOC2       float,
    SCONTOP        float,
    PREZZOEXTRA    float,
    MAGAZZ         varchar(3),
    LOTTOMAGF      varchar(10),
    FCODICEIVA     varchar(3),
    IMPPROVVFISSO  float,
    FPROVVARTICOLO float,
    FPROVVCLIENTE  float,
    FCOLLI         int,
    FPALLET        float,
    COEFPREZZO     float,
    FCENTROCOSTOR  varchar(10),
    FVOCESPESA     varchar(10),
    FCOMMESSA      varchar(10),
    FCOMPETENZA    int,
    FGRUPPORICAVO  int,
    FCONTORICAVO   varchar(6),
    FPROVENIENZA   varchar(3),
    FPID           int,
    QTYUSER1       float,
    QTYUSER2       float,
    QTYUSER3       float,
    QTYUSER4       float,
    QTYUSER5       float,
    QTYUSER6       float,
    QTYUSER7       float,
    DESCRUSER1     varchar(50),
    DESCRUSER2     varchar(50),
    DESCRUSER3     varchar(50),
    DESCRUSER4     varchar(50),
    DESCRUSER5     varchar(50),
    DESCRUSER6     varchar(50),
    DESCRUSER7     varchar(50),
    DATAUSER1      datetime,
    DATAUSER2      datetime,
    DATAUSER3      datetime,
    DATAUSER4      datetime,
    DATAUSER5      datetime,
    DATAUSER6      datetime,
    DATAUSER7      datetime,
    DESCRUSER1E    varchar(100),
    DESCRUSER2E    varchar(100),
    DESCRUSER3E    varchar(100),
    DESCRUSER4E    varchar(100),
    DESCRUSER5E    varchar(100),
    NOTERIGO       text,
    CONTROMARCA    varchar(40),
    FEORDID        varchar(20),
    FEORDDATA      datetime,
    FEORDITEM      varchar(20),
    FEINTENTO      int,
    PROGRDEPOSITO  int,
    PROGRPREV      int,
    USERNAME       varchar(20),
    DATAMODIFICA   datetime,
    SYS_CREATEDATE datetime,
    SYS_CREATEUSER varchar(20),
    SYS_UPDATEDATE datetime,
    SYS_UPDATEUSER varchar(20),
    constraint FATTURE200
        primary key (ANNO, SERIE, PROGRESSIVO, RIGO)
)
    go

create unique index FATTURE2_PROGRGENERALE_uindex
    on FATTURE2 (PROGRGENERALE)
    go

create table FATTUREPAIX_IN
(
    ID_FATTUREPAIX_IN   int identity
        constraint FATTUREPAIX_IN00
        primary key,
    IDENTIFICATIVOSDI   varchar(40),
    IDENTIFICATIVOIX    varchar(40),
    DATACONSEGNA        datetime,
    NOMEFILE            varchar(100),
    NOMEFILEMETADATI    varchar(25),
    HASH                text,
    CODICEDESTINATARIO  varchar(7),
    FORMATO             varchar(5),
    TENTATIVIINVIO      int,
    MESSAGEID           varchar(36),
    MESSAGGIO           text,
    VERSIONE            varchar(10),
    DATAORA             datetime,
    DATAORADOWNLOAD     datetime,
    FORNITORE_DENOM     varchar(40),
    FORNITORE_PIVA      varchar(16),
    DATAORAIMPORTAZIONE datetime,
    FLNOCIG             varchar,
    SYS_CREATEDATE      datetime,
    SYS_CREATEUSER      varchar(20),
    SYS_UPDATEDATE      datetime,
    SYS_UPDATEUSER      varchar(20)
)
    go

create table FORNALTERNATIVI
(
    articolo       varchar(13) not null,
    CONTOF         varchar(6)  not null,
    GRUPPOF        int         not null,
    COEFFPREZZO    float,
    SYS_CREATEDATE datetime2,
    SYS_CREATEUSER varchar(255),
    FDEFAULT       varchar,
    PREZZO         float,
    TEMPOCONSEGNA  int,
    SYS_UPDATEDATE datetime2,
    SYS_UPDATEUSER varchar(255),
    primary key (articolo, CONTOF, GRUPPOF)
)
    go

create table GO_AMMORT_CESPITE
(
    ID              varchar(36) not null
        constraint GO_AMMORT_CESPITE_PK
            primary key,
    ID_AMMORTAMENTO varchar(36) not null,
    DATA_AMM        datetime,
    DESCRIZIONE     varchar(200),
    PERC_AMM        float,
    QUOTA           float,
    FONDO           float,
    RESIDUO         float,
    ANNO            int,
    PERC_SUPER      float,
    QUOTA_SUPER     float
)
    go

create index GO_AMMORT_CESPITE_idx_1
    on GO_AMMORT_CESPITE (ID_AMMORTAMENTO)
    go

create table GO_BOX_DOCCIA
(
    id            varchar(36) not null
        constraint GO_BOX_DOCCIA_pk
            primary key,
    codice        varchar(10) not null,
    descrizione   varchar(500),
    profilo       varchar(200),
    estensibilita varchar(50),
    versione      varchar(20),
    qta           int,
    prezzo        float,
    extra         varchar(200),
    foto          varchar(500),
    posa          varchar(500),
    venduto       char default 0
)
    go

create unique index GO_BOX_DOCCIA_id_uindex
    on GO_BOX_DOCCIA (id)
    go

create index GO_BOX_DOCCIA_codice_uindex
    on GO_BOX_DOCCIA (codice)
    go

create index GO_BOX_DOCCIA_desc_uindex
    on GO_BOX_DOCCIA (descrizione)
    go

create table GO_CESPITE
(
    ID                    varchar(36)      not null
        constraint GO_CESPITE_PK
            primary key,
    TIPO_CESPITE          varchar(3)       not null,
    PROGRESSIVO1          int,
    PROGRESSIVO2          int,
    CESPITE               varchar(500),
    DATA_ACQ              datetime,
    NUM_DOC_ACQ           varchar(20),
    FORNITORE             varchar(100),
    IMPORTO               float,
    ATTIVO                char default 'T' not null,
    DATA_VEND             datetime,
    NUM_DOC_VEND          varchar(20),
    INTESTATARIO_VEND     varchar(250),
    IMPORTO_VEND          float,
    SUPER_AMMORTAMENTO    int,
    PROTOCOLLO            int,
    GIORNALE              varchar,
    ANNO                  int,
    DT_INIZIO_CALCOLO_AMM datetime,
    FL_PRIMO_ANNO         varchar
)
    go

create unique index GO_CESPITE_idx_1
    on GO_CESPITE (TIPO_CESPITE, PROGRESSIVO1, PROGRESSIVO2)
    go

create index GO_CESPITE_idx_forn
    on GO_CESPITE (FORNITORE)
    go

create index GO_CESPITE_idx_cesp
    on GO_CESPITE (CESPITE)
    go

create table GO_DEPOSITO
(
    ID   int          not null
        constraint GO_DEPOSITO_PK
            primary key,
    NOME varchar(500) not null
)
    go

create unique index GO_DEPOSITO_idx_1
    on GO_DEPOSITO (ID)
    go

create index GO_CESPITE_idx_nome
    on GO_DEPOSITO (NOME)
    go

create table GO_FIRMAORDINECLIENTE
(
    ANNO        int        not null,
    SERIE       varchar(3) not null,
    PROGRESSIVO int        not null,
    FILENAME    varchar(100),
    constraint FIRCLI00
        primary key (ANNO, SERIE, PROGRESSIVO)
)
    go

create table GO_FISCALE_RIEPILOGO
(
    ID                      int          not null
        constraint GO_FISCALE_RIEPILOGO_PK
            primary key,
    TIPO_CESPITE            varchar(3)   not null,
    DESCRIZIONE             varchar(100) not null,
    VALORE_AGGIORNATO       float,
    AMMORTAMENTO_ORDINARIO  float,
    AMMORTAMENTO_ANTICIPATO float,
    TOTALE_AMMORTAMENTO     float,
    NON_AMMORTABILE         float,
    FONDO_AMMORTAMENTI      float,
    RESIDUO                 float
)
    go

create unique index GO_FISCALE_RIEPILOGO_idx_1
    on GO_FISCALE_RIEPILOGO (TIPO_CESPITE)
    go

create table GO_LISTA_CARICO
(
    ID               int          not null
        constraint GO_LISTA_CARICO_PK
            primary key,
    AZIENDA          varchar(500) not null,
    NUMERO_ORDINE    varchar(50)  not null,
    DEPOSITO         int,
    DATA_DISPONIBILE datetime,
    PESO             float,
    TRASPORTATORE    int,
    DT_CONVALIDA     date,
    NUM_CONVALIDA    int
)
    go

create unique index GO_LISTA_CARICO_idx_1
    on GO_LISTA_CARICO (ID)
    go

create index GO_LISTA_CARICO_idx_AZIENDA
    on GO_LISTA_CARICO (AZIENDA)
    go

create index GO_LISTA_CARICO_idx_TRASPORTATORE
    on GO_LISTA_CARICO (TRASPORTATORE)
    go

create unique index GO_LISTA_CARICO_idx_NUMERO_ORDINE
    on GO_LISTA_CARICO (NUMERO_ORDINE)
    go

create table GO_NOTA_CONSEGNA
(
    id       varchar(36) not null
        constraint GO_NOTA_CONSEGNA_PK
            primary key,
    dataNota datetime    not null,
    nota     varchar(2000)
)
    go

create unique index GO_NOTA_CONSEGNA_id_uindex
    on GO_NOTA_CONSEGNA (id)
    go

create unique index GO_NOTA_CONSEGNA_data_uindex
    on GO_NOTA_CONSEGNA (dataNota)
    go

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

create table GO_ORDINE
(
    ANNO                int        not null,
    SERIE               varchar(3) not null,
    PROGRESSIVO         int        not null,
    STATUS              varchar(50),
    WARN_NO_BOLLA       char default 0,
    LOCKED              char default 0,
    USER_LOCK           varchar(100),
    NOTE                varchar(2000),
    HAS_FIRMA           char default 0,
    HAS_PRONTO_CONSEGNA char default 0,
    NOTELOGISTICA       varchar(2000),
    HAS_CARICO          varchar,
    DATA_NOTE           datetime,
    USER_NOTE           varchar(100),
    DATA_NOTE_LOGISTICA datetime,
    USER_NOTE_LOGISTICA varchar(100),
    constraint GO_ORDINE01
        primary key (ANNO, SERIE, PROGRESSIVO)
)
    go

create index GO_ORDINE_status_index
    on GO_ORDINE (STATUS)
    go

create table GO_ORDINE_DETTAGLIO
(
    ANNO                   int        not null,
    SERIE                  varchar(3) not null,
    PROGRESSIVO            int        not null,
    RIGO                   int        not null,
    STATUS                 varchar(50),
    FLAG_RISERVATO         char default 0,
    FLAG_NON_DISPONIBILE   char default 0,
    FLAG_ORDINATO          char default 0,
    FLAG_CONSEGNATO        char default 0,
    TONO                   varchar(20),
    QUANTITA_DA_CONSEGNARE float,
    QTA_CONS_NO_BOLLA      float,
    HAS_BOLLA              char default 0,
    NOTE                   varchar(2000),
    QTA_RISERVATA          float,
    FLAG_PRONTO_CONSEGNA   char default 0,
    QTA_PRONTO_CONSEGNA    float,
    PROGRGENERALE          int        not null
        constraint GO_ORDINEDETTAGLIO01
            primary key,
    DATA_CARICO            datetime2,
    DATA_DOC               datetime2,
    NUM_DOC                varchar(15),
    ANNO_MAG               int,
    SERIE_MAG              varchar(3),
    PROGRESSIVO_MAG        int,
    DATA_NOTE              datetime,
    USER_NOTE              varchar(100)
)
    go

create table GO_ORDINE_FORNITORE
(
    ANNOOAF        int        not null,
    SERIEOAF       varchar(3) not null,
    PROGRESSIVOOAF int        not null,
    FLINVIATO      char default 0,
    NOTE           varchar(2000),
    DATAINVIO      datetime,
    constraint GO_ORDINEFOR01
        primary key (ANNOOAF, SERIEOAF, PROGRESSIVOOAF)
)
    go

create table GO_ORD_VEICOLO
(
    anno          int              not null,
    serie         varchar(3)       not null,
    progressivo   int              not null,
    idVeicolo     int,
    DATA_CONSEGNA datetime,
    FL_VENDITORE  char default 'F' not null,
    ora_consegna  char,
    ordine        int,
    constraint GO_ORD_VEICOLO_pk
        primary key (anno, serie, progressivo)
)
    go

create unique index GO_ORD_VEICOLO_id_uindex
    on GO_ORD_VEICOLO (anno, serie, progressivo, idVeicolo)
    go

create table GO_QUADRATURA_CESPITE
(
    ID           varchar(36) not null
        constraint GO_QUADRATURA_CESPITE_PK
            primary key,
    ID_CESPITE   varchar(36) not null,
    ANNO         int         not null,
    AMMORTAMENTO float       not null
)
    go

create unique index GO_QUADRATURA_CESPITE_idx_1
    on GO_QUADRATURA_CESPITE (ID_CESPITE, ANNO)
    go

create table GO_REGISTROAZIONI
(
    id                varchar(36) not null
        constraint GO_REGISTROAZIONI_pk
            primary key,
    anno              int,
    progressivo       int,
    serie             varchar(3),
    rigo              int,
    username          varchar(100),
    createDate        datetime    not null,
    azione            varchar(30) not null,
    quantita          float,
    tono              varchar(20),
    qtaRiservata      float,
    qtaProntoConsegna float
)
    go

create unique index GO_REGISTROAZIONI_id_uindex
    on GO_REGISTROAZIONI (id)
    go

create table GO_TIPO_CESPITE
(
    ID                varchar(36) not null
        constraint GO_TIPO_CESPITE_PK
            primary key,
    TIPO_CESPITE      varchar(3)  not null,
    CODICE            varchar(20),
    DESCRIZIONE       varchar(100),
    PERC_AMMORTAMENTO float,
    COSTO_GRUPPO      int,
    COSTO_CONTO       varchar(6),
    AMM_GRUPPO        int,
    AMM_CONTO         varchar(6),
    FONDO_GRUPPO      int,
    FONDO_CONTO       varchar(6),
    PLUS_GRUPPO       int,
    PLUS_CONTO        varchar(6),
    MINUS_GRUPPO      int,
    MINUS_CONTO       varchar(6)
)
    go

create unique index GO_TIPO_CESPITE_idx_1
    on GO_TIPO_CESPITE (TIPO_CESPITE)
    go

create table GO_TMP_SCARICO
(
    MARTICOLO  varchar(13) not null,
    MMAGAZZINO varchar(3)  not null,
    ID_BOLLA   int         not null,
    ATTIVO     char,
    constraint GO_TMP_SCARICO_PK
        primary key (MARTICOLO, MMAGAZZINO, ID_BOLLA)
)
    go

create index GO_TMP_SCARICO_index
    on GO_TMP_SCARICO (MARTICOLO, MMAGAZZINO)
    go

create table GO_TRASPORTATORE
(
    ID   int          not null
        constraint GO_TRASPORTATORE_PK
            primary key,
    NOME varchar(500) not null
)
    go

create unique index GO_TRASPORTATORE_idx_1
    on GO_TRASPORTATORE (ID)
    go

create index GO_TRASPORTATORE_idx_nome
    on GO_TRASPORTATORE (NOME)
    go

create table GO_T_SUPER_AMMORT
(
    ID          int not null
        constraint GO_T_SUPER_AMMORT_PK
            primary key,
    DESCRIZIONE varchar(100),
    PERC        int
)
    go

create index DESCRIZIONE_idx_1
    on GO_T_SUPER_AMMORT (DESCRIZIONE)
    go

create table GO_VEICOLO
(
    id          int          not null
        constraint GO_VEICOLO_PK
            primary key,
    descrizione varchar(250) not null
)
    go

create unique index GO_VEICOLO_id_uindex
    on GO_VEICOLO (id)
    go

create table INVENTARIO
(
    PROGRGENERALE  int not null
        constraint INVENTARIO00
            primary key,
    SERIE          varchar(15),
    MAGAZZINO      varchar(3),
    DATA           datetime,
    ARTICOLO       varchar(13),
    VARIANTE1      varchar(3),
    VARIANTE2      varchar(3),
    VARIANTE3      varchar(3),
    VARIANTE4      varchar(3),
    VARIANTE5      varchar(3),
    LOTTOMAG       varchar(10),
    CODICEEAN      varchar(60),
    QUANTITA       float,
    QUANTITA2      float,
    SYS_CREATEDATE datetime,
    SYS_CREATEUSER varchar(20),
    SYS_UPDATEDATE datetime,
    SYS_UPDATEUSER varchar(20)
)
    go

create table MAGAZZINO
(
    ANNO              int        not null,
    SERIEMAGAZZINO    varchar(3) not null,
    PROGRESSIVOMAG    int        not null,
    FLAGCONTROMAG     varchar    not null,
    RIGO              int        not null,
    PROGRGENERALE     int,
    DATAOPMAGAZZINO   datetime,
    NUMDOCMAGAZZINO   varchar(15),
    DATADOCMAG        datetime,
    NUMFATTURAMAG     varchar(15),
    DATAFATTURAMAG    datetime,
    MCAUSALE          varchar(3),
    MMAGAZZINO        varchar(3),
    GRUPPOMAG         int,
    CONTOMAG          varchar(6),
    GRUPPOPROP        int,
    CONTOPROP         varchar(6),
    GRUPPOFATTURA     int,
    CONTOFATTURA      varchar(6),
    CONTROMAG         varchar(3),
    TIPORIGOMAG       varchar(2),
    MARTICOLO         varchar(13),
    CODICEEAN         varchar(60),
    VARIANTE1         varchar(3),
    VARIANTE2         varchar(3),
    VARIANTE3         varchar(3),
    VARIANTE4         varchar(3),
    VARIANTE5         varchar(3),
    MDESCRARTICOLO    varchar(60),
    MUNITA            varchar(2),
    MCOEFFICIENTE     float,
    MQUANTITA         float,
    MQUANTITAV        float,
    MQUANTITA2        float,
    VALORE            float,
    VALOREUNITARIO    float,
    PREZZO            float,
    PREZZOEXTRA       float,
    MVALUTA           varchar(3),
    MCAMBIO           float,
    SCONTOARTICOLO    float,
    SCONTOC1          float,
    SCONTOC2          float,
    SCONTOP           float,
    MPROVVARTICOLO    float,
    MPROVVCLIENTE     float,
    LOTTOMAG          varchar(10),
    CONDPAGMAG        varchar(3),
    IVAMAG            varchar(3),
    MAGENTE           varchar(3),
    DATAPRIMAMAG      datetime,
    MCENTROCOSTO      varchar(10),
    MVOCESPESA        varchar(10),
    MCOMMESSA         varchar(10),
    MCIG              varchar(15),
    MCUP              varchar(15),
    MCOLLI            int,
    MPALLET           float,
    MMODOCONSEGNA     varchar(3),
    MVETTORE          varchar(3),
    VALOREUSER        float,
    DESCRUSER1        varchar(60),
    DESCRUSER2        varchar(60),
    DESCRUSER3        varchar(60),
    DESCRUSER4        varchar(60),
    DESCRUSER5        varchar(60),
    QUANTITAUSER01    float,
    QUANTITAUSER02    float,
    QUANTITAUSER03    float,
    QUANTITAUSER04    float,
    QUANTITAUSER05    float,
    DATAUSER1         datetime,
    DATAUSER2         datetime,
    DATAUSER3         datetime,
    DATAUSER4         datetime,
    DATAUSER5         datetime,
    FLAGTRASFERITO    varchar,
    NOTEMAG           text,
    PROVENIENZA       varchar(3),
    PID               int,
    RIF_RIGA_COMMESSA int,
    PARTITACDEPOSITO  int,
    CONTROLLOBF       varchar,
    BFRIGO            int,
    BFVERIFICA        varchar,
    SETTORE           varchar(3),
    OGGETTO           varchar(5),
    RIGOGIORNALE      int,
    DATAINSERIMENTO   datetime,
    USERNAME          varchar(20),
    DATAMODIFICA      datetime,
    COSTOMEDIO        float,
    SYS_CREATEDATE    datetime,
    SYS_CREATEUSER    varchar(20),
    SYS_UPDATEDATE    datetime,
    SYS_UPDATEUSER    varchar(20),
    DATASCONTRINO     datetime,
    PID_PRIMANOTA     int,
    constraint MAGAZZINO00
        primary key (ANNO, SERIEMAGAZZINO, PROGRESSIVOMAG, FLAGCONTROMAG, RIGO)
)
    go

create unique index MAGAZZINO003
    on MAGAZZINO (PROGRGENERALE)
    go

create table MSreplication_options
(
    optname          sysname not null,
    value            bit     not null,
    major_version    int     not null,
    minor_version    int     not null,
    revision         int     not null,
    install_failures int     not null
)
    go

create table ORDCLI
(
    ANNO                  int        not null,
    SERIE                 varchar(3) not null,
    PROGRESSIVO           int        not null,
    GRUPPOCLIENTE         int,
    CONTOCLIENTE          varchar(6),
    GRUPPOFATTURA         int,
    CONTOFATTURA          varchar(6),
    TIPOFATTURA           varchar,
    DATAORDINE            datetime,
    DATARICHIESTA         datetime,
    NUMEROCONFERMA        varchar(15),
    DATACONFERMA          datetime,
    DATACONFERMACLI       datetime,
    NUMCONFERMACLI        varchar(20),
    FDATAPAGANT           datetime,
    FCODICEPAGAMENT       varchar(3),
    FCODDIFFPAG           varchar(3),
    FDATAPRIMASCAD        datetime,
    BANCAAPPOGGIO         float,
    NSBANCAINCASSO        varchar(3),
    SPESEBOLLO            varchar,
    IVAPRIMASCAD          varchar,
    FOGGETTO              varchar(5),
    AGENTE                varchar(3),
    LISTINO               varchar(3),
    MODOCONSEGNA          varchar(3),
    BOLLASOLA             varchar,
    SCONTOCLIENTE1        float,
    SCONTOCLIENTE2        float,
    SCONTOPAGAMENTO       float,
    MAGAZZINO             varchar(3),
    FVALUTA               varchar(3),
    FLINGUA               varchar(3),
    FCAMBIO               float,
    FCODICEIVA            varchar(3),
    FPROVVARTICOLO        float,
    FPROVVCLIENTE         float,
    PROVVISORIO           varchar,
    NREVISIONE            int,
    DATAREVISIONE         datetime,
    NREVORDINE            int,
    DATAREVORDINE         datetime,
    ESITOREVISIONE        varchar(40),
    USERNAMEMOD           varchar(20),
    DATAINSERIMENTO       datetime,
    RIFERIMENTO           varchar(80),
    LUOGOCONSEGNA         varchar(80),
    TELCONSEGNA           varchar(30),
    PROVCONSEGNA          varchar(2),
    DATAMODIFICA          datetime,
    USERNAME              varchar(20),
    LOCALITACONSEGNA      varchar(40),
    FAXCONSEGNA           varchar(25),
    MONTAGGIONSCURA       varchar,
    FLBLOCCOCONSEGNA      varchar,
    NOTEORD               text,
    NOTEORDPIEDE          text,
    CONFERMACLIENTE       varchar,
    FLAGTRASFERITO        varchar,
    TCOMMESSA             varchar(10),
    TDATACONFCONSEGNA     datetime,
    VETTORE               varchar(3),
    TARGA                 varchar(30),
    TARGARIMORCHIO        varchar(30),
    VETTORE2              varchar(3),
    CAUSALETRASP          varchar(3),
    NUMEROCOLLI           int,
    DATATRASPORTO         datetime,
    TOTPESO               float,
    INTESTDIVERSE         varchar(40),
    INDIRDIVERSE          varchar(30),
    LOCDIVERSE            varchar(35),
    CAPDIVERSE            varchar(6),
    PROVDIVERSE           varchar(2),
    REFERENTE             varchar(50),
    OGGETTO               text,
    CIG                   varchar(15),
    CUP                   varchar(15),
    PREFAZIONE            text,
    CONDIZIONI            text,
    PIEDEPAGINA           text,
    CONOSCENZA            text,
    RESPONSABILECOM       varchar(3),
    GRUPPOMEDIATORE       int,
    CONTOMEDIATORE        varchar(6),
    FSCONTI               varchar,
    FLSTAMPATO            varchar,
    CAMPOUSER             varchar(60),
    VALOREUSER            float,
    FLPALMARI             varchar,
    UTENTEPALMARE         varchar(20),
    FLINVIOEMAIL          varchar,
    TPROVENIENZA          varchar(3),
    TPID                  int,
    PRESTAZIONE655        int,
    FLFLUSSOETICHETTE     varchar,
    NUMORDINEPA           varchar(20),
    DATAORDINEPA          datetime,
    FLTIPODOCPA           varchar,
    CAUSALEFATTURA        varchar(200),
    TIPODATOGESTIONALEPA  varchar(10),
    ALTRIDATIGESTIONALIPA varchar(60),
    SYS_CREATEDATE        datetime,
    SYS_CREATEUSER        varchar(20),
    SYS_UPDATEDATE        datetime,
    SYS_UPDATEUSER        varchar(20),
    ID_ORDCLI             int identity,
    constraint ORDCLI00
        primary key (ANNO, SERIE, PROGRESSIVO)
)
    go

create table ORDCLI2
(
    ANNO             int        not null,
    SERIE            varchar(3) not null,
    PROGRESSIVO      int        not null,
    RIGO             int        not null,
    PROGRGENERALE    int,
    TIPORIGO         varchar(2),
    FARTICOLO        varchar(13),
    VARIANTE1        varchar(3),
    VARIANTE2        varchar(3),
    VARIANTE3        varchar(3),
    VARIANTE4        varchar(3),
    VARIANTE5        varchar(3),
    CODARTFORNITORE  varchar(25),
    FDESCRARTICOLO   varchar(50),
    CODICEEAN        varchar(60),
    DATACONFCONSEGNA datetime,
    DATARICHCONSEGNA datetime,
    QUANTITA         float,
    QUANTITAV        float,
    QUANTITA2        float,
    QTAOMAGGIO       float,
    PREZZO           float,
    FUNITAMISURA     varchar(2),
    FCOEFFICIENTE    float,
    COEFPREZZO       float,
    SCONTOARTICOLO   float,
    SCONTOC1         float,
    SCONTOC2         float,
    SCONTOP          float,
    PREZZOEXTRA      float,
    MAGAZZ           varchar(3),
    LOTTOMAGF        varchar(10),
    FCODICEIVA       varchar(3),
    FCENTROCOSTOR    varchar(10),
    IMPPROVVFISSO    float,
    FPROVVARTICOLO   float,
    FPROVVCLIENTE    float,
    FGRUPPORICAVO    int,
    FCONTORICAVO     varchar(6),
    SALDOACCONTO     varchar,
    FCOLLI           int,
    FCOMMESSA        varchar(10),
    FPALLET          float,
    PALLET           varchar(3),
    QTYUSER1         float,
    QTYUSER2         float,
    QTYUSER3         float,
    QTYUSER4         float,
    QTYUSER5         float,
    QTYUSER6         float,
    QTYUSER7         float,
    QTYUSER6I        int,
    QTYUSER7I        int,
    QTYUSER8I        int,
    QTYUSER9I        int,
    QTYUSER10I       int,
    DESCRUSER1       varchar(50),
    DESCRUSER2       varchar(50),
    DESCRUSER3       varchar(50),
    DESCRUSER4       varchar(50),
    DESCRUSER5       varchar(50),
    DESCRUSER6       varchar(50),
    DESCRUSER7       varchar(50),
    DATAUSER1        datetime,
    DATAUSER2        datetime,
    DATAUSER3        datetime,
    DATAUSER4        datetime,
    DATAUSER5        datetime,
    DATAUSER6        datetime,
    DATAUSER7        datetime,
    FPROVENIENZA     varchar(3),
    FPID             int,
    CONTROMARCA      varchar(32),
    NOTEORDCLI2      text,
    STATO            varchar,
    DATAMODIFICA     datetime,
    USERNAME         varchar(20),
    SYS_CREATEDATE   datetime,
    SYS_CREATEUSER   varchar(20),
    SYS_UPDATEDATE   datetime,
    SYS_UPDATEUSER   varchar(20),
    PID_ORDCLI       int,
    constraint ORDCLI200
        primary key (ANNO, SERIE, PROGRESSIVO, RIGO)
)
    go

create table ORDFOR
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
    constraint ORDFOR00
        primary key (ANNOOAF, SERIEOAF, PROGRESSIVOOAF)
)
    go

create table ORDFOR2
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
    constraint ORDFOR200
        primary key (ANNOOAF, SERIEOAF, PROGRESSIVOOAF, RIGO)
)
    go

create unique index ORDFOR2001
    on ORDFOR2 (PROGRGENERALE)
    go

create table PIANOCONTI
(
    GRUPPOCONTO          int        not null,
    SOTTOCONTO           varchar(6) not null,
    INTESTAZIONE         varchar(40),
    CONTINUAINTEST       varchar(40),
    TIPOCONTO            varchar,
    PARTITEAPERTE        varchar,
    DCENTROC             varchar(3),
    VALUTAC              varchar(3),
    DETTAGLIOCONTO       varchar,
    RIGOBILANCIO         int,
    FLAGTRASFERITO       varchar,
    DICHQUADROA          varchar(3),
    DICHQUADROE          varchar(3),
    DICHVENTILA          varchar,
    CLIFOR               varchar,
    FLBLOCCATO           varchar,
    DATASCADENZA         datetime,
    NUOVOGRUPPO          int,
    NUOVOCONTO           varchar(6),
    FLIVA                varchar,
    NOMENCLATURA         varchar(8),
    GRUPPOCOMP           varchar(6),
    FANALITICA           varchar,
    FCESPITE             varchar,
    FCOMMESSA            varchar,
    FLRATEI              varchar,
    LIVELLO              int,
    AZIENDAINTGR         varchar(20),
    GRUPPOINTGR          int,
    CONTOINTGR           varchar(6),
    INDIRIZZO            varchar(40),
    LOCALITA             varchar(35),
    CAP                  varchar(6),
    PROVINCIA            varchar(2),
    STATORESIDENZA       varchar(3),
    STATOESTERO          varchar(3),
    LATITUDINE           float,
    LONGITUDINE          float,
    TELEFONO             varchar(30),
    TELEFONONOTE         varchar(40),
    FAX                  varchar(25),
    CELLULARE            varchar(30),
    CELLULARENOTE        varchar(40),
    EMAIL                varchar(80),
    PEC                  varchar(80),
    INTERNET             varchar(80),
    REFERENTE            varchar(50),
    INTESTAZIONE2        varchar(40),
    CONTINUAINTEST2      varchar(40),
    INDIRIZZO2           varchar(50),
    LOCALITA2            varchar(35),
    CAP2                 varchar(6),
    PROVINCIA2           varchar(2),
    PARTITAIVA           varchar(16),
    CODICEFISCALE        varchar(16),
    CODICEUNIVOCOPA      varchar(7),
    RIFAMMINISTRAZIONEPA varchar(20),
    CAUSALEPA            varchar(100),
    FLFEORDINE           varchar,
    FERAPDENOMINAZIONE   varchar(50),
    FERAPIDCODICE        varchar(11),
    DATAINIZIOSPLIT      datetime,
    FLSPLIT              varchar,
    CODICEINDIR          varchar(6),
    DOGANA               varchar,
    CLASSE1              varchar(3),
    CLASSE2              varchar(3),
    CLASSE3              varchar(3),
    CLASSE4              varchar(3),
    CLASSE5              varchar(3),
    CLASSE6              varchar(3),
    CLASSE7              varchar(3),
    CLASSE8              varchar(3),
    CLASSE9              varchar(3),
    CLASSE10             varchar(3),
    TIPOLOGIACF          varchar,
    MESENOTRATTA1        int,
    MESENOTRATTA2        int,
    MESENOTRATTA3        int,
    GIORNOSCADENZA       int,
    DIVAPRIMASCAD        varchar,
    FLINVIOOPIVA         varchar,
    GIORNORIMANDO        int,
    DCODDIFFPAG          varchar(3),
    MAXAZIONE            varchar(3),
    DSCONTO1             float,
    DSCONTO2             float,
    DAGENTE              varchar(3),
    FLAGENTEOBBL         varchar,
    DSPESEBOLLO          varchar,
    DCODICEPAGAMENT      varchar(3),
    FLCODPAGOBBL         varchar,
    DBANCAAPPOGGIO       float,
    DLISTINO             varchar(3),
    FLLISTINOOBBL        varchar,
    FLLISTINOESC         varchar,
    DVETTORE             varchar(3),
    FLVETTOREOBBL        varchar,
    DMODOCONSEGNA        varchar(3),
    FLMODCONSOBBL        varchar,
    DTIPODOCUMENTO       varchar,
    DGRUPPOFATTURA       int,
    DCONTOFATTURA        varchar(6),
    RAGGRFATTURE         varchar,
    PERIODICITAFATT      varchar,
    REGIMEFISCALE        varchar(4),
    FLSCONTI             varchar,
    FLLOTTO              varchar,
    FLAGGIO              varchar,
    GGANTPROMO           int,
    NOTECLIENTI          text,
    NOTEFATTURA          text,
    NOTEFATTURAPIEDE     text,
    CHECKCONTFATT        varchar,
    TIPOCOMPENSO         varchar(3),
    CODFISPIGNORATO      varchar(16),
    COGNOME              varchar(30),
    NOME                 varchar(30),
    SESSO                varchar,
    DATANASCITA          datetime,
    LUOGONASCITA         varchar(35),
    PROVINCIANASCITA     varchar(2),
    CODFISCALEESTERO     varchar(30),
    FLSCHUMACKER         varchar,
    CATPARTICOLARI       varchar(2),
    DPROVVAGENTE         float,
    DPROVVCAPOAREA       float,
    DGIORNALE            varchar,
    FLGIORNALEOBBL       varchar,
    DCODICEIVA           varchar(3),
    CONTROMAGAZZINO      varchar(3),
    LINEATRASPORTO       varchar(5),
    SEQUENZACONS         int,
    MATRENASARCO         float,
    RUOLOAGENTE          int,
    DATAMANDATO          datetime,
    DVALUTA              varchar(3),
    DLINGUA              varchar(3),
    FIDOCLIENTE          int,
    FIDOASSICURATO       int,
    CODASSICURATO        varchar(10),
    PUNTIASSICURATO      int,
    DATAINIZIOASS        datetime,
    DATAFINEASS          datetime,
    GRUPPOCPARTITA       int,
    CONTOCPARTITA        varchar(6),
    BANCAPAG             varchar(3),
    CONTOCORRENTE        varchar(12),
    UTENZARID            varchar(16),
    DATARID              datetime,
    TIPOMANDATO          varchar(9),
    FREQUENZAINCASSO     varchar(4),
    CODICECIN            varchar,
    NAZIONEBANCA         varchar(2),
    CODICECINESTERO      int,
    SWIFT                varchar(11),
    BANCAESTERA          varchar(10),
    NUMCCESTERO          varchar(20),
    DVOCESPESA           varchar(10),
    ANNOINIZIORAPPORTO   int,
    DATAISCRCC           datetime,
    NUMISCRCC            varchar(11),
    CITTAISCRCC          varchar(25),
    INPSDITTA            varchar(10),
    INAILDITTA           varchar(8),
    CITTACASSAEDILE      varchar(25),
    POSIZIONECASSAEDILE  varchar(6),
    DATAPROTALBOF        datetime,
    NUMPROTALBOF         float,
    FILEALLEGATO         text,
    CIG                  varchar(15),
    CUP                  varchar(15),
    FLCIGOBBLIGATORIO    varchar,
    VALUSER1             float,
    VALUSER2             float,
    VALUSER3             float,
    VALUSER4             float,
    VALUSER5             float,
    CAMPOUSER1           varchar(30),
    CAMPOUSER2           varchar(30),
    CAMPOUSER3           varchar(30),
    CAMPOUSER4           varchar(30),
    CAMPOUSER5           varchar(30),
    APPOGGIO             varchar(150),
    PID_ANAGUNICO        int,
    FLVERIFICA           varchar,
    FLFITTIZIO           varchar,
    FLNOCF               varchar,
    CODICECRM            varchar(6),
    DATAMODIFICA         datetime,
    SYS_CREATEDATE       datetime,
    SYS_CREATEUSER       varchar(20),
    SYS_UPDATEDATE       datetime,
    SYS_UPDATEUSER       varchar(20),
    DSUFFCONTOTERZI      varchar(2),
    DATAFINESPLIT        datetime,
    GRUPPOPROMO          varchar(10),
    constraint PIANOCONTI00
        primary key (GRUPPOCONTO, SOTTOCONTO)
)
    go

create index PIANOCONTI01
    on PIANOCONTI (INTESTAZIONE)
    go

create index PIANOCONTI02
    on PIANOCONTI (CLIFOR)
    go

create index PIANOCONTI03
    on PIANOCONTI (PARTITAIVA, CODICEFISCALE)
    go

create index PIANOCONTI04
    on PIANOCONTI (PID_ANAGUNICO)
    go

create table PRIMANOTA
(
    ANNO             int     not null,
    GIORNALE         varchar not null,
    PROTOCOLLO       int     not null,
    PROGRPRIMANOTA   int     not null,
    PROGRGENERALE    int,
    DIVISIONE        varchar(5),
    ANNOPARTITA      int,
    NUMPARTITA       int,
    DATAOPERAZIONE   datetime,
    DATAMOVIMENTO    datetime,
    NUMERODOCUMENTO  varchar(15),
    CAUSALE          varchar(3),
    GRUPPOCONTO      int,
    SOTTOCONTO       varchar(6),
    DESCRSUPPL       varchar(25),
    IMPORTO          float,
    RIGOGIORNALE     int,
    CODICEPAGAMENTO  varchar(3),
    CODDIFFPAG       varchar(3),
    IVAPRIMASCAD     varchar,
    DATAPRIMASCAD    datetime,
    IMPORTOVE        float,
    DATAPRENOTATO    datetime,
    IMPPRENOTATO     float,
    DATAAUTORIZZA    datetime,
    USERAUTORIZZA    varchar(20),
    INIZIOPERIODO    datetime,
    FINEPERIODO      datetime,
    ONORARIO         float,
    SPESE            float,
    SPESENO770       float,
    TIPOCOMPENSO     varchar(3),
    RITENUTA         float,
    RITINPS          float,
    RITINAIL         float,
    RITENASARCO      float,
    ANNOENASARCO     int,
    TRIMENASARCO     int,
    FLAGTRASFERITO   varchar,
    NUMERORATA       int,
    NUMCONCILIAZIONE int,
    FLAGPROVVISORIA  varchar,
    FLAGFTBLOCCATA   varchar,
    VALUTA           varchar(3),
    CAMBIO           float,
    FLAGGENERICO     varchar,
    CONTROLLOBF      varchar,
    AGENTE           varchar(3),
    BANCAPAG         varchar(3),
    DATAVALUTA       datetime,
    CIG              varchar(15),
    CUP              varchar(15),
    VALUSER1         float,
    VALUSER2         float,
    VALUSER3         float,
    VALUSER4         float,
    VALUSER5         float,
    CAMPOUSER1       varchar(30),
    CAMPOUSER2       varchar(30),
    CAMPOUSER3       varchar(30),
    CAMPOUSER4       varchar(30),
    CAMPOUSER5       varchar(30),
    USERNAME         varchar(20),
    DATAMODIFICA     datetime,
    NOTEPRIMANOTA    text,
    PROVENIENZA      varchar,
    SYS_CREATEDATE   datetime,
    SYS_CREATEUSER   varchar(20),
    SYS_UPDATEDATE   datetime,
    SYS_UPDATEUSER   varchar(20),
    SPESEENASARCO    float,
    IBAN             varchar(40),
    ANNODIRITTO      float,
    PID              int,
    IVASP            float,
    SPESECOD8        float,
    constraint PRIMANOTA00
        primary key (ANNO, GIORNALE, PROTOCOLLO, PROGRPRIMANOTA)
)
    go

create unique index PRIMANOTA_PROGRGENERALE_uindex
    on PRIMANOTA (PROGRGENERALE)
    go

create unique index PRIMANOTA_PROGRGENERALE_PROVENIENZA_uindex
    on PRIMANOTA (PROGRGENERALE, PROVENIENZA)
    go

create table SALDIMAGAZZINO
(
    MARTICOLO      varchar(13) not null,
    VAR1           varchar(3)  not null,
    VAR2           varchar(3)  not null,
    VAR3           varchar(3)  not null,
    VAR4           varchar(3)  not null,
    VAR5           varchar(3)  not null,
    MMAGAZZINO     varchar(3)  not null,
    QTY            varchar     not null,
    QCARICHI       float,
    VCARICHI       float,
    QSCARICHI      float,
    VSCARICHI      float,
    QFISCALE       float,
    VFISCALE       float,
    QGIACENZA      float,
    SCORTAMIN      float,
    QCARICHIU      float,
    VCARICHIU      float,
    QSCARICHIU     float,
    VSCARICHIU     float,
    QFISCALEU      float,
    VFISCALEU      float,
    QGIACENZAU     float,
    VGIACENZAU     float,
    COSTOULTACQUU  float,
    DATAMODIFICA   datetime,
    SYS_CREATEDATE datetime,
    SYS_CREATEUSER varchar(20),
    SYS_UPDATEDATE datetime,
    SYS_UPDATEUSER varchar(20),
    constraint SALDIMAGAZZINO00
        primary key (MARTICOLO, VAR1, VAR2, VAR3, VAR4, VAR5, MMAGAZZINO, QTY)
)
    go

create table TCA1
(
    CODICE         varchar(3) not null
        constraint TCA100
            primary key,
    DESCRIZIONE    varchar(40),
    VALUSER        float,
    DESCRUSER      varchar(90),
    DESCRUSER2     varchar(90),
    DESCRUSER3     varchar(90),
    SYS_CREATEDATE datetime,
    SYS_CREATEUSER varchar(20),
    SYS_UPDATEDATE datetime,
    SYS_UPDATEUSER varchar(20)
)
    go

create table TCTC
(
    TIPOCESPITE     varchar(3) not null
        constraint TCTC00
            primary key,
    DESCRTIPOCESP   varchar(40),
    DESCRSUPPLCES   varchar(25),
    GRUPPOCESPITE   int,
    CONTOCESPITE    varchar(6),
    GRUPPOFONDO     int,
    CONTOFONDO      varchar(6),
    PCTPRIMOANNO    int,
    PCTAMMORD       float,
    GRUPPOAMM       int,
    CONTOAMM        varchar(6),
    PCTAMMANT       float,
    GRUPPOAMMANT    int,
    CONTOAMMANT     varchar(6),
    VOCESPESA       varchar(10),
    PCTPRIMOANNOCIV int,
    PCTAMMCIVILE    float,
    GRUPPOMINUSVAL  int,
    CONTOMINUSVAL   varchar(6),
    GRUPPOPLUSVAL   int,
    CONTOPLUSVAL    varchar(6),
    SYS_CREATEDATE  datetime,
    SYS_CREATEUSER  varchar(20),
    SYS_UPDATEDATE  datetime,
    SYS_UPDATEUSER  varchar(20)
)
    go

create table TEBA
(
    BANCAPRES        varchar(3) not null
        constraint TEBA00
            primary key,
    DESCRBANCA2      varchar(40),
    SIGLA            varchar(20),
    GRUPPOCC         int,
    CONTOCC          varchar(6),
    FIDOCC           float,
    GRUPPOSBF        int,
    CONTOSBF         varchar(6),
    FIDOCAST         float,
    GRUPPOSCONTO     int,
    CONTOSCONTO      varchar(6),
    GRUPPODOPOINC    int,
    CONTODOPOINC     varchar(6),
    GRUPPOCANTICIPO  int,
    CONTOCANTICIPO   varchar(6),
    FIDOCANTICIPO    float,
    GRUPPOINVIO      int,
    CONTOINVIO       varchar(6),
    GRUPPOLIQ        int,
    CONTOLIQ         varchar(6),
    GRUPPOPOS        int,
    CONTOPOS         varchar(6),
    PCTSCONTO        float,
    CMS              float,
    NAZIONEBANCA     varchar(2),
    CODICECINESTERO  int,
    CODICECIN        varchar,
    ABIBANCA         float,
    NUMEROCC         varchar(12),
    SWIFT            varchar(11),
    IBANESTERO       varchar(40),
    GGBANCA          int,
    PERACCREDITO     varchar,
    SPESAOP          float,
    SPESATEN         float,
    FLCALCINTDARE    varchar,
    FLCALCINTAVERE   varchar,
    RITACCONTO       float,
    DATAMAXSC        datetime,
    IMPMAXSC         float,
    NUMERO           int,
    CODTESORERIA     varchar(20),
    NOMEFILERIBA     varchar(100),
    FLREGISTRAZIONE  varchar,
    SPESEBONIFICO    float,
    FILEBONIFICO     varchar(150),
    FILERID          varchar(150),
    FILEANTFAT       varchar(150),
    FLBONIFICOUNICO  varchar,
    TIPOFILEBONIFICI varchar,
    GRUPPOADI        int,
    CONTOADI         varchar(6),
    FIDOADI          float,
    GRUPPOADE        int,
    CONTOADE         varchar(6),
    FIDOADE          float,
    SALDOINI         float,
    DATASALDO        datetime,
    SYS_CREATEDATE   datetime,
    SYS_CREATEUSER   varchar(20),
    SYS_UPDATEDATE   datetime,
    SYS_UPDATEUSER   varchar(20)
)
    go

create table TGCP
(
    CODICEPAGAMENTO    varchar(3) not null
        constraint TGCP00
            primary key,
    DESCRCP            varchar(40),
    DESCRCPESTESA      varchar(200),
    TIPOPAGAMENTO      varchar,
    FLAGIRR            varchar,
    PARTENZAPAGAM      varchar(2),
    GIORNOSCADENZA     int,
    GGDIFFERIMENTOPART int,
    GRUPPOCASSA        int,
    CONTOCASSA         varchar(6),
    SCONTOCASSA        float,
    SPESEINCASSO       float,
    FIDOPAGAMENTO      int,
    TIPOEFFETTO        varchar,
    FLAGEFFRIEP        varchar,
    FLCOMPENSARESIDUO  varchar,
    CODPAGPA           varchar(4),
    TESTOPAGAMENTO     varchar(150),
    GIORNIMESI         varchar,
    RATA1              int,
    RATA2              int,
    RATA3              int,
    RATA4              int,
    RATA5              int,
    RATA6              int,
    RATA7              int,
    RATA8              int,
    RATA9              int,
    NUMERORATE         int,
    GGPA               int,
    LIVELLO            int,
    VALUSER            float,
    DESCRUSER          varchar(20),
    MESSAGGIO          varchar(200),
    INCASSOOBBL        varchar,
    SCONTOPAGCONSENGA  float,
    SYS_CREATEDATE     datetime,
    SYS_CREATEUSER     varchar(20),
    SYS_UPDATEDATE     datetime,
    SYS_UPDATEUSER     varchar(20)
)
    go

