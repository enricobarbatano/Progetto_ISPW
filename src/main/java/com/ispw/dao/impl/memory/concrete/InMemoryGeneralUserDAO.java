package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;

public class InMemoryGeneralUserDAO extends In_MemoryDAO<Integer, GeneralUser> implements GeneralUserDAO {

    public InMemoryGeneralUserDAO() {
        super(true);
    }

    @Override
    protected Integer getId(GeneralUser entity) {
        // TODO: return entity.getIdUtente();
        throw new UnsupportedOperationException("TODO: GeneralUser.getIdUtente()");
    }

    @Override
    public GeneralUser findByEmail(String email) {
        throw new UnsupportedOperationException("TODO: findByEmail");
    }

    @Override
    public GeneralUser findById(int idUtente) {
        return load(idUtente);
    }
}
