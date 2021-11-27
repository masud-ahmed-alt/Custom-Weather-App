package com.arduino.weatherreport;


import static com.arduino.weatherreport.Constant.APIKEY;
import static com.arduino.weatherreport.Constant.BASE_URL;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private double lon = 91.736237;
    private double lat = 26.144518;
    private LocationManager locationManager;
    private Location location;
    private long location_key;
    private String url_main, TAG = "TAG";
    private Snackbar snackbar;

    private ProgressDialog dialog;


    private static final int LOCATION_ACCESS_CODE = 2051;
    private boolean isGPSEnabled = false;
    private boolean canGetLocation = true;
    private TextView temp,city,status,humidity,uvindexRate,sunrise,sunset,country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        dialog.show();
        if(!checkPermissionStatus()){
            requestPermission();
        }else {
            getLocation();
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, location -> {
            this.location = location;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("lat", Double.doubleToRawLongBits(location.getLatitude()));
            editor.putLong("lon", Double.doubleToRawLongBits(location.getLongitude()));
            editor.apply();
            updateUI();
        });
    }

    private boolean checkPermissionStatus() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initView() {
        temp = findViewById(R.id.temp);
        city = findViewById(R.id.city);
        status = findViewById(R.id.status);
        humidity = findViewById(R.id.humidityRate);
        uvindexRate = findViewById(R.id.uvindexRate);
        sunrise = findViewById(R.id.sunriseTime);
        sunset = findViewById(R.id.sunsetTime);
        country = findViewById(R.id.country);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Getting your location...");

    }

    private void requestPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if(multiplePermissionsReport.areAllPermissionsGranted()){
                            getLocation();
                        }else {
                            showSnackBarForPermission("Permission required... ");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();


    }


    public void updateUI() {
        if (location != null){
            lat = location.getLatitude();
            lon = location.getLongitude();
        }else {
            getLocation();
        }
            url_main = BASE_URL + "forecast.json?key=" + APIKEY + "&q=" + lat + "," + lon + "&aqi=yes";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url_main, response -> {
            dialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject locationObject = jsonObject.getJSONObject("location");
                city.setText(locationObject.getString("name") + " , " + locationObject.getString("region"));
                country.setText(locationObject.getString("country"));
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject forecast = jsonObject.getJSONObject("forecast");
                JSONArray forecastArray = forecast.getJSONArray("forecastday");
                JSONObject obj = forecastArray.getJSONObject(0);
                JSONObject times = obj.getJSONObject("astro");
                JSONArray hour = obj.getJSONArray("hour");
                long[] allTimes = new long[hour.length()];
                JSONObject[] conditions = new JSONObject[hour.length()];
                for(int i=0; i<hour.length(); i++){
                    JSONObject timeObj =  hour.getJSONObject(i);
                    long time_epoch = timeObj.getLong("time_epoch");
                    long date = parseInt(String.valueOf(new Date().getTime()/1000));

                    allTimes[i] = date-time_epoch;
                    conditions[i]  = hour.getJSONObject(i);


                }

                for(int i=0; i<hour.length(); i++){
                    if(allTimes[i]<0){
                        allTimes[i] = allTimes[i]*(-1);
                    }
                }
                int index = 0;
                long min = allTimes[index];

                for (int j = 1; j < allTimes.length; j++){
                    if (allTimes[j] <= min){
                        min = allTimes[j];
                        index = j;
                    }
                }

                status.setText(conditions[index].getJSONObject("condition").getString("text"));
                temp.setText(String.valueOf((int)(Math.ceil(current.getDouble("temp_c")))));
                humidity.setText(current.getInt("humidity")+"%");
                uvindexRate.setText(current.getInt("uv")+" of 10");
                sunrise.setText(times.getString("sunrise"));
                sunset.setText(times.getString("sunset"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, error -> {
            Log.d(TAG, "updateUI: " + error.getMessage());
            dialog.dismiss();
        });
        queue.add(request);
    }


    private void showSnackBarForPermission(String msg){
        LinearLayout mainView = findViewById(R.id.mainView);
        snackbar = Snackbar.make(mainView,msg,Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Enable", v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        snackbar.show();
    }

    private void showSnackBarForGPS(String msg){
        LinearLayout mainView = findViewById(R.id.mainView);
        snackbar = Snackbar.make(mainView,msg,Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Enable", v -> {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });
        snackbar.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        preferences = getSharedPreferences(MainActivity.this.getApplicationContext().getPackageName(),MODE_PRIVATE);
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showSnackBarForGPS("Please Enable GPS");
        }else {
            updateUI();
        }
        lon = Double.longBitsToDouble(preferences.getLong("lon",Double.doubleToRawLongBits(lon)));
        lat = Double.longBitsToDouble(preferences.getLong("lat",Double.doubleToRawLongBits(lat)));

    }

    @Override
    protected void onResume() {
        super.onResume();
        dialog.show();
        updateUI();
    }
}