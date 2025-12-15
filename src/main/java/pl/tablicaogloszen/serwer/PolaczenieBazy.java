package pl.tablicaogloszen.serwer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton zarządzający połączeniem z bazą danych MySQL.
 * <p>
 * Domyślna konfiguracja dla środowiska Laragon:
 * <ul>
 * <li>Host: localhost</li>
 * <li>Port: 3306</li>
 * <li>Baza: sieciowa_tablica</li>
 * <li>Użytkownik: root</li>
 * <li>Hasło: (puste)</li>
 * </ul>
 * </p>
 * 
 * @author System
 * @version 1.0
 */
public class PolaczenieBazy {
    /** URL połączenia JDBC */
    private static final String URL = "jdbc:mysql://localhost:3306/sieciowa_tablica?useSSL=false&serverTimezone=UTC";

    /** Nazwa użytkownika bazy danych */
    private static final String UZYTKOWNIK = "root";

    /** Hasło użytkownika (puste w Laragon) */
    private static final String HASLO = "";

    /** Singleton - jedno połączenie dla całej aplikacji */
    private static Connection polaczenie = null;

    /** Prywatny konstruktor - wzorzec Singleton */
    private PolaczenieBazy() {
    }

    /**
     * Zwraca połączenie z bazą danych.
     * Jeśli połączenie nie istnieje lub zostało zamknięte, tworzy nowe.
     * 
     * @return Aktywne połączenie z bazą danych
     * @throws SQLException Gdy nie można nawiązać połączenia
     */
    public static Connection pobierzPolaczenie() throws SQLException {
        if (polaczenie == null || polaczenie.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                polaczenie = DriverManager.getConnection(URL, UZYTKOWNIK, HASLO);
                System.out.println("✓ Połączono z bazą danych.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Nie znaleziono sterownika MySQL (mysql-connector-j).", e);
            }
        }
        return polaczenie;
    }

    /**
     * Zamyka połączenie z bazą danych.
     * Powinno być wywołane przy zamykaniu aplikacji.
     */
    public static void zamknijPolaczenie() {
        if (polaczenie != null) {
            try {
                polaczenie.close();
                System.out.println("✓ Połączenie z bazą zamknięte.");
            } catch (SQLException e) {
                System.err.println("Błąd zamykania połączenia: " + e.getMessage());
            }
        }
    }
}
