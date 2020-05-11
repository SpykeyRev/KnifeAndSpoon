package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class FavouriteActivity extends AppCompatActivity {
    LinearLayout layoutFeedFav;
    private String actualUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actualUser = getIntent().getExtras().get("pathIdUser").toString();
        layoutFeedFav = findViewById(R.id.layoutFeedFav);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        loadRicetteFav();
        super.onResume();
    }


    private void loadRicetteFav() {
        List<Ricetta> ricettas = new ArrayList<>();
        layoutFeedFav.removeAllViews();
        final String documentIdUtente = actualUser.split("/")[1];
        final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
        utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    final List<String> preferiti = (List<String>) result.get("Preferiti");
                    final List<String> deletedPreferiti = new ArrayList<>();
                    if(preferiti.size()==0){
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        closeActivity();
                                        break;

                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(FavouriteActivity.this);
                        builder.setMessage(getString(R.string.no_fav_yet)).setPositiveButton(getString(R.string.error_ok), dialogClickListener)
                                .show();
                    }else{
                        for (int i = 0; i < preferiti.size(); i++) {
                            final int j=i;
                            FirebaseFirestore recipeRef = FirebaseFirestore.getInstance();
                            recipeRef.collection("Ricette").document(preferiti.get(i)).get().addOnCompleteListener(
                                    new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot result = task.getResult();
                                                if (result.exists()) {
                                                    addRicetta(new Ricetta(
                                                            result.getId(),
                                                            result.get("Autore").toString(),
                                                            result.get("Titolo").toString(),
                                                            result.get("Tempo di preparazione").toString(),
                                                            result.get("Numero persone").toString(),
                                                            result.getString("Thumbnail"),
                                                            (List<Map<String, Object>>) result.get("Ingredienti"),
                                                            (List<String>) result.get("Passaggi"),
                                                            (Boolean) result.get("isApproved")
                                                    ));
                                                } else {
                                                    deletedPreferiti.add(preferiti.get(j));
                                                    for (int i = 0; i < deletedPreferiti.size(); i++) {
                                                        preferiti.remove(deletedPreferiti.get(i));
                                                    }
                                                    utentiRef.update("Preferiti", preferiti);
                                                }
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }

            }
        });
    }

    private void addRicetta(final Ricetta ricetta){
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);
        TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
        TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
        TextView txtPersoneFeed = (TextView) addView.findViewById(R.id.txtFeedPersone);
        final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
        Picasso.get().load(ricetta.getThumbnail()).into(ricettaImageFeed);
        txtNomeRicettaFeed.setText(ricetta.getTitle());
        txtTempoPreparazioneFeed.setText(ricetta.getTempo().concat(" minuti"));
        String feedPersone = "Per ".concat(Utils.personaOrPersone(ricetta.getPersone()));
        txtPersoneFeed.setText(feedPersone);
        RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
        layoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Bundle bundle = Utils.loadBundle(ricetta);
                    //Casting from imageSlider to Drawable and conversion into byteArray
                    Drawable d = ricettaImageFeed.getDrawable();
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] bitmapdata = stream.toByteArray();
                    bundle.putByteArray("Thumbnail", bitmapdata);
                    bundle.putBoolean("isAdmin", false);
                    bundle.putString("pathIdUser", actualUser);
                    checkPreferitiOnFirebase(ricetta.getId(),bundle);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        });
        layoutFeedFav.addView(addView);
    }

    private void closeActivity() {
        this.onBackPressed();
        this.finish();
    }


    public void checkPreferitiOnFirebase(final String idRicetta, final Bundle bundle)
    {
        String documentIdUtente = actualUser.split("/")[1];
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
        final Boolean[] found = {false};

        utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshots=task.getResult();
                List<String>preferiti=(List<String>) documentSnapshots.get("Preferiti");
                for (int i = 0; i < preferiti.size(); i++) {
                    if (preferiti.get(i).equals(idRicetta)) {
                        found[0] = true;
                    }
                }
                Intent intent = new Intent(FavouriteActivity.this, ShowRicettaActivity.class);
                bundle.putBoolean("isFav",found[0]);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
