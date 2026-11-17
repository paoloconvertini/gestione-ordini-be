package it.calolenoci.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Il fatturato effettivo copre le soglie cumulative, senza richiedere fatture di uguale percentuale. */
public final class RiscontroAcconti {
    private RiscontroAcconti() {}
    public record Previsto(int id, BigDecimal importoCumulato) {}
    public record Emesso(String riferimento, BigDecimal importo) {}
    public record Esito(boolean coperto) {}

    public static BigDecimal euro(BigDecimal valore) {
        return valore.setScale(2, RoundingMode.HALF_UP);
    }

    public static Map<Integer, Esito> verifica(List<Previsto> previsti, List<Emesso> emessi) {
        BigDecimal fatturato = euro(emessi.stream().map(Emesso::importo).reduce(BigDecimal.ZERO,BigDecimal::add));
        Map<Integer, Esito> risultato = new HashMap<>();
        for (Previsto p : previsti)
            risultato.put(p.id(),new Esito(p.importoCumulato().signum() >= 0
                    && fatturato.compareTo(euro(p.importoCumulato())) >= 0));
        return risultato;
    }
}
