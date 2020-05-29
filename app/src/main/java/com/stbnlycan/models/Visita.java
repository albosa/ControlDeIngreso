package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Visita {

    @SerializedName("visCod")
    @Expose
    private String visCod;

    @SerializedName("visIngreso")
    @Expose
    private String visIngreso;

    @SerializedName("visSalida")
    @Expose
    private String visSalida;

    @SerializedName("visObs")
    @Expose
    private String visObs;

    @SerializedName("visEstado")
    @Expose
    private String visEstado;

    @SerializedName("visitante")
    @Expose
    private Visitante visitante;

    @SerializedName("areaRecinto")
    @Expose
    private AreaRecinto areaRecinto;

    public Visita() {
    }

    public String getVisCod() {
        return visCod;
    }

    public void setVisCod(String visCod) {
        this.visCod = visCod;
    }

    public String getVisIngreso() {
        return visIngreso;
    }

    public void setVisIngreso(String visIngreso) {
        this.visIngreso = visIngreso;
    }

    public String getVisSalida() {
        return visSalida;
    }

    public void setVisSalida(String visSalida) {
        this.visSalida = visSalida;
    }

    public String getVisObs() {
        return visObs;
    }

    public void setVisObs(String visObs) {
        this.visObs = visObs;
    }

    public String getVisEstado() {
        return visEstado;
    }

    public void setVisEstado(String visEstado) {
        this.visEstado = visEstado;
    }

    public Visitante getVisitante() {
        return visitante;
    }

    public void setVisitante(Visitante visitante) {
        this.visitante = visitante;
    }

    public AreaRecinto getAreaRecinto() {
        return areaRecinto;
    }

    public void setAreaRecinto(AreaRecinto areaRecinto) {
        this.areaRecinto = areaRecinto;
    }
}
