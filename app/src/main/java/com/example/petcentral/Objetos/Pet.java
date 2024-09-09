package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Pet {
    private String id;
    private String nome;
    private String especie;
    private String raca;
    private String sexo;
    private Timestamp dataNascimento;
    private String fotoPerfil;

    public Pet() {

    }

    public Timestamp getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Timestamp dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Pet(Timestamp dataNascimento, String especie, String fotoPerfil, String id, String nome, String raca, String sexo) {
        this.dataNascimento = dataNascimento;
        this.especie = especie;
        this.fotoPerfil = fotoPerfil;
        this.id = id;
        this.nome = nome;
        this.raca = raca;
        this.sexo = sexo;
    }
}
