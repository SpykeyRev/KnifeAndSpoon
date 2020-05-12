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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RicetteToApproveActivity extends AppCompatActivity {

    private LinearLayout ricetteToReviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ricette_to_approve);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ricetteToReviewLayout = (LinearLayout) findViewById(R.id.layoutRicetteToApprove);
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


    private void loadRicetteToReview() {
        final List<Ricetta> obj = new ArrayList();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference ricetteRef = rootRef.collection("Ricette");
        Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", false);
        queryrRicettaApprovata.get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        if (task.isSuccessful()) {
                            for (int i = 0; i < result.size(); i++) {
                                obj.add(new Ricetta(
                                        result.getDocuments().get(i).getId(),
                                        result.getDocuments().get(i).get("Autore").toString(),
                                        result.getDocuments().get(i).get("Titolo").toString(),
                                        result.getDocuments().get(i).get("Categoria").toString(),
                                        result.getDocuments().get(i).get("Tempo di preparazione").toString(),
                                        result.getDocuments().get(i).get("Numero persone").toString(),
                                        result.getDocuments().get(i).get("Thumbnail").toString(),
                                        (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                        (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                        (Boolean) result.getDocuments().get(i).get("isApproved"),
                                        (Timestamp) result.getDocuments().get(i).get("Timestamp")
                                ));
                            }
                            if(ricetteToReviewLayout.getChildCount()!=0)
                            {
                                ricetteToReviewLayout.removeAllViews();
                            }
                            showRecipes(obj);
                        }
                    }
                }
        );
    }

    private void showRecipes(final List<Ricetta> ricettas) {
        if (ricettas.size() == 0) {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.nothing_to_show_here_admin)).setPositiveButton(getString(R.string.perfect_exclamation_mark), dialogClickListener)
                    .show();
        } else {
            for (int i = 0; i < ricettas.size(); i++) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);
                TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
                TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
                TextView txtPersoneFeed = (TextView) addView.findViewById(R.id.txtFeedPersone);
                final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
                Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);
                txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());
                txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
                String feedPersone = "Per ".concat(Utils.personaOrPersone(ricettas.get(i).getPersone()));
                txtPersoneFeed.setText(feedPersone);
                RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
                final int position = i;
                layoutContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(RicetteToApproveActivity.this, ShowRicettaActivity.class);
                            Bundle bundle = Utils.loadBundle(ricettas.get(position));
                            //Casting from imageSlider to Drawable and conversion into byteArray
                            Drawable d = ricettaImageFeed.getDrawable();
                            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            bundle.putByteArray("Thumbnail", bitmapdata);
                            bundle.putBoolean("isAdmin", true);
                            bundle.putString("ThumbnailURL", ricettas.get(position).getThumbnail());
                            bundle.putString("pathIdUser", "admin");
                            intent.putExtras(bundle);
                            startActivity(intent);

                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }

                    }
                });
                ricetteToReviewLayout.addView(addView);
            }
        }
    }

    private void closeActivity() {
        this.onBackPressed();
        this.finish();
    }

    protected void onResume() {
        loadRicetteToReview();
        super.onResume();
    }
}
