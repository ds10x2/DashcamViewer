package com.example.dashcam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.dashcam.adapter.ListItem;
import com.example.dashcam.adapter.ListItemFav;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Favorite");
        sqLiteDatabase.execSQL("create table Favorite (mID integer primary key autoincrement, Filename text, Tablename text)");

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

    public ArrayList<ListItem> getroute(Context context){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Start, Arrive FROM Driving", null);
        ArrayList<ListItem> result = new ArrayList<>();

        int startIndex = cursor.getColumnIndex("Start");
        int arriveIndex = cursor.getColumnIndex("Arrive");

        while(cursor.moveToNext()){
            String start = cursor.getString(startIndex);
            String arrive = cursor.getString(arriveIndex);

            String date = start.substring(0, 4) + "." + start.substring(4, 6) + "." + start.substring(6, 8);
            String timeDepart = start.substring(8, 10) + ":" + start.substring(10, 12);
            String timeArrive = arrive.substring(8, 10) + ":" + arrive.substring(10, 12);
            String addressDepart = null;
            String addressArrive = null;

            Cursor cursor2 = db.rawQuery("SELECT Latitude, Longitude FROM t" + start + " LIMIT 1", null);

            int latitudeIndex = cursor2.getColumnIndex("Latitude");
            int longitudeIndex = cursor2.getColumnIndex("Longitude");

            while (cursor2.moveToNext()){
                double latitude = cursor2.getDouble(latitudeIndex);
                double longitude = cursor2.getDouble(longitudeIndex);

                addressDepart = LocationUtils.getInstance().getAddressFromLocation(context, latitude, longitude);
            }

            cursor2.close();

            String query = "SELECT Latitude, Longitude FROM t" + start + " WHERE mID = (SELECT max(mID) FROM t" + start + ")";

            Cursor cursor3 = db.rawQuery(query, null);

            latitudeIndex = cursor3.getColumnIndex("Latitude");
            longitudeIndex = cursor3.getColumnIndex("Longitude");

            while (cursor3.moveToNext()){
                double latitude = cursor3.getDouble(latitudeIndex);
                double longitude = cursor3.getDouble(longitudeIndex);

                addressArrive = LocationUtils.getInstance().getAddressFromLocation(context, latitude, longitude);
            }

            cursor3.close();

            ListItem listItem = new ListItem(date, addressDepart, addressArrive, timeDepart, timeArrive, "t" + start);
            result.add(listItem);
        }

        cursor.close();
        return result;
    }

    public String getTableName(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Start FROM Driving Where mID = " + id, null);
        String tableName = null;
        if(cursor.moveToFirst()){
            tableName = "t" + cursor.getString(0);
        }
        cursor.close();
        return tableName;
    }

    public List<LatLng> getLatLng(String tableName, String time){
        SQLiteDatabase db = this.getReadableDatabase();
        List<LatLng> latLngList = new ArrayList<>();
        String query = "SELECT Latitude, Longitude FROM " + tableName + " WHERE Start = '" + time + "'";

        Cursor cursor = db.rawQuery(query, null);

        int latitudeIndex = cursor.getColumnIndex("Latitude");
        int longitudeIndex = cursor.getColumnIndex("Longitude");

        while(cursor.moveToNext()){
            double latitude = cursor.getDouble(latitudeIndex);
            double longitude = cursor.getDouble(longitudeIndex);

            LatLng latLng = new LatLng(latitude, longitude);
            latLngList.add(latLng);
        }

        cursor.close();
        return latLngList;
    }

    public ArrayList<String> getTime(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> result = new ArrayList<>();
        String query = "SELECT DISTINCT Start FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);

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
        db.execSQL("DROP TABLE IF EXISTS " + "t" + tableName);

        String query = "CREATE TABLE " + "t" + tableName
                + "(mID INTEGER PRIMARY KEY AUTOINCREMENT, Start text, Latitude real, Longitude real);";

        db.execSQL(query);
    }

    //주행 기록
    public void insertLocation(String tableName, String StartTime, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("Start", StartTime);
        cv.put("Latitude", lat);
        cv.put("Longitude", lon);

        long result = db.insert("t" + tableName, null, cv);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isFileExists(String fileName){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM Favorite WHERE Filename = '" + fileName + "'";
        Cursor cursor = db.rawQuery(query, null);

        int cnt = 0;

        if(cursor.moveToFirst()){
            cnt = cursor.getInt(0);
        }
        cursor.close();
        return cnt > 0;
    }

    public void insertFavorite(String fileName, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Filename", fileName);
        cv.put("Tablename", tableName);

        long result = db.insert("Favorite", null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFavorite(String fileName){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM Favorite WHERE Filename = '" + fileName + "'";
        db.execSQL(query);
    }

    public ArrayList<ListItemFav> getFavorite(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ListItemFav> result = new ArrayList<>();

        String query = "SELECT Filename, Tablename FROM FAVORITE";
        Cursor cursor = db.rawQuery(query, null);

        int fileNameIndex = cursor.getColumnIndex("Filename");
        int tablenameIndex = cursor.getColumnIndex("Tablename");
        while(cursor.moveToNext()){
            String filename = cursor.getString(fileNameIndex);
            String tablename = cursor.getString(tablenameIndex);

            ListItemFav listitem = new ListItemFav(filename, tablename);
            result.add(listitem);
        }
        cursor.close();
        return result;

    }


    public void dropTable(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + "t" + tableName);
    }
}
