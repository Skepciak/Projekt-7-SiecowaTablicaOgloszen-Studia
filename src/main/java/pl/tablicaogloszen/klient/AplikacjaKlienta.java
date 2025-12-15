package pl.tablicaogloszen.klient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa aplikacji klienckiej JavaFX.
 * Zarządza scenami i widokami aplikacji.
 * 
 * @author System
 * @version 2.0
 */
public class AplikacjaKlienta extends Application {

    /** Główna scena aplikacji */
    private static Stage glownaScena;

    /**
     * Punkt wejścia aplikacji JavaFX.
     * Inicjalizuje okno i ładuje widok logowania.
     * 
     * @param stage Główna scena aplikacji
     * @throws IOException Gdy nie można załadować pliku FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        glownaScena = stage;
        zaladujWidok("logowanie");
        stage.setTitle("Sieciowa Tablica Ogłoszeń 2025");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Ładuje widok FXML i ustawia go jako aktualną scenę.
     * Automatycznie dodaje arkusz stylów CSS.
     * 
     * @param fxml Nazwa pliku FXML (bez rozszerzenia)
     * @throws IOException Gdy nie można załadować pliku
     */
    public static void zaladujWidok(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(AplikacjaKlienta.class.getResource("/" + fxml + ".fxml"));
        Parent root = loader.load();

        // Załadowanie stylu CSS
        String css = AplikacjaKlienta.class.getResource("/style.css").toExternalForm();
        root.getStylesheets().add(css);

        glownaScena.setScene(new Scene(root));
    }

    /**
     * Punkt wejścia programu.
     * 
     * @param args Argumenty wiersza poleceń
     */
    public static void main(String[] args) {
        launch();
    }
}
