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
import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private EditText nome;
    private Button continua;
    private String TAG="Register";
    private CircleImageView userImage;
    private Context context=RegisterActivity.this;

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
        nome=(EditText)findViewById(R.id.txtNome);
        continua=(Button)findViewById(R.id.btnContinua);
        continua.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                checkName();
            }
        });
    }

    private void checkName()
    {
        if(nome.getText().toString().equals(""))
        {
            Utils.errorDialog(this,R.string.error_empty_name,R.string.error_ok);
        }
        else if(nome.getText().toString().contains(" ")&&(nome.getText().toString().startsWith(" ")&&nome.getText().toString().endsWith(" ")))
        {
            Utils.errorDialog(this,R.string.error_name_space,R.string.error_ok);
        }
        else if(nome.getText().toString().length()<=6||nome.getText().toString().length()>=20)
        {
            Utils.errorDialog(this, R.string.error_lenght_name,R.string.error_ok);
        }
        else
        {
            checkIfNameExists();
        }
    }


    private void checkIfNameExists()
    {
        final Context context=this;
        FirebaseFirestore rootRef=FirebaseFirestore.getInstance();
        CollectionReference utentiRef=rootRef.collection("Utenti");
        Query queryNome=utentiRef.whereEqualTo("Nome",nome.getText().toString());
        queryNome.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(DocumentSnapshot documentSnapshot:task.getResult())
                    {
                        if(documentSnapshot.exists())
                        {
                            Utils.errorDialog(context, R.string.error_name_already_taken,R.string.error_ok);
                        }
                    }
                }
                if(task.getResult().size()==0)
                {
                    registerUser();
                }
            }
        });
    }

    private void registerUser(){
        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
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

    private void launchMainActivity(){
        MainActivity.startActivity(this);
        finish();
    }


}
