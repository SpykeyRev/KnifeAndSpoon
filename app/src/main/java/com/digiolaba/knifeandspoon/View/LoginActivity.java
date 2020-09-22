package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;
    private Context context = LoginActivity.this;
    private Button btnAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SignInButton signInButton = findViewById(R.id.btnLogin);
        btnAnonymous = (Button) findViewById(R.id.btnNoLogin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection("signInToGoogle");
            }
        });

        configureGoogleClient();

        btnAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection("signInAnonymously");
            }
        });
    }

    private void configureGoogleClient() {
        // Configura Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.btnLogin);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        // Inizializza Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Controllo se l'utente è già loggato
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            user = currentUser;
            checkIfUserExist(currentUser);
        }
    }

    private void signInToGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void getSignInToGoogle() {
        signInToGoogle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Risultato dal lancio dell'Intent da GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Login tramite Google avvenuto con successo, autenticazione tramite Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Utils.showToastMessage(context, "Login Tramite Google fallito. Riprovare ");
            }
        }
    }

    //autenticazione tramite firebase
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login avvenuto con successo, si controlla se l'utente esista o meno
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            checkIfUserExist(user);
                        } else {
                            // errore
                            Utils.showToastMessage(context, "Autenticazione fallita, riprovare");
                        }
                    }
                });
    }

    private void checkIfUserExist(FirebaseUser user) {
        if (user != null) {
            FirebaseFirestore storage = FirebaseFirestore.getInstance();
            storage.collection("Utenti").whereEqualTo("Mail", user.getEmail())
                    .limit(1).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean isEmpty = task.getResult().isEmpty();
                                if (isEmpty) {
                                    //l'utente non si è mai registrato
                                    launchRegisterActivity();
                                } else {
                                    //l'utente è già registrato
                                    launchMainActivity();
                                }
                            }
                        }
                    });
        }
    }

    private void launchRegisterActivity() {
        RegisterActivity.startActivity(this);
        finish();
    }

    private void launchMainActivity() {
        MainActivity.startActivity(this);
        finish();
    }

    private void signInAnonymously() {
        firebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login avvenuto con successo
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    launchMainActivity();

                } else {
                    // errore
                    Toast.makeText(LoginActivity.this, "Autenticazione fallita, riprovare",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getSignInAnonymously() {
        signInAnonymously();
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
                    method.invoke(LoginActivity.this);
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
