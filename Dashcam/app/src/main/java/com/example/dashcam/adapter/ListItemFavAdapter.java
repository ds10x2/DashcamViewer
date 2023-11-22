package com.example.dashcam.adapter;

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


        title.setText(listItem.getVideoTitle());


        return view;
    }

    public void addItem(ArrayList<ListItemFav> itemList){
        items = itemList;
    }

}
