CREATE TABLE IF NOT EXISTS system_log (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    tipo_operazione VARCHAR(50),
    id_utente_coinvolto INT,
    descrizione TEXT,

    INDEX idx_log_timestamp (timestamp),
    INDEX idx_log_utente (id_utente_coinvolto)
);