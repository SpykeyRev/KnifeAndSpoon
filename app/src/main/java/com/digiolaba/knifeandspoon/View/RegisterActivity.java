package com.digiolaba.knifeandspoon.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    EditText nome;
    Button continua;
    String TAG="Register";
    CircleImageView userImage;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userImage=(CircleImageView)findViewById(R.id.profile_image);
        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        Picasso.get().load(fireUser.getPhotoUrl()).into(userImage);
        nome=(EditText)findViewById(R.id.Nome);
        continua=(Button)findViewById(R.id.Continua);
        continua.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser(){
        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        if(nome.getText().length()==0){
            showToastMessage("Il nome non può essere vuoto");
        }else{
            Log.d(TAG, "SHIIIT");
            Map<String, Object> user = new HashMap<>();
            user.put("Mail", fireUser.getEmail().toString());
            user.put("Nome", nome.getText().toString());
            user.put("Immagine", "");
            user.put("isAdmin", false);
            Log.d(TAG, "SHIIIT");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d(TAG, "SHIIIT");
            // Add a new document with a generated ID
            db.collection("Utenti")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            launchMainActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }
    }

    private void launchMainActivity(){
        MainActivity.startActivity(this);
        finish();
    }

    private void showToastMessage(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
