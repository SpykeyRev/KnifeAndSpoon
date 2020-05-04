package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

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

        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText= mSearchBar.getText().toString();

                firebaseRicettaSearch(searchText);

            }
        });


    }

    public void firebaseRicettaSearch(String ricetta) {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();

        FirebaseFirestore storage= FirebaseFirestore.getInstance();
        Query search = storage.collection("Ricette").orderBy("Titolo").startAt(ricetta);

        FirestoreRecyclerOptions.Builder<SearchActivity> options = new FirestoreRecyclerOptions.Builder<SearchActivity>().setQuery(search, SearchActivity.class);



    }

    public class RicettaViewHolder extends RecyclerView.ViewHolder {

       View mView;

        public RicettaViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDetails(String imageRicetta){

            ImageView image_Ricetta = (ImageView) mView.findViewById(R.id.immagineRicetta);
            TextView nome_ricetta = (TextView) mView.findViewById(R.id.nomeRicetta);
            TextView tempo_preparazione = (TextView) mView.findViewById(R.id.tempoPreparazione);
            TextView autore_ricetta = (TextView) mView.findViewById(R.id.autoreRicetta);
        }


    }

    private FirestoreRecyclerAdapter<SearchActivity,RicettaViewHolder > adapter;

    adapter = new FirestoreRecyclerAdapter<SearchActivity,RicettaViewHolder>() {
        @Override
        protected void onBindViewHolder(@NonNull holder RicettaViewHolder, int position, @NonNull SearchActivity productModel) {

        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    };
recyclerView.setAdapter(adapter);




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent=new Intent(SearchActivity.this,MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
