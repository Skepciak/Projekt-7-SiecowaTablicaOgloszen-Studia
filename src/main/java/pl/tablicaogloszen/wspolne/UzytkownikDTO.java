package pl.tablicaogloszen.wspolne;

import java.io.Serializable;

/**
 * Data Transfer Object reprezentujący użytkownika systemu.
 * Używane do przesyłania danych o zalogowanym użytkowniku.
 * 
 * @author Dawid Sułek, Dominik Rodziewicz
 * @version 1.0
 */
public class UzytkownikDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unikalny identyfikator użytkownika */
    private int id;

    /** Login użytkownika */
    private String login;

    /** Rola użytkownika (UZYTKOWNIK lub ADMIN) */
    private String rola;

    /**
     * Tworzy nowy obiekt użytkownika.
     * 
     * @param id    ID użytkownika z bazy danych
     * @param login Login użytkownika
     * @param rola  Rola (UZYTKOWNIK lub ADMIN)
     */
    public UzytkownikDTO(int id, String login, String rola) {
        this.id = id;
        this.login = login;
        this.rola = rola;
    }

    /** @return ID użytkownika */
    public int getId() {
        return id;
    }

    /** @return Login użytkownika */
    public String getLogin() {
        return login;
    }

    /** @return Rola użytkownika */
    public String getRola() {
        return rola;
    }
}
