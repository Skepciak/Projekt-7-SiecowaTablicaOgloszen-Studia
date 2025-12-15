package pl.tablicaogloszen.wspolne;

/**
 * Status odpowiedzi serwera.
 * Określa wynik wykonania operacji.
 * 
 * @author System
 * @version 1.0
 */
public enum StatusOdpowiedzi {
    /** Operacja zakończona sukcesem */
    OK,

    /** Wystąpił błąd podczas wykonywania operacji */
    BLAD,

    /** Powiadomienie push - klient powinien odświeżyć dane */
    ODSWIEZ
}
