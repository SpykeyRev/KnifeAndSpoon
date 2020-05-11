package com.digiolaba.knifeandspoon.View;

import android.app.Activity;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowRicettaActivity extends AppCompatActivity {

    private LinearLayout showIngredientiLayout, showPassaggiLayout;
    private Bundle infoToShow;
    private Boolean[] isFavourite;
    private FloatingActionButton fab_favourite;
    private Boolean[] appoggio;

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
        fab_favourite = (FloatingActionButton) findViewById(R.id.fab_favourite);
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
        isFavourite = new Boolean[]{(Boolean) infoToShow.get("isFav")};
        appoggio = new Boolean[]{(Boolean) infoToShow.get("isFav")};
        fabFavouriteSetter();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.onBackPressed();
                this.finish();
                return true;
            }
            case R.id.action_approve: {
                adminApprove();
                return true;
            }
            case R.id.action_disapprove: {
                adminDisapprove();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (infoToShow.getBoolean("isAdmin")) {
            getMenuInflater().inflate(R.menu.menu_show_ricetta, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private Bundle getInfoSelectedRicetta() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        return extras;
    }

    @Override
    protected void onPause() {
        if (isFavourite[0] != appoggio[0]) {
            setPreferiti();
            appoggio[0] = !appoggio[0];
            Intent intent=new Intent();
            intent.putExtra("docRicetta", infoToShow.get("Id").toString());
            setResult(Activity.RESULT_OK,intent);
        }
        super.onPause();
    }

    private void setPreferiti()
    {
        String documentIdUtente = infoToShow.get("pathIdUser").toString().split("/")[1];
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
        utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshots = task.getResult();
                    if (documentSnapshots.exists()) {
                        if (isFavourite[0]) {
                            List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                            preferiti.add(infoToShow.get("Id").toString());
                            utentiRef.update("Preferiti", preferiti);
                        } else {
                            List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                            preferiti.remove(infoToShow.get("Id").toString());
                            utentiRef.update("Preferiti", preferiti);
                        }

                    }
                }
            }
        });
    }

    private String getUsername(String autore) {
        try {
            Utente userRecipe = (Utente) new Utente.getUserInfoByReference(this, autore).execute().get();
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

    private void fabFavouriteSetter() {
        if (!(infoToShow.get("pathIdUser").toString().equals("anonymous"))) {
            if (!infoToShow.getBoolean("isAdmin")) {
                if (!isFavourite[0]) {
                    fab_favourite.setImageResource(R.drawable.favorite);
                } else {
                    fab_favourite.setImageResource(R.drawable.favorite_full);
                }

                fab_favourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isFavourite[0]) {
                            fab_favourite.setImageResource(R.drawable.favorite_full);
                            Utils.showSnackbar(showPassaggiLayout, getString(R.string.added_preferiti));
                            isFavourite[0] = true;
                        } else {
                            fab_favourite.setImageResource(R.drawable.favorite);
                            Utils.showSnackbar(showPassaggiLayout, getString(R.string.removed_preferiti));
                            isFavourite[0] = false;
                        }
                    }
                });
            } else {
                fab_favourite.setVisibility(View.GONE);
            }
        } else {
            fab_favourite.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            fab_favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    GoogleSignInClient client = GoogleSignIn.getClient(ShowRicettaActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                                    client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("EXIT", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowRicettaActivity.this);
                    builder.setMessage(getString(R.string.anonymous_try_new_fav)).setPositiveButton(getString(R.string.let_me_register), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                }
            });
        }
        fab_favourite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isFavourite[0]) {
                    Utils.showSnackbar(showPassaggiLayout, getString(R.string.add_preferiti));
                } else {
                    Utils.showSnackbar(showPassaggiLayout, getString(R.string.remove_preferiti));
                }
                return false;
            }
        });

    }

    private void adminApprove() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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

    private void adminDisapprove() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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

    private void changeBooleanIsApproved() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference ricetteRef = rootRef.collection("Ricette").document((String) infoToShow.get("Id"));
        ricetteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        ricetteRef.update("isApproved", true);
                        adminRedirect();
                    } else {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
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

    private void deleteRicettaAdmin() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference ricetteRef = rootRef.collection("Ricette").document((String) Objects.requireNonNull(infoToShow.get("Id")));
        final StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(Objects.requireNonNull(infoToShow.getString("ThumbnailURL")));
        ricetteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        photoRef.delete();
                        ricetteRef.delete();
                        adminRedirect();
                    } else {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
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

    private void adminRedirect() {
        this.onBackPressed();
        this.finish();
    }
}
