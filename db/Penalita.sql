CREATE TABLE IF NOT EXISTS penalita (
    id_penalita INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    data_emissione DATE,
    importo DECIMAL(10,2),
    motivazione VARCHAR(255),
    stato VARCHAR(50),

    CONSTRAINT fk_penalita_utente
        FOREIGN KEY (id_utente)
        REFERENCES general_user(id_utente)
        ON DELETE CASCADE
);