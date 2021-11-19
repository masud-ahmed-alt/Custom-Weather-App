package com.arduino.weatherreport;

public class DataModelMain {
    private String WeatherText, LocalObservationDateTime,EpochTime;



    private Temp temp;
    private TempSub tempSub;


    public DataModelMain(String weatherText, String localObservationDateTime, String epochTime, Temp temp, TempSub tempSub) {
        WeatherText = weatherText;
        LocalObservationDateTime = localObservationDateTime;
        EpochTime = epochTime;
        this.temp = temp;
        this.tempSub = tempSub;
    }

    public String getWeatherText() {
        return WeatherText;
    }

    public void setWeatherText(String weatherText) {
        WeatherText = weatherText;
    }

    public String getLocalObservationDateTime() {
        return LocalObservationDateTime;
    }

    public void setLocalObservationDateTime(String localObservationDateTime) {
        LocalObservationDateTime = localObservationDateTime;
    }

    public String getEpochTime() {
        return EpochTime;
    }

    public void setEpochTime(String epochTime) {
        EpochTime = epochTime;
    }
    public Temp getTemp() {
        return temp;
    }

    public void setTemp(Temp temp) {
        this.temp = temp;
    }

    public TempSub getTempSub() {
        return tempSub;
    }

    public void setTempSub(TempSub tempSub) {
        this.tempSub = tempSub;
    }
    private class Temp {
        private TempSub tempSub;

        public Temp(TempSub tempSub) {
            this.tempSub = tempSub;
        }

        public TempSub getTempSub() {
            return tempSub;
        }

        public void setTempSub(TempSub tempSub) {
            this.tempSub = tempSub;
        }
    }

    private class TempSub{
        double Value;
        String Unit;

        public TempSub(double value, String unit) {
            Value = value;
            Unit = unit;
        }

        public double getValue() {
            return Value;
        }

        public void setValue(double value) {
            Value = value;
        }

        public String getUnit() {
            return Unit;
        }

        public void setUnit(String unit) {
            Unit = unit;
        }
    }

}

