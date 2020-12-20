package com.example.fa.imifadba.activity.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imifadba.R;

public class AcessoActivity extends AppCompatActivity {
    //Meetodo para desativar o botão voltar do celular
    @Override
    public void onBackPressed() {

    }
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acesso);

        new Handler().postDelayed(new Runnable() {
            /*
             * Exibindo splash com um timer.
             */
            @Override
            public void run() {
                // Esse método será executado sempre que o timer acabar
                // E inicia a activity principal
                Intent i = new Intent(AcessoActivity.this, AnunciosActivity.class);
                startActivity(i);

                // Fecha esta activity
                finish();


            }
        }, SPLASH_TIME_OUT);
    }
}
