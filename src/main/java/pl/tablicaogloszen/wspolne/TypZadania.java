package pl.tablicaogloszen.wspolne;

/**
 * Typ żądania wysyłanego od klienta do serwera.
 * Definiuje rodzaj operacji do wykonania.
 */
public enum TypZadania {
    /** Logowanie użytkownika */
    LOGOWANIE,
    /** Rejestracja nowego użytkownika */
    REJESTRACJA,
    /** Pobranie wszystkich ogłoszeń */
    POBIERZ_OGLOSZENIA,
    /** Pobranie ogłoszeń z filtrowaniem */
    POBIERZ_OGLOSZENIA_FILTR,
    /** Dodanie nowego ogłoszenia */
    DODAJ_OGLOSZENIE,
    /** Edycja istniejącego ogłoszenia */
    EDYTUJ_OGLOSZENIE,
    /** Usunięcie ogłoszenia */
    USUN_OGLOSZENIE,
    /** Pobranie kategorii */
    POBIERZ_KATEGORIE,
    /** Żądanie odświeżenia (push) */
    ODSWIEZ
}
