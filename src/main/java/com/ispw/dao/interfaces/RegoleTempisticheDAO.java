package com.ispw.dao.interfaces;

//le implementazioni sarranno singleton
import com.ispw.model.entity.RegoleTempistiche;
public interface RegoleTempisticheDAO {
    RegoleTempistiche get();
    void save(RegoleTempistiche regole);
}