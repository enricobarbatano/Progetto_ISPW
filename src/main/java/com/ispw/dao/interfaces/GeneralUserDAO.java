package com.ispw.dao.interfaces;



import com.ispw.model.entity.GeneralUser;

public interface GeneralUserDAO extends DAO<Integer, GeneralUser> {

    GeneralUser findByEmail(String email);

    GeneralUser findById(int idUtente);
}
