package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FavouriteActivity extends AppCompatActivity {

    private List<Ricetta> ricettas;
    LinearLayout layoutFeedFav;
    private static int LAUNCH_SHOW_RICETTA_ACTIVITY=2912;
    private String actualUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actualUser=getIntent().getExtras().get("pathIdUser").toString();
        layoutFeedFav=findViewById(R.id.layoutFeedFav);
        loadRicetteFav();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LAUNCH_SHOW_RICETTA_ACTIVITY)
        {
            if(resultCode==RESULT_OK)
            {
                new Utente.setPreferiti(data.getExtras().getString("docRicetta"),data.getExtras().getString("docUser"),data.getExtras().getBoolean("fav")).execute();
                refresh();
            }
        }
    }

    private void loadRicetteFav()
    {
        layoutFeedFav.removeAllViews();

        try {
            ricettas= (List<Ricetta>) new Ricetta.getFavRicette(actualUser).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(ricettas.size()==0)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            closeActivity();
                            break;

                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.no_fav_yet)).setPositiveButton(getString(R.string.error_ok), dialogClickListener)
                    .show();
        }
        else
        {
            for(int i=0;i<ricettas.size();i++)
            {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);
                TextView txtNomeRicettaFeed=(TextView)addView.findViewById(R.id.txtFeedNomeRicetta);
                TextView txtTempoPreparazioneFeed=(TextView)addView.findViewById(R.id.txtFeedTempoPreparazione);
                TextView txtPersoneFeed=(TextView)addView.findViewById(R.id.txtFeedPersone);
                final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
                Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);
                txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());
                txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
                String feedPersone="Per ".concat(Utils.personaOrPersone(ricettas.get(i).getPersone()));
                txtPersoneFeed.setText(feedPersone);
                RelativeLayout layoutContainer=(RelativeLayout)addView.findViewById(R.id.layoutFeedMainAndPic);
                final int position = i;
                layoutContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            Intent intent=new Intent(FavouriteActivity.this,ShowRicettaActivity.class);
                            Bundle bundle=Utils.loadBundle(ricettas.get(position));
                            //Casting from imageSlider to Drawable and conversion into byteArray
                            Drawable d = ricettaImageFeed.getDrawable();
                            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] bitmapdata = stream.toByteArray();
                            bundle.putByteArray("Thumbnail", bitmapdata);
                            bundle.putBoolean("isAdmin",false);
                            bundle.putString("pathIdUser",actualUser);
                            bundle.putBoolean("isFav",checkPreferiti(ricettas.get(position).getId()));
                            intent.putExtras(bundle);
                            startActivityForResult(intent,LAUNCH_SHOW_RICETTA_ACTIVITY);
                        }
                        catch(RuntimeException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
                layoutFeedFav.addView(addView);
            }
        }

    }

    private Boolean checkPreferiti(String idRicetta)
    {
        Boolean found=false;
        try {
            found=new Utente.checkPreferiti(idRicetta,actualUser).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return found;
    }

    private void refresh()
    {
        loadRicetteFav();
    }

    private void closeActivity()
    {
        this.onBackPressed();
        this.finish();
    }
}