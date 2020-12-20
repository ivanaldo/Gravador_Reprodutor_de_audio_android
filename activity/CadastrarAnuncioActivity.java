package com.example.fa.imifadba.activity.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fa.imifadba.activity.helper.ConfiguracaoFirebase;
import com.example.fa.imifadba.activity.helper.Permissoes;
import com.example.fa.imifadba.activity.model.Anuncio;
import com.example.imifadba.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText campoTitulo;
    private ImageView imagem1;
    private Spinner campoEstado, campoCategoria;
    private Anuncio anuncio;
    private StorageReference storage;
    private AlertDialog dialog;
    private Button mRecordBtn;
    private TextView mRecordLabel;
    private MediaRecorder mRecord = null;
    private int i = 0;
    private  boolean parar = false;
    private String audioimi = null;
    private static final String LOG_TAG  = "Record_log";
    private MediaPlayer mediaPlayer;
    private Button reproduzirAudio;


    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private String listaFotosRecuperadas;
    private String listaURLFotos;

    private String listaAudiosRecuperados;
    private String listaURLAudios;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //Configurações iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();

        //storage = FirebaseStorage.getInstance().getReference();

        mRecordLabel = findViewById(R.id.recordeLabel);
        mRecordBtn = findViewById(R.id.buttonBtn);
        audioimi = Environment.getExternalStorageDirectory().getAbsolutePath();
        audioimi += "/recorder_audio 3gp";



        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    startRecording();
                    mRecordLabel.setText("Iniciando gravação");


                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){

                    stopRecording();
                    mRecordLabel.setText("Gravação finalizada");


                }


                return true;
            }
        });

    }


    private  void startRecording(){
        try {
        mRecord = new MediaRecorder();
        mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecord.setOutputFile(audioimi);
        mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecord.prepare();
            mRecord.start();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"Problema com seu gravador de áudio, tente novamente!", Toast.LENGTH_LONG).show();
        }

    }

    private void stopRecording(){
    try {
    mRecord.stop();
    mRecord.release();
    mRecord = null;
    }catch (Exception e){
    Log.e(LOG_TAG,"Problema com seu gravador de áudio, tente novamente!");
    }
        listaAudiosRecuperados = audioimi;
    }

    public void mediaPlayerR (View view){
        reproduzirAudio.setEnabled(false);
        try {
            Uri uri = Uri.parse( Environment.getExternalStorageDirectory().getAbsolutePath()+"/recorder_audio 3gp" ) ;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource( getApplicationContext() , uri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //metodo para saber quando a musica acaba
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();
                reproduzirAudio.setEnabled(true);

            }
        });
    }

    public void dialog(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Postagem")
                .setCancelable(false)
                .build();
        dialog.show();
    }

    public void salvarAnuncio(){

            //Salvar imagem no Storage
            String urlImagem = listaFotosRecuperadas;
            salvarFotoStorage(urlImagem);

            //Salvar audio no Storage
            String urlAudio = listaAudiosRecuperados;
            salvarAudioStorage(urlAudio);
    }

    private void salvarFotoStorage(String urlString){

        //Criar nó no storage
        final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child( anuncio.getIdAnuncio() )
                .child("imagem");

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile( Uri.parse(urlString) );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrli) {

                String urlConvertida = downloadUrli.toString();

                listaURLFotos = urlConvertida ;
                    anuncio.setFotos( listaURLFotos );
                    salvarJuntos();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
                });
            }
        });
    }

    private void salvarAudioStorage(String audio) {

        //Criar nó no storage
        final StorageReference audioAnuncio = storage.child("audios")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("audios");

        //Fazer upload do arquivo
        final UploadTask uploadTask = audioAnuncio.putFile( Uri.fromFile(new File(audio)));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                audioAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {

                        String urlConvert = downloadUrl.toString();

                        listaURLAudios = urlConvert;
                        anuncio.setAudios(listaURLAudios);
                        salvarJuntos();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        exibirMensagemErro("Falha ao fazer upload");
                        Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
                    }
                });
            }
            });
    }

    private void salvarJuntos(){

        if(  listaURLFotos != null && listaURLAudios != null ) {

            anuncio.salvar();
            dialog.dismiss();
            parar = true;
            finish();
        }
    }




    private Anuncio configurarAnuncio(){

        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();


        Anuncio anuncio = new Anuncio();
        anuncio.setEstado( estado );
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);


        return anuncio;

    }

    public void validarDadosAnuncio(View view) {

        anuncio = configurarAnuncio();
        if (listaFotosRecuperadas !=  null) {
            if (!anuncio.getEstado().isEmpty()) {
                if (!anuncio.getCategoria().isEmpty()) {
                    if (!anuncio.getTitulo().isEmpty()) {
                       if (listaAudiosRecuperados != null) {

                           salvarAnuncio();
                           contar10();
                           dialog();

                        }else {
                            exibirMensagemErro("Grave um áudio");
                        }
                    } else {
                        exibirMensagemErro("Preencha o campo título");
                    }
                } else {
                    exibirMensagemErro("Preencha o campo categoria");
                }
            } else {
                exibirMensagemErro("Preencha o campo curso");
            }
        } else {
            exibirMensagemErro("Selecione uma foto!");
        }

    }

    private void exibirMensagemErro(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "onClick: " + v.getId() );
        switch ( v.getId() ){
            case R.id.imageCadastro1 :
                Log.d("onClick", "onClick: " );
                escolherImagem(1);
                break;

        }

    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK){

            //Recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if( requestCode == 1 ){
                imagem1.setImageURI( imagemSelecionada );

            }

            listaFotosRecuperadas = caminhoImagem;

        }

    }

    private void carregarDadosSpinner(){

        //Configura spinner de estados
        String[] estados = getResources().getStringArray(R.array.Cursos);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoEstado.setAdapter( adapter );

        //Configura spinner de categorias
        String[] categorias = getResources().getStringArray(R.array.Categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoCategoria.setAdapter( adapterCategoria );


    }
    public void contar10(){

            new Thread(new Runnable() {
                public void run() {

                    for (i = 0; i < 10; i++){
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    new DownloadDado().execute();

                    }
                }
            }).start();
    }

    public class DownloadDado extends AsyncTask<String,Void,Boolean > {

        @Override
        protected Boolean doInBackground(String... params) {



                Runtime runtime = Runtime.getRuntime();
                try {
                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
                    int mExitValue = mIpAddrProcess.waitFor();

                    if (mExitValue == 0) {

                        return true;

                    }else{

                        return false;
                    }

                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    System.out.println(" Exception:" + ignore);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(" Exception:" + e);
                }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean dados) {
            // O resultado da execução em background é passado para este passo como um parâmetro.
            if (dados == true) {


            } if (dados == false) {
                if (i == 10) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
                }
            }

        }
    }


    private void inicializarComponentes(){

        campoTitulo = findViewById(R.id.editTitulo);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem1.setOnClickListener(this);
        reproduzirAudio = findViewById(R.id.botaoTeste);

        //Configura localidade para pt -> portugues BR -> Brasil
        Locale locale = new Locale("pt", "BR");


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults ){
            if( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
