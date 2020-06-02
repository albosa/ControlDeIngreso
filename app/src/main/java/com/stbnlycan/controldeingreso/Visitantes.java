package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Visitantes extends AppCompatActivity implements VisitantesAdapter.OnVisitanteClickListener {

    private ArrayList<Visitante> visitantes;
    private VisitantesAdapter visitantesAdapter;
    private Toolbar toolbar;
    private final static int REQUEST_CODE_NV = 1;
    private final static int REQUEST_CODE_EV = 2;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitantes);

        setTitle("Visitantes");

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //listaVisitantes = findViewById(R.id.visitantes);

        visitantes = new ArrayList<>();

        /*adapter = new VisitanteListAdapter(this, R.layout.adapter_view_layout, visitantes);
        adapter.setOnItemClickListener(Visitantes.this);
        listaVisitantes.setAdapter(adapter);*/


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        visitantesAdapter = new VisitantesAdapter(visitantes);
        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(visitantesAdapter);


        //getDataVisitante();
        fetchVisitantes();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nuevo_visitante:
                //Log.d("msg", "nuevo visitante");
                Intent intent = new Intent(Visitantes.this, NuevoVisitanteActivity.class);
                intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NV);
                //prueba();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NV) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Visitante visitanteResult = (Visitante) b.getSerializable("visitanteResult");
                    visitantes.add(0, visitanteResult);
                    visitantesAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                }
            }
            else if (requestCode == REQUEST_CODE_EV) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Visitante visitanteResult = (Visitante) b.getSerializable("visitanteResult");
                    Log.d("msg3", ""+b.getInt("position", -1));
                    int position = b.getInt("position", -1);
                    visitantes.add(position, visitanteResult);
                    visitantesAdapter.notifyItemInserted(position);
                    recyclerView.scrollToPosition(position);
                }
            }
        }
    }

    private void fetchVisitantes() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesAPIs listaVisitantesAPIs = retrofit.create(ListaVisitantesAPIs.class);
        Call<List<Visitante>> call = listaVisitantesAPIs.listaVisitantes();
        call.enqueue(new Callback<List<Visitante>>() {
            @Override
            public void onResponse(Call <List<Visitante>> call, retrofit2.Response<List<Visitante>> response) {
                //recintos = response.body();

                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    visitantes.add(response.body().get(i));
                }
                visitantesAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call <List<Visitante>> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //visitantes.clear();


        //getDataVisitante();
        //Toast.makeText(this, "Hola", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                visitantesAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onEventoClick(Visitante visitante, int position) {
        Log.d("msg1",""+position);
        Intent intent = new Intent(Visitantes.this, EditarVisitanteActivity.class);
        intent.putExtra("visitante", visitante);
        intent.putExtra("position", position);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_EV);
    }
}
