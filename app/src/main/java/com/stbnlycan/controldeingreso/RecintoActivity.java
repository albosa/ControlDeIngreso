package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.fragments.BusquedaCiDialogFragment;
import com.stbnlycan.interfaces.BuscarXCIAPIs;
import com.stbnlycan.interfaces.BuscarXQRAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaXCiAPIs;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Error;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class RecintoActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener, BusquedaCiDialogFragment.OnBusquedaCiListener {

    private Toolbar toolbar;
    private Recinto recintoRecibido;
    private BusquedaCiDialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recinto);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle(recintoRecibido.getRecNombre());

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Escanear QR", R.drawable.icono_scan_qr));
        cards.add(new Accion(1, "Buscar CI", R.drawable.icono_carnet));
        //cards.add(new Accion(2, "Visitas Con Salidas", R.drawable.ingreso2));
        //cards.add(new Accion(3, "Visitas Sin Salidas", R.drawable.salida2));
        cards.add(new Accion(2, "Visitas", R.drawable.icono_visita));
        cards.add(new Accion(3, "Visitantes", R.drawable.icono_visitantes));
        cards.add(new Accion(4, "Horarios", R.drawable.icono_horario));
        cards.add(new Accion(5, "Empresas", R.drawable.icono_empresa));



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecintoAdapter adapter = new RecintoAdapter(cards);
        adapter.setOnEventoClickListener(RecintoActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEventoDetailsClick(int position) {
        if(position == 0)
        {
            escaner();
        }
        else if(position == 1)
        {
            showCiDialog();
        }
        else if(position == 2)
        {
            iniciarAVisitas();
        }
        else if(position == 3)
        {
            Intent intent = new Intent(RecintoActivity.this, Visitantes.class);
            startActivity(intent);
        }
        else if(position == 4)
        {
            Intent intent = new Intent(RecintoActivity.this, Horarios.class);
            intent.putExtra("recinto", recintoRecibido);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 5)
        {
            iniciarAEmpresas();
        }
    }

    public void iniciarAVCSalida()
    {
        Intent intent = new Intent(RecintoActivity.this, VCSalidaActivity.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    public void iniciarAVSSalida()
    {
        Intent intent = new Intent(RecintoActivity.this, VSSalidaActivity.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    public void iniciarAVisitas()
    {
        Intent intent = new Intent(RecintoActivity.this, Visitas.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    public void iniciarAEmpresas()
    {
        Intent intent = new Intent(RecintoActivity.this, Empresas.class);
        startActivity(intent);
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
                //Toast.makeText(this,  "ID Participante "+result.getContents(), Toast.LENGTH_LONG).show();
                registrarSalida(result.getContents());
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showCiDialog() {
        dialogFragment = new BusquedaCiDialogFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        bundle.putSerializable("recinto", recintoRecibido);
        dialogFragment.setArguments(bundle);
        dialogFragment.setOnEventoClickListener(RecintoActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    @Override
    public void onBusquedaCiListener(Visitante visitante) {
        registrarSalidaXCi(visitante);
    }

    private void registrarSalida(final String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaAPIs registrarSalidaAPIs = retrofit.create(RegistrarSalidaAPIs.class);
        Call<JsonObject> call = registrarSalidaAPIs.registrarSalida(recintoRecibido.getRecCod(), llave);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha salido de " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();
                    buscarXQR(llave);
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    private void registrarSalidaXCi(final Visitante visitante) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaXCiAPIs registrarSalidaXCiAPIs = retrofit.create(RegistrarSalidaXCiAPIs.class);
        Call<JsonObject> call = registrarSalidaXCiAPIs.registrarSalidaXCi(recintoRecibido.getRecCod(), visitante.getVteCi());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                Log.d("msg542","hola "+visitante.getVteNombre());
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha salido de " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();
                    //buscarXCI(visitante.getVteCi());
                    iniciarRVActivity(visitante);
                    dialogFragment.dismiss();
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    private void buscarXQR(String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        BuscarXQRAPIs buscarXQRAPIs = retrofit.create(BuscarXQRAPIs.class);
        Call<Visitante> call = buscarXQRAPIs.buscarXQR(llave);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                if(visitanteRecibido.getVteCi() != null)
                {
                    Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_LONG).show();
                    iniciarRVActivity(visitanteRecibido);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No se encontró el visitante en la base de datos", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {

            }
        });
    }

    private void buscarXCI(String ci) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        BuscarXCIAPIs buscarXCIAPIs = retrofit.create(BuscarXCIAPIs.class);
        Call<List<Visitante>> call = buscarXCIAPIs.buscarXQR(ci);
        call.enqueue(new Callback<List<Visitante>>() {
            @Override
            public void onResponse(Call <List<Visitante>> call, retrofit2.Response<List<Visitante>> response) {
                List<Visitante> visitantesRecibidos = response.body();
                for(int i=0;i<visitantesRecibidos.size();i++)
                {
                    Log.d("msg164",""+visitantesRecibidos.get(i).getVteNombre());
                }

                /*Visitante visitanteRecibido = response.body();
                if(visitanteRecibido.getVteCi() != null)
                {
                    Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_LONG).show();
                    iniciarRVActivity(visitanteRecibido);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No se encontró el visitante en la base de datos", Toast.LENGTH_LONG).show();
                }*/
            }
            @Override
            public void onFailure(Call <List<Visitante>> call, Throwable t) {

            }
        });
    }

    public void iniciarRVActivity(Visitante visitanteRecibido)
    {
        Log.d("msg542","hola");
        Intent intent = new Intent(RecintoActivity.this, RegistraVisitaActivity.class);
        intent.putExtra("visitante", visitanteRecibido);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

}
