package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.RegolePenalita;

public class InMemoryRegolePenalitaDAO implements RegolePenalitaDAO {

    private volatile RegolePenalita value;

    @Override
    public RegolePenalita get() {
        return value;
    }

    @Override
    public void save(RegolePenalita regole) {
        this.value = regole;
    }
}
