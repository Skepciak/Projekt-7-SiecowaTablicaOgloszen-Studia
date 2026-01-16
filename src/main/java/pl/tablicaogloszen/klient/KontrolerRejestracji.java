package pl.tablicaogloszen.klient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.tablicaogloszen.wspolne.*;

import java.io.IOException;

/**
 * Kontroler widoku rejestracji.
 * Obsługuje formularz rejestracji nowego użytkownika z walidacją.
 * 
 * @author Dawid Sułek, Dominik Rodziewicz
 * @version 1.0
 */
public class KontrolerRejestracji {

    /** Pole na login */
    @FXML
    private TextField poleLogin;

    /** Pole na hasło */
    @FXML
    private PasswordField poleHaslo;

    /** Pole na powtórzenie hasła */
    @FXML
    private PasswordField polePowtorzHaslo;

    /** Etykieta na komunikaty */
    @FXML
    private Label etykietaInfo;

    /**
     * Obsługuje akcję rejestracji.
     * Waliduje dane i wysyła żądanie do serwera.
     */
    @FXML
    private void zarejestruj() {
        String login = poleLogin.getText().trim();
        String haslo = poleHaslo.getText();
        String powtHaslo = polePowtorzHaslo.getText();

        // Walidacja
        if (login.isEmpty() || haslo.isEmpty()) {
            etykietaInfo.setText("Wypełnij wszystkie pola.");
            return;
        }

        if (login.length() < 3) {
            etykietaInfo.setText("Login musi mieć minimum 3 znaki.");
            return;
        }

        if (haslo.length() < 4) {
            etykietaInfo.setText("Hasło musi mieć minimum 4 znaki.");
            return;
        }

        if (!haslo.equals(powtHaslo)) {
            etykietaInfo.setText("Hasła nie są identyczne.");
            return;
        }

        Zadanie zadanie = new Zadanie(TypZadania.REJESTRACJA, login + ":" + haslo);
        Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);

        if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukces");
            alert.setHeaderText(null);
            alert.setContentText("Pomyślnie się zarejestrowałeś! Możesz się teraz zalogować.");

            try {
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm());
            } catch (Exception ignored) {
            }

            alert.showAndWait();

            try {
                AplikacjaKlienta.zaladujWidok("logowanie");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            etykietaInfo.setText(odp != null ? odp.getWiadomosc() : "Błąd serwera.");
        }
    }

    /**
     * Wraca do widoku logowania.
     * 
     * @throws IOException Gdy nie można załadować widoku
     */
    @FXML
    private void wrocDoLogowania() throws IOException {
        AplikacjaKlienta.zaladujWidok("logowanie");
    }
}
