package com.arduino.weatherreport;


import static com.arduino.weatherreport.Constant.APIKEY;
import static com.arduino.weatherreport.Constant.BASE_URL;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private double lon = 91.736237;
    private double lat = 26.144518;
    private LocationManager locationManager;
    private Location location;
    private long location_key;
    private String url_main, TAG = "TAG";



    private static final int LOCATION_ACCESS_CODE = 2051;
    private boolean isGPSEnabled = false;
    private boolean canGetLocation = true;
    private TextView temp,city,status,humidity,uvindexRate,sunrise,sunset,country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();


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

    }

    private boolean requestPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_ACCESS_CODE);
            requestPermission();
            return false;
        }else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, location -> {
               this.location = location;
               updateUI();
            });
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_ACCESS_CODE && grantResults[0]==PackageManager.PERMISSION_DENIED
                && grantResults[1]==PackageManager.PERMISSION_DENIED){
            finish();
        }else {
            updateUI();
        }
    }

    private void updateUI() {
        url_main = BASE_URL+"forecast.json?key="+APIKEY+"&q="+location.getLatitude()+","+location.getLongitude() + "&aqi=yes";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url_main, response -> {
            Log.d(TAG, "updateUI: "+url_main);

            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject locationObject = jsonObject.getJSONObject("location");
                city.setText(locationObject.getString("name")+" , "+locationObject.getString("region"));
                country.setText(locationObject.getString("country"));
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject forecast = jsonObject.getJSONObject("forecast");
                JSONArray forecastArray = forecast.getJSONArray("forecastday");
                JSONObject obj = forecastArray.getJSONObject(0);
                JSONObject times = obj.getJSONObject("astro");
                updateData(current);
                updateTime(times);
                Log.e(TAG, "updateUI: "+current );
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }, error ->{
            Log.d(TAG, "updateUI: "+error.getMessage());
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


}