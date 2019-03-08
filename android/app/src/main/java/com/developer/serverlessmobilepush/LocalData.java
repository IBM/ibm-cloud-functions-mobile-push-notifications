package com.developer.serverlessmobilepush;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class LocalData {

    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static LocalData localData;
    private final String cityKey = "cityName";
    private final String latitudeKey = "latitude";
    private final String longitudeKey = "longitude";

    private LocalData(Activity activity) {
        this.activity = activity;
        this.sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + ".localData", Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public static void initialize(Activity activity) {
        localData = new LocalData(activity);
    }

    public static LocalData getInstance() {
        return localData;
    }

    public void setCityName(String cityName) {
        editor.putString(cityKey, cityName);
        editor.apply();
    }

    public void setLatitude(Double latitude) {
        editor.putLong(latitudeKey, Double.doubleToRawLongBits(latitude));
        editor.apply();
    }

    public void setLongitude(Double longitude) {
        editor.putLong(longitudeKey, Double.doubleToRawLongBits(longitude));
        editor.apply();
    }

    public String getCityName() {
        return sharedPreferences.getString(cityKey, null);
    }

    public Double getLatitude() {
        double d = Double.longBitsToDouble(sharedPreferences.getLong(latitudeKey,0));
        if (d != 0) {
            return d;
        } else {
            return null;
        }
    }

    public Double getLongitude() {
        double d = Double.longBitsToDouble(sharedPreferences.getLong(longitudeKey,0));
        if (d != 0) {
            return d;
        } else {
            return null;
        }
    }

    public boolean clearData() {
        editor.clear();
        return editor.commit();
    }

}
