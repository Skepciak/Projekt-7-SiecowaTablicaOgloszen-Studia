# 🏛️ Sieciowa Tablica Ogłoszeń

Aplikacja klient-serwer do publikowania i przeglądania ogłoszeń w czasie rzeczywistym.

---

## 📋 Wymagania systemowe

### Wymagane oprogramowanie:

| Komponent | Wersja | Link do pobrania |
|-----------|--------|------------------|
| **Java JDK** | 17+ | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) lub [OpenJDK](https://adoptium.net/) |
| **MySQL** | 8.0+ | [MySQL](https://dev.mysql.com/downloads/installer/) lub [Laragon](https://laragon.org/download/) |
| **Maven** | 3.8+ | [Maven](https://maven.apache.org/download.cgi) (opcjonalnie - wbudowany w IDE) |
| **IDE** (opcjonalnie) | - | [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) lub [Eclipse](https://www.eclipse.org/downloads/) |

> **💡 Rekomendacja:** Użyj **Laragon** - zawiera MySQL, automatycznie uruchamia serwer bazy danych.

---

## 🔧 Instalacja krok po kroku

### Krok 1: Instalacja Java JDK 17+

1. Pobierz JDK 17 z [Adoptium](https://adoptium.net/) lub [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. Zainstaluj z domyślnymi opcjami
3. Sprawdź instalację w terminalu:
   ```cmd
   java -version
   ```
   Powinno wyświetlić wersję 17 lub wyższą.

### Krok 2: Instalacja MySQL (lub Laragon)

#### Opcja A: Laragon (zalecane dla Windows)
1. Pobierz [Laragon Full](https://laragon.org/download/)
2. Zainstaluj i uruchom Laragon
3. Kliknij **"Start All"** - MySQL uruchomi się automatycznie
4. Domyślne dane:
   - Host: `localhost`
   - Port: `3306`
   - Użytkownik: `root`
   - Hasło: *(puste)*

#### Opcja B: MySQL Installer
1. Pobierz [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
2. Podczas instalacji ustaw hasło dla użytkownika `root`
3. ⚠️ **Ważne:** Jeśli ustawisz hasło, musisz je zmienić w pliku:
   ```
   src/main/java/pl/tablicaogloszen/serwer/PolaczenieBazy.java
   ```
   Na linii ~31:
   ```java
   private static final String HASLO = "twoje_haslo";
   ```

### Krok 3: Utworzenie bazy danych

1. Otwórz terminal MySQL (lub HeidiSQL/phpMyAdmin w Laragon)
2. Wykonaj skrypt z pliku `baza_danych/schema.sql`:

   **Przez terminal:**
   ```cmd
   mysql -u root -p < baza_danych/schema.sql
   ```

   **Przez HeidiSQL (Laragon):**
   - Kliknij prawym na Laragon → "Database" → "HeidiSQL"
   - Wklej zawartość pliku `schema.sql` i wykonaj (F9)

   **Lub ręcznie w MySQL:**
   ```sql
   CREATE DATABASE IF NOT EXISTS sieciowa_tablica 
   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

---

## 🚀 Uruchamianie aplikacji

### Metoda 1: Z linii poleceń (Maven)

```cmd
cd C:\Users\skept\Documents\GitHub\Projekt-7-SiecowaTablicaOgloszen-Studia
```

#### 1. Kompilacja projektu:
```cmd
mvn clean compile
```

#### 2. Uruchomienie SERWERA (najpierw!):
```cmd
mvn exec:java -Dexec.mainClass="pl.tablicaogloszen.serwer.Serwer"
```
Powinno wyświetlić:
```
===========================================
   SIECIOWA TABLICA OGŁOSZEŃ - SERWER
===========================================
✓ Serwer uruchomiony na porcie 8080
✓ Oczekiwanie na połączenia...
```

#### 3. Uruchomienie KLIENTA (w nowym terminalu):
```cmd
mvn javafx:run
```

### Metoda 2: Z IntelliJ IDEA

1. Otwórz projekt (File → Open → wybierz folder projektu)
2. Poczekaj aż Maven pobierze zależności
3. Uruchom **SERWER** (prawy klik na `Serwer.java` → Run)
4. Uruchom **KLIENTA** (prawy klik na `Start.java` → Run)

### Metoda 3: Z Eclipse

1. File → Import → Maven → Existing Maven Projects
2. Wybierz folder projektu
3. Uruchom `Serwer.java` jako Java Application
4. Uruchom `Start.java` jako Java Application

---

## 📁 Struktura projektu

```
Projekt-7-SiecowaTablicaOgloszen-Studia/
├── src/main/java/pl/tablicaogloszen/
│   ├── klient/           # Aplikacja kliencka (JavaFX)
│   │   ├── AplikacjaKlienta.java
│   │   ├── KontrolerLogowania.java
│   │   ├── KontrolerRejestracji.java
│   │   ├── KontrolerTablicy.java
│   │   ├── KlientSieciowy.java
│   │   └── Sesja.java
│   ├── serwer/           # Serwer TCP
│   │   ├── Serwer.java          ← URUCHOM NAJPIERW
│   │   ├── ObslugaKlienta.java
│   │   ├── OgloszenieDAO.java
│   │   ├── UzytkownikDAO.java
│   │   └── PolaczenieBazy.java
│   └── wspolne/          # Klasy współdzielone
│       ├── OgloszenieDTO.java
│       ├── UzytkownikDTO.java
│       └── ...
├── src/main/resources/   # Pliki FXML (widoki)
├── baza_danych/
│   └── schema.sql        # Schemat bazy danych
├── dokumentacja.puml     # Diagramy UML
└── pom.xml               # Konfiguracja Maven
```

---

## 🔐 Domyślne konto administratora

Po utworzeniu bazy dostępne jest konto:

| Login | Hasło | Rola |
|-------|-------|------|
| `admin` | `admin` | Administrator |

> ⚠️ Hasło `admin` jest niezahashowane w skrypcie SQL. W produkcji należy je zmienić!

---

## ❓ Rozwiązywanie problemów

### Problem: "Nie można połączyć się z serwerem"
- ✅ Upewnij się, że **SERWER** jest uruchomiony przed klientem
- ✅ Sprawdź czy port `8080` nie jest zajęty

### Problem: "Błąd bazy danych / Connection refused"
- ✅ Sprawdź czy MySQL jest uruchomiony (Laragon → "Start All")
- ✅ Sprawdź dane połączenia w `PolaczenieBazy.java`
- ✅ Upewnij się, że baza `sieciowa_tablica` istnieje

### Problem: "Nie znaleziono sterownika MySQL"
- ✅ Uruchom `mvn clean install` aby pobrać zależności

### Problem: JavaFX nie działa
- ✅ Używaj Java 17+
- ✅ Uruchamiaj przez `mvn javafx:run` (nie zwykłe `java -jar`)

---

## 📞 Porty używane przez aplikację

| Usługa | Port |
|--------|------|
| Serwer TCP | `8080` |
| MySQL | `3306` |

---

## 📄 Licencja

Projekt studencki - Sieciowa Tablica Ogłoszeń.
