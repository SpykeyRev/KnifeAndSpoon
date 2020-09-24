package com.digiolaba.knifeandspoon.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.Timestamp;
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

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Utente actualUser;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SwipeRefreshLayout pullToRefresh;
    private FloatingActionButton fab_main;
    private ExtendedFloatingActionButton fab_add, fab_search, fab_settings, fab_favourite;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    private Boolean isOpen = false;
    private Context context = MainActivity.this;
    private CoordinatorLayout coordinatorLayout;
    private SliderView sliderView;
    private LinearLayout layoutFeed;
    private SliderAdapter adapter;
    private FirebaseUser fireUser;
    private static int LAUNCH_SETTINGS_ACTIVITY = 1998;
    private String category_selected = null;
    private final List<Ricetta> obj = new ArrayList();
    private Boolean clickedCategoria = false;
    private ImageView antipastoTick;
    private ImageView primoTick;
    private ImageView secondoTick;
    private ImageView contornoTick;
    private ImageView dolceTick;
    private Handler handler;
    private Runnable runnable;

    public void MainActivity() {

    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }


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
        antipastoTick = (ImageView) findViewById(R.id.antipastoTick);
        primoTick = (ImageView) findViewById(R.id.primoTick);
        secondoTick = (ImageView) findViewById(R.id.secondoTick);
        contornoTick = (ImageView) findViewById(R.id.contornoTick);
        dolceTick = (ImageView) findViewById(R.id.dolceTick);
        //Set Category listeners
        setCategoryListeners();
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
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isOpen)
                    fab_main.performClick();
            }
        };
    }

    //filtro ricette per imageSlider
    private void setCategoryListeners() {
        RelativeLayout antipasto = (RelativeLayout) findViewById(R.id.antipasto_view);
        RelativeLayout primo = (RelativeLayout) findViewById(R.id.primo_view);
        RelativeLayout secondo = (RelativeLayout) findViewById(R.id.secondo_view);
        RelativeLayout contorno = (RelativeLayout) findViewById(R.id.contorno_view);
        RelativeLayout dolce = (RelativeLayout) findViewById(R.id.dolce_view);
        ImageView[] ticks = {antipastoTick, primoTick, secondoTick, contornoTick, dolceTick};
        final List<ImageView> listImageViewToRemoveVerified = new ArrayList<ImageView>(Arrays.asList(ticks));
        antipasto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickedCategoria) {
                    category_selected = "Antipasto";
                    checkConnection("loadImageSliderWithCategoryRicette");
                    antipastoTick.setVisibility(View.VISIBLE);
                    clickedCategoria = true;
                } else {
                    if (category_selected != "Antipasto") {
                        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
                            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
                        }
                        category_selected = "Antipasto";
                        checkConnection("loadImageSliderWithCategoryRicette");
                        antipastoTick.setVisibility(View.VISIBLE);
                        clickedCategoria = true;
                    } else {
                        returnToAllRicetteImageSlider(listImageViewToRemoveVerified);
                    }
                }
            }
        });
        primo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickedCategoria) {
                    category_selected = "Primo";
                    checkConnection("loadImageSliderWithCategoryRicette");
                    primoTick.setVisibility(View.VISIBLE);
                    clickedCategoria = true;
                } else {
                    if (category_selected != "Primo") {
                        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
                            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
                        }
                        category_selected = "Primo";
                        checkConnection("loadImageSliderWithCategoryRicette");
                        primoTick.setVisibility(View.VISIBLE);
                        clickedCategoria = true;
                    } else {
                        returnToAllRicetteImageSlider(listImageViewToRemoveVerified);
                    }
                }
            }
        });

        secondo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickedCategoria) {
                    category_selected = "Secondo";
                    checkConnection("loadImageSliderWithCategoryRicette");
                    secondoTick.setVisibility(View.VISIBLE);
                    clickedCategoria = true;
                } else {
                    if (category_selected != "Secondo") {
                        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
                            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
                        }
                        category_selected = "Secondo";
                        checkConnection("loadImageSliderWithCategoryRicette");
                        secondoTick.setVisibility(View.VISIBLE);
                        clickedCategoria = true;
                    } else {
                        returnToAllRicetteImageSlider(listImageViewToRemoveVerified);
                    }
                }
            }
        });
        contorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickedCategoria) {
                    category_selected = "Contorno";
                    checkConnection("loadImageSliderWithCategoryRicette");
                    contornoTick.setVisibility(View.VISIBLE);
                    clickedCategoria = true;
                } else {
                    if (category_selected != "Contorno") {
                        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
                            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
                        }
                        category_selected = "Contorno";
                        checkConnection("loadImageSliderWithCategoryRicette");
                        contornoTick.setVisibility(View.VISIBLE);
                        clickedCategoria = true;
                    } else {
                        returnToAllRicetteImageSlider(listImageViewToRemoveVerified);
                    }
                }
            }
        });
        dolce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickedCategoria) {
                    category_selected = "Dolce";
                    checkConnection("loadImageSliderWithCategoryRicette");
                    dolceTick.setVisibility(View.VISIBLE);
                    clickedCategoria = true;
                } else {
                    if (category_selected != "Dolce") {
                        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
                            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
                        }
                        category_selected = "Dolce";
                        checkConnection("loadImageSliderWithCategoryRicette");
                        dolceTick.setVisibility(View.VISIBLE);
                        clickedCategoria = true;
                    } else {
                        returnToAllRicetteImageSlider(listImageViewToRemoveVerified);
                    }
                }
            }
        });
    }

    private void returnToAllRicetteImageSlider(List<ImageView> listImageViewToRemoveVerified) {
        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
        }
        checkConnection("loadImageSliderWithRicette");
        pullToRefresh.setRefreshing(true);
        clickedCategoria = false;
    }

    //carica le corrette informazioni dell'utente attuale
    private void setUserInfo() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        fireUser = firebaseAuth.getCurrentUser();
        if (!fireUser.isAnonymous()) {
            loadAndShowUserInfo();
        }
    }

    private void loadAndShowUserInfo() {
        FirebaseFirestore.getInstance().collection("Utenti").whereEqualTo("Mail", firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            actualUser = new Utente(
                                    result.getDocuments().get(0).getId(),
                                    result.getDocuments().get(0).get("Mail").toString(),
                                    result.getDocuments().get(0).get("Nome").toString(),
                                    result.getDocuments().get(0).get("Immagine").toString(),
                                    (Boolean) result.getDocuments().get(0).get("isAdmin"),
                                    (List<String>) result.getDocuments().get(0).get("Preferiti")
                            );
                            adapter.setActualUser(actualUser);
                            TextView userName = (TextView) findViewById(R.id.userName);
                            userName.setText(actualUser.getUserName());
                            CircleImageView userImage = (CircleImageView) findViewById(R.id.profile_image);
                            Glide.with(MainActivity.this).load(actualUser.getUserImage()).into(userImage);
                        }
                    }
                }
        );
    }

    private void refresh() {
        ImageView[] ticks = {antipastoTick, primoTick, secondoTick, contornoTick, dolceTick};
        final List<ImageView> listImageViewToRemoveVerified = new ArrayList<ImageView>(Arrays.asList(ticks));
        for (int i = 0; i < listImageViewToRemoveVerified.size(); i++) {
            listImageViewToRemoveVerified.get(i).setVisibility(View.GONE);
        }
        checkConnection("loadImageSliderWithRicette");
        if (!fireUser.isAnonymous())
            loadAndShowUserInfo();
    }

    //carica ricette filtrate
    private void loadImageSliderWithCategoryRicette() {
        pullToRefresh.setRefreshing(true);
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference ricetteRef = rootRef.collection("Ricette");
        Query queryrRicettaApprovata = ricetteRef.whereEqualTo("isApproved", true).whereEqualTo("Categoria", category_selected);
        final List<Ricetta> filtered = new ArrayList<Ricetta>();
        queryrRicettaApprovata.limit(10).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        if (task.isSuccessful()) {
                            for (int i = 0; i < result.size(); i++) {
                                filtered.add(new Ricetta(
                                        result.getDocuments().get(i).getId(),
                                        result.getDocuments().get(i).get("Autore").toString(),
                                        result.getDocuments().get(i).get("Titolo").toString(),
                                        result.getDocuments().get(i).get("Categoria").toString(),
                                        result.getDocuments().get(i).get("TempoPreparazione").toString(),
                                        result.getDocuments().get(i).get("NumeroPersone").toString(),
                                        result.getDocuments().get(i).get("Thumbnail").toString(),
                                        (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                        (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                        (Boolean) result.getDocuments().get(i).get("isApproved"),
                                        (Timestamp) result.getDocuments().get(i).get("Timestamp")
                                ));
                            }
                            List<SliderItem> sliderItems = new ArrayList<SliderItem>();
                            if (adapter.getCount() != 0) {
                                for (int i = 0; i < filtered.size(); i++) {
                                    SliderItem sliderItem = new SliderItem();
                                    sliderItem.setDescription(filtered.get(i).getTitle());
                                    sliderItem.setImageUrl(filtered.get(i).getThumbnail());
                                    sliderItems.add(sliderItem);
                                }
                                adapter.renewItems(sliderItems, filtered);
                            } else {
                                for (int i = 0; i < filtered.size(); i++) {
                                    SliderItem sliderItem = new SliderItem();
                                    sliderItem.setDescription(filtered.get(i).getTitle());
                                    sliderItem.setImageUrl(filtered.get(i).getThumbnail());
                                    adapter.addItem(sliderItem, filtered.get(i));
                                }
                            }
                            if (pullToRefresh.isRefreshing()) {
                                pullToRefresh.setRefreshing(false);
                            }
                        }
                    }
                }
        );
    }

    public void getLoadImageSliderWithCategoryRicette() {
        loadImageSliderWithCategoryRicette();
    }

    //carica ricette imageSlider
    private void loadImageSliderWithRicette() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference ricetteRef = rootRef.collection("Ricette");
        Query queryRicettaApprovata = ricetteRef.orderBy("Timestamp", Query.Direction.DESCENDING).whereEqualTo("isApproved", true);
        obj.clear();
        queryRicettaApprovata.limit(10).get().addOnCompleteListener(
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
                                        result.getDocuments().get(i).get("Categoria").toString(),
                                        result.getDocuments().get(i).get("TempoPreparazione").toString(),
                                        result.getDocuments().get(i).get("NumeroPersone").toString(),
                                        result.getDocuments().get(i).get("Thumbnail").toString(),
                                        (List<Map<String, Object>>) result.getDocuments().get(i).get("Ingredienti"),
                                        (List<String>) result.getDocuments().get(i).get("Passaggi"),
                                        (Boolean) result.getDocuments().get(i).get("isApproved"),
                                        (Timestamp) result.getDocuments().get(i).get("Timestamp")
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
                                adapter.renewItems(sliderItems, obj);
                            } else {
                                for (int i = 0; i < obj.size(); i++) {
                                    SliderItem sliderItem = new SliderItem();
                                    sliderItem.setDescription(obj.get(i).getTitle());
                                    sliderItem.setImageUrl(obj.get(i).getThumbnail());
                                    adapter.addItem(sliderItem, obj.get(i));
                                }
                            }
                            layoutFeed.removeAllViews();
                            loadFeed();
                            if (pullToRefresh.isRefreshing()) {
                                pullToRefresh.setRefreshing(false);
                            }
                        }
                    }
                }
        );
    }

    public void getLoadImageSliderWithRicette() {
        loadImageSliderWithRicette();
    }

    //carica feed ricette
    private void loadFeed() {
        FeedFragment feedFragment = new FeedFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable("ricettas", (Serializable) obj);
        bundle.putString("class", getClass().getSimpleName());
        if (fireUser.isAnonymous()) {
            bundle.putString("pathIdUser", "anonymous");
        } else {
            try {
                bundle.putString("pathIdUser", actualUser.getUserId());
            } catch (Exception e) {
                FirebaseFirestore.getInstance().collection("Utenti").whereEqualTo("Mail", firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot result = task.getResult();
                                    actualUser.setUserId(result.getDocuments().get(0).getId());
                                    bundle.putString("pathIdUser", actualUser.getUserId());
                                }
                            }
                        }
                );
                refresh();
            }
        }
        feedFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.layoutFeed, feedFragment).commit();

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
                handler.postDelayed(runnable, 10000);

            }
        });
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //se l'utente è anonimo al click del FAB di aggiunta gli verrà chiesto di accedere tramite Google, poichè la funzionalità è riservata agli utenti registrati
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
                    //se l'utente è registrato allora  verrà reindirizzatto all'activity di inserimento
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
                //se l'utente è anonimo al click del FAB dei preferiti gli verrà chiesto di accedere tramite Google, poichè la funzionalità è riservata agli utenti registrati
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
                    //se l'utente è registrato allora verrà reindirizzato all'activity dei preferiti
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
    }

    //mostra i fab nella maniera opportuna a seconda dell'utente
    private void FABShowDifferentUsers() {
        if (fireUser.isAnonymous()) {
            //se l'utente è anonimo verranno impostati come grigi i FAB di aggiunta e preferiti e non verrà mostrato il FAB impostazioni
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
                handler.removeCallbacks(runnable);
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
            //se l'utente è registrato
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
                handler.removeCallbacks(runnable);
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

    //inizio metodi per gestione long click sui FAB
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
    //fine metodi per gestione long click sui FAB

    private void checkConnection(final String methodInString) {
        try {
            final Method method = getClass().getMethod("get" + methodInString.substring(0, 1).toUpperCase() + methodInString.substring(1));
            boolean conn = isNetworkAvailable();
            if (!conn) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                checkConnection(methodInString);
                                break;

                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.error_connection)).setPositiveButton(getString(R.string.error_ok), dialogClickListener).setCancelable(false)
                        .show();
            } else {
                try {
                    method.invoke(MainActivity.this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
