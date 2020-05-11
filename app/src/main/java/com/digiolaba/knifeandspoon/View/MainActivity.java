package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Utente actualUser;

    public void MainActivity() {

    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SwipeRefreshLayout pullToRefresh;
    private FloatingActionButton fab_main;
    private ExtendedFloatingActionButton fab_add, fab_search, fab_settings, fab_favourite;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    private List<Ricetta> ricettas;
    private Boolean isOpen = false;
    private Context context = MainActivity.this;
    private CoordinatorLayout coordinatorLayout;
    private SliderView sliderView;
    private LinearLayout layoutFeed;
    SliderAdapter adapter;
    private FirebaseUser fireUser;
    private static int LAUNCH_SHOW_RICETTA_ACTIVITY = 2912;
    private static int LAUNCH_SETTINGS_ACTIVITY = 1998;
    private final List<Ricetta> obj = new ArrayList();



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
        fab_favourite = (ExtendedFloatingActionButton) findViewById(R.id.fabFavoutiteMain);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
        layoutFeed = (LinearLayout) findViewById(R.id.layoutFeed);
        //Setting up firebase for userInfo
        setUserInfo();
        //Setting up imageSlider
        sliderView = findViewById(R.id.imageSlider);
        adapter = new SliderAdapter(MainActivity.this, this, FirebaseAuth.getInstance().getCurrentUser(), actualUser);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds
        sliderView.startAutoCycle();
        loadImageSliderWithRicette();
        FABClickManagement();
        FABLongClickManagement();
    }

    private void setUserInfo() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        TextView userName = (TextView) findViewById(R.id.userName);
        try {
            fireUser = firebaseAuth.getCurrentUser();
            if (!fireUser.isAnonymous()) {
                actualUser = (Utente) new Utente.getUserInfo(this, firebaseAuth.getCurrentUser().getEmail()).execute().get();
                userName.setText(actualUser.getUserName());
                CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
                Picasso.get().load(actualUser.getUserImage()).into(userImage);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void refresh() {
        loadImageSliderWithRicette();
        try {
            actualUser = (Utente) new Utente.getUserInfo(this, firebaseAuth.getCurrentUser().getEmail()).execute().get();
            CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
            Glide.with(this).load(actualUser.getUserImage()).into(userImage);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pullToRefresh.setRefreshing(false);
    }

    private void loadImageSliderWithRicette() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference ricetteRef = rootRef.collection("Ricette");
        Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", true);
        queryrRicettaApprovata.limit(10).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        if (task.isSuccessful()) {
                            for (int i = 0; i < result.size(); i++) {
                                obj.add(new Ricetta(
                                        result.getDocuments().get(i).getId(),
                                        result.getDocuments().get(i).get("Autore").toString(),
                                        result.getDocuments().get(i).get("Titolo").toString(),
                                        result.getDocuments().get(i).get("Tempo di preparazione").toString(),
                                        result.getDocuments().get(i).get("Numero persone").toString(),
                                        result.getDocuments().get(i).get("Thumbnail").toString(),
                                        (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                        (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                        (Boolean) result.getDocuments().get(i).get("isApproved")
                                ));
                            }
                            List<SliderItem> sliderItems = new ArrayList<SliderItem>();
                            if (adapter.getCount() != 0) {
                                for (int i = 0; i < obj.size(); i++) {
                                    SliderItem sliderItem = new SliderItem();
                                    sliderItem.setDescription(obj.get(i).getTitle());
                                    sliderItem.setImageUrl(obj.get(i).getThumbnail());
                                    sliderItems.add(sliderItem);
                                }
                                adapter.renewItems(sliderItems, ricettas);
                            } else {
                                for (int i = 0; i < obj.size(); i++) {
                                    SliderItem sliderItem = new SliderItem();
                                    sliderItem.setDescription(obj.get(i).getTitle());
                                    sliderItem.setImageUrl(obj.get(i).getThumbnail());
                                    adapter.addItem(sliderItem, obj.get(i));
                                }
                            }
                            if(layoutFeed.getChildCount()!=0)
                            {
                                layoutFeed.removeAllViews();
                            }
                            loadFeed();
                        }
                    }
                }
        );
        //Task<QuerySnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("Ricette").limit(10).get();
        /*try {
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
        }*/
    }

    private void loadFeed() {
        for (int i = 0; i < obj.size(); i++) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View addView = layoutInflater.inflate(R.layout.row_feed_layout, null);
            TextView txtNomeRicettaFeed = (TextView) addView.findViewById(R.id.txtFeedNomeRicetta);
            TextView txtTempoPreparazioneFeed = (TextView) addView.findViewById(R.id.txtFeedTempoPreparazione);
            TextView txtPersoneFeed = (TextView) addView.findViewById(R.id.txtFeedPersone);
            final ImageView ricettaImageFeed = (ImageView) addView.findViewById(R.id.imgFeedRicetta);
            Picasso.get().load(obj.get(i).getThumbnail()).into(ricettaImageFeed);
            txtNomeRicettaFeed.setText(obj.get(i).getTitle());
            txtTempoPreparazioneFeed.setText(obj.get(i).getTempo().concat(" minuti"));
            String feedPersone = "Per ".concat(Utils.personaOrPersone(obj.get(i).getPersone()));
            txtPersoneFeed.setText(feedPersone);
            RelativeLayout layoutContainer = (RelativeLayout) addView.findViewById(R.id.layoutFeedMainAndPic);
            final int position = i;
            layoutContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        Intent intent = new Intent(MainActivity.this, ShowRicettaActivity.class);
                        Bundle bundle = Utils.loadBundle(obj.get(position));
                        //Casting from imageSlider to Drawable and conversion into byteArray
                        Drawable d = ricettaImageFeed.getDrawable();
                        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] bitmapdata = stream.toByteArray();
                        bundle.putByteArray("Thumbnail", bitmapdata);
                        bundle.putBoolean("isAdmin", false);
                        checkPreferitiOnFirebase(obj.get(position).getId(),bundle);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }

                }
            });
            layoutFeed.addView(addView);
        }
    }

    private void checkPreferitiOnFirebase(final String idRicetta, final Bundle bundle)
    {
        String documentIdUtente = actualUser.getUserId().split("/")[1];
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference utentiRef = rootRef.collection("Utenti").document(documentIdUtente);
        final Boolean[] found = {false};
        utentiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Intent intent = new Intent(MainActivity.this, ShowRicettaActivity.class);
                if(!fireUser.isAnonymous())
                {
                    DocumentSnapshot documentSnapshots=task.getResult();
                    List<String>preferiti=(List<String>) documentSnapshots.get("Preferiti");
                    for (int i = 0; i < preferiti.size(); i++) {
                        if (preferiti.get(i).equals(idRicetta)) {
                            found[0] = true;
                        }
                    }
                    bundle.putBoolean("isFav",found[0]);
                    bundle.putString("pathIdUser", actualUser.getUserId());
                }
                else
                {
                    bundle.putBoolean("isFav",false);
                    bundle.putString("pathIdUser", "anonymous");
                }
                intent.putExtras(bundle);
                startActivityForResult(intent, LAUNCH_SHOW_RICETTA_ACTIVITY);
            }
        });
    }



    private void FABClickManagement() {
        fab_add.setClickable(false);
        fab_add.setEnabled(false);
        fab_search.setClickable(false);
        fab_search.setEnabled(false);
        fab_settings.setClickable(false);
        fab_settings.setEnabled(false);
        fab_favourite.setClickable(false);
        fab_favourite.setEnabled(false);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FABShowDifferentUsers();
            }
        });
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fireUser.isAnonymous()) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    GoogleSignInClient client = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                                    client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("EXIT", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.anonymous_try_add)).setPositiveButton(getString(R.string.let_me_register), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, InsertRicettaActivity.class);
                    intent.putExtra("actualUseridentifier", actualUser.getUserId());
                    startActivity(intent);
                }
            }
        });
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                if (!fireUser.isAnonymous()) {
                    intent.putExtra("pathIdUser", actualUser.getUserId());
                } else {
                    intent.putExtra("pathIdUser", "anonymous");
                }
                startActivity(intent);
            }
        });
        fab_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fireUser.isAnonymous()) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    GoogleSignInClient client = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                                    client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("EXIT", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.anonymous_try_fav)).setPositiveButton(getString(R.string.let_me_register), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, FavouriteActivity.class);
                    intent.putExtra("pathIdUser", actualUser.getUserId());
                    startActivity(intent);
                }
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
                intent.putExtra("id", actualUser.getUserId());
                intent.putExtra("nome", actualUser.getUserName());
                intent.putExtra("isAdmin", actualUser.getisAdmin());
                startActivityForResult(intent, LAUNCH_SETTINGS_ACTIVITY);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_SETTINGS_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                refresh();
            }
        }
        if (requestCode == LAUNCH_SHOW_RICETTA_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                new Utente.setPreferiti(this, data.getExtras().getString("docRicetta"), data.getExtras().getString("docUser"), data.getExtras().getBoolean("fav")).execute();
            }
        }
    }

    private void FABShowDifferentUsers() {
        if (fireUser.isAnonymous()) {
            fab_add.setBackgroundColor(getColor(android.R.color.darker_gray));
            fab_favourite.setBackgroundColor(getColor(android.R.color.darker_gray));
            if (isOpen) {
                fab_add.startAnimation(fab_close);
                fab_search.startAnimation(fab_close);
                fab_favourite.startAnimation(fab_close);
                fab_main.startAnimation(fab_anticlock);
                fab_add.setClickable(false);
                fab_add.setEnabled(false);
                fab_search.setClickable(false);
                fab_search.setEnabled(false);
                fab_favourite.setClickable(false);
                fab_favourite.setEnabled(false);
                isOpen = false;
            } else {
                fab_add.startAnimation(fab_open);
                fab_search.startAnimation(fab_open);
                fab_favourite.startAnimation(fab_open);
                fab_main.startAnimation(fab_clock);
                fab_add.setClickable(true);
                fab_add.setEnabled(true);
                fab_search.setClickable(true);
                fab_search.setEnabled(true);
                fab_favourite.setClickable(true);
                fab_favourite.setEnabled(true);
                isOpen = true;
            }
        } else if (!fireUser.isAnonymous()) {
            if (isOpen) {
                fab_add.startAnimation(fab_close);
                fab_search.startAnimation(fab_close);
                fab_settings.startAnimation(fab_close);
                fab_favourite.startAnimation(fab_close);
                fab_main.startAnimation(fab_anticlock);
                fab_add.setClickable(false);
                fab_add.setEnabled(false);
                fab_search.setClickable(false);
                fab_search.setEnabled(false);
                fab_settings.setClickable(false);
                fab_settings.setEnabled(false);
                fab_favourite.setClickable(false);
                fab_favourite.setEnabled(false);
                isOpen = false;
            } else {
                fab_add.startAnimation(fab_open);
                fab_search.startAnimation(fab_open);
                fab_settings.startAnimation(fab_open);
                fab_favourite.startAnimation(fab_open);
                fab_main.startAnimation(fab_clock);
                fab_add.setClickable(true);
                fab_add.setEnabled(true);
                fab_search.setClickable(true);
                fab_search.setEnabled(true);
                fab_settings.setClickable(true);
                fab_settings.setEnabled(true);
                fab_favourite.setClickable(true);
                fab_favourite.setEnabled(true);
                isOpen = true;
            }
        }


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
        fab_favourite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(coordinatorLayout, getResources().getString(R.string.preferiti));
                return false;
            }
        });
    }


 /*   public void getUserInfo(String email) {
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

    }*/

    /*@Override
    protected void onPause() {
        super.onPause();
        coordinatorLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        coordinatorLayout.setVisibility(View.VISIBLE);
    }*/
}
