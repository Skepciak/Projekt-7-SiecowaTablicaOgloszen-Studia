package pl.tablicaogloszen.klient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.tablicaogloszen.wspolne.*;
import java.io.IOException;

/**
 * Kontroler widoku logowania.
 * Obsługuje formularz logowania i przekierowanie do rejestracji.
 * 
 * @author Dawid Sułek, Dominik Rodziewicz
 * @version 1.0
 */
public class KontrolerLogowania {

    /** Pole tekstowe na login użytkownika */
    @FXML
    private TextField poleLogin;

    /** Pole na hasło użytkownika */
    @FXML
    private PasswordField poleHaslo;

    /** Etykieta na komunikaty błędów */
    @FXML
    private Label etykietaInfo;

    /**
     * Obsługuje akcję logowania.
     * Wysyła żądanie do serwera i w przypadku sukcesu przechodzi do tablicy.
     * 
     * @throws IOException Gdy nie można załadować widoku
     */
    @FXML
    private void zaloguj() throws IOException {
        String login = poleLogin.getText().trim();
        String haslo = poleHaslo.getText();

        if (login.isEmpty() || haslo.isEmpty()) {
            etykietaInfo.setText("Podaj login i hasło.");
            return;
        }

        Zadanie zadanie = new Zadanie(TypZadania.LOGOWANIE, login + ":" + haslo);
        Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);

        if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
            UzytkownikDTO user = (UzytkownikDTO) odp.getDane();
            Sesja.setZalogowanyUzytkownik(user);
            AplikacjaKlienta.zaladujWidok("tablica");
        } else {
            etykietaInfo.setText(odp != null ? odp.getWiadomosc() : "Błąd połączenia z serwerem.");
        }
    }

    /**
     * Przechodzi do widoku rejestracji.
     * 
     * @throws IOException Gdy nie można załadować widoku
     */
    @FXML
    private void idzDoRejestracji() throws IOException {
        AplikacjaKlienta.zaladujWidok("rejestracja");
    }
}
