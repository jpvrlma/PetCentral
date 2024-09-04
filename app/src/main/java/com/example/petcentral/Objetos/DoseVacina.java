package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class DoseVacina extends Dose{

    private String marca;
    private String lote;
    private String local;
    private String nomeVeterinario;
    private  boolean aplicada;

    public DoseVacina() {
    }

    public DoseVacina(boolean aplicada, String local, String lote, String marca, String nomeVeterinario) {
        this.aplicada = aplicada;
        this.local = local;
        this.lote = lote;
        this.marca = marca;
        this.nomeVeterinario = nomeVeterinario;
    }

    public boolean isAplicada() {
        return aplicada;
    }

    public void setAplicada(boolean aplicada) {
        this.aplicada = aplicada;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getNomeVeterinario() {
        return nomeVeterinario;
    }

    public void setNomeVeterinario(String nomeVeterinario) {
        this.nomeVeterinario = nomeVeterinario;
    }
}
