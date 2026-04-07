CREATE TABLE [dbo].[GO_CESPITE](
                                   [ID] [varchar](36) NOT NULL,
                                   [TIPO_CESPITE] [varchar](3) NOT NULL,
                                   [PROGRESSIVO1] [int] NULL,
                                   [PROGRESSIVO2] [int] NULL,
                                   [CESPITE] [varchar](500) NULL,
                                   [DATA_ACQ] [datetime] NULL,
                                   [NUM_DOC_ACQ] [varchar](20) NULL,
                                   [FORNITORE] [varchar](100) NULL,
                                   [IMPORTO] [float] NULL,
                                   [IMPORTO_RIVALUTAZIONE] [float] NULL,
                                   [ATTIVO] [char](1) NOT NULL,
                                   [DATA_VEND] [datetime] NULL,
                                   [NUM_DOC_VEND] [varchar](20) NULL,
                                   [INTESTATARIO_VEND] [varchar](250) NULL,
                                   [IMPORTO_VEND] [float] NULL,
                                   [SUPER_AMMORTAMENTO] [int] NULL,
                                   [PROTOCOLLO] [int] NULL,
                                   [GIORNALE] [varchar](1) NULL,
                                   [ANNO] [int] NULL,
                                   [DT_INIZIO_CALCOLO_AMM] [datetime] NULL,
                                   [FL_PRIMO_ANNO] [varchar](1) NULL,
                                   [FONDO_RIVALUTAZIONE] [float] NULL,
                                   CONSTRAINT [GO_CESPITE_PK] PRIMARY KEY CLUSTERED
                                       (
                                        [ID] ASC
                                           )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[GO_CESPITE] ADD  DEFAULT ('T') FOR [ATTIVO]
GO

create unique index GO_CESPITE_idx_1
    on GO_CESPITE (TIPO_CESPITE, PROGRESSIVO1, PROGRESSIVO2);


create index GO_CESPITE_idx_forn ON GO_CESPITE (FORNITORE);
create index GO_CESPITE_idx_cesp ON GO_CESPITE (CESPITE);