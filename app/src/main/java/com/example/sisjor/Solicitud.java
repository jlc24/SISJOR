package com.example.sisjor;

public class Solicitud {
    String id, recinto, ubicacion, estado;

    public Solicitud() {
    }

    public Solicitud(String id, String recinto, String ubicacion, String estado) {
        this.id = id;
        this.recinto = recinto;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecinto() {
        return recinto;
    }

    public void setRecinto(String recinto) {
        this.recinto = recinto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
