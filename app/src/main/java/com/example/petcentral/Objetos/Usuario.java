package com.example.petcentral.Objetos;

public class Usuario {
    private String nome;
    private String email;
    private String fotoPerfil;

    public Usuario() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Usuario(String email, String fotoPerfil, String nome) {
        this.email = email;
        this.fotoPerfil = fotoPerfil;
        this.nome = nome;
    }
}

