package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public void getLoadRicetteToReview()
    {
        loadRicetteToReview();
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
            FeedFragment feedFragment=new FeedFragment();
            Bundle bundle=new Bundle();
            bundle.putSerializable("ricettas", (Serializable) ricettas);
            bundle.putString("class",getClass().getSimpleName());
            feedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.layoutRicetteToApprove,feedFragment).commit();
        }
    }

    private void closeActivity() {
        this.onBackPressed();
        this.finish();
    }

    protected void onResume() {
        checkConnection("loadRicetteToReview");
        super.onResume();
    }

    private void checkConnection(final String methodInString)
    {
        try {
            final Method method=getClass().getMethod("get"+methodInString.substring(0,1).toUpperCase()+methodInString.substring(1));
            boolean conn=isNetworkAvailable();
            if(!conn)
            {
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
            }
            else
            {
                try {
                    method.invoke(RicetteToApproveActivity.this);
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
