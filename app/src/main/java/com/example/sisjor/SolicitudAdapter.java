package com.example.sisjor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;


import java.util.List;

public class SolicitudAdapter extends ArrayAdapter<Solicitud> {
    private final Context context;
    private final List<Solicitud> solicitudList;
    private final String userId, empresa;
    private int codigoAgregar;


    public SolicitudAdapter(@NonNull Context context, List<Solicitud> solicitudList, String userId, String empresa, int codigoAgregar) {
        super(context, R.layout.list_solicitud_layout, solicitudList);
        this.context = context;
        this.solicitudList = solicitudList;
        this.userId = userId;
        this.empresa = empresa;
        this.codigoAgregar = codigoAgregar;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.list_solicitud_layout, parent, false);

        TextView textId = rowView.findViewById(R.id.textId);
        TextView textRecinto = rowView.findViewById(R.id.textRecinto);
        TextView textUbicacion = rowView.findViewById(R.id.textUbicacion);
        TextView textEstado = rowView.findViewById(R.id.textEstado);
        Button btnDetalles = rowView.findViewById(R.id.btnDetalles);

        Solicitud solicitud = solicitudList.get(position);
        textId.setText(solicitud.getId());
        textRecinto.setText("R: " + solicitud.getRecinto());
        textUbicacion.setText("U: " + solicitud.getUbicacion());
        textEstado.setText(solicitud.getEstado());


        btnDetalles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Detalles.class);
                intent.putExtra("id", solicitud.getId());
                intent.putExtra("userId", userId);
                intent.putExtra("empresa", empresa);
                //context.startActivity(intent);
                ((Activity) context).startActivityForResult(intent,codigoAgregar);
                //showErrorDialog("User=" + userId + "\nID=" + solicitud.getId());
            }


        });
        return rowView;
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Mensaje: ");
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


}
