package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.RegoleTempistiche;

public class InMemoryRegoleTempisticheDAO implements RegoleTempisticheDAO {

    private volatile RegoleTempistiche value;

    @Override
    public RegoleTempistiche get() {
        return value;
    }

    @Override
    public void save(RegoleTempistiche regole) {
        this.value = regole;
    }
}
