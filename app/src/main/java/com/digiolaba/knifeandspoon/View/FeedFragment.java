package com.digiolaba.knifeandspoon.View;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class FeedFragment extends Fragment {

    public FeedFragment() {
        // Required empty public constructor
    }

    private List<Ricetta> ricettas;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bundle bundle=getArguments();
        //if(bundle!=null)
        //ricettas= (List<Ricetta>) bundle.getSerializable("ricettas");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_feed, container, false);
        Bundle bundle=getArguments();
        if(bundle!=null)
        {
            loadFeed(view,bundle,inflater);

        }
        return view;
    }

    private void loadFeed(View view, final Bundle bundleExt, LayoutInflater inflater)
    {
        LinearLayout frameLayout=(LinearLayout) view.findViewById(R.id.feedContainer);
        ricettas= (List<Ricetta>) bundleExt.getSerializable("ricettas");
        for (int i = 0; i < ricettas.size(); i++) {
            View addView = inflater.inflate(R.layout.row_feed_layout, null);
            TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
            TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
            TextView txtPersoneFeed = (TextView) addView.findViewById(R.id.txtFeedPersone);
            final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
            Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);
            txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());
            txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
            String feedPersone = "Per ".concat(Utils.personaOrPersone(ricettas.get(i).getPersone()));
            txtPersoneFeed.setText(feedPersone);
            RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
            final int position = i;
            layoutContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(getActivity(), ShowRicettaActivity.class);
                        Bundle bundle = Utils.loadBundle(ricettas.get(position));
                        //Casting from imageSlider to Drawable and conversion into byteArray
                        Drawable d = ricettaImageFeed.getDrawable();
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] bitmapdata = stream.toByteArray();
                        bundle.putByteArray("Thumbnail", bitmapdata);
                        bundle.putString("ThumbnailURL", ricettas.get(position).getThumbnail());
                        if(bundleExt.getString("class").equals("RicetteToApproveActivity"))
                        {
                            bundle.putBoolean("isAdmin", true);
                            bundle.putString("pathIdUser", "admin");
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                        else if(bundleExt.getString("class").equals("MainActivity"))
                        {

                            String actualUser=bundleExt.getString("pathIdUser");
                            if(actualUser.equals("anonymous"))
                            {
                                bundle.putString("pathIdUser",actualUser);
                                bundle.putBoolean("isFav",false);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            else
                            {
                                bundle.putString("pathIdUser", actualUser);
                                checkPreferitiOnFirebase(ricettas.get(position).getId(),bundle,intent,actualUser);
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }

                }
            });
            frameLayout.addView(addView);
        }
    }

    private void checkPreferitiOnFirebase(final String idRicetta, final Bundle bundle, final Intent intent, String documentIdUtente)
    {

            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
            final Boolean[] found = {false};
            utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshots=task.getResult();
                    List<String>preferiti=(List<String>) documentSnapshots.get("Preferiti");
                    for (int i = 0; i < preferiti.size(); i++) {
                        if (preferiti.get(i).equals(idRicetta)) {
                            found[0] = true;
                        }
                    }
                    bundle.putBoolean("isFav",found[0]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }



}
