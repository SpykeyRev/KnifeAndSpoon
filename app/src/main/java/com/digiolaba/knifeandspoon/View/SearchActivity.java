package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchBar;
    private ImageButton mSearchBtn;

    private RecyclerView mResultList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mSearchBtn = (ImageButton) findViewById(R.id.btn_search);

        mResultList = (RecyclerView) findViewById(R.id.result_list);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseRicettaSearch();

            }
        });

    }

    private void firebaseRicettaSearch() {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();

        //FirestoreRecyclerOptions<Ricetta, RicettaViewHolder> options = new FirestoreRecyclerOptions.Builder<Ricetta>().setQuery(query, RicettaViewHolder.class).build();

    }

    public class RicettaViewHolder extends RecyclerView.ViewHolder {

       View mView;

        public RicettaViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDetails(Context ctx, String Ricetta, String imageRicetta){
            TextView nome_ricetta = (TextView) mView.findViewById(R.id.nome_ricetta);
            TextView tempo_preparazione = (TextView) mView.findViewById(R.id.tempo_preparazione);
            ImageView image_Ricetta = (ImageView) mView.findViewById(R.id.image_ricetta);


        }


    }
}
