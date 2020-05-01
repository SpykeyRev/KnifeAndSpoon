package com.digiolaba.knifeandspoon.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Utente actualUser;
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    private FloatingActionButton fab_main, fab1_mail;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;


    Boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViewById(R.id.buttonLogout).setOnClickListener(this);
        //findViewById(R.id.buttonDisconnect).setOnClickListener(this);
        //Setting up firebase for userInfo
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            actualUser = (Utente) new Utente.getUserInfo(firebaseAuth.getCurrentUser().getEmail()).execute().get();
            TextView userName=(TextView)findViewById(R.id.userName);
            userName.setText(actualUser.getUserName());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Utente.getUserInfo(firebaseAuth.getCurrentUser().getEmail());
        CircleImageView userImage=(CircleImageView)findViewById(R.id.profile_image);
        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        Picasso.get().load(fireUser.getPhotoUrl()).into(userImage);
        //Setting up imageSlider
        ImageSlider imageSlider=(ImageSlider)findViewById(R.id.home_image_slider);

        List<SlideModel>slideModels=new ArrayList<>();
        slideModels.add((new SlideModel(R.drawable.app_logo,"Prova1")));
        slideModels.add((new SlideModel("https://wips.plug.it/cips/buonissimo.org/cms/2019/02/carbonara.jpg","Carbonara")));
        slideModels.add((new SlideModel("https://wips.plug.it/cips/buonissimo.org/cms/2019/03/ciambellone-classico.jpg","Ciambellone")));
        slideModels.add((new SlideModel("https://lacuocagalante.com/wp-content/uploads/2017/05/la-pasta-alla-amatriciana.jpg","Amatriciana")));

        imageSlider.setImageList(slideModels,true);

        fab_main = (FloatingActionButton)findViewById(R.id.btnAdd);
        fab1_mail =(FloatingActionButton) findViewById(R.id.btnGoogle);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_antilock);


        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    fab1_mail.startAnimation(fab_close);
                    fab_main.startAnimation(fab_anticlock);
                    fab1_mail.setClickable(false);
                    isOpen = false;
                } else {
                    fab1_mail.startAnimation(fab_open);
                    fab_main.startAnimation(fab_clock);
                    fab1_mail.setClickable(true);
                    isOpen = true;
                }
            }
        });
        fab1_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Email", Toast.LENGTH_SHORT).show();
            }
        });




    }




    public void getUserInfo(String email){
        final List<Utente> users = new ArrayList();
        FirebaseFirestore storage=FirebaseFirestore.getInstance();
        Task task=storage.collection("Utenti").whereEqualTo("Mail", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String id=task.getResult().getDocuments().get(0).getId();
                            String mail=task.getResult().getDocuments().get(0).get("Mail").toString();
                            String nome=task.getResult().getDocuments().get(0).get("Nome").toString();
                            String immagine=task.getResult().getDocuments().get(0).get("Immagine").toString();
                            Boolean isAdmin=(Boolean)task.getResult().getDocuments().get(0).get("isAdmin");
                            actualUser=new Utente(id,mail,nome,immagine,isAdmin);
                            TextView textView = findViewById(R.id.userName);
                            textView.setText(actualUser.getUserName());
                        }
                    }
                });

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
