package com.ispw.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.ispw.model.enums.Permesso;

public final class Gestore extends GeneralUser {
    private final List<Permesso> permessi = new ArrayList<>();

    public List<Permesso> getPermessi() { return permessi; }
}
