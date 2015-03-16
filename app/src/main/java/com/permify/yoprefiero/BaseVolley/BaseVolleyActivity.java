package com.permify.yoprefiero.BaseVolley;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.permify.yoprefiero.R;


public class BaseVolleyActivity extends Activity {

    private VolleyS volley;
    protected RequestQueue fRequestQueue;
    public ProgressDialog pDialog;

    //vamos a pedir una instancia del Singleton VolleyS que hemos creado
    // y a inicializar la RequestQueue
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volley = VolleyS.getInstance(this.getApplicationContext());
        fRequestQueue = volley.getRequestQueue();

        pDialog = new ProgressDialog(this);
    }

    //Ahora ya podríamos añadir todas nuestras Requests a la cola
    public void addToQueue(Request request, Boolean indicadorCargando) {
        if (request != null) {
            request.setTag(this);

            if (fRequestQueue == null)
                fRequestQueue = volley.getRequestQueue();

            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if(indicadorCargando) onPreStartConnection();

            //onPreStartConnection();
            fRequestQueue.add(request);
        }
    }

    public void addToQueue(Request request, Boolean indicadorCargando, String mensaje) {
        if (request != null) {
            request.setTag(this);

            if (fRequestQueue == null)
                fRequestQueue = volley.getRequestQueue();

            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            if(indicadorCargando) onPreStartConnection(mensaje);

            //onPreStartConnection();
            fRequestQueue.add(request);
        }
    }

    public void onPreStartConnection() {
        this.setProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(this.getString(R.string.msg_cargando_api));
        pDialog.show();
    }

    public void onPreStartConnection(String mensaje) {
        this.setProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(mensaje);
        pDialog.show();
    }

    public void onConnectionFinished() {
        this.setProgressBarIndeterminateVisibility(false);

        pDialog.hide();
    }

    public void onConnectionFailed(String error) {
        this.setProgressBarIndeterminateVisibility(false);

        pDialog.hide();

        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
