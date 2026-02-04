package com.ispw.bean;

public class DatiLoginBean {
    private String email;
    private String password;

    public DatiLoginBean() { /* default ctor for frameworks/serialization */ }

    public DatiLoginBean(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() {
         return password;
         }
    public void setPassword(String password) { 
        this.password = password;
     }
}

