package com.permify.yoprefiero;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.permify.yoprefiero.BaseVolley.BaseVolleyFragment;
import com.permify.yoprefiero.models.Categoria;
import com.permify.yoprefiero.models.Perfil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Agregar boton a menu: http://www.grokkingandroid.com/adding-action-items-from-within-fragments/
 */
public class PerfilFragment extends BaseVolleyFragment {
    public SharedPreferences PREFERENCIAS_SPR;
    private ServiciosRest urlRest;
    private ArrayList<Categoria> categorias;
    private LinearLayout contenedorListaCategorias;

    public Perfil perfilUsuario;

    private TextView txtNombreUsuario;
    private TextView txtEmailUsuario;
    private TextView txtCiudadUsuario;
    private TextView txtTelefonoUsuario;

    private EditText txtEditNombre;
    private EditText txtEditApellido;
    private EditText txtEditTelefono;
    private Spinner cmbRegion;
    private ImageButton btnSubirImagen;

    private MenuItem opcionEditar;
    private MenuItem opcionFinalizarEdicion;

    private ImageView imagenPerfil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Context context = getActivity().getApplicationContext();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        opcionEditar = menu.add(Menu.NONE, R.id.opcion_editar, 1, R.string.perfil_boton_editar);
        opcionEditar.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        opcionEditar.setIcon(R.drawable.ic_editar);

        opcionFinalizarEdicion = menu.add(Menu.NONE, R.id.opcion_finalizar_edicion, 2, R.string.perfil_boton_fin_edicion);
        opcionFinalizarEdicion.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        opcionFinalizarEdicion.setIcon(R.drawable.ic_check);
        opcionFinalizarEdicion.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //primero se ejecuta onOptionsItemSelected de HomeActivity, luego esta.
        switch (item.getItemId()){
            case R.id.opcion_editar:
                //Editar perfil
                this.comenzarEdicionPerfil();
                return true;

            case R.id.opcion_finalizar_edicion:
                //this.terminarEdicionPerfil();
                this.enviaEdicionPerfil();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_perfil, container, false);
        PREFERENCIAS_SPR = getActivity().getSharedPreferences("com.permify.yoprefiero", Context.MODE_PRIVATE);
        categorias = new ArrayList<Categoria>();

        //return inflater.inflate(R.layout.fragment_perfil, container, false);

        txtNombreUsuario = (TextView)layout.findViewById(R.id.txt_nombre_usuario);
        txtEmailUsuario = (TextView)layout.findViewById(R.id.txt_email_usuario);
        txtCiudadUsuario = (TextView)layout.findViewById(R.id.txt_ciudad_usuario);
        txtTelefonoUsuario = (TextView)layout.findViewById(R.id.txt_fono_usuario);

        txtEditNombre = (EditText)layout.findViewById(R.id.txt_edit_nombre);
        txtEditApellido = (EditText)layout.findViewById(R.id.txt_edit_apellido);
        txtEditTelefono = (EditText)layout.findViewById(R.id.txt_edit_fono);

        btnSubirImagen = (ImageButton)layout.findViewById(R.id.btn_subir_imagen);

        imagenPerfil = (ImageView)layout.findViewById(R.id.img_usuario);

        cmbRegion = (Spinner) layout.findViewById(R.id.cmb_comunas);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> comunaAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.regiones_chile, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        comunaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        cmbRegion.setAdapter(comunaAdapter);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Inicializa adaptador para lista de Categorias
        contenedorListaCategorias = (LinearLayout) getActivity().findViewById(R.id.lista_categorias);

        //muestra datos de usuario
        this.cargaDatosPersonales();

        //carga categorias seleccionadas
        this.cargaCategoriasUsuarios();

    }

    private void comenzarEdicionPerfil(){
        opcionEditar.setVisible(false);
        opcionFinalizarEdicion.setVisible(true);

        //copia el texto de los textview siempre que no sean los textos por defecto
        //txtEditNombre.setText(txtNombreUsuario.getText().equals(getResources().getString(R.string.perfil_txt_nombre))? "" : txtNombreUsuario.getText());
        if(perfilUsuario != null){
            txtEditNombre.setText(perfilUsuario.getNombre());
            txtEditApellido.setText(perfilUsuario.getApellido());
            txtEditTelefono.setText(perfilUsuario.getTelefono());

            cmbRegion.setSelection(perfilUsuario.getIdRegion() != null? perfilUsuario.getIdRegion() : 0);
        }

        txtNombreUsuario.setVisibility(View.INVISIBLE);
        txtEditNombre.setVisibility(View.VISIBLE);
        txtEditApellido.setVisibility(View.VISIBLE);

        //txtEditTelefono.setText(txtTelefonoUsuario.getText().equals(getResources().getString(R.string.perfil_txt_telefono))? "" : txtTelefonoUsuario.getText());
        txtTelefonoUsuario.setVisibility(View.GONE);
        txtEditTelefono.setVisibility(View.VISIBLE);

        txtCiudadUsuario.setVisibility(View.INVISIBLE);
        cmbRegion.setVisibility(View.VISIBLE);

        btnSubirImagen.setVisibility(View.VISIBLE);
    }

    private void terminarEdicionPerfil(){

        if(perfilUsuario == null){
            perfilUsuario = new Perfil();
        }

        //intercambio de botones en menu
        opcionFinalizarEdicion.setVisible(false);
        opcionEditar.setVisible(true);

        if(!txtEditNombre.getText().toString().trim().equals("")) {
            if(!txtEditApellido.getText().toString().trim().equals("")){
                txtNombreUsuario.setText(txtEditNombre.getText().toString().trim() + " " + txtEditApellido.getText().toString().trim());

                perfilUsuario.setNombre(txtEditNombre.getText().toString().trim());
                perfilUsuario.setApellido(txtEditApellido.getText().toString().trim());
            }else{
                txtNombreUsuario.setText(txtEditNombre.getText());

                perfilUsuario.setNombre(txtEditNombre.getText().toString().trim());
            }
        }else {
            if(!txtEditApellido.getText().toString().trim().equals("")) {

                perfilUsuario.setNombre(null);
                perfilUsuario.setApellido(txtEditApellido.getText().toString().trim());

            }else{
                txtNombreUsuario.setText(null);

                perfilUsuario.setNombre(null);
                perfilUsuario.setApellido(null);
            }
        }

        txtEditNombre.setVisibility(View.INVISIBLE);
        txtEditApellido.setVisibility(View.GONE);
        txtNombreUsuario.setVisibility(View.VISIBLE);

        perfilUsuario.setIdRegion((int)cmbRegion.getSelectedItemId());

        txtCiudadUsuario.setText(cmbRegion.getSelectedItem().toString());
        cmbRegion.setVisibility(View.INVISIBLE);
        txtCiudadUsuario.setVisibility(View.VISIBLE);

        if(!txtEditTelefono.getText().toString().trim().equals("")) {

            txtTelefonoUsuario.setText(txtEditTelefono.getText().toString().trim());
            perfilUsuario.setTelefono(txtEditTelefono.getText().toString().trim());

        }else {
            txtTelefonoUsuario.setText(null);
            perfilUsuario.setTelefono(null);
        }

        txtEditTelefono.setVisibility(View.INVISIBLE);
        txtTelefonoUsuario.setVisibility(View.VISIBLE);

        btnSubirImagen.setVisibility(View.INVISIBLE);
    }

    //recoger datos y enviarlos
    private void enviaEdicionPerfil(){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                //Preparacion de argumentos
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("api_key", apiKey);

                if(!txtEditNombre.getText().toString().trim().equals("")){
                    params.put("nombre", txtEditNombre.getText().toString().trim());
                }

                if(!txtEditApellido.getText().toString().trim().equals("")){
                    params.put("apellido", txtEditApellido.getText().toString().trim());
                }

                if(!txtEditTelefono.getText().toString().trim().equals("")){
                    params.put("telefono", txtEditTelefono.getText().toString().trim());
                }

                Long idRegion = cmbRegion.getSelectedItemId();
                params.put("region", idRegion.toString());

                //crea nuevo request
                String URL = urlRest.postUsuariosPerfil(idUsuario);
                Log.d("PerfilFragment", params.toString());
                JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.has("error")) {
                                    //viene con respuesta
                                    if(!response.getBoolean("error")){
                                        //edicion de perfil exitoso
                                        Toast.makeText(getActivity().getApplicationContext(), R.string.perfil_guardado_ok, Toast.LENGTH_SHORT).show();

                                        //termina edicion de perfil
                                        terminarEdicionPerfil();

                                    } else {
                                        //error desde el servidor
                                        String error = response.getString("error");
                                        Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                    }

                                    onConnectionFinished();

                                }else{
                                    //no vienen datos en el response
                                    Toast.makeText(getActivity().getApplicationContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                                    onConnectionFinished();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getActivity().getApplicationContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                                onConnectionFinished();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.networkResponse == null){
                                if(error.getClass().equals(TimeoutError.class)){
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.login_txt_timeout, Toast.LENGTH_SHORT).show();

                                }
                            }else {
                                Log.d("AgregarCategoriaFragmen", "errror " + error.toString());
                                NetworkResponse respuesta = error.networkResponse;

                                if (respuesta != null) {
                                    try {
                                        String jsonErr = new String(respuesta.data);

                                        JSONObject jsonError = new JSONObject(jsonErr);

                                        switch (respuesta.statusCode) {
                                            case 400:
                                                Toast.makeText(getActivity().getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            case 500:
                                                Toast.makeText(getActivity().getApplicationContext(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            default:
                                                onConnectionFailed(error.toString());
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });

                //agrega a la cola y ejecuta request
                addToQueue(req, true, "Guardando perfil...");
            }
        }

    }

    private void cargaDatosPersonales(){

        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if(apiKey != null && idUsuario != 0) {
                txtEmailUsuario.setText(PREFERENCIAS_SPR.getString("emailUsuario", null));

                //capturar categorias para rellenar menu
                urlRest = new ServiciosRest();

                String URL = urlRest.getUsuariosPerfil(idUsuario, apiKey);

                JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                if(response.has("perfil_completo")){
                                    Boolean perfilCompleto = response.getBoolean("perfil_completo");

                                    if(perfilCompleto){
                                        //perfil completo - rellenar

                                        if(response.has("info")){
                                            perfilUsuario = new Perfil();

                                            JSONObject info = response.getJSONObject("info");

                                            if(!info.optString("nombre").equals("null")){
                                                if(!info.optString("apellido").equals("null")){
                                                    //nombre y apellido
                                                    perfilUsuario.setNombre(info.optString("nombre"));
                                                    perfilUsuario.setApellido(info.optString("apellido"));

                                                    String nombreCompleto = info.optString("nombre") + " " + info.optString("apellido");
                                                    txtNombreUsuario.setText(nombreCompleto);
                                                }else{
                                                    //solo nombre
                                                    perfilUsuario.setNombre(info.optString("nombre"));

                                                    txtNombreUsuario.setText(info.optString("nombre"));
                                                }
                                            }

                                            if(!info.optString("ciudad").equals("null")){
                                                perfilUsuario.setCiudad(info.optString("ciudad"));

                                                txtCiudadUsuario.setText(info.optString("ciudad", "No registra"));
                                            }

                                            if(!info.optString("telefono").equals("null")){
                                                perfilUsuario.setTelefono(info.optString("telefono"));

                                                txtTelefonoUsuario.setText(info.optString("telefono"));
                                            }

                                            if(!info.optString("fotografia").equals("null")) {
                                                Log.d("imagen", info.optString("fotografia"));
                                                byte[] decodedString = Base64.decode(info.optString("fotografia"), Base64.DEFAULT);
                                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                if (decodedByte != null) {
                                                    perfilUsuario.setFotografia(decodedByte);

                                                    //imagenPerfil.setImageBitmap(decodedByte);
                                                    imagenPerfil.setImageBitmap(getRoundedShape(decodedByte));

                                                }
                                            }
                                            perfilUsuario.setIdRegion(info.optInt("id_region", 0));
                                            if(perfilUsuario.getIdRegion() != 0){
                                                String[] regiones = getResources().getStringArray(R.array.regiones_chile);
                                                txtCiudadUsuario.setText(regiones[perfilUsuario.getIdRegion()]);
                                            }
                                        }

                                    }else{
                                        //no viene respuesta
                                        //poner textos por defecto
                                        Toast.makeText(getActivity().getApplicationContext(), "No tiene perfil", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    //no viene respuesta
                                    //poner textos por defecto
                                    Toast.makeText(getActivity().getApplicationContext(), "No tiene perfil", Toast.LENGTH_SHORT).show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            onConnectionFinished();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            error.printStackTrace();
                            if(error.networkResponse == null){
                                if(error.getClass().equals(TimeoutError.class)){
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.login_txt_timeout, Toast.LENGTH_SHORT).show();

                                }
                            }else {
                                NetworkResponse respuesta = error.networkResponse;

                                try {
                                    String jsonErr = new String(respuesta.data);
                                    //Log.i("hola", jsonErr);
                                    JSONObject jsonError = new JSONObject(jsonErr);
                                    if (respuesta != null) {
                                        switch (respuesta.statusCode) {
                                            case 403:
                                                Toast.makeText(getActivity(), jsonError.optString("mensaje"), Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            case 500:
                                                Toast.makeText(getActivity(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
                                                onConnectionFinished();
                                                break;
                                            default:
                                                onConnectionFailed(error.toString());
                                        }
                                    }

                                } catch (JSONException ex) {
                                    Toast.makeText(getActivity(), "Exception: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                    //pDialog.hide();
                                }
                            }
                        }
                });

                addToQueue(req, true);
            }
        }
    }

    private void cargaCategoriasUsuarios(){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if(apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu
                urlRest = new ServiciosRest();

                String URL = urlRest.getUsuariosProductos(idUsuario, apiKey);

                JsonArrayRequest req = new JsonArrayRequest(URL,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("perfil", "entro al exito de los servicio");

                                try {

                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    for(int i=0; i<response.length(); i++){
                                        JSONObject categoria = response.getJSONObject(i);

                                        int idCategoria = categoria.getInt("id");
                                        String nombreCategoria = categoria.getString("nombre");
                                        String descripcionCategoria = categoria.getString("nombre");

                                        Categoria cat = new Categoria(idCategoria, nombreCategoria, descripcionCategoria);

                                        categorias.add(cat);

                                        View vi = inflater.inflate(R.layout.fragment_perfil_fila_categorias, null);
                                        TextView txtTitle = (TextView) vi.findViewById(R.id.txt_nombre_categoria);
                                        txtTitle.setText(cat.getNombre());
                                        contenedorListaCategorias.addView(vi);

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                onConnectionFinished();
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("homeActivity", "entro al error");

                                error.printStackTrace();
                                if(error.networkResponse == null){
                                    if(error.getClass().equals(TimeoutError.class)){
                                        Toast.makeText(getActivity().getApplicationContext(), R.string.login_txt_timeout, Toast.LENGTH_SHORT).show();

                                    }
                                }else {
                                    NetworkResponse respuesta = error.networkResponse;

                                    try {
                                        String jsonErr = new String(respuesta.data);

                                        JSONObject jsonError = new JSONObject(jsonErr);
                                        if (respuesta != null) {
                                            switch (respuesta.statusCode) {
                                                case 400:
                                                    Toast.makeText(getActivity().getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                    onConnectionFinished();
                                                    break;
                                                case 500:
                                                    Toast.makeText(getActivity().getApplicationContext(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
                                                    onConnectionFinished();
                                                    break;
                                                default:
                                                    onConnectionFailed(error.toString());
                                            }
                                        }

                                    } catch (JSONException ex) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Exception: " + ex.getMessage(), Toast.LENGTH_LONG).show();

                                    }
                                }
                            }
                        }
                );

                addToQueue(req, false);
            }
        }
    }

    //http://stackoverflow.com/questions/18378741/how-to-make-an-imageview-in-circular-shape
    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 150;
        int targetHeight = 150;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        Integer i = sourceBitmap.getWidth();
         Log.d("redondeoIMagen", i.toString());
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}
