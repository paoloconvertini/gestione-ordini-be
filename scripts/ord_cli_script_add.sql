ALTER TABLE ORDCLI ADD

    GE_STATUS varchar(50) NULL,

    GE_WARN_NO_BOLLA char(1) default 0,

    GE_LOCKED char(1) default 0,

    GE_USER_LOCK varchar(100);

