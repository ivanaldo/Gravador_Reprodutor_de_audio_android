package com.example.fa.imifadba.activity.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imifadba.R;

public class PrimeiroActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

    }

    private static int SPLASH_TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primeiro);

        new Handler().postDelayed(new Runnable() {
            /*
             * Exibindo splash com um timer.
             */
            @Override
            public void run() {
                // Esse método será executado sempre que o timer acabar
                // E inicia a activity principal
                Intent i = new Intent(PrimeiroActivity.this, AcessoActivity.class);
                startActivity(i);

                // Fecha esta activity
                finishAffinity();
            }
        }, SPLASH_TIME_OUT);
    }
}

