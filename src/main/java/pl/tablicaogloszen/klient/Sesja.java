package pl.tablicaogloszen.klient;

import pl.tablicaogloszen.wspolne.UzytkownikDTO;

/**
 * Klasa przechowująca stan sesji użytkownika.
 * Singleton-like static holder dla danych zalogowanego użytkownika.
 * 
 * @author System
 * @version 1.0
 */
public class Sesja {
    /** Aktualnie zalogowany użytkownik */
    private static UzytkownikDTO zalogowanyUzytkownik;

    /**
     * Zwraca zalogowanego użytkownika.
     * 
     * @return DTO zalogowanego użytkownika lub null jeśli niezalogowany
     */
    public static UzytkownikDTO getZalogowanyUzytkownik() {
        return zalogowanyUzytkownik;
    }

    /**
     * Ustawia zalogowanego użytkownika.
     * Wywoływane po pomyślnym logowaniu.
     * 
     * @param uzytkownik DTO zalogowanego użytkownika
     */
    public static void setZalogowanyUzytkownik(UzytkownikDTO uzytkownik) {
        zalogowanyUzytkownik = uzytkownik;
    }

    /**
     * Wylogowuje użytkownika (czyści sesję).
     */
    public static void wyloguj() {
        zalogowanyUzytkownik = null;
    }

    /**
     * Sprawdza czy użytkownik jest zalogowany.
     * 
     * @return true jeśli użytkownik jest zalogowany
     */
    public static boolean czyZalogowany() {
        return zalogowanyUzytkownik != null;
    }

    /**
     * Sprawdza czy zalogowany użytkownik jest administratorem.
     * 
     * @return true jeśli użytkownik jest adminem
     */
    public static boolean czyAdmin() {
        return zalogowanyUzytkownik != null && "ADMIN".equals(zalogowanyUzytkownik.getRola());
    }
}
