package com.digiolaba.knifeandspoon.View;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        TextView txtCategoria = (TextView) findViewById(R.id.txtCategory);
        TextView txtPersone = (TextView) findViewById(R.id.txtNumeroPersoneNumber);
        TextView txtTempo = (TextView) findViewById(R.id.txtTempoPreparazioneNumber);
        txtCategoria.setText(infoToShow.getString("Categoria"));
        ImageView category = (ImageView) findViewById(R.id.categoryImage);
        switch (infoToShow.getString("Categoria")) {
            case "Primo":
                category.setBackgroundResource(R.drawable.primo);
                break;
            case "Secondo":
                category.setBackgroundResource(R.drawable.secondo);
                break;
            case "Contorno":
                category.setBackgroundResource(R.drawable.contorno);
                break;
            case "Antipasto":
                category.setBackgroundResource(R.drawable.antipasto);
                break;
            case "Dolce":
                category.setBackgroundResource(R.drawable.dolce);
                break;
            default:
        }
        ;
        txtTempo.setText(tempo.concat(" minuti"));
        txtPersone.setText(Utils.personaOrPersone(persone));
        loadIngredienti(ingredienti);
        loadPassaggi(passaggi);
        getAndShowUsername(autore);
        isFavourite = new Boolean[]{(Boolean) infoToShow.get("isFav")};
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

    //imposta preferiti su firebase
    private void setPreferiti() {
        String documentIdUtente = infoToShow.get("pathIdUser").toString();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        final DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
        fab_favourite.setClickable(false);
        fab_favourite.setEnabled(false);
        utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshots = task.getResult();
                    if (documentSnapshots.exists()) {
                        if (isFavourite[0]) {
                            List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                            preferiti.add(infoToShow.get("Id").toString());
                            utentiRef.update("Preferiti", preferiti).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    }
                            );
                        } else {
                            List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                            preferiti.remove(infoToShow.get("Id").toString());
                            utentiRef.update("Preferiti", preferiti).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    }
                            );
                        }

                    }
                }
                fab_favourite.setClickable(true);
                fab_favourite.setEnabled(true);
            }
        });
    }

    public void getSetPreferiti() {
        setPreferiti();
    }

    //mostra informazioni utente autore della ricetta
    private void getAndShowUsername(String autore) {
        FirebaseFirestore.getInstance().collection("Utenti").document(autore).get().addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot result = task.getResult();
                            Utente userRecipe = new Utente(
                                    result.getId(),
                                    result.get("Mail").toString(),
                                    result.get("Nome").toString(),
                                    result.get("Immagine").toString(),
                                    (Boolean) result.get("isAdmin"),
                                    (List<String>) result.get("Preferiti")
                            );
                            CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
                            Picasso.get().load(userRecipe.getUserImage()).into(userImage);
                            TextView txtAutore = (TextView) findViewById(R.id.txtAutore);
                            txtAutore.setText(userRecipe.getUserName());
                        }
                    }
                }
        );
        /**/
    }

    //carica gli ingredienti
    private void loadIngredienti(List<Map<String, Object>> ingredienti) {
        for (int i = 0; i < ingredienti.size(); i++) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.show_row_ingrediente, null);
            TextView nomeIngrediente = addView.findViewById(R.id.txtNomeIngrediente);
            TextView quantitaIngrediente = addView.findViewById(R.id.txtQuantitaIngrediente);
            TextView unitaMisuraIngrediente = addView.findViewById(R.id.txtUnitaMisuraIngrediente);
            nomeIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Nome")).toString().concat(" "));
            if (!Objects.requireNonNull(ingredienti.get(i).get("Quantità")).toString().equals("0")) {
                quantitaIngrediente.setText(Objects.requireNonNull(ingredienti.get(i).get("Quantità")).toString().concat(" "));
            } else {
                quantitaIngrediente.setText("");
            }
            unitaMisuraIngrediente.setText(getCorrectUtForm(ingredienti.get(i).get("Unità misura").toString(), ingredienti.get(i).get("Quantità").toString()));
            showIngredientiLayout.addView(addView);
        }
    }

    //impostazione del testo dell'ingrediente in base alla quantità e unità misura
    private String getCorrectUtForm(String ut, String qt) {
        Float quant = Float.parseFloat(qt);
        String correctForm = "";
        if (quant == 1) {
            if (ut.equals("grammi")) {
                correctForm = "grammo";
            } else if (ut.equals("litri")) {
                correctForm = "litro";
            } else if (ut.equals("millilitri")) {
                correctForm = "millilitro";
            } else {
                correctForm = ut;
            }
        } else {
            if (ut.equals("bicchiere")) {
                if (quant >= 2) {
                    correctForm = "bicchieri";
                } else {
                    correctForm = ut;
                }
            } else if (ut.equals("cucchiaio")) {
                correctForm = "cucchiai";
            } else if (ut.equals("cucchiaino")) {
                correctForm = "cucchiaini";
            } else {
                correctForm = ut;
            }
        }
        return correctForm;
    }

    //caricamento ui dei passaggi
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

    //controllo per impostare il pulsante dei preferiti
    private void fabFavouriteSetter() {
        //se non è un utente anonimo
        if (!(infoToShow.get("pathIdUser").toString().equals("anonymous"))) {
            //se l'utente non proviene da RicetteToApproveActivity, quindi è stata selezionata una ricetta approvata
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
                        checkConnection("setPreferiti");
                    }
                });
            } else {
                //se l'utente (admin) proviene da RicetteToApproveActivity non deve essere visualizzato il pulsante dei preferiti
                fab_favourite.setVisibility(View.GONE);
            }
        } else {
            //se l'utente è anonimo
            fab_favourite.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            //al click verrà chiesto di loggarsi tramite Google, poichè la funzionalità è riservata agli utenti registrati
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
                        checkConnection("changeBooleanIsApproved");
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
                        checkConnection("deleteRicettaAdmin");
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
        //aggiorna il campo isApproved a true su firebase
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
                        /*se due utenti admin accedono alla stessa ricetta e il primo la rimuove e il secondo la approva si genererebbe un'eccezione:
                        poichè la ricetta non esiste più non è possibile modificare i suoi campi
                         */
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
                    }
                }
            }
        });
    }

    public void getChangeBooleanIsApproved() {
        changeBooleanIsApproved();
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
                        //rimuove dallo storage l'immagine della ricetta
                        photoRef.delete();
                        //rimuove dal db la ricetta
                        ricetteRef.delete();
                        adminRedirect();
                    } else {
                        /*se due utenti admin accedono alla stessa ricetta e il primo la rimuove e il secondo la rimuove si genererebbe un'eccezione:
                        poichè la ricetta non esiste più non è possibile modificare i suoi campi
                         */
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
                    }
                }
            }
        });
    }

    public void getDeleteRicettaAdmin() {
        deleteRicettaAdmin();
    }

    private void adminRedirect() {
        this.onBackPressed();
        this.finish();
    }

    private void checkConnection(final String methodInString) {
        try {
            final Method method = getClass().getMethod("get" + methodInString.substring(0, 1).toUpperCase() + methodInString.substring(1));
            boolean conn = isNetworkAvailable();
            if (!conn) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                checkConnection(methodInString);
                                break;

                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.error_connection)).setPositiveButton(getString(R.string.error_ok), dialogClickListener).setCancelable(false)
                        .show();
            } else {
                try {
                    method.invoke(ShowRicettaActivity.this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
