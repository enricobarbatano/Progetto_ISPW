package com.ispw.dao.factory;


import com.ispw.dao.interfaces.GeneralUserDAO;

public class FileSystemDAOFactory extends DAOFactory {

    @Override
    public GeneralUserDAO getGeneralUserDAO() {
        throw new UnsupportedOperationException("FileSystemDAOFactory non implementata ancora.");
    }
}
