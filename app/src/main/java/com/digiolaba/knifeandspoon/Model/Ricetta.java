package com.digiolaba.knifeandspoon.Model;
import android.os.AsyncTask;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public static class getRecipeInfo extends AsyncTask {
        String id;

        public getRecipeInfo(String id){
            this.id=id;
        }

        @Override
        protected Utente doInBackground(Object[] objects) {
            Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").get();
            Utente obj=null;
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                obj=new Utente(
                        documentSnapshot.getDocuments().get(0).getId(),
                        documentSnapshot.getDocuments().get(0).get("Mail").toString(),
                        documentSnapshot.getDocuments().get(0).get("Nome").toString(),
                        documentSnapshot.getDocuments().get(0).get("Immagine").toString(),
                        (Boolean)documentSnapshot.getDocuments().get(0).get("isAdmin")
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
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
