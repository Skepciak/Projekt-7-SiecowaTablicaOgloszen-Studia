package pl.tablicaogloszen.wspolne;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object reprezentujący ogłoszenie.
 * Używany do przesyłania danych ogłoszenia między klientem a serwerem.
 */
public class OgloszenieDTO implements Serializable {
    private static final long serialVersionUID = 2L;

    private int id;
    private String tytul;
    private String tresc;
    private String daneKontaktowe;
    private String kategoria;
    private int idAutora;
    private String autor; // login autora
    private LocalDateTime dataDodania;

    /**
     * Konstruktor pełny (do pobierania z bazy).
     */
    public OgloszenieDTO(int id, String tytul, String tresc, String daneKontaktowe,
            String kategoria, int idAutora, String autor, LocalDateTime dataDodania) {
        this.id = id;
        this.tytul = tytul;
        this.tresc = tresc;
        this.daneKontaktowe = daneKontaktowe;
        this.kategoria = kategoria;
        this.idAutora = idAutora;
        this.autor = autor;
        this.dataDodania = dataDodania;
    }

    /**
     * Konstruktor uproszczony (do dodawania nowego ogłoszenia).
     */
    public OgloszenieDTO(String tytul, String tresc, String daneKontaktowe, String kategoria, String autor) {
        this.tytul = tytul;
        this.tresc = tresc;
        this.daneKontaktowe = daneKontaktowe;
        this.kategoria = kategoria;
        this.autor = autor;
    }

    // ==================== GETTERY ====================

    /** @return ID ogłoszenia */
    public int getId() {
        return id;
    }

    /** @return Tytuł ogłoszenia */
    public String getTytul() {
        return tytul;
    }

    /** @return Treść ogłoszenia */
    public String getTresc() {
        return tresc;
    }

    /** @return Dane kontaktowe autora */
    public String getDaneKontaktowe() {
        return daneKontaktowe;
    }

    /** @return Nazwa kategorii */
    public String getKategoria() {
        return kategoria;
    }

    /** @return ID autora ogłoszenia */
    public int getIdAutora() {
        return idAutora;
    }

    /** @return Login autora */
    public String getAutor() {
        return autor;
    }

    /** @return Data dodania ogłoszenia */
    public LocalDateTime getDataDodania() {
        return dataDodania;
    }

    // ==================== SETTERY ====================

    public void setId(int id) {
        this.id = id;
    }

    public void setTytul(String tytul) {
        this.tytul = tytul;
    }

    public void setTresc(String tresc) {
        this.tresc = tresc;
    }

    public void setDaneKontaktowe(String daneKontaktowe) {
        this.daneKontaktowe = daneKontaktowe;
    }

    public void setKategoria(String kategoria) {
        this.kategoria = kategoria;
    }

    public void setIdAutora(int idAutora) {
        this.idAutora = idAutora;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setDataDodania(LocalDateTime dataDodania) {
        this.dataDodania = dataDodania;
    }

    @Override
    public String toString() {
        return "[" + kategoria + "] " + tytul + " (" + autor + ")";
    }
}
