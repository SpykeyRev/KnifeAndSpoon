package com.digiolaba.knifeandspoon.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ARG_NAME = "username";

    public static void startActivity(Context context, FirebaseUser user) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_NAME, user.getDisplayName().split(" ")[0]);
        context.startActivity(intent);
    }


    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.userName);
        if (getIntent().hasExtra(ARG_NAME)) {
            textView.setText(String.format(getIntent().getStringExtra(ARG_NAME)));
        }
        //findViewById(R.id.buttonLogout).setOnClickListener(this);
        //findViewById(R.id.buttonDisconnect).setOnClickListener(this);

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();

        ImageSlider imageSlider=findViewById(R.id.home_image_slider);

        List<SlideModel>slideModels=new ArrayList<>();
        slideModels.add((new SlideModel(R.drawable.app_logo,"Prova1")));
        slideModels.add((new SlideModel(R.drawable.ic_google,"Prova2")));
        slideModels.add((new SlideModel("https://wips.plug.it/cips/buonissimo.org/cms/2019/02/carbonara.jpg","Carbonara")));
        slideModels.add((new SlideModel("https://wips.plug.it/cips/buonissimo.org/cms/2019/03/ciambellone-classico.jpg","Ciambellone")));
        slideModels.add((new SlideModel("https://lacuocagalante.com/wp-content/uploads/2017/05/la-pasta-alla-amatriciana.jpg","Amatriciana")));





        imageSlider.setImageList(slideModels,true);
    }

    /*@Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLogout:
                signOut();
                break;
            case R.id.buttonDisconnect:
                revokeAccess();
                break;
        }
    }*/

    private void signOut() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Signed out of google");
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Revoked Access");
                    }
                });
    }

}
