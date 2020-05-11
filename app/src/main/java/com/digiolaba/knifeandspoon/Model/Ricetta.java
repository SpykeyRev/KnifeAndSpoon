package com.digiolaba.knifeandspoon.Model;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class Ricetta {
    private String id;
    private String authorId;
    private String thumbnail;
    private String title;
    private String persone;
    private String tempo;
    private List<Map<String, Object>> ingredienti;
    private List<String> steps;
    private Boolean isApproved;


    public Ricetta(String id, String authorId, String title, String tempo, String persone, String thumbnail, List<Map<String, Object>> ingredienti, List<String> steps, Boolean isApproved) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.ingredienti = ingredienti;
        this.steps = steps;
        this.tempo = tempo;
        this.persone = persone;
        this.isApproved = isApproved;
    }

    public String getAuthorId() {
        return this.authorId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Object getSteps() {
        return this.steps;
    }

    public Object getIngredienti() {
        return this.ingredienti;
    }

    public String getTempo() {
        return tempo;
    }

    public String getPersone() {
        return persone;
    }

    public String getId() {
        return id;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public void setIngredienti(List<Map<String, Object>> ingredienti) {
        this.ingredienti = ingredienti;
    }

    public void setPersone(String persone) {
        this.persone = persone;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
