package com.digiolaba.knifeandspoon.Model;
import java.util.List;
public class Ricetta {
    private String authorId;
    private String thumbnail;
    private String title;
    private List ingredienti;
    private List steps;


    Ricetta(String id, String title,String thumbnail,List ingredienti, List steps){
        this.authorId=id;
        this.title=title;
        this.thumbnail=thumbnail;
        this.ingredienti=ingredienti;
        this.steps=steps;
    }

    public String getAuthorId(){
        return this.authorId;
    }

    public String getTitle(){
        return this.title;
    }

    public String getThumbnail(){
        return this.thumbnail;
    }

    public List getSteps(){
        return this.steps;
    }

    public List getIngredienti(){
        return this.ingredienti;
    }
}
