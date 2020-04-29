package com.digiolaba.knifeandspoon.Model;

public class Utente {
    private String id;
    private String mail;
    private String nome;
    private Boolean isAdmin;
    private String image;

    Utente(String id, String mail,String nome,String image,Boolean isAdmin){
        this.id=id;
        this.mail=mail;
        this.nome=nome;
        this.image=image;
        this.isAdmin=isAdmin;
    }

    public String getUserId(){
        return this.id;
    }

    public String getUserMail(){
        return this.mail;
    }

    public String getUserName(){
        return this.nome;
    }

    public String getUserImage(){
        return this.image;
    }

    public boolean isAdmin(){
        return this.isAdmin;
    }
}
