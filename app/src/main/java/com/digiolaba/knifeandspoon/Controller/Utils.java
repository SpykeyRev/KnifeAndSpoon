package com.digiolaba.knifeandspoon.Controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

public class Utils {
    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void errorDialog(Context context, int message, int button) {
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

    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Chiudi", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }


    public static class LoadingDialog {
        private Activity activity;
        private AlertDialog dialog;
        private TextView text;

        public LoadingDialog(Activity myActivity) {
            activity = myActivity;
        }

        public void startLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.loading_dialog, null));
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            text = (TextView) dialog.findViewById(R.id.textLoading);
        }

        public void updateText(String newText) {
            if (text != null) {
                text.setText(newText);
            }
        }

        public void dismissLoadingDialog() {
            dialog.cancel();
        }
    }

    public static class SuccessDialog {
        private Activity activity;
        private AlertDialog dialog;
        private Button ok;

        public SuccessDialog(Activity myActivity) {
            activity = myActivity;
        }

        public void startLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.success_dialog, null));
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            ok = (Button) dialog.findViewById(R.id.exitSuccessDialog);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissLoadingDialog();
                    activity.finish();
                }
            });
        }

        public void dismissLoadingDialog() {
            dialog.cancel();
        }
    }

    public static class ErrorDialog {
        private Activity activity;
        private AlertDialog dialog;
        private TextView text;

        public ErrorDialog(Activity myActivity) {
            activity = myActivity;
        }

        public void startLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.error_dialog, null));
            builder.setCancelable(true);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        public void dismissLoadingDialog() {
            dialog.cancel();
        }
    }

    public static String personaOrPersone(String persone) {
        if (persone.equals("1")) {
            return persone.concat(" persona");
        } else {
            return persone.concat(" persone");
        }
    }


    public static Bundle loadBundle(Ricetta ricettas) {
        Bundle bundle = new Bundle();
        bundle.putString("Autore", ricettas.getAuthorId());
        bundle.putString("Titolo", ricettas.getTitle());
        bundle.putSerializable("Passaggi", (Serializable) ricettas.getSteps());
        bundle.putSerializable("Ingredienti", (Serializable) ricettas.getIngredienti());
        bundle.putString("Tempo", ricettas.getTempo());
        bundle.putString("Persone", ricettas.getPersone());
        bundle.putString("Id", ricettas.getId());
        bundle.putString("Categoria", ricettas.getCategoria());
        return bundle;
    }

    public static class DynamicLoadingDialog {
        private Activity activity;
        private AlertDialog dialog;
        private TextView text;

        public DynamicLoadingDialog(Activity myActivity) {
            activity = myActivity;
        }

        public void startLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.splash_loading_dialog, null));
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            text = (TextView) dialog.findViewById(R.id.textSplashLoading);
        }

        public void updateText(String newText) {
            if (text != null) {
                text.setText(newText);
            }
        }

        public void dismissLoadingDialog() {
            dialog.cancel();
        }
    }
}
