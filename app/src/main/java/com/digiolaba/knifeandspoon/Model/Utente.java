package com.digiolaba.knifeandspoon.Model;

import android.os.AsyncTask;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutionException;

import static com.google.android.gms.tasks.Tasks.await;

public class Utente {
    private String id;
    private String mail;
    private String nome;
    private Boolean isAdmin;
    private String image;

    public Utente(String id, String mail, String nome, String image, Boolean isAdmin){
        this.id=id;
        this.mail=mail;
        this.nome=nome;
        this.image=image;
        this.isAdmin=isAdmin;
    }

    /*
        This class is made for getting a certain user (for a given e-mail) anywhere from the app (is a static class, but it has to be initialized anyway)
        Usage:
        try {
            Utente actualUser = (Utente) new Utente.getUserInfo(firebaseAuth.getCurrentUser().getEmail()).execute().get();
            //Do whatever the fuck you want
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     */
    public static class getUserInfo extends AsyncTask {
        String email;

        public getUserInfo(String email){
            this.email=email;
        }

        @Override
        protected Utente doInBackground(Object[] objects) {
            Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").whereEqualTo("Mail", email).get();
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

    public String getUserId(){
        return this.id;
    }

    public String getUserMail(){
        return this.mail;
    }

    public String getUserName(){
        return this.nome;
    }

    public String getUserImage(){
        return this.image;
    }

    public boolean isAdmin(){
        return this.isAdmin;
    }
}
