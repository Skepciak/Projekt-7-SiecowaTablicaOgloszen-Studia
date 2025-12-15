package pl.tablicaogloszen.serwer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pl.tablicaogloszen.wspolne.FiltrDTO;
import pl.tablicaogloszen.wspolne.OgloszenieDTO;

/**
 * Data Access Object dla ogłoszeń.
 * Obsługuje operacje CRUD oraz filtrowanie/sortowanie.
 */
public class OgloszenieDAO {

    public boolean dodajOgloszenie(OgloszenieDTO ogloszenie, int idAutora) throws SQLException {
        String sql = "INSERT INTO ogloszenia (tytul, tresc, dane_kontaktowe, id_kategorii, id_autora, data_dodania) " +
                "VALUES (?, ?, ?, (SELECT id FROM kategorie WHERE nazwa = ? LIMIT 1), ?, NOW())";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql)) {

            pstm.setString(1, ogloszenie.getTytul());
            pstm.setString(2, ogloszenie.getTresc());
            pstm.setString(3, ogloszenie.getDaneKontaktowe());
            pstm.setString(4, ogloszenie.getKategoria());
            pstm.setInt(5, idAutora);

            return pstm.executeUpdate() > 0;
        }
    }

    public boolean edytujOgloszenie(OgloszenieDTO ogloszenie, int idUzytkownika, boolean czyAdmin) throws SQLException {
        String sql = "UPDATE ogloszenia SET tytul = ?, tresc = ?, dane_kontaktowe = ?, " +
                "id_kategorii = (SELECT id FROM kategorie WHERE nazwa = ? LIMIT 1) " +
                "WHERE id = ? AND (id_autora = ? OR ?)";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql)) {

            pstm.setString(1, ogloszenie.getTytul());
            pstm.setString(2, ogloszenie.getTresc());
            pstm.setString(3, ogloszenie.getDaneKontaktowe());
            pstm.setString(4, ogloszenie.getKategoria());
            pstm.setInt(5, ogloszenie.getId());
            pstm.setInt(6, idUzytkownika);
            pstm.setBoolean(7, czyAdmin);

            return pstm.executeUpdate() > 0;
        }
    }

    public boolean usunOgloszenie(int idOgloszenia, int idUzytkownika, boolean czyAdmin) throws SQLException {
        String sql = "DELETE FROM ogloszenia WHERE id = ? AND (id_autora = ? OR ?)";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql)) {

            pstm.setInt(1, idOgloszenia);
            pstm.setInt(2, idUzytkownika);
            pstm.setBoolean(3, czyAdmin);

            return pstm.executeUpdate() > 0;
        }
    }

    public List<OgloszenieDTO> pobierzZFiltrem(FiltrDTO filtr) throws SQLException {
        List<OgloszenieDTO> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.id, o.tytul, o.tresc, o.dane_kontaktowe, ");
        sql.append("COALESCE(k.nazwa, 'Brak kategorii') as kategoria, ");
        sql.append("o.id_autora, COALESCE(u.login, 'Nieznany') as autor, o.data_dodania ");
        sql.append("FROM ogloszenia o ");
        sql.append("LEFT JOIN kategorie k ON o.id_kategorii = k.id ");
        sql.append("LEFT JOIN uzytkownicy u ON o.id_autora = u.id "); // Zmiana na LEFT JOIN
        sql.append("WHERE 1=1 ");

        List<Object> parametry = new ArrayList<>();

        if (filtr != null) {
            if (filtr.getKategoria() != null && !filtr.getKategoria().isEmpty()) {
                sql.append("AND k.nazwa = ? ");
                parametry.add(filtr.getKategoria());
            }
            if (filtr.getAutor() != null && !filtr.getAutor().isEmpty()) {
                sql.append("AND u.login LIKE ? ");
                parametry.add("%" + filtr.getAutor() + "%");
            }
            if (filtr.getSzukajTekst() != null && !filtr.getSzukajTekst().isEmpty()) {
                sql.append("AND (o.tytul LIKE ? OR o.tresc LIKE ?) ");
                parametry.add("%" + filtr.getSzukajTekst() + "%");
                parametry.add("%" + filtr.getSzukajTekst() + "%");
            }

            if (filtr.getSortowanie() != null) {
                switch (filtr.getSortowanie()) {
                    case "DATA_ASC":
                        sql.append("ORDER BY o.data_dodania ASC");
                        break;
                    case "TYTUL_ASC":
                        sql.append("ORDER BY o.tytul ASC");
                        break;
                    case "TYTUL_DESC":
                        sql.append("ORDER BY o.tytul DESC");
                        break;
                    default:
                        sql.append("ORDER BY o.data_dodania DESC");
                        break;
                }
            } else {
                sql.append("ORDER BY o.data_dodania DESC");
            }
        } else {
            sql.append("ORDER BY o.data_dodania DESC");
        }

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                PreparedStatement pstm = pol.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametry.size(); i++) {
                pstm.setObject(i + 1, parametry.get(i));
            }

            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                lista.add(new OgloszenieDTO(
                        rs.getInt("id"),
                        rs.getString("tytul"),
                        rs.getString("tresc"),
                        rs.getString("dane_kontaktowe"),
                        rs.getString("kategoria"),
                        rs.getInt("id_autora"),
                        rs.getString("autor"),
                        rs.getTimestamp("data_dodania").toLocalDateTime()));
            }
        }

        System.out.println("DEBUG: Pobrano " + lista.size() + " ogłoszeń."); // Logowanie
        return lista;
    }

    public List<OgloszenieDTO> pobierzWszystkie() throws SQLException {
        return pobierzZFiltrem(null);
    }

    public List<String> pobierzKategorie() throws SQLException {
        List<String> kategorie = new ArrayList<>();
        String sql = "SELECT nazwa FROM kategorie ORDER BY nazwa";

        try (Connection pol = PolaczenieBazy.pobierzPolaczenie();
                Statement stm = pol.createStatement();
                ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                kategorie.add(rs.getString("nazwa"));
            }
        }
        return kategorie;
    }
}
