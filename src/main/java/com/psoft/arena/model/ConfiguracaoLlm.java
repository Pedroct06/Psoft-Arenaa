package com.psoft.arena.model;

import jakarta.persistence.*;

@Embeddable
public class ConfiguracaoLlm {
    @Column(nullable = false)
    private String nomeModelo;
    @Column(nullable = false, length = 2048)
    private String urlApi;
    @Column(nullable = false, length = 4096)
    private String chaveApiCriptografada;

    protected ConfiguracaoLlm() { }
    public ConfiguracaoLlm(String nomeModelo, String urlApi, String chaveApiCriptografada) {
        this.nomeModelo = nomeModelo;
        this.urlApi = urlApi;
        this.chaveApiCriptografada = chaveApiCriptografada;
    }
    public String getNomeModelo() { return nomeModelo; }
    public String getUrlApi() { return urlApi; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getChaveApiCriptografada() { return chaveApiCriptografada; }
}
