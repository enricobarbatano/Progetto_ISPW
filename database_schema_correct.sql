-- Schema Database Centro Sportivo (allineato ai DAO del progetto)
-- MySQL/MariaDB

DROP DATABASE IF EXISTS centro_sportivo;
CREATE DATABASE centro_sportivo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE centro_sportivo;

-- Tabella utenti (GeneralUser)
CREATE TABLE general_user (
    id_utente INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    stato_account VARCHAR(40) NOT NULL DEFAULT 'DA_CONFERMARE',
    ruolo VARCHAR(40) NOT NULL DEFAULT 'UTENTE'
) ENGINE=InnoDB;

-- Tabella campi
CREATE TABLE campi (
    id_campo INT PRIMARY KEY,
    nome VARCHAR(255),
    tipo_sport VARCHAR(255),
    costo_orario FLOAT,
    is_attivo BOOLEAN,
    flag_manutenzione BOOLEAN
) ENGINE=InnoDB;

-- Tabella prenotazioni
CREATE TABLE prenotazioni (
    id_prenotazione INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    id_campo INT NOT NULL,
    data DATE,
    ora_inizio TIME,
    ora_fine TIME,
    stato VARCHAR(40),
    notifica_richiesta BOOLEAN,
    FOREIGN KEY (id_utente) REFERENCES general_user(id_utente) ON DELETE CASCADE,
    FOREIGN KEY (id_campo) REFERENCES campi(id_campo) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Tabella pagamenti
CREATE TABLE pagamenti (
    id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
    id_prenotazione INT NOT NULL,
    importo_finale DECIMAL(10,2),
    metodo VARCHAR(40),
    stato VARCHAR(40),
    data_pagamento TIMESTAMP NULL,
    FOREIGN KEY (id_prenotazione) REFERENCES prenotazioni(id_prenotazione) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabella penalità
CREATE TABLE penalita (
    id_penalita INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    data_emissione DATE,
    importo DECIMAL(10,2),
    motivazione VARCHAR(255),
    stato VARCHAR(40),
    FOREIGN KEY (id_utente) REFERENCES general_user(id_utente) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Regole tempistiche (singleton: id=1)
CREATE TABLE regole_tempistiche (
    id INT PRIMARY KEY,
    durata_slot INT,
    ora_apertura TIME,
    ora_chiusura TIME,
    preavviso_minimo INT
) ENGINE=InnoDB;

-- Regole penalità (singleton: id=1)
CREATE TABLE regole_penalita (
    id INT PRIMARY KEY,
    valore_penalita DECIMAL(10,2),
    preavviso_minimo INT
) ENGINE=InnoDB;

-- Log di sistema
CREATE TABLE system_log (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP NULL,
    tipo_operazione VARCHAR(40),
    id_utente_coinvolto INT,
    descrizione VARCHAR(255),
    FOREIGN KEY (id_utente_coinvolto) REFERENCES general_user(id_utente) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Indici utili
CREATE INDEX idx_email ON general_user(email);
CREATE INDEX idx_prenotazioni_utente ON prenotazioni(id_utente);
CREATE INDEX idx_prenotazioni_campo ON prenotazioni(id_campo);
CREATE INDEX idx_log_utente ON system_log(id_utente_coinvolto);
CREATE INDEX idx_log_timestamp ON system_log(timestamp);

-- Righe singleton iniziali (id=1)
INSERT INTO regole_tempistiche (id, durata_slot, ora_apertura, ora_chiusura, preavviso_minimo)
VALUES (1, 60, '08:00:00', '22:00:00', 60)
ON DUPLICATE KEY UPDATE durata_slot=VALUES(durata_slot), ora_apertura=VALUES(ora_apertura), ora_chiusura=VALUES(ora_chiusura), preavviso_minimo=VALUES(preavviso_minimo);

INSERT INTO regole_penalita (id, valore_penalita, preavviso_minimo)
VALUES (1, 10.00, 60)
ON DUPLICATE KEY UPDATE valore_penalita=VALUES(valore_penalita), preavviso_minimo=VALUES(preavviso_minimo);
