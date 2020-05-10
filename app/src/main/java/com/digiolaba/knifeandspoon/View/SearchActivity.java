package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
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
import java.util.concurrent.ExecutionException;


public class SearchActivity extends AppCompatActivity {

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
        intentMain=getIntent();


        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mSearchBtn = (ImageButton) findViewById(R.id.btn_search);

        listLayout = (LinearLayout) findViewById(R.id.LayoutFeedSearch);
        searchEvent();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_SHOW_RICETTA_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                new Utente.setPreferiti(this, data.getExtras().getString("docRicetta"), data.getExtras().getString("docUser"), data.getExtras().getBoolean("fav")).execute();
            }
        }
    }

    private void searchEvent()
    {

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingListAndView();
            }
        });

        mSearchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                        {
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

    public void loadingListAndView()
    {
        if(mSearchBar.getText().length()<=0)
        {
            Utils.showSnackbar(listLayout,"Per favore inserisci qualcosa da ricercare");
        }
        else
        {
            String SearchRicetta = mSearchBar.getText().toString().substring(0,1).toUpperCase()+mSearchBar.getText().toString().substring(1);
            try {
                ricettas = (List<Ricetta>) new getRicercaSearch(SearchRicetta).execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            loadRicettaView();
        }
    }

    private void loadRicettaView() {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();
        listLayout.removeAllViews();
        if (ricettas.size() != 0) {
            for (int i = 0; i < ricettas.size(); i++) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);

                TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
                TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
                TextView txtAutoreRicette = (TextView) addView.findViewById(R.id.txtAutoreRicetta);

                final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
                Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);

                txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());
                txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
                txtAutoreRicette.setText(ricettas.get(i).getAuthorId());

                RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
                final int position = i;
                layoutContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            Intent intent = new Intent(SearchActivity.this, ShowRicettaActivity.class);
                            Bundle bundle = Utils.loadBundle(ricettas.get(position));
                            //Casting from imageSlider to Drawable and conversion into byteArray
                            Drawable d = ricettaImageFeed.getDrawable();
                            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            bundle.putByteArray("Thumbnail", bitmapdata);
                            bundle.putBoolean("isAdmin", false);
                            if (Objects.requireNonNull(intentMain.getExtras()).getString("pathIdUser").equals("anonymous")) {
                                bundle.putBoolean("isFav", false);
                            } else {
                                bundle.putBoolean("isFav", checkPreferiti(ricettas.get(position).getId()));
                            }
                            bundle.putString("pathIdUser", intentMain.getExtras().getString("pathIdUser"));
                            intent.putExtras(bundle);
                            startActivityForResult(intent, LAUNCH_SHOW_RICETTA_ACTIVITY);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                });
                listLayout.addView(addView);
            }

        }
        else
            {
                Toast.makeText(SearchActivity.this, "Ops..Non è stata trovata alcuna ricetta", Toast.LENGTH_LONG).show();
        }
    }

    private Boolean checkPreferiti(String idRicetta) {
        Boolean found = false;
        try {
            found = new Utente.checkPreferiti(this, idRicetta, intentMain.getExtras().getString("pathIdUser")).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return found;
    }

    public class getRicercaSearch extends AsyncTask {

        String SearchRicetta;

        public getRicercaSearch(String SearchRicetta) {
            this.SearchRicetta = SearchRicetta;
        }


        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            CollectionReference ricetteRef = rootRef.collection("Ricette");
            Query search = ricetteRef.whereEqualTo("isApproved", true);
            Query query = search.whereGreaterThanOrEqualTo("Titolo", SearchRicetta).whereLessThanOrEqualTo("Titolo",SearchRicetta.concat("\uf8ff"));

            Task<QuerySnapshot> documentSnapshotTask = query.get();
            List<Ricetta> obj = new ArrayList();
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                for (int i = 0; i < documentSnapshot.size(); i++) {
                    obj.add(new Ricetta(
                            documentSnapshot.getDocuments().get(i).getId(),
                            documentSnapshot.getDocuments().get(i).get("Autore").toString(),
                            documentSnapshot.getDocuments().get(i).get("Titolo").toString(),
                            documentSnapshot.getDocuments().get(i).get("Tempo di preparazione").toString(),
                            documentSnapshot.getDocuments().get(i).get("Numero persone").toString(),
                            documentSnapshot.getDocuments().get(i).get("Thumbnail").toString(),
                            (List<Map<String, Object>>) documentSnapshot.getDocuments().get(i).get("Ingredienti"),
                            (List<String>) documentSnapshot.getDocuments().get(i).get("Passaggi"),
                            (Boolean) documentSnapshot.getDocuments().get(i).get("isApproved")
                    ));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
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
