package com.digiolaba.knifeandspoon.Model;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.View.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Utente {
    private String id;
    private String mail;
    private String nome;
    private Boolean isAdmin;
    private String image;
    private List<String> favourite;

    public Utente(String id, String mail, String nome, String image, Boolean isAdmin, List<String> favourite) {
        this.id = id;
        this.mail = mail;
        this.nome = nome;
        this.image = image;
        this.isAdmin = isAdmin;
        this.favourite = favourite;
    }

    public String getUserId() {
        return this.id;
    }

    public void setUserId(String id) {
        this.id = id;
    }

    public String getUserMail() {
        return this.mail;
    }

    public String getUserName() {
        return this.nome;
    }

    public String getUserImage() {
        return this.image;
    }

    public boolean getisAdmin() {
        return this.isAdmin;
    }

    public List<String> getFavourite() {
        return this.favourite;
    }
}
