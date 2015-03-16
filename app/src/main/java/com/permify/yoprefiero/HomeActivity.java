package com.permify.yoprefiero;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.permify.yoprefiero.BaseVolley.BaseVolleyActionBarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class HomeActivity extends BaseVolleyActionBarActivity {
    private ServiciosRest urlRest;
    public SharedPreferences PREFERENCIAS_SPR;
    public RelativeLayout mainContent;

    //DRAWERLAYOUT
    private DrawerLayout drawerLayout;
    private ListView contenedorMenuPrincipal;
    private ActionBarDrawerToggle toggleMenu;
    public ArrayList<OpcionesMenu> opcionesMenu;
    private Integer numeroOpcionesFijas;
    private MenuListAdapter adaptadorMenu;

    private ImageView manoIndicadora;
    private ImageView textoBienvenida;

    private Map<String,String> mMap;

    private static final int DESDE_CAMARA = 1;
    private static final int DESDE_GALERIA = 2;
    private static final int DESDE_CROP = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        manoIndicadora = (ImageView)findViewById(R.id.imageMano);
        textoBienvenida = (ImageView)findViewById(R.id.imageTextoBienvenida);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mainContent = (RelativeLayout)findViewById(R.id.main_content);

        opcionesMenu = new ArrayList<OpcionesMenu>();

        PREFERENCIAS_SPR = getSharedPreferences("com.permify.yoprefiero", Context.MODE_PRIVATE);

        this.preparaMenuLateral();
    }


    private void preparaMenuLateral(){
        //PREPARACION MENU DRAWERLAYOUT
        //prepara opciones del menu

        opcionesMenu.add(new OpcionesMenu(R.drawable.ic_perfil, getResources().getString(R.string.menu_home_perfil), 0));
        opcionesMenu.add(new OpcionesMenu(R.drawable.ic_nueva, getResources().getString(R.string.menu_home_agregar_categoria), 0));
        opcionesMenu.add(new OpcionesMenu(R.drawable.ic_categoria, getResources().getString(R.string.menu_home_categoria), 0));

        //Numero de opciones fijas del menu (Para recargar el menu)
        numeroOpcionesFijas = opcionesMenu.size();

        //obtiene acceso al drawer y al listView que contiene el menu
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        contenedorMenuPrincipal = (ListView) findViewById(R.id.drawer_list);

        //Anula gesto swipe para abrir el menu
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //crea y asigna adaptador para rellenar opciones del menu
        adaptadorMenu = new MenuListAdapter(this, opcionesMenu);

        contenedorMenuPrincipal.setAdapter(adaptadorMenu);

        //carga menu con categorias desde api REST
        this.creaOpcionesDeCategorias();

        //crea evento on click para los elementos seleccionados del menu
        contenedorMenuPrincipal.setOnItemClickListener(new DrawerItemClickListener());

        //configura boton "hamburguesa" del menu
        toggleMenu = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_action_navigation_menu,
                R.string.drawer_open_menu,
                R.string.drawer_close_menu
        );

        //muestra menu en layout

        ActionBar barraMenu = getSupportActionBar();

        //barraMenu.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //barraMenu.setCustomView(R.layout.activity_home_menu);
        barraMenu.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0e1b22")));
        barraMenu.setDisplayShowTitleEnabled(false);

        ImageView logo = (ImageView) findViewById(android.R.id.home);
        logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_yp_app));

        //habilitar boton atras
        barraMenu.setDisplayHomeAsUpEnabled(true);
        barraMenu.setHomeButtonEnabled(true);

    }

    protected void creaOpcionesDeCategorias(){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if(apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu
                urlRest = new ServiciosRest();

                String URL = urlRest.getUsuariosProductos(idUsuario, apiKey);
                Log.d("homeActivity", URL);

                JsonArrayRequest req = new JsonArrayRequest(URL,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("homeActivity", "entro al exito -> carga menu");

                                try {
                                    for(int i=0; i<response.length(); i++){
                                        JSONObject categoria = response.getJSONObject(i);
                                        Log.d("homeActivity", categoria.getString("nombre"));

                                        int idCategoria = categoria.getInt("id");
                                        String nombreCategoria = categoria.getString("nombre");

                                        OpcionesMenu opcion = new OpcionesMenu(0, nombreCategoria, idCategoria);

                                        opcionesMenu.add(opcion);
                                    }

                                    adaptadorMenu.notifyDataSetChanged();

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
                                NetworkResponse respuesta = error.networkResponse;

                                try{
                                    String jsonErr = new String(respuesta.data);

                                    JSONObject jsonError = new JSONObject(jsonErr);
                                    if(respuesta != null){
                                        switch (respuesta.statusCode) {
                                            case 400:
                                                //Toast.makeText(getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                Log.d("HomeActivity", jsonError.optString("error"));
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

                                }catch(JSONException ex){
                                    Toast.makeText(HomeActivity.this, "Exception: "+ex.getMessage(),Toast.LENGTH_LONG).show();

                                }catch (NullPointerException ex){
                                    Toast.makeText(HomeActivity.this, "Exception: "+ex.getMessage(),Toast.LENGTH_LONG).show();

                                }
                            }
                        }
                );

                addToQueue(req, true);
            }
        }
    }

    public void actualizaOpcionesMenu(){
        //Toast.makeText(this, "Aca es cuando se actualiza el menu", Toast.LENGTH_SHORT).show();

        int i = 0;
        Iterator<OpcionesMenu> iterador = opcionesMenu.iterator();
        while (iterador.hasNext()){
            iterador.next();

            if(i >= (int)numeroOpcionesFijas){

                iterador.remove();
            }
            i++;
        }
        adaptadorMenu.notifyDataSetChanged();

        this.creaOpcionesDeCategorias();
    }

    //DRAWERLAYOUT
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggleMenu.syncState();
    }

    //DRAWERLAYOUT
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleMenu.onConfigurationChanged(newConfig);
    }

    //DRAWERLAYOUT
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(toggleMenu.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Funcion para subir una foto de perfil (No se pudo usar directamente en el fragment)
    public void onClickCambiarImagen(View v){
        final CharSequence[] options = {"Tomar fotografía", "Elegir de la galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar foto perfil");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar fotografía")){
                    //codigo que llama a la camara

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                    startActivityForResult(intent, DESDE_CAMARA);


                }else if (options[item].equals("Elegir de la galería")){
                    //Codigo que abre la galeria
                    /*Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 150);
                    intent.putExtra("outputY", 150);

                    try {

                        intent.putExtra("return-data", true);
                        startActivityForResult(Intent.createChooser(intent,
                                "Complete action using"), DESDE_GALERIA);

                    } catch (ActivityNotFoundException e) {
                        String errorMessage = "Whoops - your device doesn't support the crop action!";
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();

                    }*/

                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Completar la acción usando"), DESDE_GALERIA);

                }else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();

                }
            }
        });

        builder.show();
    }

    //Funcion que procesa la imagen devuelta por cualquiera de los INTENT anteriores

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //File croppedImageFile = new File(getFilesDir(), "test.jpg");
        File croppedImageFile = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");

        if (resultCode == RESULT_OK){

            if (requestCode == DESDE_CAMARA) {
                //INTENT nueva fotografia
                this.procesaImagenNueva(croppedImageFile);

            } else if (requestCode == DESDE_GALERIA) {
                //INTENT foto desde galeria
                this.procesaImagenDesdeGaleria(data, croppedImageFile);

            }else if(requestCode == DESDE_CROP){
                //INTENT crop image
                this.procesaRecorteImagen(croppedImageFile);
            }

        }
    }

    //SUBIR IMAGEN - RUTINAS
    //procesa nuevas imagenes desde la camara
    private void procesaImagenNueva(File archivo){

        try {
            // When the user is done picking a picture, let's start the CropImage Activity,
            // setting the output image file and size to 150x150 pixels square.
            Uri croppedImage = Uri.fromFile(archivo);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(150, 150, croppedImage);
            //cropImage.setSourceImage(data.getData());
            cropImage.setSourceImage(croppedImage);

            startActivityForResult(cropImage.getIntent(this), DESDE_CROP);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"excepcion4: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void procesaImagenNueva_old(Intent data){
        ImageView viewImage = (ImageView)findViewById(R.id.img_usuario);

        File f = new File(Environment.getExternalStorageDirectory().toString());

        for (File temp : f.listFiles()) {
            if (temp.getName().equals("temp.jpg")) {
                f = temp;
                break;
            }
        }

        try {
            Bitmap bitmap;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);

            f.delete();

            if((bitmap.getHeight() > 2448) || (bitmap.getWidth() > 3264)){
                Toast.makeText(getApplicationContext(),"Por favor reduzca la resolución la camara ( < 6mpx )", Toast.LENGTH_LONG).show();

            }else{

                String path = android.os.Environment.getExternalStorageDirectory().toString();

                OutputStream outFile;

                try {
                    //si el ancho es mayor que el ancho es porque esta la imagen de forma horizontal
                    if (bitmap.getHeight() < bitmap.getWidth()) {
                        bitmap = this.rotateImage(bitmap, 90);
                    }

                    bitmap = this.resize(bitmap, (float)200, (float)300);

                    //viewImage.setImageBitmap(bitmap);
                    viewImage.setImageBitmap(PerfilFragment.getRoundedShape(bitmap));
                    String imagenBase64 = this.getBase64(bitmap);
                    this.subirImagen(imagenBase64);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "excepcion3 " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"excepcion4 "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    //procesa imagen seleccionada desde la galeria del telefono
    private void procesaImagenDesdeGaleria(Intent data, File croppedImageFile){
        // When the user is done picking a picture, let's start the CropImage Activity,
        // setting the output image file and size to 200x200 pixels square.
        Uri croppedImage = Uri.fromFile(croppedImageFile);

        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(150, 150, croppedImage);
        cropImage.setSourceImage(data.getData());

        startActivityForResult(cropImage.getIntent(this), DESDE_CROP);
    }

    private void procesaImagenDesdeGaleria_old2(Intent data){
        ImageView viewImage = (ImageView) findViewById(R.id.img_usuario);
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        if((photo.getWidth() > 200 ) || (photo.getHeight() > 300 )){
            Toast.makeText(this.getApplicationContext(), "Debe utilizar imágenes maximo de 300 x 200 (alto x ancho o 8 mm)", Toast.LENGTH_LONG).show();

        }else{
            //viewImage.setImageBitmap(photo);
            viewImage.setImageBitmap(PerfilFragment.getRoundedShape(photo));
            String imagenBase64 = this.getBase64(photo);
            this.subirImagen(imagenBase64);
        }
    }

    //guardar por si la primera falla
    private void procesaImagenDesdeGaleria_old(Intent data) {
        ImageView viewImage = (ImageView) findViewById(R.id.img_usuario);

        Uri selectedImage = data.getData();
        String[] filePath = { MediaStore.Images.Media.DATA };

        Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
        c.moveToFirst();

        int columnIndex = c.getColumnIndex(filePath[0]);
        String picturePath = c.getString(columnIndex);
        c.close();

        Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));

        if((thumbnail.getWidth() > 200 ) || (thumbnail.getHeight() > 300 )){
            Toast.makeText(this.getApplicationContext(), "Debe utilizar imagenes maximo de 300 x 200 (alto x ancho o 8 mm)", Toast.LENGTH_LONG).show();

        }else {
            viewImage.setImageBitmap(thumbnail);
            String imagenBase64 = this.getBase64(thumbnail);
            this.subirImagen(imagenBase64);
        }
    }

    private void procesaRecorteImagen(File archivo){
        Bitmap imagen = BitmapFactory.decodeFile(archivo.getAbsolutePath());

        ImageView viewImage = (ImageView)findViewById(R.id.img_usuario);

        viewImage.setImageBitmap(PerfilFragment.getRoundedShape(imagen));
        String imagenBase64 = this.getBase64(imagen);
        this.subirImagen(imagenBase64);
    }

    //Cambia tamaño de fotografia
    public Bitmap resize(Bitmap bitmap, float newHeight, float newWidth){
        //sacamos el tamaño original
        int alto = bitmap.getHeight();
        int ancho = bitmap.getWidth();

        float scaleWidth = newWidth / alto;
        float scaleHeight = newHeight / ancho;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleHeight, scaleWidth);
        Bitmap resizeBitmap;
        //resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);
        resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, true);

        return resizeBitmap;
    }

    //rota imagen dependiendo de los grados indicados
    private Bitmap rotateImage(Bitmap bitmap, int grados){
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(grados);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    //Transforma una imagen en notacion base64
    private String getBase64(Bitmap bitmap){
        // ******************    BASE 64    *******************************
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    //Request para subir imagen en formato base64 a API REST
    public void subirImagen(String imagenBase64) {
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("api_key", apiKey);
                params.put("imagen", imagenBase64);

                String URL = urlRest.postUsuarioPerfilImagen(idUsuario);

                //crea nuevo request
                JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    if (response.has("error")) {
                                        //viene con respuesta
                                        if(!response.getBoolean("error")){
                                            //upload imagen exitosa
                                            Toast.makeText(getApplicationContext(), R.string.login_cambio_imagen, Toast.LENGTH_SHORT).show();
                                            //Log.d("foto", response.getString("base64"));

                                        } else {
                                            //error desde el servidor
                                            String error = response.getString("error");
                                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                        onConnectionFinished();

                                    }else{
                                        //no vienen datos en el response
                                        Toast.makeText(getApplicationContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                                        onConnectionFinished();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
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
                                                    Toast.makeText(getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                    onConnectionFinished();
                                                    break;
                                                case 500:
                                                    Toast.makeText(getApplicationContext().getApplicationContext(), R.string.msg_login_error_500, Toast.LENGTH_LONG).show();
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
                addToQueue(req, true, "Subiendo foto...");
            }
        }
    }

    //CLASES AUXILIARES
    ////DRAWERLAYOUT: escucha cuando el usuario hace click en un elemento del menu
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Carga ventanas (fragments) de las opciones en el layout activity_home.xml

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment ventanaOpcion = null;

            switch (position){
                case 0:
                    //Opcion Perfil
                    ventanaOpcion = new PerfilFragment();
                    break;
                case 1:
                    //Opcion Agregar Categoria
                    ventanaOpcion = new AgregarCategoriaFragment();
                    break;
                case 2:
                    //Opcion Categorias (Titulo) - no hace nada
                    int sgteOpcion = position + 1;
                    if(sgteOpcion < opcionesMenu.size()){
                        Integer i = sgteOpcion;
                        Log.d("sgteOpcion", i.toString());
                        Integer idOpcion = opcionesMenu.get(sgteOpcion).getIdOpcion();
                        String nombreOpcion = opcionesMenu.get(sgteOpcion).getTitulo();

                        ventanaOpcion = CategoriasFragment.newInstance(idOpcion, nombreOpcion);
                    }else{
                        Toast.makeText(getApplicationContext(), "Seleccione una categoría para continuar", Toast.LENGTH_SHORT).show();
                    }

                    break;
                default:
                    //Categorias cargadas dinamicamente
                    Integer idOpcion = opcionesMenu.get(position).getIdOpcion();
                    String nombreOpcion = opcionesMenu.get(position).getTitulo();

                    ventanaOpcion = CategoriasFragment.newInstance(idOpcion, nombreOpcion);

                    break;
            }

            if(ventanaOpcion != null){
                //borra imagenes de bienvenida antes de inflar fragment
                manoIndicadora.setVisibility(View.GONE);
                textoBienvenida.setVisibility(View.GONE);

                ft.replace(R.id.main_content, ventanaOpcion);
                ft.commit();

                drawerLayout.closeDrawers();
            }

        }
    }

    //DrawerLayout: Clase que define elementos del menu
    public class OpcionesMenu{
        private String titulo;
        private Integer icono;
        private Integer idOpcion;

        public OpcionesMenu(Integer icono, String titulo, Integer idOpcion){
            this.icono = icono;
            this.titulo = titulo;
            this.idOpcion = idOpcion;
        }

        public Integer getIdOpcion() {
            return idOpcion;
        }

        public void setIdOpcion(Integer idOpcion) {
            this.idOpcion = idOpcion;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public int getIcono() {
            return icono;
        }

        public void setIcono(int icono) {
            this.icono = icono;
        }
    }

    //DrawerLayout: Clase adapter para el menu
    public class MenuListAdapter extends ArrayAdapter<OpcionesMenu> {

        private final Activity context;
        //private final OpcionesMenu[] opcionesMenu;
        private final ArrayList<OpcionesMenu> opcionesMenu;

        //public MenuListAdapter(Activity context, OpcionesMenu[] opcionesMenu){
        public MenuListAdapter(Activity context, ArrayList<OpcionesMenu> opcionesMenu){
            super(context, R.layout.activity_home_formato_menu, opcionesMenu);
            this.context = context;
            this.opcionesMenu = opcionesMenu;
        }

        public View getView(int position,View view,ViewGroup parent) {

            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.activity_home_formato_menu, null, true);

            TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

            txtTitle.setText(opcionesMenu.get(position).getTitulo());

            if(opcionesMenu.get(position).getIcono() == 0){
                imageView.setImageBitmap(null);
            }else{

                imageView.setImageResource(opcionesMenu.get(position).getIcono());
            }

            return rowView;
        }
    }
}
