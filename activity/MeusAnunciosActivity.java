package com.example.fa.imifadba.activity.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa.imifadba.activity.adapter.AdapterAnuncios;
import com.example.fa.imifadba.activity.helper.ConfiguracaoFirebase;
import com.example.fa.imifadba.activity.helper.RecyclerItemClickListener;
import com.example.fa.imifadba.activity.model.Anuncio;
import com.example.imifadba.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeusAnunciosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;
    private boolean filtrandoPorCategoria = false;
    private TextView meuCurso;
    private TextView minhaCategoria;
    private List<Anuncio> b = new ArrayList<>();
    private boolean remover = false;
    private AlertDialog alerta;
    private ProgressBar progressbarCursos;
    private  boolean filtEstado = false;
    private boolean resultado = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        //Configurações iniciais
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child( ConfiguracaoFirebase.getIdUsuario() );

        inicializarComponentes();

        progressbarCursos = findViewById(R.id.progressBarMeuAnuncio);
        progressbarCursos.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurar RecyclerView
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerAnuncios.setAdapter( adapterAnuncios );

        //Adiciona evento de clique no recyclerview
        recyclerAnuncios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @Override
                            public void onItemClick(View view, final int position) {
                               // alerta
                                AlertDialog.Builder builder = new AlertDialog.Builder(MeusAnunciosActivity.this);//Cria o gerador do AlertDialog
                                builder.setTitle("Atenção");//define o titulo
                                builder.setMessage("Deseja apagar essa postagem ?");//define a mensagem
                                //define um botão como positivo
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Anuncio anuncioSelecionado = anuncios.get(position);
                                        anuncioSelecionado.remover();
                                        adapterAnuncios.notifyDataSetChanged();
                                        remover = true;
                                    }
                                });
                                //define um botão como negativo.
                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Toast.makeText(MeusAnunciosActivity.this, "Você clicou no botão Cancelar", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                alerta = builder.create();//cria o AlertDialog
                                alerta.show();//Exibe




                            }

                            @Override
                            public void onLongItemClick(View view, int position) {


                            }
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    public void filtrarPorEstado(View view){

        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o curso desejado");

        //Configurar spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        //Configura spinner de estados
        final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.Cursos);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinnerEstado.setAdapter( adapter );

        dialogEstado.setView( viewSpinner );

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroEstado = spinnerEstado.getSelectedItem().toString();

                new DownloadDado().execute();

            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();

    }

    public class DownloadDado extends AsyncTask<String,Void,Boolean > {

        @Override
        protected void onPreExecute() {

            progressbarCursos.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(String... params) {

            int i = 0;
            while (i < 10 && resultado == false) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
                    int mExitValue = mIpAddrProcess.waitFor();

                    if (mExitValue == 0) {
                        resultado = true;
                        return true;

                    }

                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    System.out.println(" Exception:" + ignore);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(" Exception:" + e);
                }

            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean dados) {
            // O resultado da execução em background é passado para este passo como um parâmetro.
            if (dados == true) {
                recuperarAnunciosPorEstado();
                progressbarCursos.setVisibility(View.GONE);
                filtrandoPorEstado = true;
                filtEstado = true;
                resultado = false;

                if (filtEstado == true) {
                    meuCurso.setText(filtroEstado);
                    minhaCategoria.setText("");
                    filtEstado = false;
                }
            } else {

                progressbarCursos.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
            }


        }
    }

    public void filtrarPorCategoria(View view){

        if( filtrandoPorEstado == true ) {

                AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
                dialogEstado.setTitle("Selecione a categoria desejada");

                //Configurar spinner
                View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

                //Configura spinner de categorias
                final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
                String[] estados = getResources().getStringArray(R.array.Categorias);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_item,
                        estados
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);

                dialogEstado.setView(viewSpinner);

                dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                        filtrandoPorCategoria = true;

                        new DownloadDados().execute();

                    }
                });

                dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog dialog = dialogEstado.create();
                dialog.show();

        }else {
            Toast.makeText(this, "Escolha primeiro um curso!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public class DownloadDados extends AsyncTask<String,Void,Boolean > {

        @Override
        protected void onPreExecute() {

            progressbarCursos.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(String... params) {

            int i = 0;
            while (i < 10 && resultado == false) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
                    int mExitValue = mIpAddrProcess.waitFor();

                    if (mExitValue == 0) {
                        resultado = true;
                        return true;

                    }

                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    System.out.println(" Exception:" + ignore);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(" Exception:" + e);
                }

            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean dados) {
            // O resultado da execução em background é passado para este passo como um parâmetro.
            if (dados == true) {
                recuperarAnunciosPorCategoria();
                progressbarCursos.setVisibility(View.GONE);
                resultado = false;

            } else {

                progressbarCursos.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
            }


        }
    }


    public void recuperarAnunciosPorCategoria(){


        if(filtrandoPorCategoria == true) {
            minhaCategoria.setText(filtroCategoria);
        }

        //Configura nó por categoria
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child( filtroCategoria );

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                anuncios.clear();
                for ( DataSnapshot ds : dataSnapshot.getChildren() ){
                    anuncios.add( ds.getValue(Anuncio.class) );
                }

                Collections.reverse( anuncios );
                adapterAnuncios.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void recuperarAnunciosPorEstado(){

        Toast.makeText(this, "Escolha uma categoria!",
                Toast.LENGTH_SHORT).show();

        //Configura nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();
                for (DataSnapshot categorias: dataSnapshot.getChildren() ){
                    for(DataSnapshot anuncios: categorias.getChildren() ){

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        b.add( anuncio );

                    }
                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void inicializarComponentes(){

        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
        meuCurso = findViewById(R.id.textCursos);
        minhaCategoria = findViewById(R.id.textCategorias);

    }

}
