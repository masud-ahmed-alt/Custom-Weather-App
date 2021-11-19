package com.arduino.weatherreport;

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


public class MainActivity extends AppCompatActivity {
    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private double lon = 91.736237;
    private double lat = 26.144518;
    private LocationManager locationManager;
    private Location location;
    private long location_key;
    private String url_main;


    private static final int LOCATION_ACCESS_CODE = 2051;
    private boolean isGPSEnabled = false;
    private boolean canGetLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(location!=null && requestPermission()) {
            Networking net = new Networking();
            String resp = net.get(this, url_main);
            Log.e("TAG", "onCreate: " + resp);
        }else {
            Log.d("TAG", "onCreate: Null Location");
        }

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
                MainActivity.this.location = location;
                Log.d("LOCATION", "onLocationChanged: LONG"+location.getLongitude()+" LAT"+location.getLatitude());

                Networking networking = new Networking();
                url_main = GET_LOC+location.getLongitude()+","+location.getLongitude();
            });
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_ACCESS_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){

        }
    }

    LocationListener locationListener = location -> MainActivity.this.location = location;

}