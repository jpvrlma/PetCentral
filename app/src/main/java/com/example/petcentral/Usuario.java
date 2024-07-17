package com.example.petcentral;

import com.google.firebase.Timestamp;

public class Usuario {
    private String nome;
    private String email;
    private String sexo;
    private Timestamp dataNascimento;

    public Usuario() {
    }

    public Usuario(String nome, String email, String sexo, Timestamp dataNascimento) {
        this.nome = nome;
        this.email = email;
        this.sexo = sexo;
        this.dataNascimento = dataNascimento;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Timestamp getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Timestamp dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}

