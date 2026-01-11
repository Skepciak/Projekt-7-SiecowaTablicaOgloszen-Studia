# 📚 Dokumentacja Struktury Projektu
## Sieciowa Tablica Ogłoszeń

---

## 1. Architektura aplikacji

Projekt wykorzystuje architekturę **klient-serwer** z rozdziałem na trzy warstwy logiczne:

```
┌─────────────────────────────────────────────────────────────────┐
│                         KLIENT (JavaFX)                          │
│    Interfejs użytkownika, kontrolery, komunikacja sieciowa       │
└────────────────────────────┬────────────────────────────────────┘
                             │ Socket TCP (port 8080)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         SERWER (Java)                            │
│     Obsługa połączeń, logika biznesowa, dostęp do bazy          │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BAZA DANYCH (MySQL)                         │
│            Przechowywanie użytkowników i ogłoszeń                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Struktura pakietów

Projekt podzielony jest na **3 główne pakiety** zgodnie z zasadą separacji odpowiedzialności:

```
pl.tablicaogloszen/
├── klient/           # Warstwa prezentacji (UI)
├── serwer/           # Warstwa logiki biznesowej i danych
└── wspolne/          # Klasy współdzielone (DTO, protokół komunikacji)
```

### 2.1 Pakiet `pl.tablicaogloszen.klient` (7 klas)

Odpowiada za **interfejs użytkownika** (JavaFX) i komunikację z serwerem.

| Klasa | Opis | Wzorzec OOP |
|-------|------|-------------|
| `AplikacjaKlienta` | Główna klasa JavaFX, zarządza scenami | **Dziedziczenie** po `Application` |
| `Start` | Punkt wejścia aplikacji | - |
| `KontrolerLogowania` | Obsługa ekranu logowania | Kontroler MVC |
| `KontrolerRejestracji` | Obsługa rejestracji użytkownika | Kontroler MVC |
| `KontrolerTablicy` | Główny widok tablicy ogłoszeń | Kontroler MVC |
| `KlientSieciowy` | Komunikacja TCP z serwerem | **Singleton** |
| `Sesja` | Przechowywanie stanu zalogowanego użytkownika | Klasa statyczna |

### 2.2 Pakiet `pl.tablicaogloszen.serwer` (7 klas)

Odpowiada za **logikę biznesową** i dostęp do bazy danych.

| Klasa | Opis | Wzorzec OOP |
|-------|------|-------------|
| `Serwer` | Główna klasa serwera TCP | Wielowątkowość (`ExecutorService`) |
| `ObslugaKlienta` | Obsługa pojedynczego klienta | **Implementacja interfejsu** `Runnable` |
| `OgloszenieDAO` | Dostęp do danych ogłoszeń | **Wzorzec DAO** |
| `UzytkownikDAO` | Dostęp do danych użytkowników | **Wzorzec DAO** |
| `PolaczenieBazy` | Zarządzanie połączeniem z bazą | **Singleton** |
| `InicjalizatorBazy` | Tworzenie struktury bazy | - |
| `Bezpieczenstwo` | Hashowanie haseł (SHA-256) | Klasa narzędziowa |

### 2.3 Pakiet `pl.tablicaogloszen.wspolne` (7 klas)

Klasy współdzielone między klientem a serwerem - **protokół komunikacyjny**.

| Klasa | Opis | Wzorzec OOP |
|-------|------|-------------|
| `Zadanie` | Żądanie klienta do serwera | **Implementacja interfejsu** `Serializable` |
| `Odpowiedz` | Odpowiedź serwera do klienta | **Implementacja interfejsu** `Serializable` |
| `OgloszenieDTO` | Transfer danych ogłoszenia | **Wzorzec DTO**, `Serializable` |
| `UzytkownikDTO` | Transfer danych użytkownika | **Wzorzec DTO**, `Serializable` |
| `FiltrDTO` | Parametry filtrowania listy | **Wzorzec DTO**, `Serializable` |
| `TypZadania` | Typy operacji (enum) | **Enumeracja** |
| `StatusOdpowiedzi` | Statusy odpowiedzi (enum) | **Enumeracja** |

---

## 3. Zastosowane koncepcje OOP

### 3.1 Dziedziczenie (Inheritance)

```java
// AplikacjaKlienta dziedziczy po klasie Application z JavaFX
public class AplikacjaKlienta extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Nadpisanie metody rodzica
    }
}
```

**Wykorzystanie:**
- `AplikacjaKlienta` → `javafx.application.Application`
- Pozwala na korzystanie z infrastruktury JavaFX (zarządzanie scenami, cykl życia aplikacji)

### 3.2 Implementacja interfejsów (Interfaces)

```java
// ObslugaKlienta implementuje interfejs Runnable dla wielowątkowości
public class ObslugaKlienta implements Runnable {
    @Override
    public void run() {
        // Kod wykonywany w oddzielnym wątku
    }
}
```

```java
// Klasy DTO implementują Serializable dla przesyłania przez sieć
public class OgloszenieDTO implements Serializable {
    private static final long serialVersionUID = 2L;
    // ...
}
```

**Zaimplementowane interfejsy:**
| Interfejs | Klasy | Cel |
|-----------|-------|-----|
| `Runnable` | `ObslugaKlienta` | Wielowątkowość |
| `Serializable` | `Zadanie`, `Odpowiedz`, `OgloszenieDTO`, `UzytkownikDTO`, `FiltrDTO` | Serializacja TCP |

### 3.3 Enkapsulacja (Encapsulation)

```java
public class UzytkownikDTO implements Serializable {
    // Pola prywatne - ukryte przed zewnętrznym dostępem
    private int id;
    private String login;
    private String rola;

    // Publiczne gettery - kontrolowany dostęp do danych
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getRola() { return rola; }
}
```

**Realizacja:**
- Wszystkie pola klas są **prywatne** (`private`)
- Dostęp przez **gettery** i **settery**
- Ukrywa szczegóły implementacji przed użytkownikiem klasy

### 3.4 Polimorfizm

```java
// Pole 'dane' może przechowywać różne typy obiektów
public class Zadanie implements Serializable {
    private Object dane;  // Może być: String, OgloszenieDTO, FiltrDTO, Integer...
}

// Serwer rzutuje na odpowiedni typ w zależności od kontekstu
OgloszenieDTO nowe = (OgloszenieDTO) zadanie.getDane();
```

**Realizacja:**
- Użycie typu `Object` pozwala przesyłać różne dane jednym protokołem
- Rzutowanie (casting) w zależności od typu żądania

---

## 4. Wzorce projektowe

### 4.1 Wzorzec DTO (Data Transfer Object)

```java
public class OgloszenieDTO implements Serializable {
    private int id;
    private String tytul;
    private String tresc;
    // ... tylko dane, brak logiki biznesowej
}
```

**Cel:** Przenoszenie danych między warstwami aplikacji bez metod biznesowych.

### 4.2 Wzorzec DAO (Data Access Object)

```java
public class OgloszenieDAO {
    public boolean dodajOgloszenie(OgloszenieDTO ogloszenie, int idAutora) { ... }
    public List<OgloszenieDTO> pobierzWszystkie() { ... }
    public boolean edytujOgloszenie(OgloszenieDTO ogloszenie, int idUzytkownika) { ... }
    public boolean usunOgloszenie(int id, int idUzytkownika) { ... }
}
```

**Cel:** Oddzielenie logiki dostępu do bazy od logiki biznesowej.

### 4.3 Wzorzec Singleton

```java
public class KlientSieciowy {
    private static KlientSieciowy instancja;
    
    public static KlientSieciowy pobierzInstancje() {
        if (instancja == null) {
            instancja = new KlientSieciowy();
        }
        return instancja;
    }
}
```

**Cel:** Zapewnienie jednej instancji połączenia sieciowego w całej aplikacji.

### 4.4 Wzorzec MVC (Model-View-Controller)

```
┌──────────────────────────────────────────────────────────────────┐
│ Model: OgloszenieDTO, UzytkownikDTO (dane)                       │
│ View:  tablica.fxml, logowanie.fxml (widoki FXML)                │
│ Controller: KontrolerTablicy, KontrolerLogowania (kontrolery)    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 5. Typy wyliczeniowe (Enumeracje)

### TypZadania - rodzaje operacji
```java
public enum TypZadania {
    LOGOWANIE,
    REJESTRACJA,
    POBIERZ_OGLOSZENIA,
    POBIERZ_OGLOSZENIA_FILTR,
    DODAJ_OGLOSZENIE,
    EDYTUJ_OGLOSZENIE,
    USUN_OGLOSZENIE,
    POBIERZ_KATEGORIE,
    ODSWIEZ
}
```

### StatusOdpowiedzi - wyniki operacji
```java
public enum StatusOdpowiedzi {
    OK,      // Sukces
    BLAD,    // Błąd
    ODSWIEZ  // Powiadomienie push
}
```

---

## 6. Diagram klas (uproszczony)

```
┌─────────────────────────────────────────────────────────────────┐
│                         <<interface>>                           │
│                          Serializable                            │
└───────────────────────────────┬─────────────────────────────────┘
                                │ implements
            ┌───────────────────┼───────────────────┐
            ▼                   ▼                   ▼
    ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
    │   Zadanie     │   │   Odpowiedz   │   │ OgloszenieDTO │
    ├───────────────┤   ├───────────────┤   ├───────────────┤
    │ -typ          │   │ -status       │   │ -id           │
    │ -dane         │   │ -dane         │   │ -tytul        │
    ├───────────────┤   │ -wiadomosc    │   │ -tresc        │
    │ +getTyp()     │   ├───────────────┤   │ ...           │
    │ +getDane()    │   │ +getStatus()  │   ├───────────────┤
    └───────────────┘   │ +getDane()    │   │ +gettery()    │
                        │ +getWiadomosc()│   │ +settery()    │
                        └───────────────┘   └───────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         <<interface>>                           │
│                           Runnable                               │
└───────────────────────────────┬─────────────────────────────────┘
                                │ implements
                                ▼
                    ┌───────────────────────┐
                    │    ObslugaKlienta     │
                    ├───────────────────────┤
                    │ -gniazdo              │
                    │ -zalogowanyUzytkownik │
                    ├───────────────────────┤
                    │ +run()                │
                    │ +wyslij()             │
                    └───────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Application                              │
│                      (javafx.application)                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │ extends
                                ▼
                    ┌───────────────────────┐
                    │   AplikacjaKlienta    │
                    ├───────────────────────┤
                    │ -glownaScena          │
                    ├───────────────────────┤
                    │ +start()              │
                    │ +zaladujWidok()       │
                    └───────────────────────┘
```

---

## 7. Podsumowanie koncepcji OOP w projekcie

| Koncepcja | Przykład w projekcie |
|-----------|---------------------|
| **Dziedziczenie** | `AplikacjaKlienta extends Application` |
| **Interfejsy** | `ObslugaKlienta implements Runnable`, klasy DTO implementują `Serializable` |
| **Enkapsulacja** | Prywatne pola + publiczne gettery/settery we wszystkich klasach |
| **Polimorfizm** | Pole `Object dane` w `Zadanie` i `Odpowiedz` |
| **Pakiety** | Logiczne grupowanie: `klient`, `serwer`, `wspolne` |
| **Enumeracje** | `TypZadania`, `StatusOdpowiedzi` |
| **Wzorce** | DTO, DAO, Singleton, MVC |
