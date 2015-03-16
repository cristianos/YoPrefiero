package com.permify.yoprefiero.models;

import android.graphics.Bitmap;

/**
 *
 */
public class Perfil {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String telefono;
    private String urlFotografia;
    private String sexo;
    private String ciudad;
    private Integer idRegion;
    private Bitmap fotografia;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Integer getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(Integer idRegion) {
        this.idRegion = idRegion;
    }

    public String getUrlFotografia() {
        return urlFotografia;
    }

    public void setUrlFotografia(String urlFotografia) {
        this.urlFotografia = urlFotografia;
    }

    public Bitmap getFotografia() {
        return fotografia;
    }

    public void setFotografia(Bitmap fotografia) {
        this.fotografia = fotografia;
    }
}
