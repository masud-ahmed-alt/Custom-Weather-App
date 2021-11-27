package com.arduino.weatherreport;

import static android.content.Context.LOCATION_SERVICE;
import static com.arduino.weatherreport.Constant.APIKEY;
import static com.arduino.weatherreport.Constant.BASE_URL;
import static java.lang.Integer.parseInt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class HomeScreen extends AppWidgetProvider {

    private static SharedPreferences preferences;
    private static double lon = 91.736237;
    private static double lat = 26.144518;
    private static LocationManager locationManager;
    private static Location locationV;
    private static String url_main, TAG = "TAG";
    private static RemoteViews remoteViews;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        preferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.home_screen);
            updateUI(context,remoteViews);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

    }

    @Override
    public void onEnabled(Context context) {
        remoteViews = new RemoteViews(context.getPackageName(),R.layout.home_screen);
        updateUI(context,remoteViews);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName watchWidget = new ComponentName(context, HomeScreen.class);
        manager.updateAppWidget(watchWidget,remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        super.onReceive(context, intent);

        remoteViews = new RemoteViews(context.getPackageName(),R.layout.home_screen);
        updateUI(context,remoteViews);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName watchWidget = new ComponentName(context, HomeScreen.class);
        manager.updateAppWidget(watchWidget,remoteViews);

    }

    private static void updateUI(Context context, RemoteViews remoteViews2) {
        if (locationV != null) {
            lat = locationV.getLatitude();
            lon = locationV.getLongitude();
        } else {
            getCurrentLocation(context);
        }
        url_main = BASE_URL + "forecast.json?key=" + APIKEY + "&q=" + lat + "," + lon + "&aqi=yes";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, url_main, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject locationObject = jsonObject.getJSONObject("location");
                remoteViews2.setTextViewText(R.id.city, locationObject.getString("name") + " , " + locationObject.getString("region"));
                remoteViews2.setTextViewText(R.id.country, locationObject.getString("country"));
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject forecast = jsonObject.getJSONObject("forecast");
                JSONArray forecastArray = forecast.getJSONArray("forecastday");
                JSONObject obj = forecastArray.getJSONObject(0);
                JSONObject times = obj.getJSONObject("astro");
                JSONArray hour = obj.getJSONArray("hour");
                long[] allTimes = new long[hour.length()];
                JSONObject[] conditions = new JSONObject[hour.length()];
                for (int i = 0; i < hour.length(); i++) {

                    JSONObject timeObj = hour.getJSONObject(i);
                    long time_epoch = timeObj.getLong("time_epoch");
                    //long time_epoch_1 = timeObj_1.getLong("time_epoch");
                    long date = parseInt(String.valueOf(new Date().getTime() / 1000));

                    allTimes[i] = date - time_epoch;
                    conditions[i] = hour.getJSONObject(i);


                }

                for (int i = 0; i < hour.length(); i++) {
                    if (allTimes[i] < 0) {
                        allTimes[i] = allTimes[i] * (-1);
                    }

                }
                int index = 0;
                long min = allTimes[index];

                for (int j = 1; j < allTimes.length; j++) {
                    if (allTimes[j] <= min) {
                        min = allTimes[j];
                        index = j;
                    }
                }

                remoteViews2.setTextViewText(R.id.status, "conditions[index].getJSONObject(condition).getString(text)");
                Log.e(TAG, "updateUI: "+conditions[index].getJSONObject("condition").getString("text"));
                remoteViews2.setTextViewText(R.id.status, current.getInt("humidity") + "%");
                remoteViews2.setTextViewText(R.id.uvindex, current.getInt("uv") + " of 10");
                remoteViews2.setTextViewText(R.id.sunriseTime, times.getString("sunrise"));
                remoteViews2.setTextViewText(R.id.sunsetTime, times.getString("sunset"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, error -> {
            Log.d(TAG, "updateUI: " + error.getMessage());
        });
        queue.add(request);
    }

    @SuppressLint("MissingPermission")
    private static void getCurrentLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, location -> {
            locationV = location;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("lat", Double.doubleToRawLongBits(location.getLatitude()));
            editor.putLong("lon", Double.doubleToRawLongBits(location.getLongitude()));
            editor.apply();
        });
    }


    @Override
    public void onDisabled(Context context) {
        Toast.makeText(context, "Widget has removed", Toast.LENGTH_SHORT).show();
    }
}