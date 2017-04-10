package com.iot.guc.jarvis.requests;

/**
 * Created by MariamMazen on 2017-04-01.
 */



import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class CustomJsonRequest extends JsonArrayRequest {

    Map<String, String> params;
    private Response.Listener listener;

    public CustomJsonRequest(int requestMethod, String url, JSONArray params,
                             Response.Listener responseListener, Response.ErrorListener errorListener) {

        super(requestMethod, url, params, responseListener,errorListener);
        this.listener = responseListener;
    }


    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

}
