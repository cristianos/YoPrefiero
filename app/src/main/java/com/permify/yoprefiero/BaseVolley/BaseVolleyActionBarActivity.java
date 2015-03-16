package com.permify.yoprefiero.BaseVolley;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.permify.yoprefiero.R;

/**
 * https://gpmess.com/blog/2014/05/28/volley-usando-webservices-en-android-de-manera-sencilla#.VMfdOi4nsZN
 */

public class BaseVolleyActionBarActivity extends ActionBarActivity{
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

            /*request.setRetryPolicy(new DefaultRetryPolicy(
                    60000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));*/
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
        this.setSupportProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(this.getString(R.string.msg_cargando_api));
        pDialog.show();
    }

    public void onPreStartConnection(String mensaje) {
        this.setSupportProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(mensaje);
        pDialog.show();
    }

    public void onConnectionFinished() {
        this.setSupportProgressBarIndeterminateVisibility(false);

        pDialog.hide();
    }

    public void onConnectionFailed(String error) {
        this.setSupportProgressBarIndeterminateVisibility(false);

        pDialog.hide();

        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
