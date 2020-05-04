package com.digiolaba.knifeandspoon.View;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.digiolaba.knifeandspoon.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;
    private TextView text;
    public LoadingDialog(Activity myActivity){
        activity=myActivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        LayoutInflater inflater= activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog,null));
        builder.setCancelable(false);
        dialog=builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        text = (TextView) dialog.findViewById(R.id.textLoading);
    }

    public void updateText(String newText){
        if(text!=null){
            text.setText(newText);
        }
    }

    public void dismissLoadingDialog(){
        dialog.cancel();
    }
}
