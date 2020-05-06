package com.digiolaba.knifeandspoon.View;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.digiolaba.knifeandspoon.Controller.SliderAdapter;
import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.Model.SliderItem;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.digiolaba.knifeandspoon.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Utente actualUser;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SwipeRefreshLayout pullToRefresh;
    private FloatingActionButton fab_main;
    private ExtendedFloatingActionButton fab_add, fab_search, fab_settings;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    private List<Ricetta> ricettas;
    private Boolean isOpen = false;
    private Context context = MainActivity.this;
    private CoordinatorLayout coordinatorLayout;
    private SliderView sliderView;
    private LinearLayout layoutFeed;
    SliderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullToRefresh = findViewById(R.id.swipeRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        //findViewById(R.id.buttonLogout).setOnClickListener(this);
        //findViewById(R.id.buttonDisconnect).setOnClickListener(this);
        fab_main = (FloatingActionButton) findViewById(R.id.fabOptions);
        fab_add = (ExtendedFloatingActionButton) findViewById(R.id.fabAdd);
        fab_search = (ExtendedFloatingActionButton) findViewById(R.id.fabSearch);
        fab_settings = (ExtendedFloatingActionButton) findViewById(R.id.fabSettings);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
        layoutFeed=(LinearLayout)findViewById(R.id.layoutFeed);
        //Setting up firebase for userInfo
        setUserInfo();
        //Setting up imageSlider
        sliderView = findViewById(R.id.imageSlider);
        adapter = new SliderAdapter(this);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds
        //sliderView.startAutoCycle();
        loadImageSliderWithRicette();
        FABClickManagement();
        FABLongClickManagement();
        loadFeed();
    }

    private void setUserInfo() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            actualUser = (Utente) new Utente.getUserInfo(firebaseAuth.getCurrentUser().getEmail()).execute().get();
            TextView userName = (TextView) findViewById(R.id.userName);
            userName.setText(actualUser.getUserName());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser fireUser = firebaseAuth.getCurrentUser();
        Picasso.get().load(fireUser.getPhotoUrl()).into(userImage);
    }

    private void refresh() {
        loadImageSliderWithRicette();
        pullToRefresh.setRefreshing(false);
    }

    private void loadImageSliderWithRicette() {
        try {
            ricettas = (List<Ricetta>) new Ricetta.getFirstTenRecipe().execute().get();
            List<SliderItem> sliderItems = new ArrayList<SliderItem>();
            if (adapter.getCount() != 0) {
                for (int i = 0; i < ricettas.size(); i++) {
                    SliderItem sliderItem = new SliderItem();
                    sliderItem.setDescription(ricettas.get(i).getTitle());
                    sliderItem.setImageUrl(ricettas.get(i).getThumbnail());
                    sliderItems.add(sliderItem);
                }
                adapter.renewItems(sliderItems, ricettas);
            } else {
                for (int i = 0; i < ricettas.size(); i++) {
                    SliderItem sliderItem = new SliderItem();
                    sliderItem.setDescription(ricettas.get(i).getTitle());
                    sliderItem.setImageUrl(ricettas.get(i).getThumbnail());
                    adapter.addItem(sliderItem, ricettas.get(i));
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loadFeed()
    {
        for(int i=0;i<ricettas.size();i++)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);
            TextView txtNomeRicettaFeed=(TextView)addView.findViewById(R.id.txtFeedNomeRicetta);
            TextView txtTempoPreparazioneFeed=(TextView)addView.findViewById(R.id.txtFeedTempoPreparazione);
            TextView txtPersoneFeed=(TextView)addView.findViewById(R.id.txtFeedPersone);
            final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
            Picasso.get().load(ricettas.get(i).getThumbnail()).into(ricettaImageFeed);
            txtNomeRicettaFeed.setText(ricettas.get(i).getTitle());

            txtTempoPreparazioneFeed.setText(ricettas.get(i).getTempo().concat(" minuti"));
            String feedPersone="Per ".concat(Utils.personaOrPersone(ricettas.get(i).getPersone()));
            txtPersoneFeed.setText(feedPersone);
            LinearLayout layoutContainer=(LinearLayout)addView.findViewById(R.id.layoutFeedMainAndPic);
            final int position = i;
            layoutContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        Intent intent=new Intent(MainActivity.this,ShowRicettaActivity.class);
                        Bundle bundle=Utils.loadBundle(ricettas.get(position));
                        //Casting from imageSlider to Drawable and conversion into byteArray
                        Drawable d = ricettaImageFeed.getDrawable();
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] bitmapdata = stream.toByteArray();
                        bundle.putByteArray("Thumbnail", bitmapdata);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    catch(RuntimeException e)
                    {
                        e.printStackTrace();
                    }

                }
            });
            layoutFeed.addView(addView);
        }
    }




    private void FABClickManagement() {
        fab_add.setClickable(false);
        fab_add.setEnabled(false);
        fab_search.setClickable(false);
        fab_search.setEnabled(false);
        fab_settings.setClickable(false);
        fab_settings.setEnabled(false);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    fab_add.startAnimation(fab_close);
                    fab_search.startAnimation(fab_close);
                    fab_settings.startAnimation(fab_close);
                    fab_main.startAnimation(fab_anticlock);
                    fab_add.setClickable(false);
                    fab_add.setEnabled(false);
                    fab_search.setClickable(false);
                    fab_search.setEnabled(false);
                    fab_settings.setClickable(false);
                    fab_settings.setEnabled(false);
                    isOpen = false;
                } else {
                    fab_add.startAnimation(fab_open);
                    fab_search.startAnimation(fab_open);
                    fab_settings.startAnimation(fab_open);
                    fab_main.startAnimation(fab_clock);
                    fab_add.setClickable(true);
                    fab_add.setEnabled(true);
                    fab_search.setClickable(true);
                    fab_search.setEnabled(true);
                    fab_settings.setClickable(true);
                    fab_settings.setEnabled(true);
                    isOpen = true;
                }
            }
        });
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InsertRicettaActivity.class);
                intent.putExtra("actualUseridentifier", actualUser.getUserId());
                startActivity(intent);
            }
        });
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        fab_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
                Drawable d = userImage.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] bitmapdata = stream.toByteArray();
                intent.putExtra("userProPic", bitmapdata);
                startActivity(intent);
            }
        });
    }

    private void FABLongClickManagement() {
        fab_main.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.menu));
                return false;
            }
        });
        fab_add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.add_ricetta));
                return false;
            }
        });
        fab_search.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.search_ricetta));
                return false;
            }
        });
        fab_settings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.settings));

                return false;
            }
        });
    }


    public void getUserInfo(String email) {
        final List<Utente> users = new ArrayList();
        FirebaseFirestore storage = FirebaseFirestore.getInstance();
        Task task = storage.collection("Utenti").whereEqualTo("Mail", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String id = task.getResult().getDocuments().get(0).getId();
                            String mail = task.getResult().getDocuments().get(0).get("Mail").toString();
                            String nome = task.getResult().getDocuments().get(0).get("Nome").toString();
                            String immagine = task.getResult().getDocuments().get(0).get("Immagine").toString();
                            Boolean isAdmin = (Boolean) task.getResult().getDocuments().get(0).get("isAdmin");
                            actualUser = new Utente(id, mail, nome, immagine, isAdmin);
                            TextView textView = findViewById(R.id.userName);
                            textView.setText(actualUser.getUserName());
                        }
                    }
                });

    }
}
