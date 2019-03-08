package com.developer.serverlessmobilepush;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class ServerlessHelper {

    private final String backendUrl;
    private RequestQueue queue;
    private TimeZone timeZone;
    private static ServerlessHelper instance;


    private ServerlessHelper(String backendUrl, Context context) {
        this.backendUrl = backendUrl;
        this.queue = Volley.newRequestQueue(context);
        this.timeZone = Calendar.getInstance().getTimeZone();
    }

    public static void initialize(String backendUrl, Context context) {
        instance = new ServerlessHelper(backendUrl, context);
    }

    public static ServerlessHelper getInstance() {
        return instance;
    }

    public void createTagAndTrigger(String tag, Double latitude, Double longitude, Boolean isAlertAll, ArrayList<String> weatherTypes, int seekBarValue, final OnCompletion onCompletion) {
        JSONObject params = new JSONObject();
        try {
            params.put("tag", tag.concat("-" + String.valueOf(seekBarValue)));
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            if (isAlertAll != null) {
                params.put("isAlertAll", isAlertAll);
            }
            if (weatherTypes != null) {
                params.put("weather", new JSONArray(weatherTypes));
            }

            params.put("cron", createCronTab(seekBarValue));
            params.put("timezone", timeZone.getID());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, backendUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    onCompletion.onSuccess(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onCompletion.onError(error);
                }
            });
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            onCompletion.onError(null);
        }
    }

    private String createCronTab(int seekBarValue) {
        return "0 " + String.valueOf(17 + seekBarValue) + " * * *";
    }

    interface OnCompletion {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }
}
