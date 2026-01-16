package pl.tablicaogloszen.serwer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Klasa odpowiedzialna za inicjalizację i migrację bazy danych.
 * Uruchamiana przy starcie serwera.
 */
public class InicjalizatorBazy {

    public static void inicjalizuj() {
        System.out.println(">> Sprawdzanie struktury bazy danych...");

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie()) {

            // 1. Tabela uzytkownicy
            wykonajSQL(pol, "CREATE TABLE IF NOT EXISTS uzytkownicy (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "login VARCHAR(50) NOT NULL UNIQUE, " +
                    "haslo_hash VARCHAR(255) NOT NULL, " +
                    "rola ENUM('UZYTKOWNIK', 'ADMIN') DEFAULT 'UZYTKOWNIK', " +
                    "data_utworzenia TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. Tabela kategorie
            wykonajSQL(pol, "CREATE TABLE IF NOT EXISTS kategorie (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nazwa VARCHAR(100) NOT NULL UNIQUE)");

            // 3. Tabela ogloszenia
            wykonajSQL(pol, "CREATE TABLE IF NOT EXISTS ogloszenia (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "tytul VARCHAR(255) NOT NULL, " +
                    "tresc TEXT NOT NULL, " +
                    "dane_kontaktowe VARCHAR(255), " +
                    "id_kategorii INT, " +
                    "id_autora INT, " +
                    "data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (id_kategorii) REFERENCES kategorie(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (id_autora) REFERENCES uzytkownicy(id) ON DELETE CASCADE)");

            // 4. MIGRACJA: Dodanie kolumny dane_kontaktowe (jeśli nie istnieje)
            try {
                wykonajSQL(pol, "ALTER TABLE ogloszenia ADD COLUMN dane_kontaktowe VARCHAR(255) AFTER tresc");
                System.out.println("   + Dodano kolumnę 'dane_kontaktowe'");
            } catch (SQLException e) {
                if (!e.getMessage().toLowerCase().contains("duplicate") && e.getErrorCode() != 1060) {
                    System.out.println("   (Info: " + e.getMessage() + ")");
                }
            }

            // 4a. MIGRACJA: Kolumna wyswietlenia (licznik popularności)
            try {
                wykonajSQL(pol, "ALTER TABLE ogloszenia ADD COLUMN wyswietlenia INT DEFAULT 0");
                System.out.println("   + Dodano kolumnę 'wyswietlenia'");
            } catch (SQLException e) {
                // Ignorujemy jeśli już istnieje
            }

            // 4b. MIGRACJA: Kolumna zgloszenia (licznik zgłoszeń)
            try {
                wykonajSQL(pol, "ALTER TABLE ogloszenia ADD COLUMN zgloszenia INT DEFAULT 0");
                System.out.println("   + Dodano kolumnę 'zgloszenia'");
            } catch (SQLException e) {
                // Ignorujemy jeśli już istnieje
            }

            // 5. Dodaj admina
            String adminPass = Bezpieczenstwo.hashuj("admin");
            wykonajSQL(pol,
                    "INSERT IGNORE INTO uzytkownicy (login, haslo_hash, rola) VALUES ('admin', '" + adminPass
                            + "', 'ADMIN')");

            // 5a. MIGRACJA HASEŁ (Fix dla istniejących kont)
            try (Statement st = pol.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id, haslo_hash FROM uzytkownicy")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String currentHash = rs.getString("haslo_hash");
                    // Jeśli hasło nie wygląda jak SHA-256 (64 znaki hex), to zahashuj je
                    if (currentHash != null && currentHash.length() != 64) {
                        String newHash = Bezpieczenstwo.hashuj(currentHash);
                        try (PreparedStatement pstUpdate = pol
                                .prepareStatement("UPDATE uzytkownicy SET haslo_hash = ? WHERE id = ?")) {
                            pstUpdate.setString(1, newHash);
                            pstUpdate.setInt(2, id);
                            pstUpdate.executeUpdate();
                            System.out.println("   ! Zaktualizowano hasło dla ID: " + id);
                        }
                    }
                }
            }

            // 6. Dodaj kategorie
            String[] kategorie = { "Motoryzacja", "Elektronika", "Nieruchomości", "Praca", "Usługi", "Inne" };
            for (String kat : kategorie) {
                try (PreparedStatement pst = pol.prepareStatement("INSERT IGNORE INTO kategorie (nazwa) VALUES (?)")) {
                    pst.setString(1, kat);
                    pst.executeUpdate();
                }
            }

            // 7. Dodaj przykładowe ogłoszenia (jeśli brak)
            try (Statement st = pol.createStatement();
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ogloszenia")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("   + Dodawanie przykładowych ogłoszeń...");

                    String sqlInsert = "INSERT INTO ogloszenia (tytul, tresc, dane_kontaktowe, id_kategorii, id_autora, data_dodania) "
                            +
                            "VALUES (?, ?, ?, (SELECT id FROM kategorie WHERE nazwa=? LIMIT 1), (SELECT id FROM uzytkownicy WHERE login=? LIMIT 1), NOW())";

                    try (PreparedStatement pst = pol.prepareStatement(sqlInsert)) {
                        pst.setString(1, "Sprzedam Opla");
                        pst.setString(2,
                                "Sprzedam Opla Corsa, rocznik 2010. Stan 'igła', Niemiec płakał jak sprzedawał.");
                        pst.setString(3, "500-100-200");
                        pst.setString(4, "Motoryzacja");
                        pst.setString(5, "admin");
                        pst.executeUpdate();

                        pst.setString(1, "Laptop Gamingowy");
                        pst.setString(2, "Sprzedam wydajnego laptopa do gier, 16GB RAM, RTX 3060.");
                        pst.setString(3, "laptop@example.com");
                        pst.setString(4, "Elektronika");
                        pst.setString(5, "admin");
                        pst.executeUpdate();
                    }
                }
            }

            System.out.println(">> Baza danych gotowa.");

        } catch (SQLException e) {
            System.err.println("!! Błąd inicjalizacji bazy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void wykonajSQL(Connection pol, String sql) throws SQLException {
        try (Statement st = pol.createStatement()) {
            st.executeUpdate(sql);
        }
    }
}
