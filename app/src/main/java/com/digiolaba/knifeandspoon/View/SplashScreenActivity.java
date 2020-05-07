package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SplashScreenActivity extends AppCompatActivity {

    String TAG = "SplashScreenActivity";
    Context context = SplashScreenActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (Utils.checkNetworkConnection(this)) {
            launchCorrectActivity();
        } else {
            Utils.showToastMessage(context, "Turn on your internet connection you KNOB");
            while (!Utils.checkNetworkConnection(this)) {

            }
            launchCorrectActivity();
        }
    }

    private void launchCorrectActivity() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if(currentUser.isAnonymous())
            {
                try {
                    List<Ricetta> ricettas = (List<Ricetta>) new Ricetta.getFirstTenRecipe().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                FirebaseFirestore storage = FirebaseFirestore.getInstance();
                storage.collection("Utenti").whereEqualTo("Mail", currentUser.getEmail())
                        .limit(1).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    boolean isEmpty = task.getResult().isEmpty();
                                    if (isEmpty) {
                                        Intent intent = new Intent(getApplicationContext(),
                                                RegisterActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Intent intent = new Intent(getApplicationContext(),
                                                MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        });
            }
        } else {
            Intent intent = new Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}