package com.example.sisjor;

//import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;

//import java.io.BufferedInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
//import java.util.HashMap;
//import java.util.Map;

import java.security.NoSuchAlgorithmException;


public class Login extends AppCompatActivity {

    private EditText t_user, t_pass;
    //private String str_user, str_pass;
    private ProgressBar progressBar;
    private CardView cardView;
    private String ip = "https://puntocombolivia.com/SISJOR/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        t_user = findViewById(R.id.editUser);
        t_pass = findViewById(R.id.editPassword);
        progressBar = findViewById(R.id.progressBar);

        cardView = findViewById(R.id.cardviewLogin);
        cardView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }

    private String md5Hash(String input){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();

            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void login (View view) {
        if (t_user.getText().toString().equals("")){
            Toast.makeText(this, "Ingrese Usuario", Toast.LENGTH_SHORT).show();
        } else if (t_pass.getText().toString().equals("")) {
            Toast.makeText(this,"Ingrese Contraseña", Toast.LENGTH_SHORT).show();
        }else {

            String str_user = t_user.getText().toString().trim();
            String str_pass = t_pass.getText().toString().trim();

            showLoadingIndicator();

            new LoginTask().execute(str_user, str_pass);
        }
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

    private class LoginTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            String str_user = params[0];
            String str_pass = params[1];

            String url1 = ip + "apk/login_apk.php";
            String serverUrl = url1 + "?user=" + str_user + "&password=" + md5Hash(str_pass);
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null){
                    result.append(line);
                }
                reader.close();

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                result = new StringBuilder("Error de conexion al servidor");
            }

            return result.toString();
        }

        protected void onPostExecute(String response){
            hideLoadingIndicator();

            try {
                JSONObject jsonResponse = new JSONObject(response);
                String message = jsonResponse.getString("message");
                if (message.equals("ingreso correctamente")){
                    int userID = jsonResponse.getInt("id");
                    String userNombre = jsonResponse.getString("nombre");
                    String userApellido = jsonResponse.getString("apellido");
                    String userNombreCompleto = userNombre + " " + userApellido;
                    String userEmail = jsonResponse.getString("correo");
                    String userImg = jsonResponse.getString("imagen");
                    int userIntTipo = jsonResponse.getInt("tipo");
                    String userTipo = "";
                    if (userIntTipo == 1){
                        userTipo = "Administrador";
                    }else{
                        userTipo = "Usuario";
                    }
                    String userEmpresa = jsonResponse.getString("empresa");
                    if ("%".equals(userEmpresa)){
                        userEmpresa = "ADMINISTRADOR";
                    }
                    String userRecinto = jsonResponse.getString("id_recinto");
                    String userRecintoName;
                    if ("%".equals(userRecinto)) {
                        userRecinto = "%";
                        userRecintoName = "ADMINISTRADOR RECINTO";
                    }else{
                        userRecintoName = jsonResponse.getString("nombre_recinto");
                    }
                    String userUbicacion = jsonResponse.getString("id_ubicacion");
                    String userUbicacionName;
                    if ("%".equals(userUbicacion)) {
                        userUbicacion = "%";
                        userUbicacionName = "ADMINISTRADOR UBICACION";
                    }else{
                        userUbicacionName = jsonResponse.getString("ubicacion");
                    }

                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    intent.putExtra("id", userID);
                    intent.putExtra("imagen", userImg);
                    intent.putExtra("nombre", userNombreCompleto);
                    intent.putExtra("correo", userEmail);
                    intent.putExtra("tipo", userTipo);
                    intent.putExtra("empresa", userEmpresa);
                    intent.putExtra("recinto", userRecinto);
                    intent.putExtra("recintoName", userRecintoName);
                    intent.putExtra("ubicacion", userUbicacion);
                    intent.putExtra("ubicacionName", userUbicacionName);
                    startActivity(intent);
                    finish();
                }else{
                    showErrorDialog(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error en el formato de respuesta" + e);
            }
        }
    }

}