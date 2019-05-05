package com.namseoul.sa.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<InfoClass> infoArr;
    private ViewHolder holder;

    public CustomAdapter(Context c, ArrayList<InfoClass> array){
        mInflater = LayoutInflater.from(c);
        infoArr = array;
    }

    @Override
    public int getCount() {
        return infoArr.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null){
            holder = new ViewHolder();
            v = mInflater.inflate(R.layout.listview_item,null);
            holder.name = (TextView)v.findViewById(R.id.tvName);
            holder.latitude = (TextView)v.findViewById(R.id.tvLatitude);
            holder.longitude = (TextView)v.findViewById(R.id.tvLongitude);
            holder.range = (TextView)v.findViewById(R.id.tvRange);

            v.setTag(holder);
        }else{
            holder = (ViewHolder)v.getTag();
        }

        InfoClass info = infoArr.get(position);

        holder.name.setText(info.name);
        holder.latitude.setText(Double.toString(info.latitude));
        holder.longitude.setText(Double.toString(info.longitude));
        holder.range.setText(Integer.toString(info.range));

        return v;
    }

    public void setArrayList(ArrayList<InfoClass> arrays){
        this.infoArr = arrays;
    }

    public ArrayList<InfoClass> getArrayList(){
        return infoArr;
    }

    private class ViewHolder{
        TextView name;
        TextView latitude;
        TextView longitude;
        TextView range;
    }


}
