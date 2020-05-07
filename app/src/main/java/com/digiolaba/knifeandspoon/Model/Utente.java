package com.digiolaba.knifeandspoon.Model;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.View.MainActivity;
import com.digiolaba.knifeandspoon.View.RegisterActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Utente {
    private String id;
    private String mail;
    private String nome;
    private Boolean isAdmin;
    private String image;

    public Utente(String id, String mail, String nome, String image, Boolean isAdmin) {
        this.id = id;
        this.mail = mail;
        this.nome = nome;
        this.image = image;
        this.isAdmin = isAdmin;
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

        public getUserInfo(String email) {
            this.email = email;
        }

        @Override
        protected Utente doInBackground(Object[] objects) {
            Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").whereEqualTo("Mail", email).get();
            Utente obj = null;
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                obj = new Utente(
                        documentSnapshot.getDocuments().get(0).getReference().getPath(),
                        documentSnapshot.getDocuments().get(0).get("Mail").toString(),
                        documentSnapshot.getDocuments().get(0).get("Nome").toString(),
                        documentSnapshot.getDocuments().get(0).get("Immagine").toString(),
                        (Boolean) documentSnapshot.getDocuments().get(0).get("isAdmin")
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public static class getUserInfoByReference extends AsyncTask {
        String path;

        public getUserInfoByReference(String path) {
            this.path = path;
        }

        @Override
        protected Utente doInBackground(Object[] objects) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").document(path.split("/")[1]).get();
            Utente obj = null;
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                obj = new Utente(
                        documentSnapshot.getReference().getPath(),
                        documentSnapshot.get("Mail").toString(),
                        documentSnapshot.get("Nome").toString(),
                        documentSnapshot.get("Immagine").toString(),
                        (Boolean) documentSnapshot.get("isAdmin")
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public static class registerUser extends AsyncTask<Boolean, Void, Boolean> {
        Activity activity;
        Map utente;
        byte[] imgData;

        public registerUser(Activity activity, Map utente, byte[] imgData) {
            this.activity = activity;
            this.utente = utente;
            this.imgData = imgData;
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final StorageReference imageRef = storageRef.child(utente.get("Nome") + ".jpg");
            Task uploadTask = imageRef.putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            utente.put("Immagine", uri.toString());
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            // Add a new document with a generated ID
                            db.collection("Utenti")
                                    .add(utente);
                            MainActivity.startActivity(activity);
                            activity.finish();

                        }
                    });
                }
            });
            try {
                Tasks.await(uploadTask);
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }


    public String getUserId() {
        return this.id;
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
}
