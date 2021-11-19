package com.arduino.weatherreport;

import static com.arduino.weatherreport.Constant.APIKEY;
import static com.arduino.weatherreport.Constant.CURR_REP;
import static com.arduino.weatherreport.Constant.GET_LOC;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
    private TextView temp,city,status,humidity,uvindexRate,sunrise,sunset;

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
        url_main = GET_LOC+location.getLatitude()+","+location.getLongitude();
        Networking net = new Networking();
        String resp = net.get(this, url_main);
        Log.d(TAG, "updateUI: "+url_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url_main, response -> {
            Log.d(TAG, "updateUI: "+response);
            try {
                JSONObject array = new JSONObject(response);
                location_key = array.getInt("Key");
                String local_name = array.getString("LocalizedName");
                updateData(location_key);

                JSONObject o3 = array.getJSONObject("Country");
                city.setText(array.getString("LocalizedName")+" , "+o3.getString("ID"));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }, error ->{
            Log.d(TAG, "updateUI: "+error.getMessage());
        });
        queue.add(request);
    }

    private void updateData(long location_key) {
        url_main = CURR_REP+location_key+"?apikey="+APIKEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET,url_main,response -> {
            Log.d(TAG, "updateData: "+response);
            try {
                JSONArray array = new JSONArray(response);
                JSONObject object = array.getJSONObject(0);
                Log.d(TAG, "updateData: "+object);
                JSONObject o1 = object.getJSONObject("Temperature");
                JSONObject o2 = o1.getJSONObject("Metric");
                double t = o2.getDouble("Value");
                temp.setText(String.valueOf((int)Math.ceil(t)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        },error -> {
            Log.d(TAG, "updateData: "+error.getMessage());
        });

        queue.add(request);
    }

}