CREATE TABLE IF NOT EXISTS pagamenti (
    id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
    id_prenotazione INT NOT NULL,
    importo_finale DECIMAL(10,2),
    metodo VARCHAR(50),
    stato VARCHAR(50),
    data_pagamento DATETIME,

    CONSTRAINT fk_pagamento_prenotazione
        FOREIGN KEY (id_prenotazione)
        REFERENCES prenotazioni(id_prenotazione)
        ON DELETE CASCADE
);