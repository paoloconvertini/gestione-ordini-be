create table GO_BOX_DOCCIA
(
    id          varchar(36)          not null,
    codice        varchar(10)          not null,
    descrizione varchar(500)          null,
    profilo       varchar(200)   null,
    estensibilita        varchar(50)          null,
    versione      varchar(20)  not null,
    qta        int null,
    prezzo        float null,
    extra varchar(200) null,
    foto varchar(500) null,
    posa varchar(500) null,
    venduto char default 0
    constraint GO_BOX_DOCCIA_pk
        primary key (id)
    );

create unique index GO_BOX_DOCCIA_id_uindex
    on GO_BOX_DOCCIA (id);
create index GO_BOX_DOCCIA_codice_uindex
    on GO_BOX_DOCCIA (codice);
create index GO_BOX_DOCCIA_desc_uindex
    on GO_BOX_DOCCIA (descrizione);