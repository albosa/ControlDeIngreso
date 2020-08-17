package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stbnlycan.models.Visitante;

public class QRActivity extends AppCompatActivity {

    private Visitante visitanteRecibido;
    private ImageView imgVisitante;
    private Toolbar toolbar;
    private TextView tvNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r);

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");

        setTitle("QR asignado");

        tvNombre = findViewById(R.id.tvNombre);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imgVisitante = findViewById(R.id.visitanteIV);

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarQR?ci=" + visitanteRecibido.getVteCi()).centerCrop().resize(500, 500).into(imgVisitante);
        tvNombre.setText(visitanteRecibido.getVteNombre()+ " "+visitanteRecibido.getVteApellidos());
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
}