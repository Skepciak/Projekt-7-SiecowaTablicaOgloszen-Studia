package pl.tablicaogloszen.serwer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pl.tablicaogloszen.wspolne.UzytkownikDTO;

/**
 * Data Access Object dla użytkowników.
 * Obsługuje operacje rejestracji i logowania.
 * 
 * @author System
 * @version 1.0
 */
public class UzytkownikDAO {

    /**
     * Rejestruje nowego użytkownika w systemie.
     * 
     * @param login     Unikalny login użytkownika
     * @param hasloHash Hasło użytkownika (powinno być zahashowane)
     * @return true jeśli rejestracja zakończyła się sukcesem, false w przypadku
     *         błędu
     */
    public boolean zarejestruj(String login, String hasloHash) {
        String sql = "INSERT INTO uzytkownicy (login, haslo_hash) VALUES (?, ?)";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql)) {

            pstm.setString(1, login);
            pstm.setString(2, hasloHash);

            int wynik = pstm.executeUpdate();
            return wynik > 0;
        } catch (SQLException e) {
            System.err.println("Błąd rejestracji użytkownika: " + e.getMessage());
            return false;
        }
    }

    /**
     * Weryfikuje dane logowania użytkownika.
     * 
     * @param login     Login użytkownika
     * @param hasloHash Hasło do weryfikacji
     * @return DTO użytkownika jeśli dane są poprawne, null w przeciwnym razie
     */
    public UzytkownikDTO zaloguj(String login, String hasloHash) {
        String sql = "SELECT id, rola FROM uzytkownicy WHERE login = ? AND haslo_hash = ?";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql)) {

            pstm.setString(1, login);
            pstm.setString(2, hasloHash);

            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String rola = rs.getString("rola");
                return new UzytkownikDTO(id, login, rola);
            }
        } catch (SQLException e) {
            System.err.println("Błąd logowania: " + e.getMessage());
        }
        return null;
    }
}
