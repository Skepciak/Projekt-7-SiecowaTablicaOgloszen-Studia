package pl.tablicaogloszen.wspolne;

import java.io.Serializable;

/**
 * Obiekt odpowiedzi przesyłany z serwera do klienta.
 * Zawiera status operacji, dane wynikowe i opcjonalny komunikat.
 * 
 * @author System
 * @version 1.0
 */
public class Odpowiedz implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Status wykonania operacji */
    private StatusOdpowiedzi status;

    /** Dane wynikowe (np. lista ogłoszeń, UzytkownikDTO) */
    private Object dane;

    /** Komunikat tekstowy dla użytkownika */
    private String wiadomosc;

    /**
     * Tworzy nową odpowiedź.
     * 
     * @param status    Status wykonania operacji
     * @param dane      Dane wynikowe (może być null)
     * @param wiadomosc Komunikat dla użytkownika
     */
    public Odpowiedz(StatusOdpowiedzi status, Object dane, String wiadomosc) {
        this.status = status;
        this.dane = dane;
        this.wiadomosc = wiadomosc;
    }

    /** @return Status odpowiedzi */
    public StatusOdpowiedzi getStatus() {
        return status;
    }

    /** @return Dane wynikowe */
    public Object getDane() {
        return dane;
    }

    /** @return Komunikat tekstowy */
    public String getWiadomosc() {
        return wiadomosc;
    }
}
