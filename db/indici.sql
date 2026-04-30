CREATE INDEX idx_prenotazioni_data ON prenotazioni(data);
CREATE INDEX idx_prenotazioni_utente ON prenotazioni(id_utente);
CREATE INDEX idx_prenotazioni_campo ON prenotazioni(id_campo);

CREATE INDEX idx_pagamenti_prenotazione ON pagamenti(id_prenotazione);

CREATE INDEX idx_penalita_utente ON penalita(id_utente);