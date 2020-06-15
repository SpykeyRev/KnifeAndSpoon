package com.digiolaba.knifeandspoon.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ricetta implements Parcelable {
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
    private String categoria;


    public Ricetta(String id, String authorId, String title, String categoria,String tempo, String persone, String thumbnail, List<Map<String, Object>> ingredienti, List<String> steps, Boolean isApproved,Timestamp timestamp) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.ingredienti = ingredienti;
        this.steps = steps;
        this.tempo = tempo;
        this.persone = persone;
        this.isApproved = isApproved;
        this.categoria=categoria;
        this.timestamp=timestamp;
    }

    protected Ricetta(Parcel in) {
        id = in.readString();
        authorId = in.readString();
        thumbnail = in.readString();
        title = in.readString();
        persone = in.readString();
        tempo = in.readString();
        steps = in.createStringArrayList();
        byte tmpIsApproved = in.readByte();
        isApproved = tmpIsApproved == 0 ? null : tmpIsApproved == 1;
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        categoria = in.readString();
    }

    public static final Creator<Ricetta> CREATOR = new Creator<Ricetta>() {
        @Override
        public Ricetta createFromParcel(Parcel in) {
            return new Ricetta(in);
        }

        @Override
        public Ricetta[] newArray(int size) {
            return new Ricetta[size];
        }
    };

    public String getAuthorId() {
        return this.authorId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getCategoria() {
        return this.categoria;
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

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.authorId);
        dest.writeString(this.thumbnail);
        dest.writeString(this.title);
        dest.writeString(this.persone);
        dest.writeString(this.tempo);
        dest.writeList(this.ingredienti);
        dest.writeStringList(this.steps);
        dest.writeValue(this.isApproved);
        dest.writeParcelable(this.timestamp, flags);
        dest.writeString(this.categoria);
    }

}
