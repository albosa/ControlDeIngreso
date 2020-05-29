package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.fragments.BusquedaCiDialogFragment;
import com.stbnlycan.interfaces.RegistrarHorarioAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaXCiAPIs;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Horario;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class RegistrarSalidasActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener, BusquedaCiDialogFragment.OnBusquedaCiListener{

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_salidas);

        setTitle("Registrar Salida");

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Escanear QR", R.drawable.icono_scan_qr));
        cards.add(new Accion(1, "Buscar CI", R.drawable.icono_carnet));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecintoAdapter adapter = new RecintoAdapter(cards);
        adapter.setOnEventoClickListener(RegistrarSalidasActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventoDetailsClick(int position) {
        Log.d("EventoDetails",""+position);
        if(position == 0)
        {
            escaner();
        }
        else if(position == 1)
        {
            showCiDialog();
        }
    }

    //Metodo para escanear
    public void escaner()
    {
        IntentIntegrator intent = new IntentIntegrator( this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);

        intent.setPrompt("Registrando Ingreso");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
    }

    public void showCiDialog() {

        final BusquedaCiDialogFragment dialogFragment = new BusquedaCiDialogFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        dialogFragment.setOnEventoClickListener(RegistrarSalidasActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() == null)
            {
                Toast.makeText(this,  "Cancelaste el escaneo de ingreso", Toast.LENGTH_LONG).show();
            }
            else
            {
                //Toast.makeText(this,  "ID Participante "+result.getContents().toString(), Toast.LENGTH_LONG).show();
                //dialogLoading.show();
                //ciExiste(session_key, sid, result.getContents().toString());
                //buscarXQR(result.getContents().toString());

                registrarSalida(getIntent().getStringExtra("recCod"), result.getContents().toString());
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registrarSalida(String recCod, String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaAPIs registrarSalidaAPIs = retrofit.create(RegistrarSalidaAPIs.class);
        Call<Visita> call = registrarSalidaAPIs.registrarSalida(recCod, llave);
        call.enqueue(new Callback<Visita>() {
            @Override
            public void onResponse(Call <Visita> call, retrofit2.Response<Visita> response) {
                Visita visitaRecibida = response.body();
                //Log.d("msg",""+horarioRecibido.getHorNombre());
                Toast.makeText(getApplicationContext(), "La salida fué registrada", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailure(Call <Visita> call, Throwable t) {

            }
        });
    }

    private void registrarSalidaXCi(String recCod, String ci) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaXCiAPIs registrarSalidaXCiAPIs = retrofit.create(RegistrarSalidaXCiAPIs.class);
        Call<Visita> call = registrarSalidaXCiAPIs.registrarSalidaXCi(recCod, ci);
        call.enqueue(new Callback<Visita>() {
            @Override
            public void onResponse(Call <Visita> call, retrofit2.Response<Visita> response) {
                Visita visitaRecibida = response.body();
                if(visitaRecibida.getVisCod() != null)
                {
                    Toast.makeText(getApplicationContext(), "La salida fué registrada", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No tiene registrado ningún ingreso o ya registró su salida.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                //Log.d("msg",""+horarioRecibido.getHorNombre());
                /*Toast.makeText(getApplicationContext(), "La salida fué registrada", Toast.LENGTH_SHORT).show();
                finish();*/
            }
            @Override
            public void onFailure(Call <Visita> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBusquedaCiListener(String ci) {
        Log.d("result", ""+ci);
        Toast.makeText(getApplicationContext(),  "ID Participante "+ci, Toast.LENGTH_LONG).show();
        registrarSalidaXCi(getIntent().getStringExtra("recCod"), ci);
    }

}
