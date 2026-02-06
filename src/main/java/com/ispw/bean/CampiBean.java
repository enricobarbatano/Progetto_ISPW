package com.ispw.bean;

import java.util.ArrayList;
import java.util.List;

public class CampiBean {

    private List<CampoBean> campi = new ArrayList<>();

    public CampiBean() {
        // costruttore di default
    }

    public List<CampoBean> getCampi() {
        return campi;
    }

    public void setCampi(List<CampoBean> campi) {
        this.campi = (campi == null) ? new ArrayList<>() : campi;
    }
}
