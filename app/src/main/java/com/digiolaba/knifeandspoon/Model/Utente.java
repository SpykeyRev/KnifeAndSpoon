package com.digiolaba.knifeandspoon.Model;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.View.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Utente {
    private String id;
    private String mail;
    private String nome;
    private Boolean isAdmin;
    private String image;
    private List<String> favourite;

    public Utente(String id, String mail, String nome, String image, Boolean isAdmin, List<String> favourite) {
        this.id = id;
        this.mail = mail;
        this.nome = nome;
        this.image = image;
        this.isAdmin = isAdmin;
        this.favourite = favourite;
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
    public static class getUserInfo extends AsyncTask<Boolean, Utente, Utente> {
        Activity activity;
        String email;
        Utils.LoadingDialog loadingDialog;

        public getUserInfo(Activity activity, String email) {
            this.email = email;
            this.loadingDialog = new Utils.LoadingDialog(activity);
        }

        @Override
        protected void onPostExecute(Utente result) {
            super.onPostExecute(result);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    loadingDialog.dismissLoadingDialog();
                    ;
                }
            }, 1000);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog.startLoadingDialog();
        }

        @Override
        protected Utente doInBackground(Boolean... booleans) {
            Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").whereEqualTo("Mail", email).get();
            Utente obj = null;
            try {
                QuerySnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                obj = new Utente(
                        documentSnapshot.getDocuments().get(0).getReference().getPath(),
                        documentSnapshot.getDocuments().get(0).get("Mail").toString(),
                        documentSnapshot.getDocuments().get(0).get("Nome").toString(),
                        documentSnapshot.getDocuments().get(0).get("Immagine").toString(),
                        (Boolean) documentSnapshot.getDocuments().get(0).get("isAdmin"),
                        (List<String>) documentSnapshot.getDocuments().get(0).get("Preferiti")
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public static class getUserInfoByReference extends AsyncTask<Boolean, Utente, Utente> {
        String path;
        Activity activity;
        Utils.LoadingDialog loadingDialog;

        public getUserInfoByReference(Activity activity, String path) {
            this.path = path;
            this.loadingDialog = new Utils.LoadingDialog(activity);
        }

        @Override
        protected void onPostExecute(Utente result) {
            super.onPostExecute(result);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    loadingDialog.dismissLoadingDialog();
                    ;
                }
            }, 1000);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog.startLoadingDialog();
        }

        @Override
        protected Utente doInBackground(Boolean... booleans) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Utenti").document(path.split("/")[1]).get();
            Utente obj = null;
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                obj = new Utente(
                        documentSnapshot.getReference().getPath(),
                        documentSnapshot.get("Mail").toString(),
                        documentSnapshot.get("Nome").toString(),
                        documentSnapshot.get("Immagine").toString(),
                        (Boolean) documentSnapshot.get("isAdmin"),
                        (List<String>) documentSnapshot.get("Preferiti")
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
        Utils.LoadingDialog loadingDialog;

        public registerUser(Activity activity, Map utente, byte[] imgData) {
            this.activity = activity;
            this.utente = utente;
            this.imgData = imgData;
            this.loadingDialog = new Utils.LoadingDialog(activity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismissLoadingDialog();
                }
            }, 1000);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog.startLoadingDialog();
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

    public static class changePicUser extends AsyncTask<Boolean, Void, Boolean> {
        Activity activity;
        Utils.LoadingDialog loadingDialog;
        String id, username;
        byte[] imgData;

        public changePicUser(Activity activity, String id, String username, byte[] imgData) {
            this.activity = activity;
            this.id = id;
            this.username = username;
            this.imgData = imgData;
            this.loadingDialog = new Utils.LoadingDialog(activity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismissLoadingDialog();
                    ;
                }
            }, 1000);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog.startLoadingDialog();
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            String documentID = id.split("/")[1];
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            final DocumentReference utentiRef = rootRef.collection("Utenti").document(documentID);
            utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            final StorageReference imageRef = storageRef.child(username + ".jpg");
                            Task uploadTask = imageRef.putBytes(imgData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            uri.toString();
                                            utentiRef.update("Immagine", uri.toString());
                                        }
                                    });
                                }
                            });
                        } else {
                            Log.e("CIAO", "CIAO");
                        }
                    }
                }
            });
            return null;
        }
    }

    public static class checkPreferiti extends AsyncTask<Boolean, Void, Boolean> {
        String documentIdRicetta, pathIdUtente;
        Activity activity;
        Utils.LoadingDialog loadingDialog;

        public checkPreferiti(Activity activity, String documentIdRicetta, String pathIdUtente) {
            this.activity = activity;
            this.documentIdRicetta = documentIdRicetta;
            this.pathIdUtente = pathIdUtente;
            this.loadingDialog = new Utils.LoadingDialog(activity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismissLoadingDialog();
                    ;
                }
            }, 1000);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog.startLoadingDialog();
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            Boolean found = false;
            String documentIdUtente = pathIdUtente.split("/")[1];
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
            Task<DocumentSnapshot> documentSnapshotTask = utentiRef.get();
            try {
                DocumentSnapshot documentSnapshots = Tasks.await(documentSnapshotTask);
                List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                for (int i = 0; i < preferiti.size(); i++) {
                    if (preferiti.get(i).equals(documentIdRicetta)) {
                        found = true;
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return found;
        }
    }

    public static class setPreferiti extends AsyncTask<Boolean, Void, Boolean> {
        String documentIdRicetta, pathIdUtente;
        Boolean fav;
        Activity activity;

        public setPreferiti(Activity activity, String documentIdRicetta, String pathIdUtente, Boolean fav) {
            this.documentIdRicetta = documentIdRicetta;
            this.pathIdUtente = pathIdUtente;
            this.fav = fav;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            String documentIdUtente = pathIdUtente.split("/")[1];
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            final DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
            utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots.exists()) {
                            if (fav) {
                                List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                                preferiti.add(documentIdRicetta);
                                utentiRef.update("Preferiti", preferiti);
                            } else {
                                List<String> preferiti = (List<String>) documentSnapshots.get("Preferiti");
                                preferiti.remove(documentIdRicetta);
                                utentiRef.update("Preferiti", preferiti);
                            }

                        }
                    }
                }
            });

            return null;
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

    public List<String> getFavourite() {
        return this.favourite;
    }
}
