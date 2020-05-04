package com.digiolaba.knifeandspoon.View;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.digiolaba.knifeandspoon.Controller.Utils;
import com.digiolaba.knifeandspoon.Model.Utente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.digiolaba.knifeandspoon.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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
        ;
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
                Intent intent=new Intent(InsertRicettaActivity.this,MainActivity.class);
                startActivity(intent);
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
                    selectImage(InsertRicettaActivity.this);
                }
            }
        });
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Fai una foto", "Scegli dalla galleria","Annulla" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Scegli la foto per il piatto");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Fai una foto")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Scegli dalla galleria")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        img_piatto.setImageBitmap(selectedImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                img_piatto.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == ALL_PERMISSIONS_RESULT) {
            for(String perm: permissionsToRequest) {
                if(!(checkSelfPermission(perm)==PackageManager.PERMISSION_GRANTED)) {
                    permissionsRejected.add(perm);
                }
            }
            if(permissionsRejected.size() > 0) {
                if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                   Utils.errorDialog(this,R.string.error_not_all_permissions,R.string.error_ok);
                }
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
                Button buttonRemove = (Button)addView.findViewById(R.id.btnRemoveIngrediente);
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
            Button buttonRemove = (Button)addView.findViewById(R.id.btnRemovePassaggio);
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
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference imageRef = storageRef.child(etTitolo.getText()+".jpg");
        img_piatto.setDrawingCacheEnabled(true);
        img_piatto.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) img_piatto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        imageRef.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUrl = uri;
                        Map<String, Object> ricettaToPush = new HashMap<>();
                        ricettaToPush.put("Autore",actualUser);
                        ricettaToPush.put("Titolo",etTitolo.getText().toString());
                        ricettaToPush.put("Tempo di preparazione",tempoPreparazione.getText().toString());
                        ricettaToPush.put("Numero persone",numeroPersone.getText().toString());
                        ricettaToPush.put("Thumbnail",uri.toString());
                        ricettaToPush.put("Passaggi",getInfoPassaggi());
                        ricettaToPush.put("Ingredienti",getInfoIngredienti());
                        publishToFirebase(ricettaToPush);
                    }
                });
            }}).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress
                        = (100.0
                        * taskSnapshot.getBytesTransferred()
                        / taskSnapshot.getTotalByteCount());
                System.out.println(progress);
            }
        });
    }

    private List<Map> getInfoIngredienti()
    {
        List<Map> ingredienti=new ArrayList<Map>();
        for(int i=0;i<allIngredienti.size();i++)
        {
            Map<String, String> mappaIngrediente = new HashMap<>();
            mappaIngrediente.put("Nome",((EditText)((RelativeLayout)allIngredienti.get(i)).getChildAt(2)).getText().toString());
            mappaIngrediente.put("Quantità",((EditText)((RelativeLayout)allIngredienti.get(i)).getChildAt(3)).getText().toString());
            mappaIngrediente.put("Unità misura",(((Spinner)((RelativeLayout)allIngredienti.get(i)).getChildAt(4)).getSelectedItem().toString()));
            ingredienti.add(mappaIngrediente);
        }
        return ingredienti;
    }

    private List<String> getInfoPassaggi()
    {
        List<String> mappaDescrizione = new ArrayList<String>();
        for(int i=0;i<allDescrizione.size();i++)
        {
            mappaDescrizione.add(((EditText)((RelativeLayout)allDescrizione.get(i)).getChildAt(2)).getText().toString());
        }
        return mappaDescrizione;
    }

    private void publishToFirebase(Map ricetta)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add a new document with a generated ID
        db.collection("Ricette")
                .add(ricetta)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Utils.errorDialog(InsertRicettaActivity.this,R.string.ricetta_in_pubblicazione,R.string.error_ok);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.errorDialog(InsertRicettaActivity.this,R.string.ricetta_in_pubblicazione_error,R.string.error_ok);                    }
                });
    }
}
