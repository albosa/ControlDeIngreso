package com.stbnlycan.controldeingreso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.stbnlycan.fragments.DFTknExpired;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EmpresaAPIs;
import com.stbnlycan.interfaces.ListaEmpresasAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.SubirImagenAPIs;
import com.stbnlycan.interfaces.TipoVisitanteAPIs;
import com.stbnlycan.interfaces.VisitanteAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditarVisitanteActivity extends AppCompatActivity implements Validator.ValidationListener{

    private ImageView visitanteIV;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private List<Recinto> areas;
    private ArrayList<Empresa> empresas;
    private ArrayList<TipoVisitante> tiposVisitante;

    private ArrayAdapter<Empresa> adapterEmpresa;
    private ArrayAdapter<TipoVisitante> adapterTipoVisitante;
    private Visitante visitante;

    @NotEmpty
    private EditText ciET;
    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText apellidosET;
    @NotEmpty
    private EditText telcelET;
    @NotEmpty
    @Email
    private EditText emailET;
    @Select
    private Spinner empresaS;
    @Select
    private Spinner tipoVisitanteS;

    private Validator validator;
    private Visitante visitanteRecibido;
    private int position;
    private Toolbar toolbar;
    private String imagenObtenida;
    private Button btnNE;
    private final static int REQUEST_CODE_NE = 1;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Uri uri;
    private Visitante visitanteResult;
    private boolean cambioFoto;
    private ProgressBar progressBar;
    private FloatingActionButton fabNF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_visitante);

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Editar visitante");
        visitanteIV = findViewById(R.id.visitanteIV);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        emailET = findViewById(R.id.email);
        empresaS = findViewById(R.id.empresa);
        tipoVisitanteS = findViewById(R.id.tipo_visitante);
        btnNE = findViewById(R.id.btnNE);
        progressBar = findViewById(R.id.progressBar);
        fabNF = findViewById(R.id.fabNF);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        cambioFoto = false;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressBar.setVisibility(View.VISIBLE);

        iniciarSpinnerEmpresa();
        iniciarSpinnerTipoVisitante();

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");
        position = getIntent().getIntExtra("position", -1);

        fetchDataEmpresa();
        fetchDataTipoVisitante();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        String url = getResources().getString(R.string.url_foto_visitantes) + visitanteRecibido.getVteImagen();
        GlideUrl glideUrl = new GlideUrl(url,
                new LazyHeaders.Builder()
                        .addHeader("Authorization", authorization)
                        .build());
        Glide.with(this)
                .load(glideUrl)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(visitanteIV);

        ciET.setText(visitanteRecibido.getVteCi());
        nombreET.setText(visitanteRecibido.getVteNombre());
        apellidosET.setText(visitanteRecibido.getVteApellidos());
        telcelET.setText(visitanteRecibido.getVteTelefono());
        emailET.setText(visitanteRecibido.getVteCorreo());

        fabNF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            uri = FileProvider.getUriForFile(getApplicationContext(),"com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4214",""+photoFile);
                            imagenObtenida = photoFile.toString();
                        }
                        else
                        {
                            uri = FileProvider.getUriForFile(getApplicationContext(),"com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4215",""+photoFile);
                            imagenObtenida = photoFile.toString();
                        }
                    }
                }
            }
        });

        btnNE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditarVisitanteActivity.this, NuevaEmpresa.class);
                startActivityForResult(intent, REQUEST_CODE_NE);
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        return image;
    }

    public void iniciarSpinnerEmpresa() {
        empresas = new ArrayList<>();
        Empresa empresa = new Empresa();
        empresa.setEmpCod("cod");
        //empresa.setEmpNombre("SELECCIONE UNA EMPRESA");
        empresa.setEmpNombre("CARGANDO...");
        empresa.setEmpObs("obs");
        empresas.add(empresa);
        adapterEmpresa = new ArrayAdapter<Empresa>(this, R.layout.style_spinner, empresas){
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textview = (TextView) view;
                if (position == 0) {
                    textview.setTextColor(Color.GRAY);
                } else {
                    textview.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterEmpresa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaS.setAdapter(adapterEmpresa);
        empresaS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Empresa empresa = (Empresa) parent.getSelectedItem();
                displayEmpresaData(empresa);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void iniciarSpinnerTipoVisitante() {
        tiposVisitante = new ArrayList<>();
        TipoVisitante tipoVisitante = new TipoVisitante();
        tipoVisitante.setTviCod("cod");
        //tipoVisitante.setTviNombre("SELECCIONE TIPO DE VISITANTE");
        tipoVisitante.setTviNombre("CARGANDO...");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");
        tiposVisitante.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, R.layout.style_spinner, tiposVisitante){
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textview = (TextView) view;
                if (position == 0) {
                    textview.setTextColor(Color.GRAY);
                } else {
                    textview.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterTipoVisitante.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoVisitanteS.setAdapter(adapterTipoVisitante);
        tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TipoVisitante tipoVisitante = (TipoVisitante) parent.getSelectedItem();
                displayTipoVisitanteData(tipoVisitante);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayEmpresaData(Empresa empresa) {
        String cod = empresa.getEmpCod();
        String nombre = empresa.getEmpNombre();
        String obs = empresa.getEmpObs();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + obs;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    private void displayTipoVisitanteData(TipoVisitante tipoVisitante) {
        String cod = tipoVisitante.getTviCod();
        String nombre = tipoVisitante.getTviNombre();
        String descripcion = tipoVisitante.getTviDescripcion();
        String estado = tipoVisitante.getHorEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            redimensionarImg();
            Gson gson = new Gson();
            String descripcion = gson.toJson(visitanteRecibido);
            visitanteIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_person_gray_96dp));
            progressBar.setVisibility(View.VISIBLE);
            subirImagen(descripcion);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NE) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Empresa empresaResult = (Empresa) b.getSerializable("empresaResult");
                    empresas.add(1, empresaResult);
                    empresaS.setSelection(1, true);
                }
            }
        }
    }

    public void redimensionarImg()
    {
        try
        {
            /*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName,".jpg",storageDir);
            currentPhotoPath = image.getAbsolutePath();*/


            // we'll start with the original picture already open to a file
            File imgFileOrig = new File(imagenObtenida); //change "getPic()" for whatever you need to open the image file.
            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());

            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();

            final int destWidth = 600;//or the width you need

            if(origWidth > destWidth)
            {
                // picture is wider than we want it, we calculate its target height
                int destHeight = origHeight/( origWidth / destWidth ) ;
                // we create an scaled bitmap so it reduces the image, not just trim it
                Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);

                if(origWidth > origHeight)
                {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    b2 = Bitmap.createBitmap(b2, 0, 0, b2.getWidth(), b2.getHeight(), matrix, true);
                }

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                b2.compress(Bitmap.CompressFormat.JPEG,100 , outStream);
                // we save the file, at least until we have made use of it
                //File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "test.jpg");
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imgFileOrig.getName());
                f.createNewFile();
                //write the bytes in file
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(outStream.toByteArray());
                // remember close de FileOutput
                fo.close();
            }
        }
        catch (Exception ex)
        {

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(cambioFoto)
                {
                    cambioFoto = false;
                    Intent intent = new Intent();
                    intent.putExtra("visitanteResult", visitanteResult);
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else
                {
                    finish();
                }
                return false;
            case R.id.action_editar_visitante:
                validator.validate();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ev, menu);

        if(rol.equals("USER") || rol.equals(""))
        {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).setVisible(false);
            //btnNF.setEnabled(false);
            fabNF.setEnabled(false);
            ciET.setFocusable(false);
            ciET.setFocusableInTouchMode(false);
            nombreET.setFocusable(false);
            nombreET.setFocusableInTouchMode(false);
            apellidosET.setFocusable(false);
            apellidosET.setFocusableInTouchMode(false);
            telcelET.setFocusable(false);
            telcelET.setFocusableInTouchMode(false);
            emailET.setFocusable(false);
            emailET.setFocusableInTouchMode(false);
            empresaS.setEnabled(false);
            btnNE.setEnabled(false);
            tipoVisitanteS.setEnabled(false);
        }
        return true;
    }

    private void cerrarSesion() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LogoutAPIs logoutAPIs = retrofit.create(LogoutAPIs.class);
        Call<Void> call = logoutAPIs.logout(pref.getString("access_token", null));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call <Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    private void fetchDataEmpresa() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasAPIs listaEmpresasAPIs = retrofit.create(ListaEmpresasAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasAPIs.listaEmpresas("0","50", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    int pos = -1;
                    for(int i = 0 ; i < response.body().getlEmpresa().size() ; i++)
                    {
                        empresas.add(response.body().getlEmpresa().get(i));
                        if(response.body().getlEmpresa().get(i).getEmpCod().equals(visitanteRecibido.getEmpresa().getEmpCod()))
                        {
                            pos = i+1;
                        }
                    }
                    empresas.get(0).setEmpNombre("SELECCIONE UNA EMPRESA");
                    adapterEmpresa.notifyDataSetChanged();
                    empresaS.setSelection(pos, true);
                }
            }
            @Override
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {

            }
        });
    }

    private void fetchDataTipoVisitante() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        TipoVisitanteAPIs tipoVisitanteAPIs = retrofit.create(TipoVisitanteAPIs.class);
        Call<List<TipoVisitante>> call = tipoVisitanteAPIs.listaTipoVisitante(authorization);
        call.enqueue(new Callback<List<TipoVisitante>>() {
            @Override
            public void onResponse(Call <List<TipoVisitante>> call, retrofit2.Response<List<TipoVisitante>> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    int pos = -1;
                    for(int i = 0 ; i < response.body().size() ; i++)
                    {
                        tiposVisitante.add(response.body().get(i));
                        if(response.body().get(i).getTviCod().equals(visitanteRecibido.getTipoVisitante().getTviCod()))
                        {
                            pos = i+1;
                        }
                    }
                    tiposVisitante.get(0).setTviNombre("SELECCIONE TIPO DE VISITANTE");
                    adapterTipoVisitante.notifyDataSetChanged();
                    tipoVisitanteS.setSelection(pos, true);
                }
            }
            @Override
            public void onFailure(Call <List<TipoVisitante>> call, Throwable t) {

            }
        });
    }

    private void editarVisitante() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        VisitanteAPIs visitanteAPIs = retrofit.create(VisitanteAPIs.class);
        Call<Visitante> call = visitanteAPIs.editarVisitante(visitante, authorization);
        call.enqueue(new Callback <Visitante> () {
            @Override
            public void onResponse(Call <Visitante> call, Response<Visitante> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    Visitante visitanteRecibido = response.body();
                    Toast.makeText(getApplicationContext(), "El visitante fué actualizado", Toast.LENGTH_LONG).show();
                    //visitante.setVteEstado("ACT");

                    Intent intent = new Intent();
                    intent.putExtra("visitanteResult", visitanteRecibido);
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {
                Log.d("msg132",""+t);
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        if(hasNullOrEmptyDrawable(visitanteIV))
        {
            Toast.makeText(this, "Debes añadir una fotografía", Toast.LENGTH_LONG).show();
        }
        else
        {
            visitante = new Visitante();
            visitante.setVteCi(ciET.getText().toString());
            visitante.setVteCorreo(emailET.getText().toString());
            visitante.setVteImagen(visitanteRecibido.getVteImagen());
            visitante.setVteNombre(nombreET.getText().toString().toUpperCase());
            visitante.setVteApellidos(apellidosET.getText().toString().toUpperCase());
            visitante.setVteTelefono(telcelET.getText().toString());
            visitante.setVteDireccion("");
            visitante.setVteEstado(visitanteRecibido.getVteEstado());
            visitante.setVteLlave(visitanteRecibido.getVteLlave());
            visitante.setVteFecha(visitanteRecibido.getVteFecha());

            TipoVisitante tipoVisitante = (TipoVisitante) tipoVisitanteS.getSelectedItem();
            Empresa empresa = (Empresa) empresaS.getSelectedItem();
            visitante.setTipoVisitante(tipoVisitante);
            visitante.setEmpresa(empresa);

            showLoadingwDialog();
            Gson gson = new Gson();
            String descripcion = gson.toJson(visitante);

            editarVisitante();
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages
            if (view instanceof EditText) {
                //((EditText) view).setError(message);
                ((EditText) view).setError("Este campo es requerido");
            }
            else if (view instanceof Spinner) {
                //((TextView) ((Spinner) view).getSelectedView()).setError(message);
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean hasNullOrEmptyDrawable(ImageView iv)
    {
        Drawable drawable = iv.getDrawable();
        BitmapDrawable bitmapDrawable = drawable instanceof BitmapDrawable ? (BitmapDrawable)drawable : null;

        return bitmapDrawable == null || bitmapDrawable.getBitmap() == null;
    }

    void showLoadingwDialog() {

        final LoadingFragment dialogFragment = new LoadingFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    private void subirImagen(String descripcion) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        SubirImagenAPIs subirImagenAPIs = retrofit.create(SubirImagenAPIs.class);
        File file = new File(imagenObtenida);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        Call<Visitante> call = subirImagenAPIs.subirImagen(part, description, authorization);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response <Visitante> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Se guardó la nueva imágen", Toast.LENGTH_LONG).show();
                    visitanteResult = response.body();

                    visitanteRecibido.setVteImagen(response.body().getVteImagen());
                    cambioFoto = true;

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    String url = getResources().getString(R.string.url_foto_visitantes) + response.body().getVteImagen();
                    GlideUrl glideUrl = new GlideUrl(url,
                            new LazyHeaders.Builder()
                                    .addHeader("Authorization", authorization)
                                    .build());
                    Glide.with(getApplication())
                            .load(glideUrl)
                            .centerCrop()
                            .apply(new RequestOptions().override(width, width))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    //holder.doiImagen.setVisibility(View.VISIBLE);
                                    //holder.progressBar.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(visitanteIV);
                /*Gson gson2 = new Gson();
                String descripcion2 = gson2.toJson(visitanteCB);
                Log.d("msgCB2", descripcion2);*/
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(cambioFoto)
        {
            cambioFoto = false;
            Intent intent = new Intent();
            intent.putExtra("visitanteResult", visitanteResult);
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void showTknExpDialog() {
        DFTknExpired dfTknExpired = new DFTknExpired();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dfTknExpired.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogTknExpLoading");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dfTknExpired.show(ft, "dialogTknExpLoading");
    }
}
