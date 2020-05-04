package com.digiolaba.knifeandspoon.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class SearchActivity extends AppCompatActivity {

    private EditText mSearchBar;
    private ImageButton mSearchBtn;
    private RecyclerView mResultList;

    private FirestoreRecyclerAdapter<Ricetta, RicettaViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mSearchBtn = (ImageButton) findViewById(R.id.btn_search);


        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
    }


    public class RicettaViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public RicettaViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(String nome) {

            ImageView image_Ricetta = (ImageView) mView.findViewById(R.id.immagineRicetta);
            TextView nome_ricetta = (TextView) mView.findViewById(R.id.nomeRicetta);
            TextView tempo_preparazione = (TextView) mView.findViewById(R.id.tempoPreparazione);
            TextView autore_ricetta = (TextView) mView.findViewById(R.id.autoreRicetta);

            nome_ricetta.setText(nome);
        }


    }

    public void firebaseRicettaSearch(String ricetta) {

        Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_LONG).show();

        FirebaseFirestore storage = FirebaseFirestore.getInstance();
        Query search = storage.collection("Ricette").orderBy("Titolo").startAt(ricetta);

        /*FirestoreRecyclerOptions.Builder<SearchActivity> options = new FirestoreRecyclerOptions.Builder<SearchActivity>().setQuery(search, SearchActivity.class);


            adapter = new FirestoreRecyclerAdapter<Ricetta, RicettaViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull RicettaViewHolder holder, int position, @NonNull Ricetta ricetta) {
                    holder.setDetails(ricetta.getTitle());
                }

                @NonNull
                @Override
                public RicettaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search, parent, false);
                    return new RicettaViewHolder(view);
                }
            };
            mResultList.setAdapter(adapter);

        */

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
