-- Migrazione dallo schema "singolare" allo schema allineato ai DAO
-- Esegui a blocchi, solo quelli compatibili con il tuo DB attuale.
-- Consigliato: fare backup prima.

USE centro_sportivo;

-- 1) Crea le tabelle target (se mancanti)
CREATE TABLE IF NOT EXISTS campi (
    id_campo INT PRIMARY KEY,
    nome VARCHAR(255),
    tipo_sport VARCHAR(255),
    costo_orario FLOAT,
    is_attivo BOOLEAN,
    flag_manutenzione BOOLEAN
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prenotazioni (
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

CREATE TABLE IF NOT EXISTS pagamenti (
    id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
    id_prenotazione INT NOT NULL,
    importo_finale DECIMAL(10,2),
    metodo VARCHAR(40),
    stato VARCHAR(40),
    data_pagamento TIMESTAMP NULL,
    FOREIGN KEY (id_prenotazione) REFERENCES prenotazioni(id_prenotazione) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS penalita (
    id_penalita INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    data_emissione DATE,
    importo DECIMAL(10,2),
    motivazione VARCHAR(255),
    stato VARCHAR(40),
    FOREIGN KEY (id_utente) REFERENCES general_user(id_utente) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS regole_tempistiche (
    id INT PRIMARY KEY,
    durata_slot INT,
    ora_apertura TIME,
    ora_chiusura TIME,
    preavviso_minimo INT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS regole_penalita (
    id INT PRIMARY KEY,
    valore_penalita DECIMAL(10,2),
    preavviso_minimo INT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS system_log (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP NULL,
    tipo_operazione VARCHAR(40),
    id_utente_coinvolto INT,
    descrizione VARCHAR(255),
    FOREIGN KEY (id_utente_coinvolto) REFERENCES general_user(id_utente) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2) Migrazione dati da schema singolare (se esistono tabelle vecchie)
--    Se una tabella non esiste, salta il blocco corrispondente.

-- CAMPI: da campo -> campi
INSERT INTO campi (id_campo, nome, tipo_sport, costo_orario, is_attivo, flag_manutenzione)
SELECT id_campo, nome, tipo_campo, NULL, attivo, flag_manutenzione
FROM campo;

-- PRENOTAZIONI: da prenotazione -> prenotazioni
INSERT INTO prenotazioni (id_prenotazione, id_utente, id_campo, data, ora_inizio, ora_fine, stato, notifica_richiesta)
SELECT id_prenotazione, id_utente, id_campo,
       DATE(data_inizio), TIME(data_inizio), TIME(data_fine), stato, NULL
FROM prenotazione;

-- PAGAMENTI: da pagamento -> pagamenti
INSERT INTO pagamenti (id_pagamento, id_prenotazione, importo_finale, metodo, stato, data_pagamento)
SELECT id_pagamento, id_prenotazione, importo, metodo_pagamento, stato_pagamento, data_pagamento
FROM pagamento;

-- PENALITA: da penalita (vecchia) -> penalita (nuova)
-- Se la tua penalita vecchia ha data_applicazione, mappala su data_emissione.
INSERT INTO penalita (id_penalita, id_utente, data_emissione, importo, motivazione, stato)
SELECT id_penalita, id_utente, DATE(data_applicazione), importo, motivazione, NULL
FROM penalita;

-- REGOLE PENALITA: da regola_penalita -> regole_penalita (singleton)
-- Scegli una riga di default (es. la prima) e salvala come id=1.
INSERT INTO regole_penalita (id, valore_penalita, preavviso_minimo)
SELECT 1, percentuale_penalita, preavviso_minimo_minuti
FROM regola_penalita
ORDER BY id_regola_penalita ASC
LIMIT 1
ON DUPLICATE KEY UPDATE valore_penalita=VALUES(valore_penalita), preavviso_minimo=VALUES(preavviso_minimo);

-- 3) Inizializza singleton se vuoti
INSERT INTO regole_tempistiche (id, durata_slot, ora_apertura, ora_chiusura, preavviso_minimo)
VALUES (1, 60, '08:00:00', '22:00:00', 60)
ON DUPLICATE KEY UPDATE durata_slot=VALUES(durata_slot), ora_apertura=VALUES(ora_apertura), ora_chiusura=VALUES(ora_chiusura), preavviso_minimo=VALUES(preavviso_minimo);

INSERT INTO regole_penalita (id, valore_penalita, preavviso_minimo)
VALUES (1, 10.00, 60)
ON DUPLICATE KEY UPDATE valore_penalita=VALUES(valore_penalita), preavviso_minimo=VALUES(preavviso_minimo);

-- 4) (Opzionale) Verifica e pulizia manuale:
-- DROP TABLE campo, prenotazione, pagamento, regola_campo, regola_penalita;