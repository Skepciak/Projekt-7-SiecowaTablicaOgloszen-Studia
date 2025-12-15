-- =====================================================
-- SIECIOWA TABLICA OGŁOSZEŃ - SCHEMAT BAZY DANYCH
-- =====================================================

-- Tworzenie bazy danych
CREATE DATABASE IF NOT EXISTS sieciowa_tablica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sieciowa_tablica;

-- Tabela użytkowników
CREATE TABLE IF NOT EXISTS uzytkownicy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    haslo_hash VARCHAR(255) NOT NULL,
    rola ENUM('UZYTKOWNIK', 'ADMIN') DEFAULT 'UZYTKOWNIK',
    data_utworzenia TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela kategorii
CREATE TABLE IF NOT EXISTS kategorie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nazwa VARCHAR(100) NOT NULL UNIQUE
);

-- Tabela ogłoszeń (z polem dane_kontaktowe)
CREATE TABLE IF NOT EXISTS ogloszenia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tytul VARCHAR(255) NOT NULL,
    tresc TEXT NOT NULL,
    dane_kontaktowe VARCHAR(255),
    id_kategorii INT,
    id_autora INT,
    data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_kategorii) REFERENCES kategorie(id) ON DELETE SET NULL,
    FOREIGN KEY (id_autora) REFERENCES uzytkownicy(id) ON DELETE CASCADE
);

-- Przykładowe dane - Kategorie
INSERT IGNORE INTO kategorie (nazwa) VALUES 
('Motoryzacja'), 
('Elektronika'), 
('Nieruchomości'), 
('Praca'), 
('Usługi'),
('Inne');

-- Przykładowy Admin (hasło: admin - w produkcji użyć hashowanego)
INSERT IGNORE INTO uzytkownicy (login, haslo_hash, rola) VALUES 
('admin', 'admin', 'ADMIN');

-- =====================================================
-- MIGRACJA (jeśli tabela już istnieje bez dane_kontaktowe)
-- =====================================================
-- ALTER TABLE ogloszenia ADD COLUMN dane_kontaktowe VARCHAR(255) AFTER tresc;
