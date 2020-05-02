package com.digiolaba.knifeandspoon.Controller;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.digiolaba.knifeandspoon.View.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class Utils {
    public static boolean checkNetworkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void errorDialog(Context context,int message,int button){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(message))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnackbar(View view, String message)
    {
        Snackbar.make(view,message,Snackbar.LENGTH_LONG).setAction("Chiudi", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    public static class LoadSpinner
    {
        ProgressDialog nDialog;
        public LoadSpinner(Context context){
            nDialog = new ProgressDialog(context);
            String[] texts={"Scolo la Pasta",
                    "Affetto il Ciauscolo",
                    "Friggo le Patatine",
                    "Inforno la Pizza",
                    "Preparo il Pane con l'olio",
                    "Inforno i Vincisgrassi"};
            nDialog.setMessage(texts[new Random().nextInt((5 - 0) + 1) + 0]);
            nDialog.setIndeterminate(true);
            nDialog.setCancelable(true);
        }

        public void show(){
            nDialog.show();
        }

        public void close(){
            nDialog.cancel();
        }
    }
}
