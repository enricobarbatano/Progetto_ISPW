package com.ispw.dao.interfaces;



import java.util.List;

import com.ispw.model.entity.GeneralUser;

public interface GeneralUserDAO extends DAO<Integer, GeneralUser> {

    List<GeneralUser> findAll();

    GeneralUser findByEmail(String email);

    GeneralUser findById(int idUtente);
}
