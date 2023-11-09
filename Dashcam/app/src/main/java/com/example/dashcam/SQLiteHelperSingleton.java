package com.example.dashcam;

import android.content.Context;

public class SQLiteHelperSingleton {
    private static SQLiteHelper instance;
    private SQLiteHelperSingleton(){

    }
    public static synchronized SQLiteHelper getInstance(Context context){
        if (instance == null){
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }
}
