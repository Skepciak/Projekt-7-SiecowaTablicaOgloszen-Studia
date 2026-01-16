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

            // 6. Dodaj kategorie (Wiedźmin Style)
            String[] kategorie = { "Kontrakty na potwory", "Handel i wymiana", "Poszukiwani", "Usługi i rzemiosło",
                    "Sprawy wioskowe", "Zaginięcia", "Ogłoszenia królewskie", "Inne zlecenia" };
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
                    System.out.println("   + Dodawanie przykładowych ogłoszeń (Wiedźmin)...");

                    String sqlInsert = "INSERT INTO ogloszenia (tytul, tresc, dane_kontaktowe, id_kategorii, id_autora, data_dodania, wyswietlenia, zgloszenia) "
                            + "VALUES (?, ?, ?, (SELECT id FROM kategorie WHERE nazwa=? LIMIT 1), (SELECT id FROM uzytkownicy WHERE login=? LIMIT 1), NOW(), ?, ?)";

                    try (PreparedStatement pst = pol.prepareStatement(sqlInsert)) {
                        // 1. Gryf
                        pst.setString(1, "Kontrakt: Gryf z Białego Sadu");
                        pst.setString(2,
                                "Bestia o skrzydłach orła i ciele lwa zaatakowała nasz targ. Nagroda: 300 koron.");
                        pst.setString(3, "Sołtys Białego Sadu");
                        pst.setString(4, "Kontrakty na potwory");
                        pst.setString(5, "admin");
                        pst.setInt(6, 45); // wyświetlenia
                        pst.setInt(7, 0); // zgłoszenia
                        pst.executeUpdate();

                        // 2. Miecz
                        pst.setString(1, "Sprzedam miecz srebrny");
                        pst.setString(2, "Wykuty w Kaer Morhen, idealny na Utopce. Lekko używany.");
                        pst.setString(3, "Karczma w Wyzimie");
                        pst.setString(4, "Handel i wymiana");
                        pst.setString(5, "admin");
                        pst.setInt(6, 12);
                        pst.setInt(7, 0);
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
