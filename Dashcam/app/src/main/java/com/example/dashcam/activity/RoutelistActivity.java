package com.example.dashcam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.dashcam.SQLiteHelper;
import com.example.dashcam.SQLiteHelperSingleton;
import com.example.dashcam.activity.MapActivity;
import com.example.dashcam.adapter.ListItem;
import com.example.dashcam.adapter.ListItemAdapter;
import com.example.dashcam.databinding.ActivityRoutelistBinding;

import java.util.ArrayList;

public class RoutelistActivity extends AppCompatActivity {

    private ActivityRoutelistBinding viewBinding;
    private String tableName;
    SQLiteHelper sqLiteHelper;
    private ArrayAdapter mAdapter;
    private ArrayList arRoutes;

    private ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityRoutelistBinding.inflate(getLayoutInflater()); //바인딩 클래스의 인스턴스 생성
        setContentView(viewBinding.getRoot());
        sqLiteHelper = SQLiteHelperSingleton.getInstance(this);

        //arRoutes = sqLiteHelper.getDriving();
        //mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arRoutes);
        //viewBinding.routeList.setAdapter(mAdapter);
        //viewBinding.routeList.setOnItemClickListener(mItemClickListener);

        adapter = new ListItemAdapter();

        ArrayList<ListItem> routes = sqLiteHelper.getroute(this);
        adapter.addItem(routes);
        viewBinding.routeList.setAdapter(adapter);
        viewBinding.routeList.setOnItemClickListener(mItemClickListener);



    }

    private void setItem(){

    }


    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){

            String strID = (String) arRoutes.get(position);

            int ID = Integer.parseInt(strID);

            tableName = sqLiteHelper.getTableName(ID);

            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            intent.putExtra("TableName", tableName);
            startActivity(intent);


        }
    };
}