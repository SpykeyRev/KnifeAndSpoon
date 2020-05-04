package com.digiolaba.knifeandspoon.View;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Ricetta;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;

import com.digiolaba.knifeandspoon.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private Boolean textOK=false;
    private LinearLayout ingredientiLayout;
    private Button addIngrediente;
    private Button addPassaggio;
    private LinearLayout passaggiLayout;
    private List<View>allDescrizione,allIngredienti;
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
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.toolbar_layout_ins);
        fab_foto = (FloatingActionButton) findViewById(R.id.fab_ins_foto);
        img_piatto=(ImageView)findViewById(R.id.img_piatto);
        etTitolo=(EditText)findViewById(R.id.etTitolo);
        numeroPersone=(EditText)findViewById(R.id.etNumeroPersone);
        tempoPreparazione=(EditText)findViewById(R.id.etTempoPreparazione);
        spCategoria=(Spinner)findViewById(R.id.spinnerCategoria);
        ingredientiLayout=(LinearLayout)findViewById(R.id.layoutIngredienti);
        addIngrediente=(Button)findViewById(R.id.addIngrediente);
        addPassaggio=(Button)findViewById(R.id.addPassaggio);
        passaggiLayout=(LinearLayout)findViewById(R.id.listPassaggi);
        allDescrizione=new ArrayList<View>();
        allIngredienti=new ArrayList<View>();
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                actualUser= null;
            }
            else
            {
                actualUser= extras.getString("actualUseridentifier");
            }
        }
        else
        {
            actualUser= (String) savedInstanceState.getSerializable("actualUseridentifier");
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        loadSpinnerCategoria();
        snackForInfoPhoto();
        checkPermissionAndPhoto();
        changeToolbatTitle();
        notifyUserifTitoloNotCorrect();
        addIngrediente();
        addPassaggio();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert_ricetta,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                /*Intent intent=new Intent(InsertRicettaActivity.this,MainActivity.class);
                startActivity(intent);*/
                this.onBackPressed();
                return true;
            }
            case R.id.publishRicetta:
            {
                pubblicaRicetta();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }

        }
    }

    private void loadSpinnerCategoria()
    {
        ArrayAdapter<String>items=new ArrayAdapter<String>(InsertRicettaActivity.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.categoria));
        items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(items);
    }


    private void snackForInfoPhoto()
    {
        fab_foto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.showSnackbar(fab_foto,getResources().getString(R.string.insert_foto));
                return false;
            }
        });
    }

    private void checkPermissionAndPhoto()
    {
        fab_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissions.add(Manifest.permission.CAMERA);
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.INTERNET);
                permissionsToRequest = findUnaskedPermissions(permissions);
                if(permissionsToRequest.size() > 0)
                {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
                }
                else
                    {
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

            if(bitmap!=null)
            {
                img_piatto.setImageBitmap(bitmap);
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
        for(ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if(outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for(ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }
        Intent mainIntent = allIntents.get(allIntents.size()-1);
        for(Intent intent : allIntents) {
            if(intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
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
        if(getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "propic.png"));
        }
        return outputFileUri;
    }

    private ArrayList findUnaskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for(String perm : wanted) {
            if(!(checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED)) {
                result.add(perm);
            }
        }

        return result;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == ALL_PERMISSIONS_RESULT) {
            for(String perm: permissionsToRequest) {
                if(!(checkSelfPermission(perm)==PackageManager.PERMISSION_GRANTED)) {
                    permissionsRejected.add(perm);
                }
            }
            if(permissionsRejected.size() > 0) {
                if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    Utils.errorDialog(InsertRicettaActivity.this,R.string.error_not_all_permissions,R.string.error_ok);
                }
            }
            else {
                startActivityForResult(getPickImageChooserIntent(), PICK_IMAGE);
            }
        }
    }

    private void changeToolbatTitle()
    {
        etTitolo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(etTitolo.getText().toString().length()==0||(etTitolo.getText().toString().contains(" ")&&(etTitolo.getText().toString().startsWith(" ")&&etTitolo.getText().toString().endsWith(" "))))
                {
                    collapsingToolbarLayout.setTitle(getResources().getString(R.string.title_activity_insert_ricetta));
                    textOK=false;
                }
                else
                {
                    collapsingToolbarLayout.setTitle(etTitolo.getText().toString());
                    textOK=true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    void notifyUserifTitoloNotCorrect()
    {
        etTitolo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(!textOK)
                    {
                        Animation shake = AnimationUtils.loadAnimation(InsertRicettaActivity.this, R.anim.shake);
                        etTitolo.startAnimation(shake);
                        etTitolo.setHintTextColor(ContextCompat.getColor(InsertRicettaActivity.this,R.color.colorPrimary));
                    }
                }
            }
        });
    }

    private void addIngrediente()
    {
        addIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.add_ingrediente_layout, null);
                allIngredienti.add(addView);
                final Spinner spinner=(Spinner)addView.findViewById(R.id.spinnerUnitaMisura);
                final TextInputEditText etQuantita=(TextInputEditText)addView.findViewById(R.id.etQuantita);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(spinner.getSelectedItem().toString().equals("q.b.")){
                            Log.i("DII",spinner.getSelectedItem().toString());
                            etQuantita.setEnabled(false);
                        }else{
                            etQuantita.setEnabled(true);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                FloatingActionButton buttonRemove = (FloatingActionButton)addView.findViewById(R.id.btnRemovePassaggio);
                loadSpinnerUnitaMisura(addView);
                buttonRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allIngredienti.remove(addView);
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }
                });
                ingredientiLayout.addView(addView);
            }
        });
    }

    private void loadSpinnerUnitaMisura(View addView)
    {
        Spinner spUnitMisura = (Spinner) addView.findViewById(R.id.spinnerUnitaMisura);
        ArrayAdapter<String>items=new ArrayAdapter<String>(InsertRicettaActivity.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.unita_misura));
        items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnitMisura.setAdapter(items);
    }

    private void addPassaggio()
    {
        addPassaggio.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View addView = layoutInflater.inflate(R.layout.add_passaggio_layout, null);
            allDescrizione.add(addView);
            FloatingActionButton buttonRemove = (FloatingActionButton)addView.findViewById(R.id.btnRemovePassaggio);
            buttonRemove.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    allDescrizione.remove(addView);
                    ((LinearLayout)addView.getParent()).removeView(addView);
                }
            });
            passaggiLayout.addView(addView);
        }});

    }


    private void pubblicaRicetta()
    {
        Map<String, Object> ricettaToPush = new HashMap<>();
        ricettaToPush.put("Autore",actualUser);
        ricettaToPush.put("Titolo",etTitolo.getText().toString());
        ricettaToPush.put("Tempo di preparazione",tempoPreparazione.getText().toString());
        ricettaToPush.put("Numero persone",numeroPersone.getText().toString());
        ricettaToPush.put("Passaggi",getInfoPassaggi());
        ricettaToPush.put("Ingredienti",getInfoIngredienti());
        publishToFirebase(ricettaToPush);
    }

    private List<Map> getInfoIngredienti()
    {
        List<Map> ingredienti=new ArrayList<Map>();
        for(int i=0;i<allIngredienti.size();i++)
        {
            Map<String, String> mappaIngrediente = new HashMap<>();
            mappaIngrediente.put("Nome",((TextInputEditText)((FrameLayout)((TextInputLayout)((LinearLayout)((LinearLayout)((RelativeLayout)allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getChildAt(0)).getText().toString());
            mappaIngrediente.put("Quantità",((EditText)((FrameLayout)((TextInputLayout)((LinearLayout)((LinearLayout)((LinearLayout)((RelativeLayout)allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getChildAt(0)).getText().toString());
            mappaIngrediente.put("Unità misura",(((Spinner)((LinearLayout)((LinearLayout)((LinearLayout)((RelativeLayout)allIngredienti.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(1)).getChildAt(1)).getSelectedItem().toString()));
            ingredienti.add(mappaIngrediente);
        }
        return ingredienti;
    }

    private List<String> getInfoPassaggi()
    {
        List<String> mappaDescrizione = new ArrayList<String>();
        for(int i=0;i<allDescrizione.size();i++)
        {
            mappaDescrizione.add(((EditText)((FrameLayout)((TextInputLayout)((LinearLayout)((RelativeLayout) allDescrizione.get(i)).getChildAt(0)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getText().toString());
        }
        return mappaDescrizione;
    }

    private void publishToFirebase(Map ricetta)
    {
        img_piatto.setDrawingCacheEnabled(true);
        img_piatto.buildDrawingCache();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference imageRef = storageRef.child(ricetta.get("Titolo")+".jpg");
        Bitmap bitmap = ((BitmapDrawable) img_piatto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imgData = baos.toByteArray();
        new Ricetta.publishRecipe(this,ricetta,imgData).execute();
    }
}
