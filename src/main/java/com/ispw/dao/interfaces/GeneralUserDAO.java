package com.ispw.dao.interfaces;


import com.ispw.model.entity.GeneralUser;

public interface GeneralUserDAO {
    GeneralUser findByEmail(String email);
    GeneralUser findById(int idUtente);
}
