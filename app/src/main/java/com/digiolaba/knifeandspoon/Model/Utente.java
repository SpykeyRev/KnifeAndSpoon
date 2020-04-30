package com.digiolaba.knifeandspoon.Model;

import android.os.AsyncTask;

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

    /*public static Utente getUserInfo(String email){
        final List<Utente> users=new ArrayList<Utente>();
        FirebaseFirestore storage=FirebaseFirestore.getInstance();
        try {
            await(storage.collection("Utenti").whereEqualTo("Mail", email)
                    .limit(1).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                String id=task.getResult().getDocuments().get(0).getId();
                                String mail=task.getResult().getDocuments().get(0).get("Mail").toString();
                                String nome=task.getResult().getDocuments().get(0).get("Nome").toString();
                                String immagine=task.getResult().getDocuments().get(0).get("Immagine").toString();
                                Boolean isAdmin=(Boolean)task.getResult().getDocuments().get(0).get("isAdmin");
                                users.add(new Utente(id,mail,nome,immagine,isAdmin));
                            }
                        }
                    }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return users.get(0);
    }*/

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
