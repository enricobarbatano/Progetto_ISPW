CREATE TABLE IF NOT EXISTS fatture (
    id_fattura INT AUTO_INCREMENT PRIMARY KEY,
    id_prenotazione INT NOT NULL,
    id_utente INT NOT NULL,
    codice_fiscale_cliente VARCHAR(32),
    data_emissione DATE,
    link_pdf VARCHAR(255),

    CONSTRAINT fk_fattura_prenotazione
        FOREIGN KEY (id_prenotazione)
        REFERENCES prenotazioni(id_prenotazione)
        ON DELETE CASCADE,

    CONSTRAINT fk_fattura_utente
        FOREIGN KEY (id_utente)
        REFERENCES general_user(id_utente)
        ON DELETE CASCADE
);