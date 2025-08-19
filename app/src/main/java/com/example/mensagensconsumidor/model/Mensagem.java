package com.example.mensagensconsumidor.model;

public class Mensagem {
    private final long id;
    private final String texto;
    private final String autor;

    public Mensagem(long id, String texto, String autor) {
        this.id = id;
        this.texto = texto;
        this.autor = autor;
    }

    public long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public String getAutor() {
        return autor;
    }
}