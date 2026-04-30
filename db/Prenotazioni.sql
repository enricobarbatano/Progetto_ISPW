CREATE TABLE IF NOT EXISTS prenotazioni (
    id_prenotazione INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    id_campo INT NOT NULL,
    data DATE NOT NULL,
    ora_inizio TIME NOT NULL,
    ora_fine TIME,
    stato VARCHAR(50) NOT NULL,
    notifica_richiesta BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_prenotazione_utente
        FOREIGN KEY (id_utente)
        REFERENCES general_user(id_utente)
        ON DELETE CASCADE,

    CONSTRAINT fk_prenotazione_campo
        FOREIGN KEY (id_campo)
        REFERENCES campi(id_campo)
        ON DELETE CASCADE
);