-- =====================================================
-- SIECIOWA TABLICA OGŁOSZEŃ - SCHEMAT BAZY DANYCH
-- Stylizacja: Wiedźmin 3 / Tablica kontraktów
-- =====================================================
-- Tworzenie bazy danych
CREATE DATABASE IF NOT EXISTS sieciowa_tablica CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sieciowa_tablica;

-- Tabela użytkowników
CREATE TABLE IF NOT EXISTS uzytkownicy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    haslo_hash VARCHAR(255) NOT NULL,
    rola ENUM ('UZYTKOWNIK', 'ADMIN') DEFAULT 'UZYTKOWNIK',
    data_utworzenia TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela kategorii (stylizowane na Wiedźmina)
CREATE TABLE IF NOT EXISTS kategorie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nazwa VARCHAR(100) NOT NULL UNIQUE
);

-- Tabela ogłoszeń
CREATE TABLE IF NOT EXISTS ogloszenia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tytul VARCHAR(255) NOT NULL,
    tresc TEXT NOT NULL,
    dane_kontaktowe VARCHAR(255),
    id_kategorii INT,
    id_autora INT,
    data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    wyswietlenia INT DEFAULT 0,
    zgloszenia INT DEFAULT 0,
    FOREIGN KEY (id_kategorii) REFERENCES kategorie (id) ON DELETE SET NULL,
    FOREIGN KEY (id_autora) REFERENCES uzytkownicy (id) ON DELETE CASCADE
);

-- =====================================================
-- KATEGORIE W STYLU WIEDŹMINA 3
-- =====================================================
INSERT IGNORE INTO kategorie (nazwa)
VALUES
    ('Kontrakty na potwory'),
    ('Handel i wymiana'),
    ('Poszukiwani'),
    ('Usługi i rzemiosło'),
    ('Sprawy wioskowe'),
    ('Zaginięcia'),
    ('Ogłoszenia królewskie'),
    ('Inne zlecenia');

-- =====================================================
-- UŻYTKOWNICY
-- =====================================================
-- Admin (hasło zahashowane: admin)
INSERT IGNORE INTO uzytkownicy (login, haslo_hash, rola)
VALUES
    (
        'admin',
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
        'ADMIN'
    );

-- Przykładowi użytkownicy (hasło: test123)
INSERT IGNORE INTO uzytkownicy (login, haslo_hash, rola)
VALUES
    (
        'geralt',
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae',
        'UZYTKOWNIK'
    ),
    (
        'jaskier',
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae',
        'UZYTKOWNIK'
    ),
    (
        'yennefer',
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae',
        'UZYTKOWNIK'
    ),
    (
        'triss',
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae',
        'UZYTKOWNIK'
    ),
    (
        'zoltan',
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae',
        'UZYTKOWNIK'
    );

-- =====================================================
-- PRZYKŁADOWE OGŁOSZENIA W STYLU WIEDŹMINA 3
-- =====================================================
-- Kontrakty na potwory (kategoria 1)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Kontrakt: Gryf z Białego Sadu',
        'Bestia o skrzydłach orła i ciele lwa zaatakowała nasz targ. Trzech kupców nie żyje, towar zniszczony. Potrzebujemy doświadczonego wiedźmina. Nagroda: 300 koron, negocjowalne.',
        'Sołtys Białego Sadu, karczma Pod Złotym Ściegiem',
        1,
        1,
        NOW () - INTERVAL 2 DAY
    ),
    (
        'Kontrakt: Utopce w studni',
        'Coś siedzi w naszej studni. Woda zrobiła się czerwona, a kto próbował zajrzeć - ten nie wrócił. Składamy się całą wsią na nagrodę.',
        'Starszyzna wsi Murky Waters',
        1,
        1,
        NOW () - INTERVAL 5 DAY
    ),
    (
        'Kontrakt: Widłogon w winnicach',
        'Przeklęta gadzina niszczy nasze winogrona! Atakuje nocą, zabija robotników. Płacimy beczką najlepszego Sangreal i sztabką złota.',
        'Winnica Corvo Bianco, Toussaint',
        1,
        1,
        NOW () - INTERVAL 1 DAY
    ),
    (
        'Kontrakt: Leszy w Puszczy Wyzimskiej',
        'Drwale boją się wchodzić do lasu. Mówią o stworzeniu z gałęzi i mchu, które zabija każdego po zmroku. 500 koron za łeb bestii.',
        'Cech Drwali Wyzimskich',
        1,
        2,
        NOW () - INTERVAL 3 DAY
    ),
    (
        'Kontrakt: Wilkołak pod Oxenfurtem',
        'Co pełnia księżyca ginie bydło, a ostatnio zaginął pastuch. Ślady prowadzą do ruin starego młyna. 200 koron nagrody.',
        'Proboszcz parafii w Oxenfurcie',
        1,
        1,
        NOW () - INTERVAL 7 DAY
    );

-- Handel i wymiana (kategoria 2)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Sprzedam miecz wiedźmiński',
        'Srebrne ostrze wykute w Kaer Morhen. Ostrze jak brzytwa, rękojeść owinięta skórą gryfa. Po dziadku, który ponoć znał Vesemira. Cena: 800 koron.',
        'Karczma Pod Pryczą, Oxenfurt',
        2,
        2,
        NOW () - INTERVAL 4 DAY
    ),
    (
        'Kupię gwintowe karty',
        'Kolekcjoner poszukuje rzadkich kart do gwinta. Szczególnie interesuję się bohaterami Północy i kartami nilfgaardzkimi. Płacę złotem!',
        'Turniej gwinta w novigradzkim cyrku',
        2,
        3,
        NOW () - INTERVAL 1 DAY
    ),
    (
        'Wymienię zioła na eliksiry',
        'Posiadam świeży zapas berberysu, celandyny i piołunu. Szukam kogoś kto zna się na alchemii. Wymienię na eliksiry lecznicze.',
        'Zielnik w Hierarch Square, Novigrad',
        2,
        4,
        NOW () - INTERVAL 2 DAY
    ),
    (
        'Na sprzedaż: Koń rasy Nilfgaardzkiej',
        'Doskonały rumak, szkolony przez jeźdźców cesarza. Szybki jak wiatr, posłuszny jak pies. Cena: 2000 koron, ale wart każdej.',
        'Stajnie królewskie w Vizimie',
        2,
        1,
        NOW () - INTERVAL 6 DAY
    );

-- Poszukiwani (kategoria 3)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'POSZUKIWANY: Whoreson Junior',
        'Za zbrodnie przeciwko koronie i ludności cywilnej. Nagroda: 500 koron żywego, 200 martwego. Ostatnio widziany w dokach Novigradu.',
        'Straż Miejska Novigradu',
        3,
        1,
        NOW () - INTERVAL 10 DAY
    ),
    (
        'Poszukiwana: Philippa Eilhart',
        'Czarodziejka oskarżona o zdradę stanu. Niezwykle niebezpieczna, potrafi zmieniać postać w sowę. Nie podchodzić! Zgłosić lokalizację.',
        'Łowcy Czarownic Redanii',
        3,
        1,
        NOW () - INTERVAL 8 DAY
    ),
    (
        'Informacja o bandytach',
        'Banda Pożeraczy Truposza napadła na nasz konwój. Ukrywają się w lasach koło Flotsam. Nagroda za każdą głowę!',
        'Gildia Kupiecka Temerii',
        3,
        1,
        NOW () - INTERVAL 3 DAY
    );

-- Usługi i rzemiosło (kategoria 4)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Nauka szermierki - mistrz z Toussaint',
        'Były kapitan gwardii księżnej Anny Henrietty oferuje lekcje walki mieczem. Style: południowy, północny, nilfgaardzki. Dyskrecja gwarantowana.',
        'Zaułek za Akademią Rycerską, Beauclair',
        4,
        5,
        NOW () - INTERVAL 1 DAY
    ),
    (
        'Kowal poszukuje czeladnika',
        'Stary mistrz z Mahakamu szuka młodego, chętnego do nauki. Nauczę kuć miecze i zbroje. Wymagana siła i cierpliwość.',
        'Kuźnia Pod Młotem, dzielnica krasnoludów',
        4,
        6,
        NOW () - INTERVAL 5 DAY
    ),
    (
        'Wróżenie z kart i kryształów',
        'Madame Irena przepowiada przyszłość. Specjalizacja: miłość, fortuna, unikanie śmierci. Gwarancja 70% trafności!',
        'Namiot przy placu Hierarch, Novigrad',
        4,
        4,
        NOW () - INTERVAL 2 DAY
    ),
    (
        'Naprawiam zbroje i ostrza',
        'Rusznikarz z wieloletnim doświadczeniem. Naprawiam wszystko od mieczów po kusze. Rabat dla wiedźminów!',
        'Warsztat przy bramie wschodniej, Novigrad',
        4,
        6,
        NOW () - INTERVAL 4 DAY
    );

-- Sprawy wioskowe (kategoria 5)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Studnia do oczyszczenia',
        'Woda w studni zaczęła śmierdzieć. Potrzebujemy kogoś odważnego kto zejdzie i sprawdzi co tam siedzi. Płacimy jedzeniem.',
        'Sołtys wsi Białe Sady',
        5,
        1,
        NOW () - INTERVAL 3 DAY
    ),
    (
        'Pomoc przy żniwach',
        'Brakuje rąk do pracy. Wojna zabrała naszych młodych. Każdy chętny dostanie strawę i dach nad głową.',
        'Wieś Lindenvale',
        5,
        1,
        NOW () - INTERVAL 1 DAY
    ),
    (
        'Strach na wróble do naprawy',
        'Nasz strach na wróble się zepsuł, a wrony zjadają ziarno. Szukamy kogoś z talentem do majsterkowania.',
        'Farma pod Oxenfurtem',
        5,
        2,
        NOW () - INTERVAL 6 DAY
    );

-- Zaginięcia (kategoria 6)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Zaginął mój brat - górnik',
        'Poszedł do kopalni tydzień temu i nie wrócił. Inni mówią, że słyszeli krzyki z głębin. Błagam o pomoc!',
        'Chata przy kopalni w Mahakamie',
        6,
        1,
        NOW () - INTERVAL 4 DAY
    ),
    (
        'Zaginęła córka herbatnika',
        'Mała Klara (7 lat) zniknęła podczas jarmarku. Blond włosy, niebieska sukienka. Nagroda za każdą informację!',
        'Dom przy ulicy Złotników, Novigrad',
        6,
        3,
        NOW () - INTERVAL 2 DAY
    ),
    (
        'Gdzie jest mój pies?',
        'Mój owczarek Bursztyn zaginął. Brązowa sierść, jedno ucho sterczące. Ostatnio widziany koło lasu. Tęsknię za nim.',
        'Chłopiec z farmy Mulbrydale',
        6,
        2,
        NOW () - INTERVAL 5 DAY
    );

-- Ogłoszenia królewskie (kategoria 7)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Dekret królewski: Zakaz magii',
        'Z rozkazu Jego Królewskiej Mości Radowida V - wszelkie praktyki magiczne są zakazane pod karą śmierci. Czarodziejów należy zgłaszać Łowcom Czarownic.',
        'Kancelaria Królewska w Oxenfurcie',
        7,
        1,
        NOW () - INTERVAL 14 DAY
    ),
    (
        'Pobór do armii',
        'Każdy zdolny mężczyzna między 16 a 50 rokiem życia winien stawić się w koszarach. Wojna z Nilfgaardem wymaga ofiar!',
        'Dowództwo Armii Redańskiej',
        7,
        1,
        NOW () - INTERVAL 10 DAY
    ),
    (
        'Turniej rycerski w Beauclair',
        'Jej Wysokość Księżna Anna Henrietta zaprasza na coroczny turniej. Nagroda główna: tytuł szlachecki i 10 000 koron!',
        'Pałac w Beauclair, Toussaint',
        7,
        1,
        NOW () - INTERVAL 7 DAY
    );

-- Inne zlecenia (kategoria 8)
INSERT INTO
    ogloszenia (
        tytul,
        tresc,
        dane_kontaktowe,
        id_kategorii,
        id_autora,
        data_dodania
    )
VALUES
    (
        'Szukam towarzysza podróży',
        'Bard wyrusza w trasę po Północy. Poszukuję ochrony - przystojny wiedźmin mile widziany. Płacę piosenkami i towarzystwem!',
        'Jaskier, karczma Pod Dziurawą Stopą',
        8,
        3,
        NOW () - INTERVAL 1 DAY
    ),
    (
        'Nauka czytania i pisania',
        'Uczę prostych ludzi liter i cyfr. Bezpłatnie, z dobroci serca. Przynieść własne pióro i papier.',
        'Świątynia Melitele, Ellander',
        8,
        4,
        NOW () - INTERVAL 3 DAY
    ),
    (
        'Zlecę przeprawę przez Pontar',
        'Muszę przedostać się na drugą stronę rzeki z ładunkiem. Dyskrecja wymagana. Dobrze płacę.',
        'Przystań w Oxenfurcie, łódź z czerwonym żaglem',
        8,
        5,
        NOW () - INTERVAL 2 DAY
    );