CREATE TABLE IF NOT EXISTS regole_tempistiche (
    id INT PRIMARY KEY,
    durata_slot INT NOT NULL,
    ora_apertura TIME,
    ora_chiusura TIME,
    preavviso_minimo INT
);