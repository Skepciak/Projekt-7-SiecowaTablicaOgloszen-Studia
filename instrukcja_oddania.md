# 📦 PAKIET ODDANIOWY - INSTRUKCJA

Oto kompletna lista rzeczy, które musisz przygotować do oddania, oraz plan działania na zajęciach.

---

## 1. CO WYSŁAĆ PROWADZĄCEMU?

Zazwyczaj prowadzący wymagają dwóch rzeczy:
1. **Sprawozdanie (PDF)** - To wygenerujesz z LaTeXa.
2. **Kod źródłowy (ZIP)** - Spakowany projekt.

### ⚠️ Jak poprawnie spakować kod (WAŻNE!)
Prowadzący **nie chcą** śmieci systemowych ani skompilowanych plików (folder `target`). Projekt musi być czysty.

**Kroki pakowania:**
1. Otwórz terminal w folderze projektu.
2. Wpisz: `mvn clean` (to usunie folder `target` i `dependency` - zmniejszy wagę z 50MB do 200KB!).
3. Usuń (lub po prostu nie zaznaczaj przy pakowaniu) foldery ukryte:
   - `.git`
   - `.vscode`
   - `.gemini`
   - `.history`
4. Spakuj do ZIP tylko:
   - folder `src` (tam jest kod)
   - folder `baza_danych` (tam jest SQL)
   - plik `pom.xml` (konfiguracja Mavena)
   - opcjonalnie: `README.md` (jeśli masz instrukcję)

**Nazwa pliku:** `Projekt7_ImieNazwisko_Grupa.zip`

---

## 2. PRZEBIEG PREZENTACJI (SHOW) 🎬

Masz 5 minut na zrobienie wrażenia. Nie trać czasu na nudy.

### KROK 1: Przygotowanie (Zrób to *PRZED* podejściem prowadzącego)
- [ ] Uruchom **XAMPP/MySQL** (baza musi działać).
- [ ] Otwórz **Excel/Notatnik** ze swoją "ściągą do obrony".
- [ ] Wyczyść terminal w VS Code.
- [ ] Miej otwarty **folder projektu** w VS Code.

### KROK 2: Wstęp (30 sekund)
> "Dzień dobry. Tematem projektu jest **Sieciowa Tablica Ogłoszeń**, ale zrealizowana w unikalnym stylu **Wiedźmińskim**. Architektura to Klient-Serwer na Socketach TCP, wielowątkowa, z bazą MySQL."

### KROK 3: Uruchomienie (1 minuta)
1. **Pokaż kod `Serwer.java`** i powiedz: *"Serwer używa puli wątków ExecutorService do obsługi wielu klientów naraz."*
2. **Uruchom Serwer** na oczach prowadzącego. Pokaż komunikat: `✓ Serwer uruchomiony na porcie 8080`.
3. **Uruchom Klienta (Instancja 1 - Admin):** Zaloguj się jako `admin` / `admin123`.
4. **Uruchom Klienta (Instancja 2 - Gość):** Zaloguj się jako `janusz` / `haslo123` (lub zarejestruj nowe konto).

### KROK 4: Demo Real-Time (2 minuty) - TO JEST EFEKT WOW 🔥
1. Ustaw okna obok siebie (Admin po lewej, Janusz po prawej).
2. Jako **Janusz**: Dodaj nowe ogłoszenie ("Zlecenie na Gryfa").
3. Pokaż, że u **Admina** ogłoszenie pojawiło się **NATYCHMIAST** (bez odświeżania ręcznego). To zasługa mechanizmu "push" w `Serwer.powiadomWszystkich()`.
4. Jako **Admin**: Kliknij "🚩 Zgłoszone Zlecenia" (pokaż, że Janusz tego przycisku nie ma).
5. Jako **Janusz**: Zgłoś jakieś ogłoszenie (kliknij flagę).
6. Jako **Admin**: Pokaż, że zgłoszenie się pojawiło. Usuń ogłoszenie.

### KROK 5: Kod (1 minuta)
Prowadzący zapyta: *"A jak zrobiliście to odświeżanie?"*
Pokaż: `ObslugaKlienta.java` -> pętla `while` i `nasluchuj()` w kliencie.

Prowadzący zapyta: *"Jak zabezpieczone są hasła?"*
Pokaż: `Bezpieczenstwo.java` -> SHA-256.

### KROK 6: Zakończenie
Pokaż wygenerowany **Raport (plik txt)** i podziękuj.

---

## 3. CHECKLISTA PRZED WEJŚCIEM DO SALI ✅
* [ ] Laptop naładowany?
* [ ] JDK 17 ustawione w zmiennych środowiskowych?
* [ ] Baza danych "sieciowa_tablica" istnieje w MySQL?
* [ ] Hasło do bazy w `PolaczenieBazy.java` zgodne z tym na laptopie (puste czy root?)?
* [ ] Projekt kompiluje się bez błędów (`mvn clean compile`)?
* [ ] Masz pod ręką plik PDF ze sprawozdaniem (żeby pokazać diagramy jakby pytał)?

**Powodzenia! Z takim przygotowaniem to formalność.** 🐺
