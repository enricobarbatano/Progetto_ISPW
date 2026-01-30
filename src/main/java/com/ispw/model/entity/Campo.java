package com.ispw.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Campo implements Serializable{
    private int idCampo;
    private String nome;
    private String tipoSport;
    private Float costoOrario;
    private boolean isAttivo;
    private boolean flagManutenzione;

    // opzionale (evita di popolarla sempre nei DAO)
    private final List<Prenotazione> listaPrenotazioni = new ArrayList<>();



    public int getIdCampo() { return idCampo; }
    public void setIdCampo(int idCampo) { this.idCampo = idCampo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipoSport() { return tipoSport; }
    public void setTipoSport(String tipoSport) { this.tipoSport = tipoSport; }

    public Float getCostoOrario() { return costoOrario; }
    public void setCostoOrario(Float costoOrario) { this.costoOrario = costoOrario; }

    public boolean isAttivo() { return isAttivo; }
    public void setAttivo(boolean attivo) { isAttivo = attivo; }

    public boolean isFlagManutenzione() { return flagManutenzione; }
    public void setFlagManutenzione(boolean flagManutenzione) { this.flagManutenzione = flagManutenzione; }

    
    public List<Prenotazione> getListaPrenotazioni() {
    return Collections.unmodifiableList(listaPrenotazioni);
    }
    public void aggiungiPrenotazione(Prenotazione p) {
    listaPrenotazioni.add(p);
    }

}
