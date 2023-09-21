package com.example.sisjor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Agregar extends AppCompatActivity {

    private ProgressBar progressBar;
    private Spinner spinnerUbicacion;
    private MultiAutoCompleteTextView multiAlmacen;
    private EditText editText, editHora;
    private Calendar calendar;
    private String ip = "https://puntocombolivia.com/SISJOR/";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        FloatingActionButton fab = findViewById(R.id.floatBtnGuardar);
        Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.baseline_save_24);
        int nuevoAncho = 64;
        int nuevoAlto = 64;

        if (icon1 != null) {
            icon1.setBounds(0, 0, nuevoAncho, nuevoAlto);
        }
        icon1.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        fab.setImageDrawable(icon1);

        CardView cardView1 = findViewById(R.id.cardView1);
        cardView1.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        CardView cardView2 = findViewById(R.id.cardView2);
        cardView2.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        CardView cardView3 = findViewById(R.id.cardView3);
        cardView3.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        progressBar = findViewById(R.id.progressBarAgregar);
        spinnerUbicacion = findViewById(R.id.spinnerUbicacion);
        //spinnerAlmacen = findViewById(R.id.spinnerAlmacen);
        multiAlmacen = findViewById(R.id.multiAlmacen);

        Intent intent = getIntent();
        if (intent != null){
            String userId = intent.getStringExtra("userId");
            String recintoName = intent.getStringExtra("recintoName");
            int recinto = intent.getIntExtra("recinto", 0);
            String ubicacion = intent.getStringExtra("ubicacion");

            TextView txtUserId = findViewById(R.id.txtUserId);
            txtUserId.setText(userId);
            TextView txtRecintoName = findViewById(R.id.txtRecintoName);
            txtRecintoName.setText(recintoName);
            TextView txtRecinto = findViewById(R.id.txtRecinto);
            txtRecinto.setText(String.valueOf(recinto));

            showLoadingIndicator();

            ConsultarUbicacionesTask consultarUbicacionesTask = new ConsultarUbicacionesTask();
            consultarUbicacionesTask.execute(String.valueOf(recinto), ubicacion);
        }

        //Capturar Fecha actual y mostrar en el TextView
        TextView txtFechaHoy = findViewById(R.id.txtFechaSolicitud);
        Date fechaActual = new Date();
        Date horaActual = new Date();

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaHoy = formatoFecha.format(fechaActual);
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horaHoy = formatoHora.format(horaActual);

        txtFechaHoy.setText(fechaHoy);

        editText = findViewById(R.id.editFecha);
        editText.setText(fechaHoy);

        editHora = findViewById(R.id.editHora);
        editHora.setText(horaHoy);

        TextView txtRecinto = findViewById(R.id.txtRecinto);
        //SpinnerAlmacen

        spinnerUbicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ubicacionSeleccionada = (String) parent.getItemAtPosition(position);

                String recintoSeleccionado = txtRecinto.getText().toString();

                showLoadingIndicator();

                multiAlmacen.setText("");

                new EncargadoTask().execute(recintoSeleccionado, ubicacionSeleccionada);

                ConsultarAlmacenesTask consultarAlmacenesTask = new ConsultarAlmacenesTask();
                consultarAlmacenesTask.execute(ubicacionSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editText = findViewById(R.id.editFecha);
        editHora = findViewById(R.id.editHora);
        calendar = Calendar.getInstance();

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        editHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
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

    private class ConsultarUbicacionesTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            String recinto = params[0];
            String ubicacion = params[1];
            String url1 = ip + "apk/ubicaciones.php";
            String apiUrl = url1 + "?recinto=" + recinto + "&ubicacion=" + ubicacion;
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

        protected void onPostExecute(String response) {
            hideLoadingIndicator();
            try {
                JSONArray jsonArray = new JSONArray(response);
                List<String> ubicacionesList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String idUbicacion = jsonObject.getString("id_ubicacion");
                    String nombreUbicacion = jsonObject.getString("ubicacion");
                    ubicacionesList.add(idUbicacion + " " + nombreUbicacion);
                }

                ArrayAdapter<String> ubicacionAdapter = new ArrayAdapter<>(Agregar.this, android.R.layout.simple_spinner_item, ubicacionesList);
                ubicacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUbicacion.setAdapter(ubicacionAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error en el formato de respuesta" + e);
            }
        }
    }

    private class ConsultarAlmacenesTask extends AsyncTask<String, Void, String>{
        String apiUrl;
        protected  String doInBackground(String... params){
            String ubicacionSeleccionada = params[0];
            int espacioIndex = ubicacionSeleccionada.indexOf(" ");
            if (espacioIndex != -1) {
                ubicacionSeleccionada = ubicacionSeleccionada.substring(0, espacioIndex);
            }
            apiUrl = ip + "apk/almacen.php?ubicacion=" + ubicacionSeleccionada;
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

        protected void onPostExecute(String response) {
            hideLoadingIndicator();
            try {
                JSONArray jsonArray = new JSONArray(response);
                List<String> almacenesList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String nombreAlmacen = jsonObject.getString("almacen");
                    almacenesList.add(nombreAlmacen);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Agregar.this, android.R.layout.simple_dropdown_item_1line, almacenesList);
                multiAlmacen.setAdapter(adapter);

                multiAlmacen.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {
                    @Override
                    public int findTokenStart(CharSequence text, int cursor) {
                        int i = cursor;
                        while (i > 0 && (text.charAt(i - 1) != ',' && text.charAt(i - 1) != ';')) {
                            i--;
                        }
                        while (i < cursor && text.charAt(i) == ' ') {
                            i++;
                        }
                        return i;
                    }

                    @Override
                    public int findTokenEnd(CharSequence text, int cursor) {
                        int i = cursor;
                        int len = text.length();
                        while (i < len) {
                            if (text.charAt(i) == ',' || text.charAt(i) == ';') {
                                return i;
                            } else {
                                i++;
                            }
                        }
                        return len;
                    }

                    @Override
                    public CharSequence terminateToken(CharSequence text) {
                        int i = text.length();
                        while (i > 0 && text.charAt(i - 1) == ' ') {
                            i--;
                        }
                        if (i > 0 && (text.charAt(i - 1) == ',' || text.charAt(i - 1) == ';')) {
                            return text;
                        } else {
                            if (text instanceof Spanned) {
                                SpannableString sp = new SpannableString(text + ", ");
                                TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                                return sp;
                            } else {
                                return text + ", ";
                            }
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error en el formato de respuesta" + e);
            }
        }
    }

    private class EncargadoTask extends AsyncTask<String, Void, String>{
        String apiUrl;
        @Override
        protected String doInBackground(String... params) {
            String recinto = params[0];
            String ubicacion = params[1];

            int espacioIndex = ubicacion.indexOf(" ");
            if (espacioIndex != -1) {
                ubicacion = ubicacion.substring(0, espacioIndex);
            }

            String server = ip + "apk/encargado.php";
            apiUrl = server + "?recinto=" + recinto + "&ubicacion=" + ubicacion;

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
                TextView txtUserSolicitud = findViewById(R.id.txtUserSolicitud);
                TextView txtUserSolicitudName = findViewById(R.id.txtUserSolicitudName);
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("message")){
                    String mensaje = jsonResponse.getString("message");
                    showErrorDialog("Encargados: " + mensaje + "\n" + apiUrl);
                }else {
                    String id = jsonResponse.getString("id");
                    String operador = jsonResponse.getString("nombre");
                    txtUserSolicitud.setText(id);
                    txtUserSolicitudName.setText(operador);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void showDatePickerDialog(){
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        @SuppressLint("DefaultLocale") String selectedDate = String.format("%02d/%02d/%04d",day, month+1,year);
                        editText.setText(selectedDate);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void showTimePickerDialog(){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        @SuppressLint("DefaultLocale") String selectedTime = String.format("%02d:%02d:%02d", hourOfDay, minute, second);

                        editHora.setText(selectedTime);
                    }
                },
                hour, minute, true
        );

        timePickerDialog.show();
    }

    public void guardarSolicitud(View view) throws ParseException {

        TextView txtUserSolicitud = findViewById(R.id.txtUserId);
        String userSolicitud = txtUserSolicitud.getText().toString();

        TextView txtRecinto = findViewById(R.id.txtRecinto);
        String idRecinto = txtRecinto.getText().toString();

        Spinner spinnerUbicacion = findViewById(R.id.spinnerUbicacion);
        String idUbicacion = spinnerUbicacion.getSelectedItem().toString();
        int espacioIndex = idUbicacion.indexOf(" ");
        if (espacioIndex != -1) {
            idUbicacion = idUbicacion.substring(0, espacioIndex);
        }

        TextView txtFecha = findViewById(R.id.txtFechaSolicitud);
        String fechaStr = txtFecha.getText().toString();
        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date fecha = fechaFormat.parse(fechaStr);
        SimpleDateFormat mysqlFechaFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fechaSol = mysqlFechaFormat.format(fecha);

        EditText editCantidad = findViewById(R.id.editCantidad);
        String cantidad = editCantidad.getText().toString();

        MultiAutoCompleteTextView multiAlmacen = findViewById(R.id.multiAlmacen);
        String almacen = multiAlmacen.getText().toString();
        if (almacen.equals("")){
            Toast.makeText(this, "Agregar almacenes", Toast.LENGTH_SHORT).show();
        } else if (cantidad.equals("0") || cantidad.equals("")) {
            Toast.makeText(this,"Cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
        }else {
            EditText editFecha = findViewById(R.id.editFecha);
            String fechaReqStr = editFecha.getText().toString();
            SimpleDateFormat fechaFormat2 = new SimpleDateFormat("dd/MM/yyyy");
            Date fecha2 = fechaFormat2.parse(fechaReqStr);
            SimpleDateFormat mysqlFechaFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            String fechaReq = mysqlFechaFormat2.format(fecha2);

            EditText editHora = findViewById(R.id.editHora);
            String horaReq = editHora.getText().toString();

            EditText editObservacion = findViewById(R.id.editObservacion);
            String observacion = editObservacion.getText().toString().toUpperCase();

            String estado = "PENDIENTE";

            TextView txtOperador = findViewById(R.id.txtUserSolicitud);
            String idOperador = txtOperador.getText().toString();

            TextView txtOperadorName = findViewById(R.id.txtUserSolicitudName);
            String nombreOperador = txtOperadorName.getText().toString();

            TextView txtRecintoName = findViewById(R.id.txtRecintoName);
            String nombreRecinto = txtRecintoName.getText().toString();

            Spinner spinnerUbicacionNombre = findViewById(R.id.spinnerUbicacion);
            String nombreUbicacion = spinnerUbicacionNombre.getSelectedItem().toString();
            int espacioIndex2 = nombreUbicacion.indexOf(" ");
            if (espacioIndex2 != -1) {
                nombreUbicacion = nombreUbicacion.substring(espacioIndex2 + 1);
            }

            String url = ip + "apk/registrarSolicitud.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            String finalIdUbicacion = idUbicacion;
            String finalNombreUbicacion = nombreUbicacion;
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String message = jsonResponse.getString("message");
                                String estado = jsonResponse.getString("status");

                                if ("success".equals(estado)) {
                                    showSuccessDialog(message);

                                }else if ("error".equals(estado)) {
                                    showErrorDialog(message);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                showErrorDialog("Error al procesar la respuesta del servidor");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showErrorDialog("Error de conexion: " + error.toString());
                        }
                    }) {
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userSolicitud", userSolicitud);
                    params.put("idRecinto", idRecinto);
                    params.put("idUbicacion", finalIdUbicacion);
                    params.put("fechaSol", fechaSol);
                    params.put("cantidad", cantidad);
                    params.put("almacen", almacen);
                    params.put("fechaReq", fechaReq);
                    params.put("horaReq", horaReq);
                    params.put("observacion", observacion);
                    params.put("estado", estado);
                    params.put("idOperador", idOperador);
                    params.put("nombreOperador", nombreOperador);
                    params.put("nombreRecinto", nombreRecinto);
                    params.put("nombreUbicacion", finalNombreUbicacion);

                    return params;
                }
            };

            queue.add(request);
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