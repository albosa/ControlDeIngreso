package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.Usuario;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class DetallesUsuario extends AppCompatActivity {

    private ImageView visitanteIV;
    @NotEmpty
    private EditText usernameET;
    @NotEmpty
    private EditText emailET;
    @NotEmpty
    private EditText fullnameET;
    @NotEmpty
    private EditText occupationET;
    @NotEmpty
    private EditText phoneET;
    @NotEmpty
    private EditText addressET;
    @Select
    private EditText rolET;
    private Toolbar toolbar;

    private Usuario usuarioRecibido;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_usuario);

        usuarioRecibido = (Usuario) getIntent().getSerializableExtra("usuario");

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        setTitle("Nuevo usuario");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;


        visitanteIV = findViewById(R.id.visitanteIV);
        usernameET = findViewById(R.id.username);
        emailET = findViewById(R.id.email);
        fullnameET = findViewById(R.id.fullname);
        occupationET = findViewById(R.id.occupation);
        phoneET = findViewById(R.id.phone);
        addressET = findViewById(R.id.address);
        rolET = findViewById(R.id.rol);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", authorization)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(client))
                .build();
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + usuarioRecibido.getPic()).resize(width, width).into(visitanteIV);


        usernameET.setText(usuarioRecibido.getUsername());
        emailET.setText(usuarioRecibido.getEmail());
        fullnameET.setText(usuarioRecibido.getFullname());
        occupationET.setText(usuarioRecibido.getOccupation());
        phoneET.setText(usuarioRecibido.getPhone());
        addressET.setText(usuarioRecibido.getAddress());
        rolET.setText(usuarioRecibido.getRol().getDescripcion());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_eu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intentS = new Intent(DetallesUsuario.this, LoginActivity.class);
                startActivity(intentS);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
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
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }
}
