package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        checkConnection();
    }

    private void checkConnection()
    {
        Boolean conn=isNetworkAvailable();
        if(!conn)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            checkConnection();
                            break;

                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_connection)).setPositiveButton(getString(R.string.error_ok), dialogClickListener)
                    .show();
        }
        else
        {
            launchCorrectActivity();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void launchCorrectActivity() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                finish();
            } else {
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