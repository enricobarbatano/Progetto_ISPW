package com.ispw.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.ispw.model.enums.Permesso;

public final class Gestore extends GeneralUser {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity gestore.
    // A2) IO: lista permessi.
    // ========================
    private final List<Permesso> permessi = new ArrayList<>();

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getPermessi: accesso ai permessi.
    // ========================

    public List<Permesso> getPermessi() { return permessi; }
}
