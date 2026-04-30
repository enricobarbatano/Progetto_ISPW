
package com.ispw.bootstrap.setup;

import java.util.List;

import com.ispw.model.entity.Campo;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Gestore;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.UtenteFinale;

public class SetupData {
    public List<GeneralUser> generalUsers;
    public List<UtenteFinale> utentiFinali;
    public List<Gestore> gestori;
    public List<Campo> campi;
    public RegolePenalita regolePenalita;
    public RegoleTempistiche regoleTempistiche;
}
