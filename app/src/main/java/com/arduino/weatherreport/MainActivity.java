package com.arduino.weatherreport;


import static com.arduino.weatherreport.Constant.APIKEY;
import static com.arduino.weatherreport.Constant.BASE_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
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


    private void updateUI() {
        Timestamp timestamp= new Timestamp(System.currentTimeMillis());
        if (location != null){
            lat = location.getLatitude();
            lon = location.getLongitude();
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
                for(int i=0; i<hour.length(); i++){

                    JSONObject timeObj = hour.getJSONObject(i);
                    long time_epoch = timeObj.getLong("time_epoch");
                    JSONObject timeObj_1;

//                    String time = timeObj.getString("time");
//                    Date date =new Date(time);
//                    Log.e(TAG, "updateUI: "+date);
//                    if(i<23) {
//                         timeObj_1 = hour.getJSONObject(i + 1);
//                    }else {
//                        timeObj_1 = hour.getJSONObject(1);
//                    }
//                    long time_epoch_1 = timeObj_1.getLong("time_epoch");
//                    Log.d(TAG, "updateUI: "+time_epoch+" curr  = "+System.currentTimeMillis());
//                    if (time_epoch <= (timestamp.getTime())) {
//                        if(time_epoch_1 >= (timestamp.getTime())){
//                            JSONObject obk = timeObj.getJSONObject("condition");
//                            status.setText(obk.getString("text"));
//                        }
//
//                    }else {
//
//                    }

                }

                Log.d(TAG, "updateUI: "+timestamp.getTime()+"   : "+hour.get(0));
                updateData(current);
                updateTime(times);

                Log.e(TAG, "updateUI: " + current);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, error -> {
            Log.d(TAG, "updateUI: " + error.getMessage());
            dialog.dismiss();
        });
        queue.add(request);
    }

    private void updateTime(JSONObject times) throws JSONException{
        sunrise.setText(times.getString("sunrise"));
        sunset.setText(times.getString("sunset"));

    }

    private void updateData(JSONObject current) throws JSONException{
        temp.setText(String.valueOf((int)(Math.ceil(current.getDouble("temp_c")))));
        humidity.setText(current.getInt("humidity")+"%");
        uvindexRate.setText(current.getInt("uv")+" of 10");
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
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showSnackBarForGPS("Please Enable GPS");
        }else {
            updateUI();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        dialog.show();
        updateUI();
    }
}