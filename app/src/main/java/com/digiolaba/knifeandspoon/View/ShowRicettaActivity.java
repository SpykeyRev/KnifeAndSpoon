package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ShowRicettaActivity extends AppCompatActivity {

    private LinearLayout showIngredientiLayout,showPassaggiLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ricetta);
        showIngredientiLayout=findViewById(R.id.layoutIngredientiShow);
        showPassaggiLayout=findViewById(R.id.layoutPassaggiShow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ricetta);
        Bundle infoToShow=getInfoSelectedRicetta();
        String autore=infoToShow.getString("Autore");
        byte[] thumbnail=infoToShow.getByteArray("Thumbnail");
        ImageView img_piatto_show=findViewById(R.id.img_piatto_show);
        Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
        img_piatto_show.setImageDrawable(image);
        String titolo=infoToShow.getString("Titolo");
        List<String> passaggi=(ArrayList<String>)infoToShow.getSerializable("Passaggi");
        List<Map<String, Object>> ingredienti= (List<Map<String, Object>>) infoToShow.getSerializable("Ingredienti");
        String tempo=infoToShow.getString("Tempo");
        String persone=infoToShow.getString("Persone");
        toolbar.setTitle(titolo);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_ins_foto);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView txtPersone=(TextView)findViewById(R.id.txtNumeroPersoneNumber);
        TextView txtTempo=(TextView)findViewById(R.id.txtTempoPreparazioneNumber);
        TextView txtAutore=(TextView)findViewById(R.id.txtAutore);
        txtTempo.setText(tempo.concat(" minuti"));
        txtPersone.setText(personaOrPersone(persone));
        txtAutore.setText(getUsername(autore));
        loadIngredienti(ingredienti);
        loadPassaggi(passaggi);
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

    private String personaOrPersone(String persone)
    {
        if(persone.equals("1"))
        {
            return persone.concat(" persona");
        }
        else
        {
            return persone.concat(" persone");
        }
    }


    private String getUsername(String autore)
    {
        try {
            Utente userRecipe = (Utente) new Utente.getUserInfoByReference(autore).execute().get();
            return userRecipe.getUserName();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadIngredienti(List<Map<String, Object>> ingredienti)
    {
        for(int i=0;i<ingredienti.size();i++)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.show_row_ingrediente, null);
            TextView nomeIngrediente=addView.findViewById(R.id.txtNomeIngrediente);
            TextView quantitaIngrediente=addView.findViewById(R.id.txtQuantitaIngrediente);
            TextView unitaMisuraIngrediente=addView.findViewById(R.id.txtUnitaMisuraIngrediente);
            nomeIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Nome")).toString().concat(" "));
            quantitaIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Quantità")).toString().concat(" "));
            unitaMisuraIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Unità misura")).toString().concat(" "));
            showIngredientiLayout.addView(addView);
        }
    }

    private void loadPassaggi(List<String> passaggi)
    {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addView = layoutInflater.inflate(R.layout.show_row_ingrediente, null);
        //Vaire dichiariazioni textview=addview.findviewbyid(Rblabla);
        //set text edit view
        showPassaggiLayout.addView(addView);

    }
}
