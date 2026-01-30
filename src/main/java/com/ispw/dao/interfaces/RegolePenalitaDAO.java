package com.ispw.dao.interfaces;

//le implementazioni sarrano singleton
import com.ispw.model.entity.RegolePenalita;
public interface RegolePenalitaDAO {
    RegolePenalita get();
    void save(RegolePenalita regole);
}