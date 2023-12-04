package com.example.dashcam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.dashcam.listView.ListItem;
import com.example.dashcam.listView.ListItemFav;
import com.example.dashcam.listView.ListItemFavAdapter;
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

        //주행 기록 저장 테이블
        //주행 시작 시각, 도착 시각
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Driving");
        sqLiteDatabase.execSQL("create table Driving (mID integer primary key autoincrement, Start text, Arrive text);");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Favorite");
        sqLiteDatabase.execSQL("create table Favorite (mID integer primary key autoincrement, Filename text, Tablename text)");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Event");
        sqLiteDatabase.execSQL("create table Event (mID integer primary key autoincrement, Tablename text, Filename text)");

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

    public String getAddress(String tableName, String videoName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Latitude, Longitude FROM " + tableName + " WHERE Start ='" + videoName + "'", null);
        String result = null;
        int latIndex  = cursor.getColumnIndex("Latitude");
        int lonIndex = cursor.getColumnIndex("Longitude");

        while(cursor.moveToNext()){
            double latitude = cursor.getDouble(latIndex);
            double longitude = cursor.getDouble(lonIndex);

            result = LocationUtils.getInstance().getAddressFromLocation(context, latitude, longitude);
            if(result.length() > 5){
                break;
            }
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

            Cursor cursor2 = db.rawQuery("SELECT Latitude, Longitude FROM t" + start, null);

            int latitudeIndex = cursor2.getColumnIndex("Latitude");
            int longitudeIndex = cursor2.getColumnIndex("Longitude");

            while (cursor2.moveToNext()){
                double latitude = cursor2.getDouble(latitudeIndex);
                double longitude = cursor2.getDouble(longitudeIndex);

                addressDepart = LocationUtils.getInstance().getAddressFromLocation(context, latitude, longitude);
                if(addressDepart != "Unknown Location"){
                    break;
                }
            }

            cursor2.close();

            String query = "SELECT Latitude, Longitude FROM t" + start + " ORDER BY mID DESC";

            Cursor cursor3 = db.rawQuery(query, null);

            latitudeIndex = cursor3.getColumnIndex("Latitude");
            longitudeIndex = cursor3.getColumnIndex("Longitude");

            while (cursor3.moveToNext()){
                double latitude = cursor3.getDouble(latitudeIndex);
                double longitude = cursor3.getDouble(longitudeIndex);

                addressArrive = LocationUtils.getInstance().getAddressFromLocation(context, latitude, longitude);
                if(addressArrive != "Unknown Location"){
                    break;
                }
            }

            cursor3.close();

            ListItem listItem = new ListItem(date, addressDepart, addressArrive, timeDepart, timeArrive, "t" + start);
            result.add(listItem);
        }

        cursor.close();
        return result;
    }

    public ArrayList<String> getEvent(String tableName){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT Filename FROM Event Where Tablename = '" + tableName + "'", null);
        while(cursor.moveToNext()){
            result.add(cursor.getString(0));
        }
        cursor.close();
        return result;
    }

    public String getTableNamewitheTitle(String videoTitle){
        SQLiteDatabase db = this.getReadableDatabase();
        String result = null;
        Cursor cursor = db.rawQuery("SELECT Start FROM Driving", null);
        while(cursor.moveToNext()){
            if (videoTitle.compareTo(cursor.getString(0)) >= 0) {
                if(result == null || cursor.getString(0).compareTo(result) > 0)
                    result = cursor.getString(0);
            }
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

    public void insertEvent(String tableName, String fileName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("Tablename", "t" + tableName);
        cv.put("Filename", fileName);

        long result = db.insert("Event", null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }
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

    public ArrayList<ListItemFav> getEvent(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ListItemFav> result = new ArrayList<>();
        String query = "SELECT Filename, Tablename FROM Event";
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
