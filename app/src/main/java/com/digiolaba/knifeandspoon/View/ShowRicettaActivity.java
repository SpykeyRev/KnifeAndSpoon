package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowRicettaActivity extends AppCompatActivity {

    private LinearLayout showIngredientiLayout, showPassaggiLayout;
    private Boolean isAdmin;
    private Bundle infoToShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ricetta);
        showIngredientiLayout = findViewById(R.id.layoutIngredientiShow);
        showPassaggiLayout = findViewById(R.id.layoutPassaggiShow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ricetta);
        infoToShow = getInfoSelectedRicetta();
        String autore = infoToShow.getString("Autore");
        byte[] thumbnail = infoToShow.getByteArray("Thumbnail");
        ImageView img_piatto_show = findViewById(R.id.img_piatto_show);
        Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
        img_piatto_show.setImageDrawable(image);
        String titolo = infoToShow.getString("Titolo");
        List<String> passaggi = (ArrayList<String>) infoToShow.getSerializable("Passaggi");
        List<Map<String, Object>> ingredienti = (List<Map<String, Object>>) infoToShow.getSerializable("Ingredienti");
        String tempo = infoToShow.getString("Tempo");
        String persone = infoToShow.getString("Persone");
        toolbar.setTitle(titolo);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_ins_foto);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView txtPersone = (TextView) findViewById(R.id.txtNumeroPersoneNumber);
        TextView txtTempo = (TextView) findViewById(R.id.txtTempoPreparazioneNumber);
        TextView txtAutore = (TextView) findViewById(R.id.txtAutore);
        txtTempo.setText(tempo.concat(" minuti"));
        txtPersone.setText(Utils.personaOrPersone(persone));
        txtAutore.setText(getUsername(autore));
        loadIngredienti(ingredienti);
        loadPassaggi(passaggi);
        isAdmin=infoToShow.getBoolean("isAdmin");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                /*Intent intent = new Intent(ShowRicetta.this, MainActivity.class);
            startActivity(intent);*/
                this.onBackPressed();
                this.finish();
                return true;
            }
            case R.id.action_approve:
            {
                adminApprove();
                return true;
            }
            case R.id.action_disapprove:
            {
                adminDisapprove();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isAdmin)
        {
            getMenuInflater().inflate(R.menu.menu_show_ricetta,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private Bundle getInfoSelectedRicetta() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        return extras;
    }




    private String getUsername(String autore) {
        try {
            Utente userRecipe = (Utente) new Utente.getUserInfoByReference(autore).execute().get();
            CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
            Picasso.get().load(userRecipe.getUserImage()).into(userImage);
            return userRecipe.getUserName();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadIngredienti(List<Map<String, Object>> ingredienti) {
        for (int i = 0; i < ingredienti.size(); i++) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.show_row_ingrediente, null);
            TextView nomeIngrediente = addView.findViewById(R.id.txtNomeIngrediente);
            TextView quantitaIngrediente = addView.findViewById(R.id.txtQuantitaIngrediente);
            TextView unitaMisuraIngrediente = addView.findViewById(R.id.txtUnitaMisuraIngrediente);
            nomeIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Nome")).toString().concat(" "));
            quantitaIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Quantità")).toString().concat(" "));
            unitaMisuraIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Unità misura")).toString().concat(" "));
            showIngredientiLayout.addView(addView);
        }
    }

    private void loadPassaggi(List<String> passaggi) {
        for (int i = 0; i < passaggi.size(); i++) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.show_row_passaggio, null);
            TextView testoPassaggio = addView.findViewById(R.id.txtPassaggioShow);
            TextView numeroPassaggio = addView.findViewById(R.id.txtNumeroPassaggioShow);
            numeroPassaggio.setText(String.valueOf(i + 1));
            testoPassaggio.setText(passaggi.get(i));
            showPassaggiLayout.addView(addView);
        }
    }

    private void adminApprove()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                            changeBooleanIsApproved();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di voler approvare questa ricetta?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void adminDisapprove()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                            deleteRicettaAdmin();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di NON APPROVARE questa ricetta?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void changeBooleanIsApproved()
    {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference ricetteRef = rootRef.collection("Ricette").document((String) infoToShow.get("Id"));
        ricetteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot=task.getResult();
                    if(documentSnapshot.exists())
                    {
                        ricetteRef.update("isApproved",true);
                        adminRedirect();
                    }
                    else
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        adminRedirect();
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowRicettaActivity.this);
                        builder.setMessage("C'è stato un errore").setPositiveButton("Ricarica", dialogClickListener).show();
                        Log.e("Update error", "Failed with: ", task.getException());
                    }
                }
            }
        });
    }

    private void deleteRicettaAdmin()
    {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference ricetteRef = rootRef.collection("Ricette").document((String) infoToShow.get("Id"));
        ricetteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot=task.getResult();
                    if(documentSnapshot.exists())
                    {
                        ricetteRef.delete();
                        adminRedirect();
                    }
                    else
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        adminRedirect();
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowRicettaActivity.this);
                        builder.setMessage("C'è stato un errore").setPositiveButton("Ricarica", dialogClickListener).show();
                        Log.e("Update error", "Failed with: ", task.getException());
                    }
                }
            }
        });
    }

    private void adminRedirect()
    {
        this.onBackPressed();
        this.finish();
    }
}
