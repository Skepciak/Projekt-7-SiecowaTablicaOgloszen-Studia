# 🎬 Scenariusz Prezentacji Projektu
## "Sieciowa Tablica Ogłoszeń" (styl Wiedźmin)
### Czas: ~10-15 minut

---

# CZĘŚĆ 1: WSTĘP (1-2 min)

**Co mówisz:**
> "Dzień dobry. Przedstawię projekt 'Sieciowa Tablica Ogłoszeń' - aplikację klient-serwer w Javie z JavaFX i MySQL. Interfejs jest stylizowany na klimat Wiedźmina - tablica ogłoszeń jak w karczmie, gdzie wiedźmini szukają zleceń."

**Pokaż:** Diagram przypadków użycia

> "System ma trzech aktorów:
> - Gość może się tylko zalogować lub zarejestrować
> - Użytkownik (jak wiedźmin) ma pełny dostęp do tablicy zleceń
> - Administrator (właściciel karczmy) zarządza zgłoszeniami i raportami"

---

# CZĘŚĆ 2: DEMONSTRACJA DZIAŁANIA (5-7 min)

## Krok 1: Uruchom serwer
**Co robisz:** Uruchom `Serwer.java`

**Co mówisz:**
> "Uruchamiam serwer - to jakby otwarcie karczmy. Serwer nasłuchuje na porcie 5000 i obsługuje wielu klientów jednocześnie."

---

## Krok 2: Uruchom klienta - Rejestracja
**Co robisz:** Uruchom `AplikacjaKlienta.java`, kliknij "Zarejestruj"

**Co mówisz:**
> "Klient łączy się z serwerem. Jako gość widzę ekran logowania w ciemnym, klimatycznym stylu. Zarejestruję nowe konto."

**Pokaż:** Wpisz login i hasło

> "Hasła są hashowane SHA-256 - nawet gdyby ktoś włamał się do bazy, nie pozna haseł."

---

## Krok 3: Logowanie i tablica
**Co robisz:** Zaloguj się, pokaż tablicę

**Co mówisz:**
> "Po zalogowaniu widzę tablicę ogłoszeń stylizowaną na pergaminy przypięte do korka. Każde ogłoszenie to karteczka z losową rotacją - jak prawdziwe zlecenia w karczmie."

---

## Krok 4: Dodanie ogłoszenia
**Co robisz:** Kliknij "✨ Nowe Zlecenie", wypełnij formularz

**Co mówisz:**
> "Dodam nowe zlecenie - przycisk 'Nowe Zlecenie' ze znacznikiem ✨. Wybieram kategorię, wpisuję tytuł i treść."

> "Po dodaniu serwer powiadamia wszystkich klientów - każdy widzi nowy pergamin natychmiast."

---

## Krok 5: Filtrowanie i sortowanie
**Co robisz:** Użyj filtrów kategorii, autora, tekstu

**Co mówisz:**
> "Mogę filtrować ogłoszenia według kategorii, autora lub szukać w treści. Mogę też sortować - po dacie, tytule lub popularności."

---

## Krok 6: Szczegóły ogłoszenia
**Co robisz:** Kliknij na kartę ogłoszenia

**Co mówisz:**
> "Kliknięcie otwiera szczegóły i zwiększa licznik wyświetleń - to mierzy popularność ogłoszenia."

---

## Krok 7: Zgłaszanie (jako zwykły użytkownik)
**Co robisz:** Kliknij flagę 🚩 na cudzym ogłoszeniu

**Co mówisz:**
> "Użytkownik może zgłosić nieodpowiednie ogłoszenie klikając flagę. Licznik zgłoszeń rośnie."

---

## Krok 8: Panel administratora
**Co robisz:** Wyloguj, zaloguj jako admin (login: admin, hasło: admin123)

**Co mówisz:**
> "Teraz zaloguję się jako administrator. Widzę dodatkowy przycisk 'Zgłoszone Zlecenia'."

**Pokaż:** Kliknij "Zgłoszone Zlecenia"

> "Tu widzę wszystkie zgłoszone ogłoszenia posortowane od najbardziej zgłaszanych. Mogę je usunąć."

---

## Krok 9: Generowanie raportu
**Co robisz:** Kliknij "Raport"

**Co mówisz:**
> "Administrator może też wygenerować raport tekstowy ze wszystkimi ogłoszeniami i zapisać go do pliku."

---

# CZĘŚĆ 3: ARCHITEKTURA (2-3 min)

**Pokaż:** Diagram klas

**Co mówisz:**
> "Projekt ma trzy pakiety:
> - **wspolne** - klasy DTO do przesyłania danych między klientem a serwerem
> - **serwer** - logika serwerowa, DAO do bazy danych
> - **klient** - aplikacja JavaFX z kontrolerami FXML"

> "Użyłem wzorców projektowych:
> - Singleton dla połączenia z bazą i klienta sieciowego
> - DAO do operacji na bazie
> - MVC do interfejsu graficznego"

---

# CZĘŚĆ 4: KOD (2-3 min)

**Pokaż:** Fragment kodu (wybierz jeden)

## Opcja A: Hashowanie hasła
```java
// Bezpieczenstwo.java
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(haslo.getBytes(UTF_8));
```
> "Hasło jest hashowane SHA-256 - jednokierunkowo, nieodwracalnie."

## Opcja B: Obsługa wielowątkowa
```java
// Serwer.java
ExecutorService executor = Executors.newCachedThreadPool();
executor.execute(new ObslugaKlienta(socket));
```
> "Każdy klient jest obsługiwany w osobnym wątku."

## Opcja C: Powiadomienia real-time
```java
// Serwer.java
for (ObslugaKlienta k : klienci) {
    k.wyslij(new Odpowiedz(ODSWIEZ, null, "Odswiezenie"));
}
```
> "Po każdej zmianie serwer powiadamia wszystkich klientów."

---

# CZĘŚĆ 5: PODSUMOWANIE (1 min)

**Co mówisz:**
> "Podsumowując - zrealizowałem aplikację klient-serwer z:
> - Wielowątkową obsługą wielu klientów
> - Bazą MySQL z JDBC
> - Interfejsem JavaFX
> - Hashowaniem haseł SHA-256
> - Powiadomieniami w czasie rzeczywistym
> - Systemem ról użytkownik/administrator"

> "Dziękuję, jestem gotowy na pytania."

---

# 🔥 MOŻLIWE PYTANIA I ODPOWIEDZI

| Pytanie | Odpowiedź |
|---------|-----------|
| Dlaczego Singleton? | Potrzebuję jednej instancji połączenia z bazą/klienta sieciowego |
| Jak działa wielowątkowość? | ExecutorService tworzy osobny wątek dla każdego klienta |
| Jak hashowane hasła? | SHA-256, jednokierunkowe, nieodwracalne |
| Co to DAO? | Data Access Object - oddziela logikę od bazy danych |
| Jak działają powiadomienia? | Serwer ma listę klientów i wysyła do wszystkich Odpowiedz z statusem ODSWIEZ |
| Dlaczego Serializable? | Żeby przesyłać obiekty przez ObjectOutputStream |
| Jak walidacja danych? | Sprawdzam puste pola, zgodność haseł, uprawnienia przed operacjami |

---

## ✅ CHECKLIST PRZED PREZENTACJĄ
- [ ] Serwer MySQL działa
- [ ] Baza `tablicaogloszen` istnieje
- [ ] Konto admin (admin/admin123) działa
- [ ] Masz kilka przykładowych ogłoszeń w bazie
- [ ] Przetestowałeś wszystkie funkcje
- [ ] Znasz odpowiedzi na pytania powyżej
