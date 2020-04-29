package com.digiolaba.knifeandspoon.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.digiolaba.knifeandspoon.R;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    EditText nome;
    Button continua;
    FirebaseUser user;

    public static void startActivity(Context context, FirebaseUser user) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.putExtra("user", user);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nome=(EditText)findViewById(R.id.Nome);
        continua=(Button)findViewById(R.id.Continua);
    }
}
