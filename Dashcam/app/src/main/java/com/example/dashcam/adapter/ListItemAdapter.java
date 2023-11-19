package com.example.dashcam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dashcam.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {
    ArrayList<ListItem> items = new ArrayList<ListItem>();
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
        ListItem listItem = items.get(i);

        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_item, viewGroup, false);
        }

        TextView textDate = view.findViewById(R.id.textDate);
        //TextView textDistance = view.findViewById(R.id.textDistance);
        TextView addressDepart = view.findViewById(R.id.addressDepart);
        TextView addressArrive = view.findViewById(R.id.addressArrive);
        TextView timeDepart = view.findViewById(R.id.timeDepart);
        TextView timeArrive = view.findViewById(R.id.timeArrive);

        textDate.setText(listItem.getDate());
        //textDistance.setText(listItem.getDistance());
        addressDepart.setText(listItem.getAddressDepart());
        addressArrive.setText(listItem.getAddressArrive());
        timeDepart.setText(listItem.getTimeDepart());
        timeArrive.setText(listItem.getTimeArrive());

        return view;
    }

    public void addItem(ArrayList<ListItem> itemList){
        items = itemList;
    }
}
