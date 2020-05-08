package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class SearchActivity extends AppCompatActivity {

    private EditText mSearchBar;
    private ImageButton mSearchBtn;
    private List<Ricetta> ricettas;
    private LinearLayout listLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mSearchBtn = (ImageButton) findViewById(R.id.btn_search);

        listLayout = (LinearLayout) findViewById(R.id.LayoutFeedSearch);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String SearchRicetta =mSearchBar.getText().toString();
                try {
                    ricettas = (List<Ricetta>) new getRicercaSearch(SearchRicetta).execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                loadRicettaView();
            }
        });
    }

    private void loadRicettaView() {

        for(int i=0; i < ricettas.size(); i++)
        {
            LayoutInflater layoutInflater =(LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View addView = layoutInflater.inflate(R.layout.list_layout, null);
                TextView txtnomeRicetta = (TextView) addView.findViewById(R.id.nomeRicetta);
                TextView txtTempoPreparazione = (TextView)addView.findViewById(R.id.tempoPreparazione);
                TextView txtAutore = (TextView) addView.findViewById(R.id.autoreRicetta);

                final ImageView ricettaImage = (ImageView) addView.findViewById(R.id.imageSearchRicetta);
                Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImage);

                txtnomeRicetta.setText(ricettas.get(i).getTitle());
                txtTempoPreparazione.setText(ricettas.get(i).getTempo().concat(" minuti"));
                txtAutore.setText(ricettas.get(i).getAuthorId());

            LinearLayout layoutContainer = (LinearLayout) addView.findViewById(R.id.layoutFeedMainAndPic2);
            final int position = i;
            layoutContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {


                        Intent intent = new Intent(SearchActivity.this, ShowRicettaActivity.class);
                        Bundle bundle = Utils.loadBundle(ricettas.get(position));

                        //casting
                        Drawable drawable = ricettaImage.getDrawable();
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] bitmapdata = stream.toByteArray();
                        bundle.putByteArray("Thumbnail", bitmapdata);
                        bundle.putBoolean("isAdmin", false);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
            listLayout.addView(addView);
        }


    }

    public static class getRicercaSearch extends AsyncTask {

        String SearchRicetta;

        public getRicercaSearch(String SearchRicetta){
            this.SearchRicetta= SearchRicetta;
        }


        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            CollectionReference ricetteRef = rootRef.collection("Ricette");
            Query search = ricetteRef.whereEqualTo("isApproved", true).startAt(SearchRicetta);
            Task<QuerySnapshot> documentSnapshotTask=search.get();
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
                            (Boolean)documentSnapshot.getDocuments().get(i).get("isApproved")
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
