package com.example.dashcam.listView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dashcam.R;

import java.util.ArrayList;

public class ListItemFavAdapter extends BaseAdapter {
    ArrayList<ListItemFav> items = new ArrayList<ListItemFav>();
    Context context;
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        context = viewGroup.getContext();
        ListItemFav listItem = items.get(i);

        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listviewfav, viewGroup, false);
        }

        TextView title = view.findViewById(R.id.videoTitle);

        String videoTitle = listItem.getVideoTitle();
        String date = videoTitle.substring(0, 4) + "년 " + videoTitle.substring(4, 6) + "월 " + videoTitle.substring(6, 8) + "일 " + videoTitle.substring(8, 10) + "시 " + videoTitle.substring(10, 12) + "분 " + videoTitle.substring(12, 14) + "초";

        title.setText(date);


        return view;
    }

    public void addItem(ArrayList<ListItemFav> itemList){
        items = itemList;
    }

}
