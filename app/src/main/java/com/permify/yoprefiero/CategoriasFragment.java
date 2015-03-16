package com.permify.yoprefiero;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.permify.yoprefiero.BaseVolley.BaseVolleyFragment;
import com.permify.yoprefiero.models.Contenido;
import com.permify.yoprefiero.InfinitePagerAdapter.view.InfinitePagerAdapter;
import com.permify.yoprefiero.InfinitePagerAdapter.view.InfiniteViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass
 * http://thehayro.blogspot.de/2013/09/infiniteviewpager-infinite-paging.html
 */
public class CategoriasFragment extends BaseVolleyFragment {
    private Integer idCategoria;
    private String nombreCategoria;

    private Integer TOTAL_SLIDES;
    private Integer CURRENT_PAGE;


    private ArrayList<Contenido> filasContenidos;
    public SharedPreferences PREFERENCIAS_SPR;
    private ServiciosRest urlRest;

    InfiniteViewPager viewPager;

    public CategoriasFragment() {
        // Required empty public constructor
    }

    //crea nueva instancia del fragment, pero con argumentos desde HomeActivity
    static CategoriasFragment newInstance(Integer idCategoria, String nombreCategoria) {
        CategoriasFragment fragment = new CategoriasFragment();

        //Agrega argumentos
        Bundle args = new Bundle();
        args.putInt("id_categoria", idCategoria);
        args.putString("nombre_categoria", nombreCategoria);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.idCategoria = getArguments() != null ? getArguments().getInt("id_categoria") : 0;
        this.nombreCategoria = getArguments() != null? getArguments().getString("nombre_categoria") : null;

        PREFERENCIAS_SPR = getActivity().getSharedPreferences("com.permify.yoprefiero", Context.MODE_PRIVATE);
        urlRest = new ServiciosRest();

        filasContenidos = new ArrayList<Contenido>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_categorias, container, false);

        this.obtieneContenidosPorCategoria(v);

        return v;
    }


    /**
     * Funcion consulta API de contenidos
     * JSON contenidos a partir de una categoria
     */
    public void obtieneContenidosPorCategoria(final View v){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu
                String URL = urlRest.getProductosContenidos(idCategoria, apiKey);
                Log.d("catFragment", URL);

                JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("catFragment", "exito");

                                    TOTAL_SLIDES = response.getInt("total");
                                    CURRENT_PAGE = response.getInt("current_page");

                                    JSONArray dataContenidos = response.getJSONArray("data");

                                    for(int i=0; i<dataContenidos.length(); i++){

                                        JSONObject contenidos = dataContenidos.getJSONObject(i);

                                        Contenido objetoContenido = new Contenido();
                                        objetoContenido.setId(contenidos.getInt("id"));
                                        objetoContenido.setTipo(contenidos.getString("tipo"));
                                        objetoContenido.setTitulo(contenidos.getString("titulo"));
                                        objetoContenido.setDescripcion(contenidos.getString("descripcion"));
                                        objetoContenido.setUrlImagen(contenidos.getString("imagen"));
                                        objetoContenido.setPrecio(contenidos.getInt("precio"));
                                        objetoContenido.setAccion(contenidos.getString("accion"));

                                        filasContenidos.add(objetoContenido);

                                    }

                                    //Obtiene numero de contenidos devueltos por la API
                                    //NUMERO_SLIDES = filasContenidos.size();

                                    //final InfiniteViewPager viewPager = (InfiniteViewPager) v.findViewById(R.id.infinite_viewpager);
                                    viewPager = (InfiniteViewPager) v.findViewById(R.id.infinite_viewpager);
                                    viewPager.setAdapter(new MyInfinitePagerAdapter(0));
                                    viewPager.setPageMargin(0);

                                    /*viewPager.setOnInfinitePageChangeListener(new InfiniteViewPager.OnInfinitePageChangeListener() {
                                        @Override
                                        public void onPageScrolled(final Object indicator, final float positionOffset,
                                                                   final int positionOffsetPixels) {
                                            Log.d("InfiniteViewPager", "onPageScrolled ".concat(String.valueOf(indicator)));
                                        }

                                        @Override
                                        public void onPageSelected(final Object indicator) {
                                            Log.d("InfiniteViewPager", "onPageSelected " + indicator.toString());
                                        }

                                        @Override
                                        public void onPageScrollStateChanged(final int state) {
                                            Log.d("InfiniteViewPager", "state " + String.valueOf(state));
                                        }
                                    });*/

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                onConnectionFinished();
                            }
                        },
                        new Response.ErrorListener() {
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
                                                //Toast.makeText(getActivity().getApplicationContext(), jsonError.optString("error"), Toast.LENGTH_LONG).show();
                                                onConnectionFinished();

                                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                Fragment ventanaOpcion = null;
                                                //ventanaOpcion = new SinContenidoFragment();
                                                ventanaOpcion = SinContenidoFragment.newInstance(idCategoria, nombreCategoria);
                                                ft.replace(R.id.main_content, ventanaOpcion);
                                                ft.commit();

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
                        });

                addToQueue(req, true, "Cargando contenido...");
            }
        }
    }

    /**
     * La misma funcion, solo que el formato de la url incluye el argumento "page"
     * y no inicializa el adaptador para el InfiniteViewPager, solo le notifica cambios
     */
    public void obtieneContenidosPorCategoria(int pagina){
        if(PREFERENCIAS_SPR.contains("apiKey") && PREFERENCIAS_SPR.contains("idUsuario")) {
            String apiKey = PREFERENCIAS_SPR.getString("apiKey", null);
            Integer idUsuario = PREFERENCIAS_SPR.getInt("idUsuario", 0);

            if (apiKey != null && idUsuario != 0) {
                //capturar categorias para rellenar menu
                String URL = urlRest.getProductosContenidos(idCategoria, apiKey, pagina);
                Log.d("catFragment", URL);

                JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("catFragment", "exito");

                                    TOTAL_SLIDES = response.getInt("total");
                                    CURRENT_PAGE = response.getInt("current_page");

                                    JSONArray dataContenidos = response.getJSONArray("data");

                                    for(int i=0; i<dataContenidos.length(); i++){

                                        JSONObject contenidos = dataContenidos.getJSONObject(i);

                                        Contenido objetoContenido = new Contenido();
                                        objetoContenido.setId(contenidos.getInt("id"));
                                        objetoContenido.setTipo(contenidos.getString("tipo"));
                                        objetoContenido.setTitulo(contenidos.getString("titulo"));
                                        objetoContenido.setDescripcion(contenidos.getString("descripcion"));
                                        objetoContenido.setUrlImagen(contenidos.getString("imagen"));
                                        objetoContenido.setPrecio(contenidos.getInt("precio"));
                                        objetoContenido.setAccion(contenidos.getString("accion"));

                                        filasContenidos.add(objetoContenido);

                                    }

                                    viewPager.getAdapter().notifyDataSetChanged();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                onConnectionFinished();
                            }
                        },
                        new Response.ErrorListener() {
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
                        });
                addToQueue(req, false);
            }
        }
    }

    /**
     * Clase Adaptador personalizado de InfinitePagerAdapter
     * Scroll infinito a Slides
     */
    private class MyInfinitePagerAdapter extends InfinitePagerAdapter<Integer> {
        private ImageLoader mImageLoader;
        private RequestQueue mRequestQueue;

        /**
         * Standard constructor.
         *
         * @param initValue the initial indicator value the ViewPager should start with.
         */
        public MyInfinitePagerAdapter(final Integer initValue) {
            super(initValue);

            mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
                public void putBitmap(String url, Bitmap bitmap) {
                    mCache.put(url, bitmap);
                }
                public Bitmap getBitmap(String url) {
                    return mCache.get(url);
                }
            });

        }

        @Override
        public ViewGroup instantiateItem(Integer indicator) {
            Log.d("InfiniteViewPager", "instantiating page " + indicator);

            if(filasContenidos.size() > 0 && indicator >= 0 && indicator < filasContenidos.size()){
                if(indicator == (filasContenidos.size() - 1)){
                    //Log.d("InfiniteViewPager", "Deberia ya cargar la sgte pagina "+(CURRENT_PAGE + 1)+" page " + indicator);

                    obtieneContenidosPorCategoria(CURRENT_PAGE + 1);
                }
                final LinearLayout layout = (LinearLayout) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.fragment_categorias_slide, null);

                //Log.d("categoria", indicator.toString());
                final Contenido contenido = filasContenidos.get(indicator);

                TextView txtTituloCategoria = (TextView)layout.findViewById(R.id.txt_titulo_categoria);
                TextView txtTituloContenido = (TextView)layout.findViewById(R.id.txt_titulo_contenido);
                TextView txtDescripcion = (TextView)layout.findViewById(R.id.txt_descripcion_contenido);
                TextView txtPrecio = (TextView)layout.findViewById(R.id.txt_precio_contenido);
                Button btnAccion = (Button)layout.findViewById(R.id.btn_accion);

                //Carga y muestra la imagen asociada al contenido
                NetworkImageView image = (NetworkImageView) layout.findViewById(R.id.imagen_contenido);
                Log.d("cargaImagen", contenido.getUrlImagen());
                image.setImageUrl(contenido.getUrlImagen(), mImageLoader);

                //carga la info
                txtTituloCategoria.setText(String.format("%s", nombreCategoria));
                txtTituloContenido.setText(String.format("%s", contenido.getTitulo()));
                txtDescripcion.setText(String.format("%s", contenido.getDescripcion()));
                txtPrecio.setText(String.format("$%d", contenido.getPrecio()));

                //crea el evento onclick para el boton accion
                btnAccion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //redirige a la url que contiene el origen del contenido
                        Uri uriUrl = Uri.parse(contenido.getAccion());
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                });

                layout.setTag(indicator);
                return layout;
            }

            /*layout vacio*/
            final LinearLayout layout_null = (LinearLayout) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.fragment_categorias_slide_no_content, null);

            TextView txtTituloCategoria = (TextView)layout_null.findViewById(R.id.txt_titulo_categoria);
            txtTituloCategoria.setText(String.format("%s", nombreCategoria));

            Button btnInicioSlide = (Button)layout_null.findViewById(R.id.btn_inicio_slide);
            btnInicioSlide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setCurrentIndicator(0);
                    //viewPager.setCurrentItem(0, true);
                    viewPager.setCurrentIndicator(0);
                }
            });

            layout_null.setTag(indicator);

            return layout_null;

        }

        @Override
        public Integer getNextIndicator() {
            return getCurrentIndicator() + 1;
        }

        @Override
        public Integer getPreviousIndicator() {
            return getCurrentIndicator() - 1;
        }

        @Override
        public String getStringRepresentation(final Integer currentIndicator) {
            return String.valueOf(currentIndicator);
        }

        @Override
        public Integer convertToIndicator(final String representation) {
            return Integer.valueOf(representation);
        }

    }

}
