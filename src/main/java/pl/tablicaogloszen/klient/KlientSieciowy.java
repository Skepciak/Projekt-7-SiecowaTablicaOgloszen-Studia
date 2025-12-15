package pl.tablicaogloszen.klient;

import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import pl.tablicaogloszen.wspolne.*;

/**
 * Singleton odpowiedzialny za komunikację sieciową z serwerem.
 * Obsługuje wysyłanie żądań i odbieranie powiadomień push.
 */
public class KlientSieciowy {
    private static final String ADRES = "localhost";
    private static final int PORT = 8080;

    private static KlientSieciowy instancja;
    private Socket gniazdo;
    private ObjectOutputStream wyjscie;
    private ObjectInputStream wejscie;
    private boolean polaczony = false;

    private KontrolerTablicy kontrolerTablicy;

    private Odpowiedz ostatniaOdpowiedz;
    private final Object lock = new Object();

    private KlientSieciowy() {
        polacz();
    }

    private void polacz() {
        try {
            gniazdo = new Socket(ADRES, PORT);
            wyjscie = new ObjectOutputStream(gniazdo.getOutputStream());
            wejscie = new ObjectInputStream(gniazdo.getInputStream());
            polaczony = true;

            // Wątek nasłuchujący odpowiedzi i powiadomień
            Thread watekNasluchu = new Thread(this::nasluchuj);
            watekNasluchu.setDaemon(true);
            watekNasluchu.start();

            System.out.println("Połączono z serwerem.");

        } catch (ConnectException e) {
            polaczony = false;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd połączenia");
                alert.setHeaderText("Nie można połączyć się z serwerem");
                alert.setContentText("Upewnij się, że serwer jest uruchomiony na porcie " + PORT
                        + ".\n\nUruchom najpierw klasę Serwer, a następnie spróbuj ponownie.");
                alert.showAndWait();
                System.exit(1);
            });
        } catch (IOException e) {
            polaczony = false;
            e.printStackTrace();
        }
    }

    public static synchronized KlientSieciowy pobierzInstancje() {
        if (instancja == null) {
            instancja = new KlientSieciowy();
        }
        return instancja;
    }

    public void ustawKontrolerTablicy(KontrolerTablicy kontroler) {
        this.kontrolerTablicy = kontroler;
    }

    public boolean czyPolaczony() {
        return polaczony;
    }

    /**
     * Wysyła żądanie i czeka na odpowiedź (synchronicznie, max 5 sekund).
     * Metoda synchronizowana, aby zapobiec przeplataniu się żądań/odpowiedzi z
     * wielu wątków.
     */
    public synchronized Odpowiedz wyslijISprawdz(Zadanie zadanie) {
        if (!polaczony) {
            return new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Brak połączenia z serwerem.");
        }

        try {
            synchronized (lock) {
                ostatniaOdpowiedz = null;
                wyjscie.writeObject(zadanie);
                wyjscie.flush();
                lock.wait(5000);
                return ostatniaOdpowiedz;
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return new Odpowiedz(StatusOdpowiedzi.BLAD, null, "Błąd komunikacji z serwerem.");
        }
    }

    /**
     * Wątek nasłuchujący wszystkich odpowiedzi od serwera.
     */
    private void nasluchuj() {
        try {
            while (polaczony) {
                Object obj = wejscie.readObject();

                if (obj instanceof Odpowiedz) {
                    Odpowiedz odp = (Odpowiedz) obj;
                    if (odp.getStatus() == StatusOdpowiedzi.ODSWIEZ) {
                        // Powiadomienie push - odśwież listę
                        if (kontrolerTablicy != null) {
                            Platform.runLater(() -> kontrolerTablicy.odswiezListe());
                        }
                    } else {
                        // Odpowiedź na żądanie użytkownika
                        synchronized (lock) {
                            ostatniaOdpowiedz = odp;
                            lock.notifyAll();
                        }
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("Serwer zamknął połączenie.");
        } catch (IOException | ClassNotFoundException e) {
            if (polaczony) {
                e.printStackTrace();
            }
        } finally {
            polaczony = false;
        }
    }
}
