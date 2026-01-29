package com.ispw.dao.factory;


import com.ispw.dao.interfaces.GeneralUserDAO;

public class MemoryDAOFactory extends DAOFactory {

    @Override
    public GeneralUserDAO getGeneralUserDAO() {
        throw new UnsupportedOperationException("MemoryDAOFactory non implementata ancora.");
    }
}


