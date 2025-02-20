package br.com.alura.screenmatch.model;

public enum Categoria {

    ACAO("Action"),
    ROMANCE("Romance"),
    COMEDIA("Comedy"),
    DRAMA("Drama"),
    CRIME("Crime");

    private String categoriaOmdb;


    Categoria(String categoriaOmdb) {
        this.categoriaOmdb = categoriaOmdb;
        
    }
}
