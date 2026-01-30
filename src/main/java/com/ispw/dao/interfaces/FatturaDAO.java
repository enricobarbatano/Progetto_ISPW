package com.ispw.dao.interfaces;

import com.ispw.model.entity.Fattura;
public interface FatturaDAO extends DAO<Integer, Fattura> {
    Fattura findLastByUtente(int idUtente);
}