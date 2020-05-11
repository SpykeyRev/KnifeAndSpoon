package com.digiolaba.knifeandspoon.Model;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

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
    private Timestamp timestamp;


    public Ricetta(String id, String authorId, String title, String tempo, String persone, String thumbnail, List<Map<String, Object>> ingredienti, List<String> steps, Boolean isApproved,Timestamp timestamp) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.ingredienti = ingredienti;
        this.steps = steps;
        this.tempo = tempo;
        this.persone = persone;
        this.isApproved = isApproved;
        this.timestamp=timestamp;
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

    public Timestamp getTimestamp() {
        return this.timestamp;
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

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp=timestamp;
    }
}
