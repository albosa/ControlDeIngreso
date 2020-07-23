package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stbnlycan.adapters.RecintosAdapter;
import com.stbnlycan.interfaces.RecintosAPIs;
import com.stbnlycan.models.Recinto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements RecintosAdapter.OnEventoClickListener{

    private RecyclerView recyclerView;
    private RecintosAdapter adapter;
    private List<Recinto> recintos;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recintos = new ArrayList<>();

        /*recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new RecintosAdapter(recintos);
        adapter.setOnEventoClickListener(MainActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));



        fetchRecintos();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualizarRecintos();
            }
        });
    }

    private void fetchRecintos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RecintosAPIs recintosAPIs = retrofit.create(RecintosAPIs.class);
        Call<List<Recinto>> call = recintosAPIs.listaRecintos();
        call.enqueue(new Callback<List<Recinto>>() {
            @Override
            public void onResponse(Call <List<Recinto>> call, retrofit2.Response<List<Recinto>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    recintos.add(response.body().get(i));
                }
                adapter = new RecintosAdapter(recintos);
                adapter.setOnEventoClickListener(MainActivity.this);

                recyclerView.setAdapter(adapter);

                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call <List<Recinto>> call, Throwable t) {

            }
        });
    }

    private void actualizarRecintos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RecintosAPIs recintosAPIs = retrofit.create(RecintosAPIs.class);
        Call<List<Recinto>> call = recintosAPIs.listaRecintos();
        call.enqueue(new Callback<List<Recinto>>() {
            @Override
            public void onResponse(Call <List<Recinto>> call, retrofit2.Response<List<Recinto>> response) {
                //recintos = response.body();
                recintos.clear();
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    recintos.add(response.body().get(i));
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onFailure(Call <List<Recinto>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onEventoClick(Recinto recinto) {
        Log.d("Recinto",""+recinto.getRecNombrea());

        Intent intent = new Intent(MainActivity.this, RecintoActivity.class);
        //intent.putExtra("recCod",recinto.getRecCod());
        //intent.putExtra("recNombre",recinto.getRecNombre());
        intent.putExtra("recinto", recinto);
        startActivity(intent);
        //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
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
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}
