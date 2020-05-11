package com.digiolaba.knifeandspoon.Controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.SliderItem;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.digiolaba.knifeandspoon.View.ShowRicettaActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SliderAdapter extends
        SliderViewAdapter<SliderAdapter.SliderAdapterVH> {
    private Activity activity;
    private Context context;
    private FirebaseUser fireUser;
    private Utente actualUser;
    private List<SliderItem> mSliderItems = new ArrayList<>();
    private List<Ricetta> ricettas = new ArrayList<>();

    public SliderAdapter(Activity activity, Context context, FirebaseUser fireUser, Utente actualUser) {
        this.activity=activity;
        this.context = context;
        this.fireUser = fireUser;
        this.actualUser = actualUser;
    }

    public void setActualUser(Utente actualUser){
        this.actualUser=actualUser;
    }

    public void renewItems(List<SliderItem> sliderItems, List<Ricetta> ricettas) {
        this.mSliderItems = sliderItems;
        this.ricettas = ricettas;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        this.ricettas.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(SliderItem sliderItem, Ricetta ricetta) {
        this.mSliderItems.add(sliderItem);
        this.ricettas.add(ricetta);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {
        SliderItem sliderItem = mSliderItems.get(position);
        viewHolder.textViewDescription.setText(sliderItem.getDescription());
        viewHolder.textViewDescription.setTextSize(16);
        viewHolder.textViewDescription.setTextColor(Color.WHITE);
        Glide.with(viewHolder.itemView)
                .load(sliderItem.getImageUrl())
                .centerCrop()
                .into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, ShowRicettaActivity.class);
                final Bundle bundle = Utils.loadBundle(ricettas.get(position));
                //Casting from imageSlider to Drawable and conversion into byteArray
                Drawable d = viewHolder.imageViewBackground.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] bitmapdata = stream.toByteArray();
                bundle.putByteArray("Thumbnail", bitmapdata);
                if (!fireUser.isAnonymous()) {
                    bundle.putString("pathIdUser", actualUser.getUserId());
                    String documentIdUtente = actualUser.getUserId().split("/")[1];
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
                    utentiRef.get().addOnCompleteListener(
                            new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot result = task.getResult();
                                        List<String> preferiti = (List<String>) result.get("Preferiti");
                                        Boolean found=false;
                                        for (int i = 0; i < preferiti.size(); i++) {
                                            if (preferiti.get(i).equals(ricettas.get(position).getId())) {
                                                found = true;
                                            }
                                        }
                                        bundle.putBoolean("isFav", found);
                                        intent.putExtras(bundle);
                                        context.startActivity(intent);
                                    }
                                }
                            }
                    );

                } else {
                    bundle.putString("pathIdUser", "anonymous");
                    bundle.putBoolean("isFav", false);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void checkPreferiti(final String idRicetta) {

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageViewBackground;
        ImageView imageGifContainer;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}