package it.calolenoci.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Pattern;

/** Regole pure: le righe Plurima restano la fonte delle condizioni di pagamento. */
public final class RegolePagamento {
    private RegolePagamento() {}
    private static final Locale IT = Locale.ITALIAN;
    private static final Pattern MESE = Pattern.compile(
            "(?iu)\\b(GENNAIO|FEBBRAIO|MARZO|APRILE|MAGGIO|GIUGNO|LUGLIO|AGOSTO|SETTEMBRE|OTTOBRE|NOVEMBRE|DICEMBRE)(?:\\s+\\d{4})?\\b");

    public record Riga(int id, int rigo, String articolo, String descrizione,
                       BigDecimal percentuale, LocalDate data) {
        public boolean acconto() { return "*ACCONTO".equals(articolo); }
    }

    public static boolean dataPredefinita(Riga riga, LocalDate conferma) {
        return riga.data() == null || Objects.equals(riga.data(), conferma);
    }

    public static LocalDate scadenza(Riga riga, LocalDate conferma) {
        if (dataPredefinita(riga, conferma)) return riga.acconto() ? conferma : null;
        return riga.data().withDayOfMonth(1);
    }

    public static Map<Integer, String> anomalie(List<Riga> righe, LocalDate conferma) {
        Map<Integer, List<String>> errori = new LinkedHashMap<>();
        righe.forEach(r -> errori.put(r.id(), new ArrayList<>()));
        long acconti = righe.stream().filter(Riga::acconto).count();
        long saldi = righe.size() - acconti;
        if (conferma == null) aggiungiTutti(errori, "Data conferma ordine mancante");
        if (acconti > 3) aggiungiTutti(errori, "Sono previsti al massimo tre acconti");
        if (saldi != 1) aggiungiTutti(errori, "È prevista una sola riga *SALDO");
        BigDecimal totale = BigDecimal.ZERO;
        boolean percentualiValide = true;
        Set<LocalDate> date = new HashSet<>();
        for (Riga r : righe) {
            if (r.percentuale() == null || (r.percentuale().signum() < 0 || (r.acconto() && r.percentuale().signum() == 0)) || r.percentuale().compareTo(BigDecimal.valueOf(100)) > 0) {
                errori.get(r.id()).add("Percentuale mancante o non compresa tra 0 e 100");
                percentualiValide = false;
            } else totale = totale.add(r.percentuale());
            if (r.acconto() && acconti > 1) {
                if (dataPredefinita(r, conferma))
                    errori.get(r.id()).add("Più acconti: impostare una data diversa dalla conferma ordine");
                else if (!date.add(r.data())) {
                    righe.stream().filter(x -> x.acconto() && Objects.equals(x.data(), r.data()))
                            .forEach(x -> errori.get(x.id()).add("Più acconti con la stessa data"));
                }
            }
        }
        if (percentualiValide && totale.compareTo(BigDecimal.valueOf(100)) != 0)
            aggiungiTutti(errori, "La somma delle percentuali acconti e saldo deve essere 100");
        Map<Integer, String> risultato = new LinkedHashMap<>();
        errori.forEach((id, messaggi) -> risultato.put(id, String.join("; ", new LinkedHashSet<>(messaggi))));
        return risultato;
    }

    private static void aggiungiTutti(Map<Integer, List<String>> errori, String messaggio) {
        errori.values().forEach(e -> e.add(messaggio));
    }

    public static String senzaMese(String descrizione) {
        return MESE.matcher(Objects.toString(descrizione, "")).replaceFirst("").replaceAll(" {2,}", " ").stripTrailing();
    }

    public static String descrizione(Riga riga, LocalDate conferma, int annoCorrente) {
        String originale = Objects.toString(riga.descrizione(), "").stripTrailing();
        if (!riga.acconto()) return originale;
        if (dataPredefinita(riga, conferma))
            return originale;
        String mese = riga.data().getMonth().getDisplayName(TextStyle.FULL, IT).toUpperCase(IT);
        if (riga.data().getYear() != annoCorrente) mese += " " + riga.data().getYear();
        var matcher = MESE.matcher(originale);
        if (matcher.find()) return matcher.replaceFirst(mese);
        return originale.isEmpty() ? mese : originale + " " + mese;
    }
}
