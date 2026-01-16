package pl.tablicaogloszen.serwer;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import pl.tablicaogloszen.wspolne.*;

/**
 * Wątek obsługujący jednego klienta.
 * Każdy klient który się połączy dostaje swój wątek tej klasy.
 * 
 * W metodzie run() jest pętla która odbiera żądania od klienta
 * i wywołuje odpowiednie metody DAO (logowanie, dodawanie ogłoszeń itp.)
 * 
 * @author Dawid Sułek, Dominik Rodziewicz
 */
public class ObslugaKlienta implements Runnable {
    private Socket gniazdo;
    private ObjectOutputStream wyjscie;
    private ObjectInputStream wejscie;
    private UzytkownikDTO zalogowanyUzytkownik;

    public ObslugaKlienta(Socket gniazdo) {
        this.gniazdo = gniazdo;
    }

    @Override
    public void run() {
        try {
            wyjscie = new ObjectOutputStream(gniazdo.getOutputStream());
            wejscie = new ObjectInputStream(gniazdo.getInputStream());

            while (true) {
                try {
                    Object obiekt = wejscie.readObject();
                    if (obiekt instanceof Zadanie) {
                        obslozZadanie((Zadanie) obiekt);
                    }
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // Error
        } finally {
            Serwer.usunKlienta(this);
            zamknijPolaczenie();
        }
    }

    private void obslozZadanie(Zadanie zadanie) throws IOException {
        UzytkownikDAO uzytkownikDAO = new UzytkownikDAO();
        OgloszenieDAO ogloszenieDAO = new OgloszenieDAO();

        try {
            switch (zadanie.getTyp()) {
                case LOGOWANIE: {
                    String[] dane = ((String) zadanie.getDane()).split(":", 2);
                    UzytkownikDTO uzytkownik = uzytkownikDAO.zaloguj(dane[0], dane[1]);
                    if (uzytkownik != null) {
                        zalogowanyUzytkownik = uzytkownik;
                        wyslij(new Odpowiedz(StatusOdpowiedzi.OK, uzytkownik, "Zalogowano pomyślnie."));
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błędny login lub hasło."));
                    }
                    break;
                }
                case REJESTRACJA: {
                    String[] dane = ((String) zadanie.getDane()).split(":", 2);
                    boolean utworzono = uzytkownikDAO.zarejestruj(dane[0], dane[1]);
                    if (utworzono) {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Konto utworzone."));
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd rejestracji. Login zajęty?"));
                    }
                    break;
                }
                case POBIERZ_OGLOSZENIA: {
                    wyslij(new Odpowiedz(StatusOdpowiedzi.OK, ogloszenieDAO.pobierzWszystkie(), "Pobrano ogłoszenia."));
                    break;
                }
                case POBIERZ_OGLOSZENIA_FILTR: {
                    FiltrDTO filtr = (FiltrDTO) zadanie.getDane();
                    wyslij(new Odpowiedz(StatusOdpowiedzi.OK, ogloszenieDAO.pobierzZFiltrem(filtr),
                            "Pobrano ogłoszenia."));
                    break;
                }
                case POBIERZ_KATEGORIE: {
                    wyslij(new Odpowiedz(StatusOdpowiedzi.OK, ogloszenieDAO.pobierzKategorie(), "Pobrano kategorie."));
                    break;
                }
                case DODAJ_OGLOSZENIE: {
                    if (zalogowanyUzytkownik != null) {
                        OgloszenieDTO nowe = (OgloszenieDTO) zadanie.getDane();
                        boolean dodano = ogloszenieDAO.dodajOgloszenie(nowe, zalogowanyUzytkownik.getId());
                        if (dodano) {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Ogłoszenie dodane."));
                            Serwer.powiadomWszystkich();
                        } else {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd dodawania ogłoszenia."));
                        }
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Musisz być zalogowany."));
                    }
                    break;
                }
                case EDYTUJ_OGLOSZENIE: {
                    if (zalogowanyUzytkownik != null) {
                        OgloszenieDTO ed = (OgloszenieDTO) zadanie.getDane();
                        boolean admin = "ADMIN".equals(zalogowanyUzytkownik.getRola());
                        boolean ok = ogloszenieDAO.edytujOgloszenie(ed, zalogowanyUzytkownik.getId(), admin);
                        if (ok) {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Zaktualizowano."));
                            Serwer.powiadomWszystkich();
                        } else {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd edycji (brak uprawnień?)."));
                        }
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Musisz być zalogowany."));
                    }
                    break;
                }
                case USUN_OGLOSZENIE: {
                    if (zalogowanyUzytkownik != null) {
                        int id = (Integer) zadanie.getDane();
                        boolean admin = "ADMIN".equals(zalogowanyUzytkownik.getRola());
                        boolean ok = ogloszenieDAO.usunOgloszenie(id, zalogowanyUzytkownik.getId(), admin);
                        if (ok) {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Usunięto."));
                            Serwer.powiadomWszystkich();
                        } else {
                            wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd usuwania (brak uprawnień?)."));
                        }
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Musisz być zalogowany."));
                    }
                    break;
                }
                case ODSWIEZ: {
                    wyslij(new Odpowiedz(StatusOdpowiedzi.OK, ogloszenieDAO.pobierzWszystkie(), "Odświeżono."));
                    break;
                }
                case ZGLOS_OGLOSZENIE: {
                    if (zalogowanyUzytkownik != null) {
                        int id = (Integer) zadanie.getDane();
                        ogloszenieDAO.zglosOgloszenie(id);
                        wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Zgłoszono ogłoszenie."));
                        Serwer.powiadomWszystkich();
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Musisz być zalogowany."));
                    }
                    break;
                }
                case POBIERZ_SZCZEGOLY: {
                    int id = (Integer) zadanie.getDane();
                    ogloszenieDAO.zwiekszWyswietlenia(id);
                    // Zwracamy odświeżoną listę (klient pobierze szczegóły sam)
                    wyslij(new Odpowiedz(StatusOdpowiedzi.OK, null, "Wyświetlenie zarejestrowane."));
                    break;
                }
                case POBIERZ_ZGLOSZONE: {
                    if (zalogowanyUzytkownik != null && "ADMIN".equals(zalogowanyUzytkownik.getRola())) {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.OK, ogloszenieDAO.pobierzZgloszone(),
                                "Zgłoszone ogłoszenia."));
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Brak uprawnień."));
                    }
                    break;
                }
                case GENERUJ_RAPORT: {
                    if (zalogowanyUzytkownik != null && "ADMIN".equals(zalogowanyUzytkownik.getRola())) {
                        String raport = ogloszenieDAO.generujRaport();
                        wyslij(new Odpowiedz(StatusOdpowiedzi.OK, raport, "Raport wygenerowany."));
                    } else {
                        wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Brak uprawnień."));
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd bazy danych: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            wyslij(new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd serwera: " + e.getMessage()));
        }
    }

    public void wyslij(Odpowiedz odpowiedz) {
        try {
            wyjscie.writeObject(odpowiedz);
            wyjscie.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zamknijPolaczenie() {
        try {
            if (gniazdo != null)
                gniazdo.close();
        } catch (IOException e) {
        }
    }
}
