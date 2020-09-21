package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText nome;
    private Button continua;
    private String TAG = "Register";
    private ImageView userImage;
    private Context context = RegisterActivity.this;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userImage = (ImageView) findViewById(R.id.profile_image);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        Picasso.get().load(fireUser.getPhotoUrl()).into(userImage);
        nome = (EditText) findViewById(R.id.txtNome);
        continua = (Button) findViewById(R.id.btnContinua);
        continua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection("checkName");
            }
        });
    }

    private void checkName() {
        if (nome.getText().toString().trim().equals("")) {
            Utils.errorDialog(this, R.string.error_empty_name, R.string.error_ok);
        } else if (nome.getText().toString().contains(" ") && (nome.getText().toString().startsWith(" ") && nome.getText().toString().endsWith(" "))) {
            Utils.errorDialog(this, R.string.error_name_space, R.string.error_ok);
        } else if (nome.getText().toString().length() < 6 || nome.getText().toString().length() > 20) {
            Utils.errorDialog(this, R.string.error_lenght_name, R.string.error_ok);
        } else {
            checkIfNameExists();
        }
    }

    public void getCheckName() {
        checkName();
    }


    //controlla se l'username è già stato usato da un altro utente
    private void checkIfNameExists() {
        final Context context = this;
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference utentiRef = rootRef.collection("Utenti");
        Query queryNome = utentiRef.whereEqualTo("Nome", nome.getText().toString());
        queryNome.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        if (documentSnapshot.exists()) {
                            Utils.errorDialog(context, R.string.error_name_already_taken, R.string.error_ok);
                        }
                    }
                }
                if (task.getResult().size() == 0) {
                    registerUser();
                }
            }
        });
    }

    //registrazione dell'utente su firebase
    private void registerUser() {
        List<String> preferiti = new ArrayList<String>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        final Map<String, Object> user = new HashMap<>();
        user.put("Mail", fireUser.getEmail().toString());
        user.put("Nome", nome.getText().toString());
        user.put("isAdmin", false);
        user.put("Preferiti", preferiti);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference imageRef = storageRef.child(user.get("Nome") + ".jpg");
        Bitmap bitmap = ((BitmapDrawable) userImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imgData = baos.toByteArray();
        imageRef.putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        user.put("Immagine", uri.toString());
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Utenti")
                                .add(user);
                        MainActivity.startActivity(RegisterActivity.this);
                        RegisterActivity.this.finish();
                    }
                });
            }
        });
    }

    private void checkConnection(final String methodInString) {
        try {
            final Method method = getClass().getMethod("get" + methodInString.substring(0, 1).toUpperCase() + methodInString.substring(1));
            boolean conn = isNetworkAvailable();
            if (!conn) {
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
            } else {
                try {
                    method.invoke(RegisterActivity.this);
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
