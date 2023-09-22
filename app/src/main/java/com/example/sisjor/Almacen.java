package com.example.sisjor;

public class Almacen {
    private final String nombre;
    private Boolean selected;

    public Almacen(String nombre) {
        this.nombre = nombre;
        this.selected = false;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
