package com.ispw.bean;


public class DatiAccountBean  {
    

    private int idUtente;
    private String nome;
    private String email;
    private String telefono;
    private String indirizzo;

    public DatiAccountBean() {
        //Nota: costruttore no-args intenzionalmente vuoto.
    }

    public int getIdUtente() { 
        return idUtente; 
    }
    public void setIdUtente(int idUtente) {
         this.idUtente = idUtente; 
        }

    public String getNome() { 
        return nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getEmail() { 
        return email;
     }
    public void setEmail(String email) {
         this.email = email; 
        }

    public String getTelefono() { 
        return telefono; 
    }
    public void setTelefono(String telefono) {
         this.telefono = telefono;
         }

    public String getIndirizzo() { 
        return indirizzo; 
    }
    public void setIndirizzo(String indirizzo) { 
        this.indirizzo = indirizzo; 
    }
}
