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

    public class Ingrediente {
        final String Nome;
        final Double Quantità;
        final String Ut;

        public Ingrediente(String Nome, Double Quantità, String Ut) {
            this.Nome = Nome;
            this.Quantità = Quantità;
            this.Ut = Ut;
        }
    }

    public class Step {
        final int Numero;
        final String Testo;

        public Step(int Numero, String Testo) {
            this.Numero = Numero;
            this.Testo = Testo;
        }
    }

    public static void getTenRecipe(final Callable<Ricetta> methodParam) {
        final List<Ricetta> obj = new ArrayList();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference ricetteRef = rootRef.collection("Ricette");
        Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", true);
        queryrRicettaApprovata.limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot result = task.getResult();
                if (task.isSuccessful()) {
                    for (int i = 0; i < result.size(); i++) {
                        obj.add(new Ricetta(
                                result.getDocuments().get(i).getId(),
                                result.getDocuments().get(i).get("Autore").toString(),
                                result.getDocuments().get(i).get("Titolo").toString(),
                                result.getDocuments().get(i).get("Tempo di preparazione").toString(),
                                result.getDocuments().get(i).get("Numero persone").toString(),
                                result.getDocuments().get(i).get("Thumbnail").toString(),
                                (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                (Boolean) result.getDocuments().get(i).get("isApproved")
                        ));
                    }
                    try {
                        methodParam.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   /* List<SliderItem> sliderItems = new ArrayList<SliderItem>();
                    if (adapter.getCount() != 0) {
                        for (int i = 0; i < obj.size(); i++) {
                            SliderItem sliderItem = new SliderItem();
                            sliderItem.setDescription( obj.get(i).getTitle());
                            sliderItem.setImageUrl( obj.get(i).getThumbnail());
                            sliderItems.add(sliderItem);
                        }
                        adapter.renewItems(sliderItems, ricettas);
                    } else {
                        for (int i = 0; i < obj.size(); i++) {
                            SliderItem sliderItem = new SliderItem();
                            sliderItem.setDescription( obj.get(i).getTitle());
                            sliderItem.setImageUrl( obj.get(i).getThumbnail());
                            adapter.addItem(sliderItem,  obj.get(i));
                        }
                    }*/
                }
            }
        });
    }


    public static class getFirstTenRecipe extends AsyncTask {
        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            CollectionReference ricetteRef = rootRef.collection("Ricette");
            Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", true);
            Task<QuerySnapshot> documentSnapshotTask = queryrRicettaApprovata.limit(10).get();
            //Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Ricette").limit(10).get();
            List<Ricetta> obj = new ArrayList();
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                for (int i = 0; i < documentSnapshot.size(); i++) {
                    obj.add(new Ricetta(
                            documentSnapshot.getDocuments().get(i).getId(),
                            documentSnapshot.getDocuments().get(i).get("Autore").toString(),
                            documentSnapshot.getDocuments().get(i).get("Titolo").toString(),
                            documentSnapshot.getDocuments().get(i).get("Tempo di preparazione").toString(),
                            documentSnapshot.getDocuments().get(i).get("Numero persone").toString(),
                            documentSnapshot.getDocuments().get(i).get("Thumbnail").toString(),
                            (List<Map<String, Object>>) documentSnapshot.getDocuments().get(i).get("Ingredienti"),
                            (List<String>) documentSnapshot.getDocuments().get(i).get("Passaggi"),
                            (Boolean) documentSnapshot.getDocuments().get(i).get("isApproved")
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

    public static class getRecipeToReview extends AsyncTask {
        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            CollectionReference ricetteRef = rootRef.collection("Ricette");
            Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", false);
            Task<QuerySnapshot> documentSnapshotTask = queryrRicettaApprovata.get();
            List<Ricetta> obj = new ArrayList();
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                for (int i = 0; i < documentSnapshot.size(); i++) {
                    obj.add(new Ricetta(
                            documentSnapshot.getDocuments().get(i).getId(),
                            documentSnapshot.getDocuments().get(i).get("Autore").toString(),
                            documentSnapshot.getDocuments().get(i).get("Titolo").toString(),
                            documentSnapshot.getDocuments().get(i).get("Tempo di preparazione").toString(),
                            documentSnapshot.getDocuments().get(i).get("Numero persone").toString(),
                            documentSnapshot.getDocuments().get(i).get("Thumbnail").toString(),
                            (List<Map<String, Object>>) documentSnapshot.getDocuments().get(i).get("Ingredienti"),
                            (List<String>) documentSnapshot.getDocuments().get(i).get("Passaggi"),
                            (Boolean) documentSnapshot.getDocuments().get(i).get("isApproved")
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

        public getRecipeInfo(String id) {
            this.id = id;
        }

        @Override
        protected Ricetta doInBackground(Object[] objects) {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Ricette").document(id);
            Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
            Ricetta obj = null;
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                Log.i("DIO", documentSnapshot.get("Ingredienti").toString());
                obj = new Ricetta(
                        documentSnapshot.getId(),
                        documentSnapshot.get("Autore").toString(),
                        documentSnapshot.get("Titolo").toString(),
                        documentSnapshot.get("Tempo di preparazione").toString(),
                        documentSnapshot.get("Numero persone").toString(),
                        documentSnapshot.getString("Thumbnail"),
                        (List<Map<String, Object>>) documentSnapshot.get("Ingredienti"),
                        (List<String>) documentSnapshot.get("Passaggi"),
                        (Boolean) documentSnapshot.get("isApproved")
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public static class getFavRicette extends AsyncTask {

        String pathIdUser;

        public getFavRicette(String pathIdUser) {
            this.pathIdUser = pathIdUser;
        }

        @Override
        protected List<Ricetta> doInBackground(Object[] objects) {
            String documentIdUtente = pathIdUser.split("/")[1];
            List<Ricetta> obj = new ArrayList();
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
            Task<DocumentSnapshot> documentSnapshotTask = utentiRef.get();
            try {
                DocumentSnapshot documentSnapshots = Tasks.await(documentSnapshotTask);
                List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                List<String> deletedPreferiti = new ArrayList<>();
                for (int i = 0; i < preferiti.size(); i++) {
                    Task<DocumentSnapshot> documentSnapshotRicetteTask = rootRef.collection("Ricette").document(preferiti.get(i)).get();
                    DocumentSnapshot documentRicetteSnapshot = Tasks.await(documentSnapshotRicetteTask);
                    if (documentRicetteSnapshot.exists()) {
                        obj.add(new Ricetta(
                                documentRicetteSnapshot.getId(),
                                documentRicetteSnapshot.get("Autore").toString(),
                                documentRicetteSnapshot.get("Titolo").toString(),
                                documentRicetteSnapshot.get("Tempo di preparazione").toString(),
                                documentRicetteSnapshot.get("Numero persone").toString(),
                                documentRicetteSnapshot.getString("Thumbnail"),
                                (List<Map<String, Object>>) documentRicetteSnapshot.get("Ingredienti"),
                                (List<String>) documentRicetteSnapshot.get("Passaggi"),
                                (Boolean) documentRicetteSnapshot.get("isApproved")
                        ));
                    } else {
                        deletedPreferiti.add(preferiti.get(i));
                    }
                }
                if (deletedPreferiti.size() != 0) {
                    for (int i = 0; i < deletedPreferiti.size(); i++) {
                        preferiti.remove(deletedPreferiti.get(i));
                    }
                    utentiRef.update("Preferiti", preferiti);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;

        }
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
