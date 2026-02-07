package com.ispw.model.entity;

import java.io.Serializable;
import java.time.LocalTime;

public final class RegoleTempistiche implements Serializable {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity regole tempistiche.
    // A2) IO: configurazione slot e orari.

    private int idConfig = 1;

    private int durataSlot;          // minuti
    private LocalTime oraApertura;
    private LocalTime oraChiusura;
    private int preavvisoMinimo;

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) costruttori: mapping/serializzazione.
    // L2) getters/setters: accesso ai campi.

    public RegoleTempistiche() {}

    public RegoleTempistiche(int durataSlot, LocalTime oraApertura, LocalTime oraChiusura, int preavvisoMinimo) {
        this.durataSlot = durataSlot;
        this.oraApertura = oraApertura;
        this.oraChiusura = oraChiusura;
        this.preavvisoMinimo = preavvisoMinimo;
    }

    public int getIdConfig() { return idConfig; }
    public void setIdConfig(int idConfig) { this.idConfig = idConfig; }

    public int getDurataSlot() { return durataSlot; }
    public void setDurataSlot(int durataSlot) { this.durataSlot = durataSlot; }

    public LocalTime getOraApertura() { return oraApertura; }
    public void setOraApertura(LocalTime oraApertura) { this.oraApertura = oraApertura; }

    public LocalTime getOraChiusura() { return oraChiusura; }
    public void setOraChiusura(LocalTime oraChiusura) { this.oraChiusura = oraChiusura; }

    public int getPreavvisoMinimo() { return preavvisoMinimo; }
    public void setPreavvisoMinimo(int preavvisoMinimo) { this.preavvisoMinimo = preavvisoMinimo; }
}

