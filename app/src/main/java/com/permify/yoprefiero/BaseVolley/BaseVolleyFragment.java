package com.permify.yoprefiero.BaseVolley;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.permify.yoprefiero.R;

public class BaseVolleyFragment extends Fragment {
    private VolleyS volley;
    protected RequestQueue fRequestQueue;
    public ProgressDialog pDialog;

    //vamos a pedir una instancia del Singleton VolleyS que hemos creado
    // y a inicializar la RequestQueue
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volley = VolleyS.getInstance(getActivity().getApplicationContext());
        fRequestQueue = volley.getRequestQueue();

        pDialog = new ProgressDialog(getActivity());
    }

    //Ahora ya podríamos añadir todas nuestras Requests a la cola
    public void addToQueue(Request request, boolean indicadorCargando) {
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

            fRequestQueue.add(request);
        }
    }

    public void addToQueue(Request request, boolean indicadorCargando, String mensajeCargando){
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

            if(indicadorCargando) onPreStartConnection(mensajeCargando);

            fRequestQueue.add(request);
        }
    }

    public void onPreStartConnection() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(this.getString(R.string.msg_cargando_api));
        pDialog.show();
    }

    public void onPreStartConnection(String mensaje){
        getActivity().setProgressBarIndeterminateVisibility(true);

        pDialog.setMessage(mensaje);
        pDialog.show();
    }

    public void onConnectionFinished() {
        getActivity().setProgressBarIndeterminateVisibility(false);

        pDialog.hide();
    }

    public void onConnectionFailed(String error) {
        getActivity().setProgressBarIndeterminateVisibility(false);

        pDialog.hide();

        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }
}
