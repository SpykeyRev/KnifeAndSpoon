package com.digiolaba.knifeandspoon.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.digiolaba.knifeandspoon.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowRicetta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ricetta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ricetta);
        Bundle infoToShow=getInfoSelectedRicetta();
        String autore=infoToShow.getString("Autore");
        String thumbnail=infoToShow.getString("Thumbnail");
        String titolo=infoToShow.getString("Titolo");
        List<String> passaggi=(ArrayList<String>)infoToShow.getSerializable("Passaggi");
        List<Map<String, Object>> ingredienti= (List<Map<String, Object>>) infoToShow.getSerializable("Ingredienti");
        toolbar.setTitle(titolo);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_ins_foto);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView testoIngredienti=findViewById(R.id.txtShowIngredienti);
        testoIngredienti.setText(passaggi.get(0).toString());
        Log.i("CIAO",thumbnail);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /*Intent intent = new Intent(ShowRicetta.this, MainActivity.class);
            startActivity(intent);*/
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bundle getInfoSelectedRicetta()
    {
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        return extras;
    }

}
