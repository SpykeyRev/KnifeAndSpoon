package com.digiolaba.knifeandspoon.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.digiolaba.knifeandspoon.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InsertRicettaActivity extends AppCompatActivity {


    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 107;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView img_piatto;
    private EditText etTitolo;
    private FloatingActionButton fab_foto;
    private LinearLayout ingredientiLayout;
    private Button addIngrediente;
    private Button addPassaggio;
    private LinearLayout passaggiLayout;
    private List<View> allDescrizione, allIngredienti;
    private Spinner spCategoria;
    private EditText numeroPersone;
    private EditText tempoPreparazione;
    private String actualUser;
    private final static int PICK_IMAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_ricetta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout_ins);
        fab_foto = (FloatingActionButton) findViewById(R.id.fab_ins_foto);
        img_piatto = (ImageView) findViewById(R.id.img_piatto);
        etTitolo = (EditText) findViewById(R.id.etTitolo);
        numeroPersone = (EditText) findViewById(R.id.etNumeroPersone);
        tempoPreparazione = (EditText) findViewById(R.id.etTempoPreparazione);
        spCategoria = (Spinner) findViewById(R.id.spinnerCategoria);
        ingredientiLayout = (LinearLayout) findViewById(R.id.layoutIngredienti);
        addIngrediente = (Button) findViewById(R.id.addIngrediente);
        addPassaggio = (Button) findViewById(R.id.addPassaggio);
        passaggiLayout = (LinearLayout) findViewById(R.id.listPassaggi);
        TextInputLayout tiLTitolo = (TextInputLayout) findViewById(R.id.layoutTitoloInserimento);
        TextInputLayout tiLPersone = (TextInputLayout) findViewById(R.id.layoutNumeroPersone);
        TextInputLayout tiLTempo = (TextInputLayout) findViewById(R.id.layoutTempo);
        allDescrizione = new ArrayList<View>();
        allIngredienti = new ArrayList<View>();
        EditText persone = (EditText) findViewById(R.id.etNumeroPersone);
        EditText tempo = (EditText) findViewById(R.id.etTempoPreparazione);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                actualUser = null;
            } else {
                actualUser = extras.getString("actualUseridentifier");
            }
        } else {
            actualUser = (String) savedInstanceState.getSerializable("actualUseridentifier");
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        loadSpinnerCategoria();
        snackForInfoPhoto();
        checkPermissionAndPhoto();
        changeToolbatTitle();
        checkEmptyMainEditText(persone);
        checkEmptyMainEditText(tempo);
        checkEmptyMainEditText(etTitolo);
        addIngrediente();
        addPassaggio();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert_ricetta, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                this.onBackPressed();
                this.finish();
                return true;
            }
            case R.id.publishRicetta: {
                pubblicaRicetta();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }

        }
    }

    private void loadSpinnerCategoria() {
        ArrayAdapter<String> items = new ArrayAdapter<String>(InsertRicettaActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categoria));
        items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(items);
    }


    private void snackForInfoPhoto() {
        fab_foto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(fab_foto, getResources().getString(R.string.insert_foto));
                return false;
            }
        });
    }

    private void checkPermissionAndPhoto() {
        fab_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissions.add(Manifest.permission.CAMERA);
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.INTERNET);
                permissionsToRequest = findUnaskedPermissions(permissions);
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
                } else {
                    startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PICK_IMAGE) {
            Bitmap bitmap = null;
            if (resultCode == RESULT_OK) {
                if (getPickImageResultUri(intent) != null) {
                    Uri picUri = getPickImageResultUri(intent);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmap = (Bitmap) intent.getExtras().get("data");
                }
            }

            if (bitmap != null) {
                Glide.with(InsertRicettaActivity.this).load(bitmap).centerCrop().into(img_piatto);
                //img_piatto.setImageBitmap(bitmap);
            }

        }
    }

    private Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    private Intent getPickImageChooserIntent() {
        Uri outputFileUri = getCaptureImageOutputUri();
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);
        Intent chooserIntent = Intent.createChooser(mainIntent, getString(R.string.selsorgente));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));
        return chooserIntent;
    }

    public Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "propic.png"));
        }
        return outputFileUri;
    }

    private ArrayList findUnaskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!(checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED)) {
                result.add(perm);
            }
        }

        return result;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (String perm : permissionsToRequest) {
                if (!(checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED)) {
                    permissionsRejected.add(perm);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    Utils.errorDialog(InsertRicettaActivity.this, R.string.error_not_all_permissions, R.string.error_ok);
                }
            } else {
                startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE);
            }
        }
    }

    private void changeToolbatTitle() {
        etTitolo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etTitolo.getText().toString().length() == 0 || (etTitolo.getText().toString().contains(" ") && (etTitolo.getText().toString().startsWith(" ") && etTitolo.getText().toString().endsWith(" ")))) {
                    collapsingToolbarLayout.setTitle(getResources().getString(R.string.title_activity_insert_ricetta));
                } else {
                    collapsingToolbarLayout.setTitle(etTitolo.getText().toString());

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void addIngrediente() {
        addIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.add_ingrediente_layout, null);
                allIngredienti.add(addView);
                final Spinner spinner = (Spinner) addView.findViewById(R.id.spinnerUnitaMisura);
                final TextInputEditText etQuantita = (TextInputEditText) addView.findViewById(R.id.etQuantita);
                final TextInputLayout t = (TextInputLayout) addView.findViewById(R.id.layout_quantita);
                checkEmptyEditText(addView, R.id.etNomeIngrediente);
                loadSpinnerUnitaMisura(addView, spinner);
                checkEmptyQuantitaEditText(addView, R.id.etQuantita, spinner);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (spinner.getSelectedItem().toString().equals("q.b.")) {
                            etQuantita.setEnabled(false);
                            t.setVisibility(View.GONE);
                        } else {
                            etQuantita.setEnabled(true);
                            t.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                FloatingActionButton buttonRemove = (FloatingActionButton) addView.findViewById(R.id.btnRemovePassaggio);
                buttonRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allIngredienti.remove(addView);
                        ((LinearLayout) addView.getParent()).removeView(addView);
                    }
                });
                ingredientiLayout.addView(addView);
            }
        });
    }

    private void loadSpinnerUnitaMisura(View addView, Spinner spUnitMisura) {
        ArrayAdapter<String> items = new ArrayAdapter<String>(InsertRicettaActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.unita_misura));
        items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnitMisura.setAdapter(items);
    }

    private void addPassaggio() {
        addPassaggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.add_passaggio_layout, null);
                allDescrizione.add(addView);
                checkEmptyEditText(addView, R.id.etDescrizione);
                FloatingActionButton buttonRemove = (FloatingActionButton) addView.findViewById(R.id.btnRemovePassaggio);
                buttonRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allDescrizione.remove(addView);
                        ((LinearLayout) addView.getParent()).removeView(addView);
                    }
                });
                passaggiLayout.addView(addView);
            }
        });
    }

    private void checkEmptyMainEditText(final EditText e) {

        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (e.getText().toString().length() == 0 || e.getText().toString().trim().equals("")) {
                        Animation shake = AnimationUtils.loadAnimation(InsertRicettaActivity.this, R.anim.shake);
                        v.setAnimation(shake);
                        e.setError(getString(R.string.error_empty_thing));
                    }
                }
            }
        });
    }


    private void checkEmptyEditText(View v, int id_et) {
        final EditText e = (EditText) v.findViewById(id_et);
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (e.getText().toString().length() == 0 || e.getText().toString().trim().equals("")) {
                        Animation shake = AnimationUtils.loadAnimation(InsertRicettaActivity.this, R.anim.shake);
                        v.setAnimation(shake);
                        e.setError(getString(R.string.error_empty_thing));
                    }
                }
            }
        });
    }

    private void checkEmptyQuantitaEditText(View v, int id_et, final Spinner spinner) {

        final EditText e = (EditText) v.findViewById(id_et);
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (spinner.getSelectedItem().toString().equals("q.b.")) {
                    e.setText("");
                } else {
                    if (!hasFocus) {
                        if (e.getText().toString().length() == 0 || e.getText().toString().trim().equals("")) {
                            Animation shake = AnimationUtils.loadAnimation(InsertRicettaActivity.this, R.anim.shake);
                            v.setAnimation(shake);
                            e.setError(getString(R.string.error_empty_thing));
                        }
                    }
                }
            }
        });
    }

    private void pubblicaRicetta() {
        if (etTitolo.getText().toString().trim().equals("")) {
            Utils.errorDialog(InsertRicettaActivity.this, R.string.error_no_titolo, R.string.error_ok);
        } else if (tempoPreparazione.getText().toString().equals("") || tempoPreparazione.getText().toString().equals("0")) {
            Utils.errorDialog(InsertRicettaActivity.this, R.string.error_no_tempo, R.string.error_ok);
        } else if (numeroPersone.getText().toString().equals("") || numeroPersone.getText().toString().equals("0")) {
            Utils.errorDialog(InsertRicettaActivity.this, R.string.error_no_persone, R.string.error_ok);
        } else {
            List<Map> ingredienti = getInfoIngredienti();
            List<String> passaggi = getInfoPassaggi();
            if (ingredienti != null && passaggi != null) {
                for (int i = 0; i < passaggi.size(); i++) {
                    if (passaggi.get(i).toString().trim().equals("")) {
                        passaggi.remove(passaggi.get(i));
                    }
                }
                for (int i = 0; i < ingredienti.size(); i++) {
                    if (ingredienti.get(i).get("Nome").toString().trim().equals("") && ingredienti.get(i).get("Quantità").toString().trim().equals("")) {
                        ingredienti.remove(ingredienti.get(i));
                    }
                }
                if (passaggi.size() == 0 || ingredienti.size() == 0) {
                    Utils.errorDialog(InsertRicettaActivity.this, R.string.error_no_passaggi_ingredienti, R.string.error_ok);
                } else {
                    Map<String, Object> ricettaToPush = new HashMap<>();
                    ricettaToPush.put("Autore", actualUser);
                    ricettaToPush.put("Titolo", etTitolo.getText().toString().trim());
                    ricettaToPush.put("Tempo di preparazione", tempoPreparazione.getText().toString());
                    ricettaToPush.put("Numero persone", numeroPersone.getText().toString());
                    ricettaToPush.put("Passaggi", getInfoPassaggi());
                    ricettaToPush.put("Ingredienti", getInfoIngredienti());
                    publishToFirebase(ricettaToPush);
                }
            } else {
                Utils.errorDialog(InsertRicettaActivity.this, R.string.error_no_passaggi_ingredienti, R.string.error_ok);
            }
        }
    }

    private List<Map> getInfoIngredienti() {
        List<Map> ingredienti = new ArrayList<Map>();
        for (int i = 0; i < allIngredienti.size(); i++) {
            Map<String, String> mappaIngrediente = new HashMap<>();
            try {
                mappaIngrediente.put("Nome", ((TextInputEditText) ((FrameLayout) ((TextInputLayout) ((LinearLayout) ((LinearLayout) ((RelativeLayout) allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getChildAt(0)).getText().toString().trim());
                mappaIngrediente.put("Quantità", ((TextInputEditText) ((FrameLayout) ((TextInputLayout) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((RelativeLayout) allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getChildAt(0)).getText().toString().trim());
                mappaIngrediente.put("Unità misura", (((Spinner) ((LinearLayout) ((LinearLayout) ((LinearLayout) ((RelativeLayout) allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(1)).getChildAt(1)).getSelectedItem().toString().trim()));
            } catch (Exception e) {
                return null;
            }


            ingredienti.add(mappaIngrediente);
        }
        return ingredienti;
    }

    private List<String> getInfoPassaggi() {
        List<String> mappaDescrizione = new ArrayList<String>();
        for (int i = 0; i < allDescrizione.size(); i++) {

            try {
                mappaDescrizione.add(((EditText) ((FrameLayout) ((TextInputLayout) ((LinearLayout) ((RelativeLayout) allDescrizione.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getText().toString().trim());
            } catch (Exception e) {
                return null;
            }
        }
        return mappaDescrizione;
    }

    private void publishToFirebase(Map ricetta) {
        img_piatto.setDrawingCacheEnabled(true);
        img_piatto.buildDrawingCache();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference imageRef = storageRef.child(ricetta.get("Titolo") + "_ricetta.jpg");
        Bitmap bitmap = ((BitmapDrawable) img_piatto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imgData = baos.toByteArray();
        new Ricetta.publishRecipe(InsertRicettaActivity.this, ricetta, imgData).execute();
    }
}
