package pl.tablicaogloszen.serwer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pl.tablicaogloszen.wspolne.Odpowiedz;
import pl.tablicaogloszen.wspolne.StatusOdpowiedzi;

/**
 * Główna klasa serwera aplikacji "Sieciowa Tablica Ogłoszeń".
 * <p>
 * Serwer nasłuchuje na określonym porcie (domyślnie 8080) i obsługuje
 * wielu klientów jednocześnie za pomocą puli wątków.
 * </p>
 * 
 * <h2>Funkcjonalności:</h2>
 * <ul>
 * <li>Obsługa wielu klientów jednocześnie (ThreadPool)</li>
 * <li>Powiadamianie wszystkich klientów o zmianach (real-time)</li>
 * <li>Zarządzanie połączeniami z bazą danych MySQL</li>
 * </ul>
 * 
 * @author System
 * @version 2.0
 */
public class Serwer {
    /** Port na którym nasłuchuje serwer */
    private static final int PORT = 8080;

    /** Zbiór aktywnych połączeń klientów */
    private static Set<ObslugaKlienta> klienci = Collections.synchronizedSet(new HashSet<>());

    /** Pula wątków do obsługi klientów */
    private static ExecutorService pulaWatkow = Executors.newCachedThreadPool();

    /**
     * Punkt wejścia serwera.
     * Uruchamia serwer i oczekuje na połączenia klientów.
     * 
     * @param args Argumenty wiersza poleceń (nieużywane)
     */
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   SIECIOWA TABLICA OGŁOSZEŃ - SERWER");
        System.out.println("===========================================");

        try (ServerSocket gniazdoSerwera = new ServerSocket(PORT)) {
            // Inicjalizacja bazy danych (automigracja)
            InicjalizatorBazy.inicjalizuj();

            System.out.println("✓ Serwer uruchomiony na porcie " + PORT);
            System.out.println("✓ Oczekiwanie na połączenia...\n");

            while (true) {
                Socket gniazdoKlienta = gniazdoSerwera.accept();
                System.out.println("→ Nowy klient: " + gniazdoKlienta.getInetAddress());

                ObslugaKlienta klient = new ObslugaKlienta(gniazdoKlienta);
                klienci.add(klient);
                pulaWatkow.execute(klient);

                System.out.println("  Aktywni klienci: " + klienci.size());
            }
        } catch (IOException e) {
            System.err.println("✗ Błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Usuwa klienta z listy aktywnych połączeń.
     * Wywoływane gdy klient się rozłączy.
     * 
     * @param klient Handler klienta do usunięcia
     */
    public static void usunKlienta(ObslugaKlienta klient) {
        klienci.remove(klient);
        System.out.println("← Klient rozłączony. Pozostało: " + klienci.size());
    }

    /**
     * Wysyła powiadomienie o konieczności odświeżenia do wszystkich klientów.
     * Używane gdy dane ogłoszeń zostały zmienione (dodanie/edycja/usunięcie).
     */
    public static void powiadomWszystkich() {
        System.out.println("⟳ Wysyłanie powiadomienia do " + klienci.size() + " klientów...");
        for (ObslugaKlienta klient : klienci) {
            klient.wyslij(new Odpowiedz(StatusOdpowiedzi.ODSWIEZ, null, "Dane zaktualizowane."));
        }
    }
}
