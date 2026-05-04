CREATE TABLE IF NOT EXISTS richieste_disdetta (
  id_richiesta INT AUTO_INCREMENT PRIMARY KEY,
  id_prenotazione INT NOT NULL,
  id_utente INT NOT NULL,
  timestamp_richiesta TIMESTAMP NOT NULL,
  timestamp_decisione TIMESTAMP NULL,
  penale_stimata DECIMAL(10,2) NULL,
  rimborso_stimato DECIMAL(10,2) NULL,
  stato VARCHAR(20) NOT NULL,
  nota_utente VARCHAR(255) NULL,
  nota_gestore VARCHAR(255) NULL,
  id_gestore_decisione INT NULL
);