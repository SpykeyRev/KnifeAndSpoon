package com.digiolaba.knifeandspoon.Controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.SliderItem;
import com.digiolaba.knifeandspoon.R;
import com.digiolaba.knifeandspoon.View.ShowRicettaActivity;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends
        SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private Context context;
    private List<SliderItem> mSliderItems = new ArrayList<>();
    private List<Ricetta> ricettas = new ArrayList<>();

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public void renewItems(List<SliderItem> sliderItems,List<Ricetta> ricettas) {
        this.mSliderItems = sliderItems;
        this.ricettas=ricettas;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        this.ricettas.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(SliderItem sliderItem,Ricetta ricetta) {
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
                Intent intent=new Intent(context, ShowRicettaActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("Autore",ricettas.get(position).getAuthorId());
                //Casting from imageSlider to Drawable and conversion into byteArray
                Drawable d=viewHolder.imageViewBackground.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                bundle.putByteArray("Thumbnail",bitmapdata);
                bundle.putString("Titolo",ricettas.get(position).getTitle());
                bundle.putSerializable("Passaggi", (Serializable) ricettas.get(position).getSteps());
                bundle.putSerializable("Ingredienti", (Serializable) ricettas.get(position).getIngredienti());
                bundle.putString("Tempo",ricettas.get(position).getTempo());
                bundle.putString("Persone",ricettas.get(position).getPersone());
                bundle.putString("Autore",ricettas.get(position).getAuthorId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
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