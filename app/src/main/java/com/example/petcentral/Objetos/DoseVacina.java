package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class DoseVacina extends Dose{

    private String marca;
    private String lote;
    private String local;
    private String nomeVeterinario;

    public DoseVacina() {
    }

    public DoseVacina(String marca, String lote, String local, String nomeVeterinario) {
        this.marca = marca;
        this.lote = lote;
        this.local = local;
        this.nomeVeterinario = nomeVeterinario;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getNomeVeterinario() {
        return nomeVeterinario;
    }

    public void setNomeVeterinario(String nomeVeterinario) {
        this.nomeVeterinario = nomeVeterinario;
    }
}
