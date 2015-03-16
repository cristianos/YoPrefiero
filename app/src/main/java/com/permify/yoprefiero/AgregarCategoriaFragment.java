package com.permify.yoprefiero;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * http://danielme.com/2013/10/09/diseno-android-listview-con-checkbox/
 */
public class AgregarCategoriaFragment extends BaseVolleyFragment {
    List<RowCategoria> rows ;
    private ArrayList<RowCategoria> todasLasCategorias;
    public SharedPreferences PREFERENCIAS_SPR;
    private ServiciosRest urlRest;
    private ListView lista;

    public AgregarCategoriaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_agregar_categoria, container, false);

        urlRest = new ServiciosRest();

        PREFERENCIAS_SPR = getActivity().getSharedPreferences("com.permify.yoprefiero", Context.MODE_PRIVATE);

        todasLasCategorias = new ArrayList<RowCategoria>();

        lista = (ListView)layout.findViewById(R.id.lista_checkbox_listview);

        final ListaCheckArrayAdapter listaCheckArrayAdapter = new ListaCheckArrayAdapter(getActivity(), todasLasCategorias);
        lista.setAdapter(listaCheckArrayAdapter);

        Log.d("AgregarCategoria", todasLasCategorias.toString());
        this.cargaTodasLasCategorias(listaCheckArrayAdapter);
        Log.d("AgregarCategoria1", todasLasCategorias.toString());

        //carga lista con todas las categorias disponibles

        //al hacer click en una fila
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Click en la fila de ListView
                //El evento para manejar la seleccion por el checkbox se encuentra en el adaptador

                //registra la seleccion
                todasLasCategorias.get(position).toggleChecked();

                //propaga hacia el evento change del checkbox
                Holder h = (Holder)view.getTag();
                h.getCheckBox().setChecked(todasLasCategorias.get(position).isChecked());

            }
        });

        return layout;
    }

    //funcion que carga todas las categorias registradas en la api REST
    private void cargaTodasLasCategorias(final ListaCheckArrayAdapter listaCheckArrayAdapter){

        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu

                String URL = urlRest.getProductos(apiKey);

                JsonArrayRequest req = new JsonArrayRequest(URL,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d("catFragment", "exito");

                                    for(int i=0; i<response.length(); i++){

                                        JSONObject categoria = response.getJSONObject(i);

                                        RowCategoria opcion = new RowCategoria();
                                        opcion.setId(categoria.getInt("id"));
                                        opcion.setTitle(categoria.getString("nombre"));
                                        opcion.setSubtitle(categoria.getString("descripcion"));

                                        todasLasCategorias.add(opcion);

                                        //Log.d("AgregarCategoria_", todasLasCategorias.toString());

                                    }

                                    //listaCheckArrayAdapter.notifyDataSetChanged();

                                    //funcion que selecciona categorias registradas con anterioridad
                                    seleccionaCategoriasUsuarioEnLista(listaCheckArrayAdapter);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //onConnectionFinished();
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("catFragment", "fracaso");
                                error.printStackTrace();
                                NetworkResponse respuesta = error.networkResponse;

                                try{
                                    String jsonErr = new String(respuesta.data);

                                    JSONObject jsonError = new JSONObject(jsonErr);
                                    if(respuesta != null){
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

                                }catch(JSONException ex){
                                    Toast.makeText(getActivity().getApplicationContext(), "Exception: "+ex.getMessage(),Toast.LENGTH_LONG).show();

                                }
                            }
                        }
                );

                addToQueue(req, true);
            }
        }
    }

    //funcion que selecciona las categorias que ya estan asociadas al usuario
    public void seleccionaCategoriasUsuarioEnLista(final ListaCheckArrayAdapter listaCheckArrayAdapter){

        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if(apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu

                String URL = urlRest.getUsuariosProductos(idUsuario, apiKey);

                JsonArrayRequest req = new JsonArrayRequest(URL,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("homeActivity", "entro al exito");

                                try {
                                    for(int i=0; i<response.length(); i++){
                                        JSONObject resp_categoria = response.getJSONObject(i);

                                        int idCategoria = resp_categoria.getInt("id");

                                        for (RowCategoria categoria : todasLasCategorias) {
                                            if(idCategoria == categoria.getId()){
                                                categoria.setChecked(true);
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                listaCheckArrayAdapter.notifyDataSetChanged();
                                onConnectionFinished();
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("homeActivity", "entro al error");

                                error.printStackTrace();
                                NetworkResponse respuesta = error.networkResponse;

                                try{
                                    String jsonErr = new String(respuesta.data);

                                    JSONObject jsonError = new JSONObject(jsonErr);
                                    if(respuesta != null){
                                        switch (respuesta.statusCode) {
                                            case 400:
                                                //Toast.makeText(getActivity().getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                Log.d("AgregarCategoriaFragmen", jsonError.optString("error"));
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

                                }catch(JSONException ex){
                                    Toast.makeText(getActivity().getApplicationContext(), "Exception: "+ex.getMessage(),Toast.LENGTH_LONG).show();

                                }

                                listaCheckArrayAdapter.notifyDataSetChanged();

                            }
                        }
                );

                addToQueue(req, true);
            }
        }
    }

    //llama recursos REST para crear o destruir la relacion usuario-categoria
    private void toggleSeleccionCategoria(RowCategoria seleccion){
        if(seleccion.isChecked()){
           //selecciono elemento
            this.creaAsociacionUsuarioCategoria(seleccion);
        }else{
            //no selecciono elemento
            this.destruyeAsociacionUsuarioCategoria(seleccion);

        }
    }

    private void creaAsociacionUsuarioCategoria(RowCategoria seleccion){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                Log.d("dentro", "oka");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("api_key", apiKey);
                params.put("id_producto", seleccion.getId().toString());

                String URL = urlRest.postUsuariosProductos(idUsuario);

                //crea nuevo request
                JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    if (response.has("error")) {
                                        //viene con respuesta
                                        if(!response.getBoolean("error")){
                                            //Asociacion exitosa
                                            Toast.makeText(getActivity().getApplicationContext(), R.string.msg_ok_add_categoria, Toast.LENGTH_SHORT).show();

                                            //LLAMA A METODO DE ACTIVITY
                                            //recarga menu
                                            ((HomeActivity)getActivity()).actualizaOpcionesMenu();
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
                addToQueue(req, true);
            }
        }

    }

    private void destruyeAsociacionUsuarioCategoria(RowCategoria seleccion){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                Log.d("dentro eliminar", "oka");

                //String URL = urlRest.deleteUsuariosProductos(idUsuario, seleccion.getId(), apiKey);
                String URL = urlRest.deleteUsuariosProductos2(idUsuario, seleccion.getId(), apiKey);
                Log.d("quitarCategoria", apiKey);
                Log.d("quitarCategoria", URL);
                /*JsonObjectRequest solicitud = new JsonObjectRequest(
                    Request.Method.DELETE,
                    URL,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.has("error")) {
                                    //viene con respuesta
                                    if(!response.getBoolean("error")){
                                        //Asociacion exitosa
                                        Toast.makeText(getActivity().getApplicationContext(), R.string.msg_ok_del_categoria, Toast.LENGTH_SHORT).show();

                                        //LLAMA A METODO DE ACTIVITY
                                        //recarga menu
                                        ((HomeActivity)getActivity()).actualizaOpcionesMenu();
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
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("borrar categoria", "errror " + error.toString());
                            NetworkResponse respuesta = error.networkResponse;

                            if(respuesta != null){
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
                );*/

                JsonObjectRequest solicitud = new JsonObjectRequest(
                        Request.Method.GET,
                        URL,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.has("error")) {
                                        //viene con respuesta
                                        if(!response.getBoolean("error")){
                                            //Asociacion exitosa
                                            Toast.makeText(getActivity().getApplicationContext(), R.string.msg_ok_del_categoria, Toast.LENGTH_SHORT).show();

                                            //LLAMA A METODO DE ACTIVITY
                                            //recarga menu
                                            ((HomeActivity)getActivity()).actualizaOpcionesMenu();
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
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("borrar categoria", "errror " + error.toString());
                        NetworkResponse respuesta = error.networkResponse;

                        if(respuesta != null){
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
                );
                //agrega a la cola y ejecuta request
                addToQueue(solicitud, true);
            }
        }
    }

    //Clase Row que define cada categoria en la lista
    public class RowCategoria{
        private Integer id;
        private String title;
        private String subtitle;
        private boolean checked;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTitle(){
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getSubtitle()
        {
            return subtitle;
        }

        public void setSubtitle(String subtitle)
        {
            this.subtitle = subtitle;
        }

        public boolean isChecked()
        {
            return checked;
        }

        public void setChecked(boolean checked)
        {
            this.checked = checked;
        }

        public void toggleChecked()
        {
            checked = !checked;
        }

    }

    //Adapter para la lista de opciones
    public class ListaCheckArrayAdapter extends ArrayAdapter<RowCategoria>{
        private LayoutInflater layoutInflater;

        public ListaCheckArrayAdapter(Context context, ArrayList<RowCategoria> objects){
            super(context, 0, objects);
            //super(context, R.layout.fragment_agregar_categoria_formato_fila, objects);
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            // holder pattern

            Holder holder = null;

            if (convertView == null){
                holder = new Holder();

                convertView = layoutInflater.inflate(R.layout.fragment_agregar_categoria_formato_fila, null);

                holder.setTextViewTitle((TextView) convertView.findViewById(R.id.textViewTitle));
                holder.setTextViewSubtitle((TextView) convertView.findViewById(R.id.textViewSubtitle));
                holder.setCheckBox((CheckBox) convertView.findViewById(R.id.checkBox));

                if (position % 2 == 0){
                    convertView.setBackgroundResource(R.drawable.listview_fila_1);
                } else {
                    convertView.setBackgroundResource(R.drawable.listview_fila_2);
                }

                convertView.setTag(holder);

            }else{
                holder = (Holder) convertView.getTag();
            }

            final RowCategoria row = getItem(position);
            final View fila= convertView;

            holder.getTextViewTitle().setText(row.getTitle());
            holder.getTextViewSubtitle().setText(row.getSubtitle());
            holder.getCheckBox().setTag(row.getTitle());
            holder.getCheckBox().setChecked(row.isChecked());
            //holder.getCheckBox().setBackgroundColor(getResources().getColor(R.color.amarillo_prefiero));

            //changeBackground(getContext(), convertView, row.isChecked());

            holder.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean isChecked)
                {
                    //asegura que se modifica la Row originalmente asociado a este checkbox
                    //para evitar que al reciclar la vista se reinicie el row que antes se mostraba en esta
                    //fila. Es imprescindible tagear el Row antes de establecer el valor del checkbox
                    if (row.getTitle().equals(view.getTag().toString()))
                    {
                        row.setChecked(isChecked);

                        //pinta seleccion
                        //changeBackground(ListaCheckArrayAdapter.this.getContext(), fila, isChecked);

                        //Toast.makeText(getActivity(), row.getTitle()+" check", Toast.LENGTH_SHORT).show();

                        //llama funcion para actualizar seleccion en API REST
                        //se llama desde el evento click del checkbox, por el orden en la
                        // ejecucion de los eventos:
                        //click en fila listview: ListView Click -> CheckBox Click -> Funcion seleccion categoria
                        //click en checkbox: CheckBox Click -> funcion seleccion categoria

                        Log.d("registra", "categoria agregada");
                        toggleSeleccionCategoria(row);

                    }
                }
            });

            return convertView;
        }

        /**
         * Set the background of a row based on the value of its checkbox value. Checkbox has its own style.
         */
        @SuppressLint("NewApi")
        @SuppressWarnings("deprecation")
        private void changeBackground(Context context, View row, boolean checked)
        {
            if (row != null)
            {
                Drawable drawable = context.getResources().getDrawable(R.drawable.listview_selector_checked);
                if (checked)
                {
                    drawable = context.getResources().getDrawable(R.drawable.listview_selector_checked);
                }
                else
                {
                    drawable = context.getResources().getDrawable(R.drawable.listview_selector);
                }
                int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    row.setBackgroundDrawable(drawable);
                } else {
                    row.setBackground(drawable);
                }
            }
        }
    }

    //clase que soluciona problema de recuperacion de layouts al hacer scroll en una ListView
    private class Holder{
        TextView textViewTitle;
        TextView textViewSubtitle;
        CheckBox checkBox;

        public TextView getTextViewTitle()
        {
            return textViewTitle;
        }

        public void setTextViewTitle(TextView textViewTitle)
        {
            this.textViewTitle = textViewTitle;
        }

        public TextView getTextViewSubtitle()
        {
            return textViewSubtitle;
        }

        public void setTextViewSubtitle(TextView textViewSubtitle)
        {
            this.textViewSubtitle = textViewSubtitle;
        }

        public CheckBox getCheckBox()
        {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox)
        {
            this.checkBox = checkBox;
        }
    }

}
