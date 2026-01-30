package com.ispw.dao.interfaces;

import java.util.List;

import com.ispw.model.entity.Campo;

public interface CampoDAO extends DAO<Integer, Campo> {
    List<Campo> findAll();
    Campo findById(int idCampo);
}