package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "ARTICOLI_TAB")
@Getter
@Setter
public class Articolo extends PanacheEntityBase {

    @Id
    @Column(length = 13)
    private String articolo;

    @Column(length = 50)
    private String descrArticolo;

    @Column(length = 40)
    private String descrArtSuppl;

    @Column(length = 5000, columnDefinition = "text")
    private String descrEstesa;

    @Column(name = "ORDINAMENTO")
    private String ordinamento;

    @Column(length = 2)
    private String unitaMisura;

    @Column(name = "UNITAMISURASEC")
    private String unitaMisuraSec;

    @Column(name = "COEFFICIENTE")
    private Double coefficiente;

    @Column(name = "UNITAMISURA2")
    private String unitaMisura2;

    @Column(name = "UNITAMISURAPRO")
    private String unitaMisuraPro;

    @Column(name = "COEFFICIENTEPRO")
    private Double coefficientePro;

    @Column
    private Float costoBase;

    @Column(name = "COSTOLAVORO")
    private Double costoLavoro;

    @Column
    private Double prezzoBase;

    @Column(name = "PREZZOUMSEC")
    private String prezzoUmSec;

    @Column(name = "PREZZOEXTRA")
    private Double prezzoExtra;

    @Column(name = "SCONTOBASE")
    private Double scontoBase;

    @Column(name = "CODICEIVA")
    private String codiceIva;

    @Column(name = "CODDECIMALIPREZZO")
    private String codDecimaliPrezzo;

    @Column(name = "PROVVAGENTE")
    private Double provvAgente;

    @Column(name = "CALCOLAPROVV")
    private String calcolaProvv;

    @Column(name = "GRUPPOVENDITE")
    private Integer gruppoVendite;

    @Column(name = "CONTOVENDITE")
    private String contoVendite;

    @Column(length = 3)
    private String classeA1;

    @Column(name = "CLASSEA2")
    private String classea2;

    @Column(name = "CLASSEA3")
    private String classea3;

    @Column(name = "CLASSEA4")
    private String classea4;

    @Column(name = "CLASSEA5")
    private String classea5;

    @Column(name = "CLASSEA6")
    private String classea6;

    @Column(name = "CLASSEA7")
    private String classea7;

    @Column(name = "CLASSEA8")
    private String classea8;

    @Column(name = "CLASSEA9")
    private String classea9;

    @Column(name = "CLASSEA10")
    private String classea10;

    @Column(name = "FLAGLISTINO")
    private String flagListino;

    @Column(name = "GRUPPOACQUISTI")
    private Integer gruppoAcquisti;

    @Column(name = "CONTOACQUISTI")
    private String contoAquisti;

    @Column(name = "QUANTITAUSER01")
    private Double quantitaUser01;

    @Column(name = "QUANTITAUSER02")
    private Double quantitaUser02;

    @Column(name = "QUANTITAUSER03")
    private Double quantitaUser03;

    @Column(name = "QUANTITAUSER04")
    private Double quantitaUser04;

    @Column(name = "QUANTITAUSER05")
    private Double quantitaUser05;

    @Column(name = "CAMPOUSER1")
    private String campoUser1;

    @Column(name = "CAMPOUSER2")
    private String campoUser2;

    @Column(name = "CAMPOUSER3")
    private String campoUser3;

    @Column(name = "CAMPOUSER4")
    private String campoUser4;

    @Column(name = "CAMPOUSER5")
    private String campoUser5;

    @Column(name = "ARTICOLORAGGR")
    private String articoloRaggr;

    @Column(name = "RIFORIGINALE")
    private String rifOriginale;

    @Column(name = "NOTEARTICOLO")
    private String noteArticolo;

    @Column(name = "NOMENCLATURA")
    private String nomenclatura;

    @Column(name = "IVAAGEVOLATA")
    private String ivaAgevolata;

    @Column(name = "TIPODOCUMENTOFE")
    private String tipoDocumentoFe;

    @Column(name = "PESO")
    private Double peso;

    @Column(name = "PESONETTO")
    private Double pesoNetto;

    @Column(name = "QTAPERCONF")
    private Double qtaPerConf;

    @Column(name = "DIMPERCONF")
    private String dimPerConf;

    @Column(name = "PESOPERCONF")
    private Double pesoPerConf;

    @Column(name = "QTABUSTA")
    private Double qtaBusta;

    @Column(name = "PESOBUSTA")
    private Double pesoBusta;

    @Column(name = "PALLET")
    private String pallet;

    @Column(name = "QTAPALLET")
    private Double qtaPallet;

    @Column(name = "GESTIONESCORTA")
    private String gestioneScorta;

    @Column(name = "UBICAZIONE")
    private String ubicazione;

    @Column(name = "DEPOSITOREPARTO")
    private String depositoReparto;

    @Column(name = "QTAREPARTO")
    private Double qtaReparto;

    @Column(name = "LOTTOMINIMO")
    private Double lottoMinimo;

    @Column(name = "QTALOTTO")
    private Double qtaLotto;

    @Column(name = "PUNTORIORDINO")
    private Double puntoRiordino;

    @Column(name = "SCORTAMINIMA")
    private Double scortaMinima;

    @Column(name = "GGAPPROVVIG")
    private Integer ggaPProvvig;

    @Column(name = "SCORTAREPARTO")
    private Double scortaReparto;

    @Column(name = "QTAMINIMAFATT")
    private Double qtaMinimaFatt;

    @Column(name = "LOTTOMINIMOFATT")
    private Integer lottoMinimoFatt;

    @Column(name = "QUALITA")
    private String qualita;

    @Column(name = "MODULOETK")
    private String moduloetk;

    @Column(name = "TEMPOPROD")
    private Double tempoProd;

    @Column(name = "ARTDISTINTABASE")
    private String artDistintaBase;

    @Column(name = "ARTICOLOC")
    private String articoLoc;

    @Column(name = "VARIANTE1")
    private String variante1;

    @Column(name = "VARIANTE2")
    private String variante2;

    @Column(name = "VARIANTE3")
    private String variante3;

    @Column(name = "VARIANTE4")
    private String variante4;

    @Column(name = "VARIANTE5")
    private String variante5;

    @Column(name = "AGGRV1")
    private String aggrv1;

    @Column(name = "AGGRV2")
    private String aggrv2;

    @Column(name = "AGGRV3")
    private String aggrv3;

    @Column(name = "AGGRV4")
    private String aggrv4;

    @Column(name = "AGGRV5")
    private String aggrv5;

    @Column(name = "ARTICOLOVUOTO")
    private String articoloVuoto;

    @Column(name = "QTYVUOTI")
    private Double qtyVuoti;

    @Column(name = "TIPORIGOGRUPPI")
    private String tipoRigoGruppi;

    @Column(name = "CESPITE")
    private String cespite;

    @Column(name = "BLOCCATO")
    private String bloccato;

    @Column(name = "PROGR1")
    private Integer progr1;

    @Column(name = "PROGR2")
    private Integer progr2;

    @Column(name = "QTARIF")
    private Double qtaRif;

    @Column(name = "TIPOARTICOLO")
    private String tipoArticolo;

    @Column(name = "CAUSALEINEVASO")
    private String causaleInevaso;

    @Column(name = "SERVIZIO")
    private Double servizio;

    @Column(name = "FLGIORNIMESISCAD")
    private String flGiorniMesiScad;

    @Column(name = "PERIODOSCADENZA")
    private Integer periodoScadenza;

    @Column(name = "FLNUMEROSERIE")
    private String flNumeroSerie;

    @Column(name = "FLASSORTIMENTO")
    private String flAssortimento;

    @Column(name = "FLCODICEEAN")
    private String flCodiceEan;

    @Column(name = "FLPALLET")
    private String flPallet;

    @Column(length = 1, name = "FLTRATTATO")
    private String flTrattato;

    @Column(name = "FLAGTRASFERITO")
    private String flagTrasferito;

    @Column(name = "FLFABBRICAZIONE")
    private String flFabbricazione;

    @Column(name = "IMMAGINE")
    private String immagine;

    @Column(name = "LINKSCHEDA")
    private String linkScheda;

    @Column(name = "LINK1")
    private String link1;

    @Column(name = "LINK2")
    private String link2;

    @Column(name = "LINK3")
    private String link3;

    @Column(name = "PUBBLICAZIONE")
    private Integer pubblicazione;

    @Column(name = "FLB2B")
    private String flB2B;

    @Column(name = "FLB2C")
    private String flB2C;

    @Column(name = "NOTECATALOGO")
    private String noteCatalogo;

    @Column(name = "APPOGGIO")
    private String appoggio;

    @Column(name = "OMAGGIABILE")
    private String omaggiabile;

    @Column(name = "RENDIBILE")
    private String rendibile;

    @Column(name = "GRUPPOFORNITORE")
    private Integer gruppoFornitore;

    @Column(name = "CONTOFORNITORE")
    private String contoFornitore;

    @Column(name = "DOCUMENTO")
    private String documento;

    @Column(name = "SYS_CREATEDATE")
    private Date createDate;

    @Column(name = "SYS_UPDATEDATE")
    private Date updateDate;

    @Column(name = "SYS_CREATEUSER")
    private String createUser;

    @Column(name = "SYS_UPDATEUSER")
    private String updateUser;

    @Column(length = 1, name = "FLLOTTO")
    private String flagLotto;

    @Column(length = 1, name = "FLSCONTI")
    private String flagSconti;

    @Column(name = "FLCOEFFTEORICO")
    public String flCoeffTeorico;

    @Column(name = "FLUM2PRODUZIONE")
    public String flUm2Produzione;

    @Column(name = "FLUM2VENDITA")
    public String flUm2Vendita;

    @Column(name = "FLUMSECACQUISTI")
    public String flUmSecAcquisti;

    @Column(name = "FLUMSECVENDITA")
    public String flUmSecVendita;

    @Column(name = "PROVVCAPOAREA")
    public Float provvCapoArea;

    @Column(name = "DATAMODIFICA")
    public LocalDateTime dataModifica;

    @Column(name = "USERNAME", length = 20)
    public String username;

    @Column(name = "FLCONF", length = 1)
    public String flConf;

    @Column(name = "DESCRBREVE", length = 20)
    public String descrBreve;

    @Column(name = "COLLISTRATO")
    public Integer colliStrato;

}
