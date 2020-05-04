package com.digiolaba.knifeandspoon.Model;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Ricetta {
    private String id;
    private String authorId;
    private String thumbnail;
    private String title;
    private String tempo;
    private List<Map<String,Object>> ingredienti;
    private List<String> steps;


    Ricetta(String id, String authorId, String title, String tempo, String thumbnail, List<Map<String,Object>> ingredienti, List<String> steps){
        this.id=id;
        this.authorId=authorId;
        this.title=title;
        this.thumbnail=thumbnail;
        this.ingredienti= ingredienti;
        this.steps= steps;
        this.tempo=tempo;
    }

    public class Ingrediente{
        final String Nome;
        final Double Quantità;
        final String Ut;

        public Ingrediente(String Nome,Double Quantità,String Ut) {
            this.Nome=Nome;
            this.Quantità=Quantità;
            this.Ut=Ut;
        }
    }

    public class Step{
        final int Numero;
        final String Testo;

        public Step(int Numero,String Testo) {
            this.Numero=Numero;
            this.Testo=Testo;
        }
    }


    public static class getFirstTenRecipe extends AsyncTask{
        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Ricette").limit(10).get();
            List<Ricetta> obj=new ArrayList();
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                for(int i=0;i<documentSnapshot.size();i++){
                    obj.add(new Ricetta(
                            documentSnapshot.getDocuments().get(i).getId(),
                            documentSnapshot.getDocuments().get(i).get("Autore").toString(),
                            documentSnapshot.getDocuments().get(i).get("Titolo").toString(),
                            documentSnapshot.getDocuments().get(i).get("Tempo di preparazione").toString(),
                            documentSnapshot.getDocuments().get(i).get("Thumbnail").toString(),
                            (List<Map<String,Object>>) documentSnapshot.getDocuments().get(i).get("Ingredienti"),
                            (List<String>) documentSnapshot.getDocuments().get(i).get("Passaggi")
                    ));
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public static class getRecipeInfo extends AsyncTask {
        String id;

        public getRecipeInfo(String id){
            this.id=id;
        }

        @Override
        protected Ricetta doInBackground(Object[] objects) {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Ricette").document(id);
            Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
            Ricetta obj=null;
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                Log.i("DIO", documentSnapshot.get("Ingredienti").toString());
                obj=new Ricetta(
                        documentSnapshot.getId(),
                        documentSnapshot.get("Autore").toString(),
                        documentSnapshot.get("Titolo").toString(),
                        documentSnapshot.get("Tempo di preparazione").toString(),
                        documentSnapshot.getString("Thumbnail"),
                        (List<Map<String,Object>>) documentSnapshot.get("Ingredienti"),
                        (List<String>) documentSnapshot.get("Passaggi")
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

    public Object getSteps(){
        return this.steps;
    }

    public Object getIngredienti(){
        return this.ingredienti;
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

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
