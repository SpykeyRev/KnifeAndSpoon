package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private ImageButton back;
    private EditText mSearchBar;
    private ImageButton mSearchBtn;
    private List<Ricetta> ricettas;
    private LinearLayout listLayout;
    private Intent intentMain;
    private static int LAUNCH_SHOW_RICETTA_ACTIVITY = 2912;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        intentMain = getIntent();

        back=(ImageButton) findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mSearchBtn = (ImageButton) findViewById(R.id.btn_search);

        listLayout = (LinearLayout) findViewById(R.id.LayoutFeedSearch);
        searchEvent();

    }

    private void searchEvent() {

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingListAndView();
            }
        });
        mSearchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER: {
                            loadingListAndView();
                        }
                        return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void loadingListAndView() {
        if (mSearchBar.getText().length() <= 0) {
            Utils.showSnackbar(listLayout, "Per favore inserisci qualcosa da ricercare");
        } else {
            final String SearchRicetta = mSearchBar.getText().toString();
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            CollectionReference ricetteRef = rootRef.collection("Ricette");
            Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", true);
            ricettas=new ArrayList<Ricetta>();
            queryrRicettaApprovata.get().addOnCompleteListener(
                    new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            QuerySnapshot result = task.getResult();
                            if (task.isSuccessful()) {
                                for (int i = 0; i < result.size(); i++) {
                                    if (result.getDocuments().get(i).get("Titolo").toString().toLowerCase().contains(SearchRicetta.toLowerCase())) {
                                        ricettas.add(new Ricetta(
                                                result.getDocuments().get(i).getId(),
                                                result.getDocuments().get(i).get("Autore").toString(),
                                                result.getDocuments().get(i).get("Titolo").toString(),
                                                result.getDocuments().get(i).get("Categoria").toString(),
                                                result.getDocuments().get(i).get("TempoPreparazione").toString(),
                                                result.getDocuments().get(i).get("NumeroPersone").toString(),
                                                result.getDocuments().get(i).get("Thumbnail").toString(),
                                                (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                                (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                                (Boolean) result.getDocuments().get(i).get("isApproved"),
                                                (Timestamp) result.getDocuments().get(i).get("Timestamp")
                                        ));
                                    }
                                }
                                loadRicettaView();
                            }
                        }
                    }
            );
        }
    }

    private void loadRicettaView() {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();
        listLayout.removeAllViews();
        if (ricettas.size() != 0) {
            for (int i = 0; i < ricettas.size(); i++) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);

                TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
                TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
                final TextView txtAutoreRicette = (TextView) addView.findViewById(R.id.txtAutoreRicetta);

                final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
                Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);


                txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());
                txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
                final int finalI = i;
                FirebaseFirestore.getInstance().collection("Utenti").document(ricettas.get(i).getAuthorId()).get().addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot result = task.getResult();
                                    txtAutoreRicette.setText(result.get("Nome").toString());
                                    RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
                                    final int position = finalI;
                                    layoutContainer.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                final Intent intent = new Intent(SearchActivity.this, ShowRicettaActivity.class);
                                                final Bundle bundle = Utils.loadBundle(ricettas.get(position));
                                                //Casting from imageSlider to Drawable and conversion into byteArray
                                                Drawable d = ricettaImageFeed.getDrawable();
                                                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                                                byte[] bitmapdata = stream.toByteArray();
                                                bundle.putByteArray("Thumbnail", bitmapdata);
                                                bundle.putBoolean("isAdmin", false);
                                                bundle.putString("pathIdUser", intentMain.getExtras().getString("pathIdUser"));
                                                if (Objects.requireNonNull(intentMain.getExtras()).getString("pathIdUser").equals("anonymous")) {
                                                    bundle.putBoolean("isFav", false);
                                                    intent.putExtras(bundle);
                                                    startActivityForResult(intent, LAUNCH_SHOW_RICETTA_ACTIVITY);
                                                } else {
                                                    String documentIdUtente = intentMain.getExtras().getString("pathIdUser");
                                                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                                                    DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
                                                    utentiRef.get().addOnCompleteListener(
                                                            new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot result = task.getResult();
                                                                        List<String> preferiti = (List<String>) result.get("Preferiti");
                                                                        Boolean found = false;
                                                                        for (int i = 0; i < preferiti.size(); i++) {
                                                                            if (preferiti.get(i).equals(ricettas.get(position).getId())) {
                                                                                found = true;
                                                                            }
                                                                        }
                                                                        bundle.putBoolean("isFav", found);
                                                                        intent.putExtras(bundle);
                                                                        startActivityForResult(intent, LAUNCH_SHOW_RICETTA_ACTIVITY);
                                                                    }
                                                                }
                                                            }
                                                    );
                                                }
                                            } catch (RuntimeException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    listLayout.addView(addView);
                                }
                            }
                        }
                );
            }

        } else {
            Toast.makeText(SearchActivity.this, "Ops..Non è stata trovata alcuna ricetta", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
