package com.permify.yoprefiero;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.permify.yoprefiero.BaseVolley.BaseVolleyActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class LoginActivity extends BaseVolleyActivity {
    public EditText txtEmail;
    public EditText txtPass;

    public SharedPreferences PREFERENCIAS_SPR;
    private ServiciosRest serviciosRest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("hola", "hola mundo");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_wait);

        //prepara base url de servicios rest
        serviciosRest = new ServiciosRest();

        //carga las sharedpreferences creadas para yo prefiero
        PREFERENCIAS_SPR = getSharedPreferences("com.permify.yoprefiero", Context.MODE_PRIVATE);

        //detecta si ya existe apiKey en SharedPreferences
        if(PREFERENCIAS_SPR.contains("apiKey")){
            Log.i("hola", "tiene api key");

            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if(apiKey != null && idUsuario != 0){
                Log.i("hola", "va a autorizar");
                //revisa si apiKey es valida
                this.autorizaApiKeyExistente(apiKey, idUsuario, false);

            }else{
                Log.i("hola", "api key invalida o null");
                //infla layout para presentar a usuario
                setContentView(R.layout.activity_login);

                //inicializa el enlace a elementos de layout
                txtEmail = (EditText) this.findViewById(R.id.txtEmail);
                txtPass = (EditText) this.findViewById(R.id.txtPass);
            }

        }else{
            Log.i("hola", "no contiene api key");

            //infla layout para presentar a usuario
            setContentView(R.layout.activity_login);

            //inicializa el enlace a elementos de layout
            txtEmail = (EditText) this.findViewById(R.id.txtEmail);
            txtPass = (EditText) this.findViewById(R.id.txtPass);
        }
    }

    //acción al hacer click en INGRESAR
    public void onClickLogin(View v){

        String email = txtEmail.getText().toString();
        String passw = txtPass.getText().toString();

        if(!email.equals("") || !passw.equals("")){
            //ejecuta llamada a API para autorizar login
            this.ejecutarRegistroApiKey(email, passw, "desde_login");

        }else{
            Toast.makeText(this, R.string.msg_login_empty_values, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickReintentar(View v){
        if(PREFERENCIAS_SPR.contains("apiKey")) {
            Log.i("hola", "tiene api key");

            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                Log.i("hola", "va a autorizar");
                //revisa si apiKey es valida
                this.autorizaApiKeyExistente(apiKey, idUsuario, true);
            }
        }
    }

    //Recurso solicitado para crear ApiKey
    public void ejecutarRegistroApiKey(final String email, final String password, final String origen){
        if(!email.equals("") || !password.equals("")){
            final String URL = serviciosRest.postAutorizador();

            //preparacion de parametros enviados por POST
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("email", email);
            params.put("password", password);

            //crea nuevo request
            JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if(!response.has("error")){
                                    //no contiene error desde servidor
                                    //guarda apiKey y datos en SharedPreferences

                                    SharedPreferences.Editor editorPrefer = PREFERENCIAS_SPR.edit();
                                    editorPrefer.putString("apiKey", response.getString("api_key"));
                                    editorPrefer.putInt("idUsuario", response.getInt("id"));
                                    editorPrefer.putString("emailUsuario", response.getString("email"));
                                    editorPrefer.commit();

                                    //detecta si es la primera vez que inicia sesion, pide datos de perfil
                                    //sino a pantalla home
                                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(i);
                                    finish();

                                    onConnectionFinished();
                                }else{
                                    //error desde el servidor - muestra mensaje
                                    String error = response.getString("error");
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            NetworkResponse respuesta = error.networkResponse;

                            if(respuesta != null){
                                switch (respuesta.statusCode) {
                                    case 403:
                                        Toast.makeText(getApplicationContext(), R.string.msg_login_error_401, Toast.LENGTH_LONG).show();
                                        onConnectionFinished();
                                        break;

                                    case 404:
                                        //usuario no encontrado,
                                        //llamar a servicio para crear usuario
                                        ejecutarRegistroUsuario(email, password);
                                        break;

                                    case 500:
                                        Toast.makeText(getApplicationContext(), R.string.msg_login_error_401, Toast.LENGTH_LONG).show();
                                        onConnectionFinished();
                                        break;

                                    default:
                                        Toast.makeText(getApplicationContext(), respuesta.statusCode, Toast.LENGTH_LONG).show();
                                        onConnectionFailed(error.toString());
                                }
                            }
                        }
                    });

            //agrega a la cola y ejecuta request
            if (origen.equals("desde_login")) {
                addToQueue(req, true, getResources().getString(R.string.login_msg_popup_desde_login));


            } else if (origen.equals("desde_registro")) {
                addToQueue(req, true, getResources().getString(R.string.login_msg_popup_desde_registro));

            }
        }else{
            Toast.makeText(this, R.string.msg_login_empty_values, Toast.LENGTH_SHORT).show();
        }
    }

    //Recurso solicitado para crear usuario
    public void ejecutarRegistroUsuario(final String email, final String password){

        if(!email.equals("") || !password.equals("")){
            String REGISTRO_URL = serviciosRest.postUsuarios();

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("email", email);
            params.put("password", password);

            JsonObjectRequest req = new JsonObjectRequest(REGISTRO_URL, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.has("error")) {
                                    //viene con respuesta
                                    if(!response.getBoolean("error")){
                                        //creacion de usuario con exito
                                        ejecutarRegistroApiKey(email, password, "desde_registro");

                                    } else {
                                        //error desde el servidor
                                        String error = response.getString("error");
                                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                        onConnectionFinished();
                                    }

                                }else{
                                    //no vienen datos en el response
                                    Toast.makeText(getApplicationContext(), R.string.login_msg_error_servidor, Toast.LENGTH_SHORT).show();
                                    onConnectionFinished();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), R.string.login_msg_error_servidor, Toast.LENGTH_SHORT).show();
                                onConnectionFinished();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.networkResponse == null){
                                if(error.getClass().equals(TimeoutError.class)){
                                    Toast.makeText(getApplicationContext(), R.string.login_txt_timeout, Toast.LENGTH_SHORT).show();
                                    onConnectionFinished();
                                }
                            }else {
                                NetworkResponse respuesta = error.networkResponse;

                                if (respuesta != null) {
                                    switch (respuesta.statusCode) {
                                        case 403:
                                            Toast.makeText(getApplicationContext(), R.string.msg_login_error_401, Toast.LENGTH_LONG).show();
                                            onConnectionFinished();
                                            break;
                                        case 500:
                                            Toast.makeText(getApplicationContext(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
                                            onConnectionFinished();
                                            break;
                                        default:
                                            onConnectionFailed(error.toString());
                                    }
                                }
                            }
                        }
                    });

            addToQueue(req, true, getResources().getString(R.string.login_msg_popup_new_user));

        }else{
            Toast.makeText(this, R.string.msg_login_empty_values, Toast.LENGTH_SHORT).show();
        }
    }

    //autoriza api-key para comenzar
    public void autorizaApiKeyExistente(final String apiKey, final Integer idUsuario, boolean activaIndicador){
        if(apiKey != null && idUsuario != 0){
            Log.i("hola", "dentro de la autorizacion");

            String AUTORIZADOR_URL = serviciosRest.getAutorizador(apiKey, idUsuario);
            Log.d("hola", AUTORIZADOR_URL);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, AUTORIZADOR_URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.i("hola", "request con exito");
                                if(response.has("valido")){
                                    Boolean esValido = response.getBoolean("valido");

                                    if(esValido){
                                        //apiKey valida
                                        //mandar al home
                                        Log.d("hola", "apikey valida");
                                        onConnectionFinished();

                                        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(i);
                                        finish();

                                    }else{
                                        //apiKey no valida
                                        Log.d("hola", "api key no valida");
                                        //infla layout para presentar a usuario
                                        onConnectionFinished();

                                        setContentView(R.layout.activity_login);
                                        txtEmail = (EditText) findViewById(R.id.txtEmail);
                                        txtPass = (EditText) findViewById(R.id.txtPass);

                                    }
                                }else{
                                    //no viene respuesta
                                    String mensaje_error = response.getString("mensaje");
                                    Toast.makeText(getApplicationContext(), mensaje_error, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("hola", "error response");
                            error.printStackTrace();

                            if(error.networkResponse == null){
                                if(error.getClass().equals(TimeoutError.class)){
                                    setContentView(R.layout.activity_login_timeout);
                                    onConnectionFinished();
                                }
                            }else {
                                NetworkResponse respuesta = error.networkResponse;

                                try {
                                    String jsonErr = new String(respuesta.data);

                                    JSONObject jsonError = new JSONObject(jsonErr);
                                    if (respuesta != null) {
                                        switch (respuesta.statusCode) {
                                            case 403:
                                                Toast.makeText(getApplicationContext(), jsonError.optString("mensaje"), Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            case 500:
                                                Toast.makeText(getApplicationContext(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            default:
                                                onConnectionFailed(error.toString());
                                        }
                                    }

                                } catch (JSONException ex) {
                                    Toast.makeText(LoginActivity.this, "Exception: " + ex.getMessage(), Toast.LENGTH_LONG).show();

                                }
                            }
                        }
                    });

            if(activaIndicador){
                addToQueue(req, true, getResources().getString(R.string.login_msg_popup_valida));

            }else{
                addToQueue(req, false);
            }
        }
    }
}
