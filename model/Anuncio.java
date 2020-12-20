package com.example.fa.imifadba.activity.model;
import com.example.fa.imifadba.activity.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.Serializable;
import java.util.List;


public class Anuncio implements Serializable {

    private String idAnuncio;
    private String estado;
    private String categoria;
    private String titulo;
    private String fotos;
    private String audios;

    public Anuncio() {
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios");
        setIdAnuncio( anuncioRef.push().getKey() );

    }

    public void salvar(){

        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios");

        anuncioRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);

        salvarAnuncioPublico();

    }

    public void salvarAnuncioPublico(){

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        anuncioRef.child( getEstado() )
                .child( getCategoria() )
                .child( getIdAnuncio() )
                .setValue(this);

    }

    public void remover(){

        String idUsuario = ConfiguracaoFirebase.getIdUsuario();
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child( idUsuario )
                .child( getIdAnuncio() );

        anuncioRef.removeValue();
        removerAnuncioPublico();

    }

    public void removerAnuncioPublico() {

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(getEstado())
                .child(getCategoria())
                .child(getIdAnuncio());

        anuncioRef.removeValue();

        StorageReference  storageReference = ConfiguracaoFirebase.getFirebaseStorage()
                .child("audios")
                .child("anuncios")
                .child( getIdAnuncio() )
                .child("audios");
        storageReference.delete();

        StorageReference  storageReferences = ConfiguracaoFirebase.getFirebaseStorage()
                .child("imagens")
                .child("anuncios")
                .child( getIdAnuncio() )
                .child("imagem");
        storageReferences.delete();

    }

    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAudios() { return audios; }

    public void setAudios(String audios) { this.audios = audios; }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

}
