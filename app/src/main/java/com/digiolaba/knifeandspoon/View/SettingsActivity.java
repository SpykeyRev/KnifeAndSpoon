package com.digiolaba.knifeandspoon.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button logOut;
    private Button changeProPic;
    private Button reviewRicettaAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
        Glide.with(this).load(extras.get("userProPic"))
                .centerCrop()
                .into(userImage);
        logOut = findViewById(R.id.btnLogOut);
        changeProPic = findViewById(R.id.btnChangeProPic);
        logOutClick();
        changeProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something
            }
        });

        loadAdminButton(extras.getBoolean("isAdmin"));
    }

    private void logOutClick()
    {
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInClient client = GoogleSignIn.getClient(SettingsActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
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
            }
        });
    }
    private void loadAdminButton(Boolean isAdmin)
    {
        if(isAdmin)
        {
            reviewRicettaAdmin=(Button)findViewById(R.id.btnApproveRicettaAdmin);
            reviewRicettaAdmin.setVisibility(View.VISIBLE);
            reviewRicettaAdmin.setClickable(true);
            reviewRicettaAdmin.setEnabled(true);
            reviewRicettaAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(SettingsActivity.this,RicetteToApproveActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
