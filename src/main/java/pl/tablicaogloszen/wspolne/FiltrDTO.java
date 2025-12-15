package pl.tablicaogloszen.wspolne;

import java.io.Serializable;

/**
 * Data Transfer Object dla parametrów filtrowania/sortowania ogłoszeń.
 */
public class FiltrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String kategoria;
    private String autor;
    private String szukajTekst;
    private String sortowanie; // "DATA_DESC", "DATA_ASC", "TYTUL_ASC", "TYTUL_DESC"

    /**
     * Tworzy nowy filtr z podanymi parametrami.
     * 
     * @param kategoria   Nazwa kategorii do filtrowania (null = wszystkie)
     * @param autor       Login autora do filtrowania (null = wszyscy)
     * @param szukajTekst Tekst do wyszukania w tytule/treści (null = bez
     *                    wyszukiwania)
     * @param sortowanie  Sposób sortowania wyników
     */
    public FiltrDTO(String kategoria, String autor, String szukajTekst, String sortowanie) {
        this.kategoria = kategoria;
        this.autor = autor;
        this.szukajTekst = szukajTekst;
        this.sortowanie = sortowanie != null ? sortowanie : "DATA_DESC";
    }

    public String getKategoria() {
        return kategoria;
    }

    public String getAutor() {
        return autor;
    }

    public String getSzukajTekst() {
        return szukajTekst;
    }

    public String getSortowanie() {
        return sortowanie;
    }
}
