package com.example.sisjor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.versionedparcelable.VersionedParcel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Detalles extends AppCompatActivity {
    private String ip = "https://puntocombolivia.com/SISJOR/";
    private ProgressBar progressBar;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles);

        FloatingActionButton procesar = findViewById(R.id.floatBtnProcesar);
        Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.baseline_check_24);
        int nuevoAncho = 128;
        int nuevoAlto = 128;

        if (icon1 != null) {
            icon1.setBounds(0, 0, nuevoAncho, nuevoAlto);
        }
        assert icon1 != null;
        icon1.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        procesar.setImageDrawable(icon1);

        progressBar = findViewById(R.id.progressBarLoad);

        CardView cardView1 = findViewById(R.id.cardView1);
        cardView1.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        CardView cardView2 = findViewById(R.id.cardView2);
        cardView2.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        showLoadingIndicator();

        Intent intent = getIntent();
        if (intent != null) {
            String userId = intent.getStringExtra("userId");
            String solicitudId = intent.getStringExtra("id");
            String empresa = intent.getStringExtra("empresa");

            TextView txtUserId = findViewById(R.id.txtUserIdDetalles);
            txtUserId.setText(userId);
            @SuppressLint("CutPasteId") TextView txtSolicitudId = findViewById(R.id.txtIdSolicitudDetalles);
            txtSolicitudId.setText(solicitudId);
            TextView txtUserEmpresa = findViewById(R.id.txtEmpresaDetalle);
            txtUserEmpresa.setText(empresa);

        }else{
            showErrorDialog("No hay datos");
        }

        @SuppressLint("CutPasteId") TextView txtSolicitud = findViewById(R.id.txtIdSolicitudDetalles);
        String idSolicitud = txtSolicitud.getText().toString();

        new DetalleSolicitudTask().execute(idSolicitud);

        TextView txtOperador = findViewById(R.id.txtOperadorDetalle);
        TextView txtSolicitante = findViewById(R.id.txtSolicitanteDetalle);
        TextView txtUsuario = findViewById(R.id.txtUsuarioDetalle);
        @SuppressLint("CutPasteId") FloatingActionButton floatBtnProcesar = findViewById(R.id.floatBtnProcesar);
        @SuppressLint("CutPasteId") TextView empresa = findViewById(R.id.txtEmpresaDetalle);
        String NEmpresa = empresa.getText().toString();
        TextView txtEstado = findViewById(R.id.txtEstadoDetalle);
        String estado = txtEstado.getText().toString();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if ("PROCESADO".equals(estado) || "ALBO".equals(NEmpresa)){
                    floatBtnProcesar.setVisibility(View.GONE);
                    txtUsuario.setText("Supervisor:");
                    txtOperador.setVisibility(View.VISIBLE);
                    txtSolicitante.setVisibility(View.GONE);
                }else if ("PUNTOCOM".equals(NEmpresa)) {
                    txtUsuario.setText("Concesonario:");
                    txtOperador.setVisibility(View.GONE);
                    txtSolicitante.setVisibility(View.VISIBLE);
                    floatBtnProcesar.setVisibility(View.VISIBLE);
                }
            }
        },500);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoadingIndicator(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator(){
        progressBar.setVisibility(View.GONE);
    }

    private void showErrorDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private class DetalleSolicitudTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String idSolicitud = params[0];
            String url1 = ip + "apk/detalleSolicitud.php";
            String apiUrl = url1 + "?id=" + idSolicitud;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error de conexión al servidor";
            }

            return result.toString();
        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String response) {
            hideLoadingIndicator();
            try {
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("message")){
                    String mensaje = jsonResponse.getString("message");
                    showErrorDialog(mensaje);
                }else {
                    TextView txtRecinto = findViewById(R.id.txtRecintoNameDetalle);
                    txtRecinto.setText(jsonResponse.getString("nombre_recinto"));
                    TextView txtUbicacion = findViewById(R.id.txtUbicacionDetalle);
                    txtUbicacion.setText(jsonResponse.getString("ubicacion"));

                    SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    SimpleDateFormat formatoDeseado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    TextView txtFechaSolicitud = findViewById(R.id.txtFechaSolicitudDetalle);
                    String fechaSolicitud = jsonResponse.getString("fecha_sol");
                    TextView txtFecha = findViewById(R.id.txtFechaDetalle);
                    String fechaReq = jsonResponse.getString("fecha_req");
                    TextView txtHora = findViewById(R.id.txtHoraDetalle);
                    txtHora.setText(jsonResponse.getString("hora_req"));
                    try {
                        Date fecha = formatoOriginal.parse(fechaSolicitud);
                        Date fechaReqFormat = formatoOriginal.parse(fechaReq);
                        String fechaFormat = formatoDeseado.format(fecha);
                        txtFechaSolicitud.setText(fechaFormat);
                        txtFecha.setText(formatoDeseado.format(fechaReqFormat).toString());
                    } catch (ParseException e) {
                       e.printStackTrace();
                    }


                    TextView txtAlmacen = findViewById(R.id.txtAlmacenDetalle);
                    txtAlmacen.setText(jsonResponse.getString("almacen"));

                    TextView txtCantidad = findViewById(R.id.txtCantidadDetalle);
                    txtCantidad.setText(jsonResponse.getString("cantidad"));
                    TextView txtObservacion = findViewById(R.id.txtObservacionDetalle);
                    String observacion = jsonResponse.getString("observaciones");

                    if ("".equals(observacion)){
                        txtObservacion.setText("NINGUNA");
                    }else {
                        txtObservacion.setText(observacion);
                    }

                    FloatingActionButton floatbtnProcesar = findViewById(R.id.floatBtnProcesar);
                    TextView txtEstado = findViewById(R.id.txtEstadoDetalle);
                    String estado = jsonResponse.getString("status");
                    if ("PENDIENTE".equals(estado)){
                        txtEstado.setText("PENDIENTE");
                        txtEstado.setTextColor(Color.RED);
                        floatbtnProcesar.setVisibility(View.VISIBLE);
                    }else if ("PROCESADO".equals(estado)){
                        txtEstado.setText("PROCESADO");
                        txtEstado.setTextColor(Color.GREEN);
                        floatbtnProcesar.setVisibility(View.GONE);
                    }

                    TextView txtOperador = findViewById(R.id.txtOperadorDetalle);
                    txtOperador.setText(jsonResponse.getString("operador"));
                    TextView txtUsuario = findViewById(R.id.txtSolicitanteDetalle);
                    String nombre = jsonResponse.getString("nombre");
                    String apellido = jsonResponse.getString("apellido");
                    txtUsuario.setText(nombre + " " + apellido);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error en el formato de respuesta" + e);
            }
        }
    }

    private class procesarSolTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String userid = params[1];
            String url1 = ip + "apk/procesarSolicitud.php";
            String apiUrl = url1 + "?id=" + id + "&userId=" + userid;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error de conexión al servidor";
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                String estado = jsonResponse.getString("status");
                String mensaje = jsonResponse.getString("message");
                if ("success".equals(estado)){
                    showSuccessDialog(mensaje);
                }else {
                    showErrorDialog(estado);
                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void procesarSolicitud(View view) throws ParseException {
        TextView txtIdSolicitud = findViewById(R.id.txtIdSolicitudDetalles);
        String idSolicitud = txtIdSolicitud.getText().toString();
        TextView txtUserId = findViewById(R.id.txtUserIdDetalles);
        String userId = txtUserId.getText().toString();
        TextView empresa = findViewById(R.id.txtEmpresaDetalle);
        String NEmpresa = empresa.getText().toString();
        TextView txtEstado = findViewById(R.id.txtEstadoDetalle);
        String estado = txtEstado.getText().toString();

        if ("PUNTOCOM".equals(NEmpresa) && "PENDIENTE".equals(estado)){
            new procesarSolTask().execute(idSolicitud, userId);
        }
    }

    private void showSuccessDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("actualizarEstado", true);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    }
                });
        builder.create().show();
    }
}