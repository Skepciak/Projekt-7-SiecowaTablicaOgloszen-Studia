package pl.tablicaogloszen.wspolne;

import java.io.Serializable;

/**
 * Obiekt żądania przesyłany od klienta do serwera.
 * Enkapsuluje typ operacji i dane potrzebne do jej wykonania.
 * 
 * @author System
 * @version 1.0
 */
public class Zadanie implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Typ żądania określający operację do wykonania */
    private TypZadania typ;

    /** Dane związane z żądaniem (np. OgloszenieDTO, FiltrDTO, String) */
    private Object dane;

    /**
     * Tworzy nowe żądanie.
     * 
     * @param typ  Typ operacji do wykonania
     * @param dane Dane potrzebne do wykonania operacji
     */
    public Zadanie(TypZadania typ, Object dane) {
        this.typ = typ;
        this.dane = dane;
    }

    /** @return Typ żądania */
    public TypZadania getTyp() {
        return typ;
    }

    /** @return Dane żądania */
    public Object getDane() {
        return dane;
    }
}
