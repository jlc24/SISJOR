package com.example.sisjor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    private ProgressBar progressBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String ip = "https://puntocombolivia.com/SISJOR/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FloatingActionButton fab = findViewById(R.id.floatBtn1);
        Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.baseline_group_add_24);
        int nuevoAncho = 64;
        int nuevoAlto = 64;

        if (icon1 != null) {
            icon1.setBounds(0, 0, nuevoAncho, nuevoAlto);
        }
        icon1.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        fab.setImageDrawable(icon1);

        progressBar = findViewById(R.id.progressBarLoading);

        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        Intent intent = getIntent();
        if (intent != null){
            int userId = intent.getIntExtra("id", 0);
            String userImg = intent.getStringExtra("imagen");
            String userName = intent.getStringExtra("nombre");
            String userEmail = intent.getStringExtra("correo");
            String userTipo = intent.getStringExtra("tipo");
            String userEmpresa = intent.getStringExtra("empresa");
            String userRecinto = intent.getStringExtra("recinto");
            String userRecintoName= intent.getStringExtra("recintoName");
            String userUbicacion = intent.getStringExtra("ubicacion");
            String userUbicacionName = intent.getStringExtra("ubicacionName");

            TextView txtUserID = headerView.findViewById(R.id.txtUserId);
            txtUserID.setText(String.valueOf(userId));
            TextView txtUserName = headerView.findViewById(R.id.txtUserName);
            txtUserName.setText(userName);
            TextView txtUserEmail = headerView.findViewById(R.id.txtUserEmail);
            txtUserEmail.setText(userEmail);
            TextView txtUserTipo = headerView.findViewById(R.id.txtUserTipo);
            txtUserTipo.setText(userTipo);
            TextView txtUserEmpresa = headerView.findViewById(R.id.txtUserEmpresa);
            txtUserEmpresa.setText(userEmpresa);
            TextView txtUserRecinto = headerView.findViewById(R.id.txtUserRecinto);
            txtUserRecinto.setText(String.valueOf(userRecinto));
            TextView txtUserRecintoName = headerView.findViewById(R.id.txtUserRecintoName);
            txtUserRecintoName.setText(userRecintoName);
            TextView txtUserUbicacion = headerView.findViewById(R.id.txtUserUbicacion);
            txtUserUbicacion.setText(String.valueOf(userUbicacion));
            TextView txtUserUbicacionName = headerView.findViewById(R.id.txtUserUbicacionName);
            txtUserUbicacionName.setText(userUbicacionName);
        }

        TextView txtCopyright = headerView.findViewById(R.id.txtCopyright);
        txtCopyright.setText(getString(R.string.copyright));

        TextView txtEmpresa = headerView.findViewById(R.id.txtUserEmpresa);
        String userEmpresa = txtEmpresa.getText().toString();
        FloatingActionButton floatBtn1 = findViewById(R.id.floatBtn1);

        if ("ALBO".equals(userEmpresa)){
            floatBtn1.setVisibility(View.VISIBLE);
        }else if ("PUNTOCOM".equals(userEmpresa) || "ADMINISTRADOR".equals(userEmpresa)) {
            floatBtn1.setVisibility(View.GONE);
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView txtUserID = headerView.findViewById(R.id.txtUserId);
        String userid = txtUserID.getText().toString();

        showLoadingIndicator();
        new EstadoTask(this, userid, "PENDIENTE", userEmpresa).execute(userid, "PENDIENTE", userEmpresa);
        new EstadoTask(this, userid, "PROCESADO", userEmpresa).execute(userid, "PROCESADO", userEmpresa);

        new ListSolicitudTask(this, userid, userEmpresa).execute(userid, userEmpresa);
        hideLoadingIndicator();
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void cerrarSesion(MenuItem item){
        Intent intent = new Intent(this, Login.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        //finishAffinity();
    }

    private void showLoadingIndicator(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator(){
        progressBar.setVisibility(View.GONE);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mensaje: ");
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public class EstadoTask extends AsyncTask<String, Void, String>{

        private WeakReference<Home> activityReference;
        private String userId, estado, empresa, apiUrl;

        EstadoTask(Home activity, String userId, String estado, String empresa) {
            activityReference = new WeakReference<>(activity);
            this.userId = userId;
            this.estado = estado;
            this.empresa = empresa;
        }
        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String estado = params[1];
            String empresa = params[2];
            String server = ip + "apk/contEstado.php";
            apiUrl = server + "?userid=" + userId + "&estado=" + estado + "&empresa=" + empresa;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null){
                    result.append(line);
                }
                reader.close();
                connection.disconnect();
            }catch (IOException e) {
                e.printStackTrace();
                result = new StringBuilder("Error de conexion al servidor");
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                String estado = jsonResponse.getString("estado");
                String status = jsonResponse.getString("status");
                TextView txtNumPendiente = findViewById(R.id.txtNumPendiente);
                TextView txtNumProcesado = findViewById(R.id.txtNumProcesadas);
                if ("PENDIENTE".equals(status)){
                    txtNumPendiente.setText(estado);
                } else if ("PROCESADO".equals(status)) {
                    txtNumProcesado.setText(estado);
                }
            }catch (JSONException e){
                e.printStackTrace();
                //showErrorDialog(apiUrl);
            }
        }
    }

    private static final int CODIGO_AGREGAR = 1;

    public void agregarEstibador (View view){
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView userId = headerView.findViewById(R.id.txtUserId);
        TextView recintoId = headerView.findViewById(R.id.txtUserRecinto);
        TextView recintoName = headerView.findViewById(R.id.txtUserRecintoName);
        TextView ubicacionId = headerView.findViewById(R.id.txtUserUbicacion);

        String user = userId.getText().toString();
        int txtUserRecintoId = Integer.parseInt(recintoId.getText().toString());
        String txtRecintoName = recintoName.getText().toString();
        String txtUserUbicacionId = ubicacionId.getText().toString();

        Intent intent = new Intent(getApplicationContext(), Agregar.class);
        intent.putExtra("userId", user);
        intent.putExtra("recinto", txtUserRecintoId);
        intent.putExtra("recintoName", txtRecintoName);
        intent.putExtra("ubicacion", txtUserUbicacionId);
        startActivityForResult(intent, CODIGO_AGREGAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODIGO_AGREGAR) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getBooleanExtra("actualizarEstado", false)) {
                    NavigationView navigationView = findViewById(R.id.navigationView);
                    View headerView = navigationView.getHeaderView(0);
                    TextView txtUserID = headerView.findViewById(R.id.txtUserId);
                    String userid = txtUserID.getText().toString();
                    TextView txtEmpresa = headerView.findViewById(R.id.txtUserEmpresa);
                    String userEmpresa = txtEmpresa.getText().toString();
                    showLoadingIndicator();
                    new EstadoTask(this, userid, "PENDIENTE", userEmpresa).execute(userid, "PENDIENTE", userEmpresa);
                    new EstadoTask(this, userid, "PROCESADO", userEmpresa).execute(userid, "PROCESADO", userEmpresa);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new ListSolicitudTask(Home.this, userid, userEmpresa).execute(userid, userEmpresa);
                            hideLoadingIndicator();
                        }
                    },500);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ListSolicitudTask extends AsyncTask<String, Void, String> {
        private final WeakReference<Home> activityReference;
        private String userId, apiUrl, empresa;
        ListView listSolicitudes = findViewById(R.id.listSolicitud);
        ListSolicitudTask(Home activity, String userId, String empresa) {
            activityReference = new WeakReference<>(activity);
            this.userId = userId;
            this.empresa = empresa;
        }

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String empresa = params[1];
            String server = ip + "apk/tablaSolicitud.php";
            apiUrl = server + "?user=" + userId + "&empresa=" + empresa;

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
            }catch (IOException e) {
                e.printStackTrace();
                result = new StringBuilder("Error de conexion al servidor");
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            Home activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            try {
                JSONArray jsonArray = new JSONArray(response);
                List<Solicitud> solicitudList = new ArrayList<>();
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.has("message")){
                    String mensaje = jsonObject.getString("message");
                    showErrorDialog(mensaje);
                }else{
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        String recinto = jsonObject.getString("nombre_recinto");
                        String ubicacion = jsonObject.getString("ubicacion");
                        String estado = jsonObject.getString("status");

                        solicitudList.add(new Solicitud(id, recinto, ubicacion, estado));
                    }
                    NavigationView navigationView = findViewById(R.id.navigationView);
                    View headerView = navigationView.getHeaderView(0);
                    TextView txtUserID = headerView.findViewById(R.id.txtUserId);
                    String userId = txtUserID.getText().toString();
                    TextView txtUserEmpresa = headerView.findViewById(R.id.txtUserEmpresa);
                    String empresa = txtUserEmpresa.getText().toString();

                    SolicitudAdapter adapter = new SolicitudAdapter(activity, solicitudList, userId, empresa, CODIGO_AGREGAR);

                    activity.runOnUiThread(() -> {
                        ListView listSolicitud = activity.findViewById(R.id.listSolicitud);
                        listSolicitud.setAdapter(adapter);
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("No se pudo conectar al servidor. " + e.toString());
            }
        }
    }

    public void VerProcesados (View view){
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView txtNombre = headerView.findViewById(R.id.txtUserName);
        String nombre = txtNombre.getText().toString();
        showErrorDialog("Solicitudes Procesados:");
    }
}