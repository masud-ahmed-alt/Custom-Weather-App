package com.arduino.weatherreport;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Networking {

    private String resp;

    public String get(Context context, String url){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> resp = response, error -> resp = error.getMessage());
        queue.add(request);
        return resp;
    }
}
