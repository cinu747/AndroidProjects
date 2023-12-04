package com.example.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefrenceManager {

    static public String EMAIL = "email";

    Context context;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public PrefrenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Payment", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            editor = sharedPreferences.edit();
        } else {
            editor = null;
        }
    }

    public void clearPreferences() {
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }

    public void setData(String key, String data) {
        sharedPreferences.edit().putString(key, data).apply();

    }

    public String getData(String key) {
        return sharedPreferences.getString(key, "");


    }

}
