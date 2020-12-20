package com.example.fa.imifadba.activity.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fa.imifadba.activity.adapter.AdapterAnuncios;
import com.example.fa.imifadba.activity.helper.ConfiguracaoFirebase;
import com.example.fa.imifadba.activity.helper.RecyclerItemClickListener;
import com.example.fa.imifadba.activity.model.Anuncio;
import com.example.imifadba.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;
    private boolean filtrandoPorCategoria = false;
    private TextView curso;
    private TextView categoria;
    private List<Anuncio> a = new ArrayList<>();
    private ProgressBar progressbarCurso;
    private  boolean filtEstado = false;
    private boolean resultado = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        inicializarComponentes();

        progressbarCurso = findViewById(R.id.progressBarDetalhesAnuncios);
        progressbarCurso.setVisibility(View.GONE);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        //Configurar RecyclerView
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recyclerAnunciosPublicos.setAdapter( adapterAnuncios );

        //recuperarAnunciosPublicos();

        //Aplicar evento de clique
        recyclerAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Anuncio anuncioSelecionado = listaAnuncios.get( position );
                                Intent i = new Intent(AnunciosActivity.this, DetalhesAnuncioActivity.class);
                                i.putExtra("anuncioSelecionado", anuncioSelecionado );
                                startActivity( i );
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

    public void filtrarPorEstado(View view) {

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
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEstado.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);

            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroEstado = spinnerEstado.getSelectedItem().toString();

                    new DownloadDadoA().execute();

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

    public class DownloadDadoA extends AsyncTask<String,Void,Boolean > {
        @Override
        protected void onPreExecute() {
            progressbarCurso.setVisibility(View.VISIBLE);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            int i = 0;
            while (i <= 10 && resultado == false) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;

                try {
                    Runtime runtime = Runtime.getRuntime();
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
                progressbarCurso.setVisibility(View.GONE);
                filtrandoPorEstado = true;
                filtEstado = true;
                resultado = false;


                if (filtEstado == true) {
                    curso.setText(filtroEstado);
                    categoria.setText("");
                    filtEstado = false;
                }
            } else {

                progressbarCurso.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
            }

        }
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
                        a.add( anuncio );
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
                    new DownloadDadoAs().execute();
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

    public class DownloadDadoAs extends AsyncTask<String,Void,Boolean > {

        @Override
        protected void onPreExecute() {

            progressbarCurso.setVisibility(View.VISIBLE);

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
               progressbarCurso.setVisibility(View.GONE);
               resultado = false;

            } else {

                progressbarCurso.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void recuperarAnunciosPorCategoria(){


        if(filtrandoPorCategoria == true) {
            categoria.setText(filtroCategoria);
        }

        //Configura nó por categoria
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child( filtroCategoria );

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaAnuncios.clear();
                for ( DataSnapshot anuncios : dataSnapshot.getChildren() ){
                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add( anuncio );
                }

                Collections.reverse( listaAnuncios );
                adapterAnuncios.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


   //Meetodo para desativar o botão voltar do celular
   /* @Override
    public void onBackPressed() {

    }*/
   @Override
   public void onBackPressed() {
       Intent i = new Intent(AnunciosActivity.this, PrimeiroActivity.class);
       startActivity(i);
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if( autenticacao.getCurrentUser() == null ){//usuario deslogado
            menu.setGroupVisible(R.id.group_aluno,true);

        }else {//Usuario logado
            menu.setGroupVisible(R.id.group_professor, true);

        }

        return super.onPrepareOptionsMenu(menu);
    }
        @Override
        public boolean onOptionsItemSelected (MenuItem item){

        switch (item.getItemId()) {
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case R.id.menu_aluno:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case R.id.menu_anuncios:
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;
            case R.id.menu_sobre_aluno:
                startActivity(new Intent(getApplicationContext(), SobreActivity.class));
                break;
            case R.id.menu_sobre_professor:
                startActivity(new Intent(getApplicationContext(), SobreActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }


        public void inicializarComponentes () {

        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
        curso = findViewById(R.id.textCurso);
        categoria = findViewById(R.id.textCategoria);

    }
//aguarda tempo para resposta
            /*try {
                new Thread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/


    }
