package com.arduino.weatherreport;

public class Constant {
    private static final String APIKEY = "E1N8GSl3RflGsE2P2R7AtGfgGnaVs5tW";
    public static final String BASE_URL = "https://dataservice.accuweather.com/locations/v1/";
    public static final String GET_LOC = BASE_URL+"cities/geoposition/search?apikey="+APIKEY+"&q=";
}
