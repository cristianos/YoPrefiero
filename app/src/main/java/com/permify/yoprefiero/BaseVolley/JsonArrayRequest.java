package com.permify.yoprefiero.BaseVolley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 *  Solucion para realizar una peticion POST y esperar un Array de respuesta por JSON
 *  http://stackoverflow.com/questions/18048806/volley-sending-a-post-request-using-jsonarrayrequest
 */
public class JsonArrayRequest extends JsonRequest<JSONArray> {

    public JsonArrayRequest(int method, String url, JSONObject jsonRequest,
                            Response.Listener<JSONArray> listener,
                            Response.ErrorListener errorListener) {

        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(new JSONArray(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
