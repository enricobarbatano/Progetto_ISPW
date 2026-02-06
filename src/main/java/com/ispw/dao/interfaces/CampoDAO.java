package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Campo;

public interface CampoDAO extends DAO<Integer, Campo> {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: DAO per entita Campo.
    // A2) IO: operazioni specifiche di ricerca.
    // ========================

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) findAll/findById: query campo.
    // ========================
    List<Campo> findAll();
    Campo findById(int idCampo);
}