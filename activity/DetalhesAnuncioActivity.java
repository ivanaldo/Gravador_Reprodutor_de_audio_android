package com.example.fa.imifadba.activity.activity;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fa.imifadba.activity.model.Anuncio;
import com.example.imifadba.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;


public class DetalhesAnuncioActivity extends AppCompatActivity {

    private TextView titulo;
    private TextView estado;
    private TextView categoria;
    private Anuncio anuncioSelecionado;
    private Button reproduceAudio;
    private MediaPlayer mediaPlayer = null;
    private ImageView imageViewF;
    private ProgressBar progressbar;
    private  int progresso;
    private String som;
    private int a;
    private int b = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_anuncio);

        //Configurar toolbar
        getSupportActionBar().setTitle("Aula");

        //Incializar componentes de interface
        inicializarComponentes();

        //Recupera anúncio para exibicao
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if (anuncioSelecionado != null) {

            titulo.setText(anuncioSelecionado.getTitulo());
            estado.setText(anuncioSelecionado.getEstado());
            categoria.setText(anuncioSelecionado.getCategoria());
            imageViewF.setImageURI(Uri.parse(anuncioSelecionado.getFotos()));

            Glide.with(getApplicationContext()).load(anuncioSelecionado.getFotos()).into(imageViewF);
        }

        progressbar = findViewById(R.id.progressBarDetalhesAnuncios);
        progressbar.setVisibility(View.GONE);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        reproduceAudio = findViewById(R.id.buttonAudio);
        reproduceAudio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                progressbar.setVisibility(View.VISIBLE);
                reproduceAudio.setEnabled(false);

                 final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                 final StorageReference storageReference = firebaseStorage.getReferenceFromUrl(anuncioSelecionado.getAudios());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(Uri uri) {

                            som = uri.toString();
                            mediaplaye();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Audio Error", e.getMessage());
                        }
                    });

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i <= 100; i++) {

                                 progresso = i;
                               if (mediaPlayer.isPlaying() == true || b == 1){

                                   break;
                               }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressbar.setProgress(progresso);

                                        if (mediaPlayer.isPlaying() == true) {
                                            progressbar.setVisibility(View.GONE);

                                        }if (progresso == 100 && mediaPlayer.isPlaying() == false) {
                                            Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
                                            progressbar.setVisibility(View.GONE);
                                            reproduceAudio.setEnabled(true);
                                        }
                                    }
                                });

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
        });
    }
    public void mediaplaye() {

        if (progresso != 100) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(som);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
               mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();

            //metodo para saber quando a musica acaba
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    reproduceAudio.setEnabled(true);

                }
            });
        }
    }
    @Override
    public void onBackPressed(){
        b = 1;
           new Contar().execute();
    }

    public class Contar extends AsyncTask<String,Void,Boolean > {

        @Override
        protected Boolean doInBackground(String... params) {

            for (a = 0; a < 10; a++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;

            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean dados) {

           if (dados == true){
               mediaPlayer.stop();
               mediaPlayer.reset();
               finish();
           }

        }
    }
    private void inicializarComponentes () {
            imageViewF = findViewById(R.id.imageViewFoto);
            titulo = findViewById(R.id.textTituloDetalhe);
            categoria = findViewById(R.id.textCategoriaDetalhe);
            estado = findViewById(R.id.textEstadoDetalhe);

        }
    }
