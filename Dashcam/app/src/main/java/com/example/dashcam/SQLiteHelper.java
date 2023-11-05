package com.example.dashcam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;
    public SQLiteHelper(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    //앱이 최초 설치 시 실행될 onCreate 메소드
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Driving");
        //sqLiteDatabase.execSQL("create table Members (mID integer primary key autoincrement, Name text, Age integer);");
        //sqLiteDatabase.execSQL("INSERT INTO Members VALUES (1, 'Kim', 20);");
        //sqLiteDatabase.execSQL("INSERT INTO Members VALUES (2, 'Lee', 30);");

        //주행 기록 저장 테이블
        //주행 시작 시각, 도착 시각
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Driving");
        sqLiteDatabase.execSQL("create table Driving (mID integer primary key autoincrement, Start text, Arrive text);");

        //위치 기록 테이블
        //위도, 경도 저장
        //mID는 Driving 테이블의 mID 외래키
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Location");
        //sqLiteDatabase.execSQL("create table Location (mID integer, dID integer, Latitude real, Longitude real, FOREIGN KEY (dID) REFERENCES Driving(mID));");
    }

    //앱이 업데이트 될 때 실행될 onUpgrade 메소드
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){

    }

    public ArrayList<String> getDriving(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT mID FROM Driving", null);
        ArrayList<String> result = new ArrayList<>();
        while(cursor.moveToNext()){
            result.add(cursor.getString(0));
        }
        cursor.close();
        return result;
    }

    public void insertDriving(String st, String ar){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("Start", st);
        cv.put("Arrive", ar);

        long result = db.insert("Driving", null, cv);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    //주행기록 시작할 때마다 table 생성
    public void createTable(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String query = "CREATE TABLE " + tableName
                + "(mID INTEGER PRIMARY KEY AUTOINCREMENT, Latitude real, Longitude real);";

        db.execSQL(query);
    }

    //주행 기록
    public void insertLocation(String tableName, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("Latitude", lat);
        cv.put("Longitude", lon);

        long result = db.insert(tableName, null, cv);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    public void dropTable(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String query = "DELETE FROM Driving WHERE mID = '" + tableName + "';";
        db.execSQL(query);
    }
}
