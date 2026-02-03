package com.ispw.controller.graphic;
import com.ispw.bean.DatiLoginBean;

public interface GraphicLoginController 
extends NavigableController {
    void effettuaLogin(DatiLoginBean credenziali); // delega a LogicControllerGestioneAccesso.verificaCredenziali(...)
    void logout();                                  // delega a LogicControllerGestioneAccesso.logout(...)
    void vaiARegistrazione();                       // nav a "registrazione"
    void vaiAHome();                                // nav a "home"
}
