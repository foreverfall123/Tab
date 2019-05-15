package com.namseoul.sa.tab;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapServiceActivity extends AppCompatActivity implements OnMapReadyCallback {

    SharedPreferences sp;
    String strContact;
    Type listType;
    List<GPSData> datas;
    Gson gson;
    ArrayList<LatLng> arrayPoints;
    PolylineOptions polylineOptions;

    ParentService parentService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        gson = new GsonBuilder().create();
        sp = getSharedPreferences("shared",MODE_PRIVATE);
        strContact = sp.getString("gps","");

        listType = new TypeToken<ArrayList<GPSData>>(){}.getType();
        if(strContact == null){
            Log.i("널값은","strContact");
        }else if(listType == null){
            Log.i("널값은","listType");
        }

        datas = gson.fromJson(strContact,listType);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParentService.MyBinder binder = (ParentService.MyBinder)service;
            parentService = binder.getMyService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(datas != null){
            for(GPSData data : datas){
                LatLng ll = new LatLng(data.getla(),data.getlo());
                MarkerOptions marker = new MarkerOptions();
                marker.position(ll)
                        .title(data.getna());
                googleMap.addMarker(marker);
                arrayPoints.add(ll);
            }
            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLUE)
                    .width(5)
                    .addAll(arrayPoints);
            googleMap.addPolyline(polylineOptions);
        }



        Intent i = new Intent(this,ParentService.class);
        bindService(i,conn, Context.BIND_AUTO_CREATE);

        while(parentService==null){
            try{
                Thread.sleep(1000);
            }catch(Exception e){

            }
        }

        parentService.sendrealtime();

        while(true){
            LatLng ll = parentService.getchildgps();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("아이의 현재위치")
                    .position(ll);
            googleMap.addMarker(markerOptions);
            try{
                Thread.sleep(1000);
            }catch(Exception e){

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        parentService.sendstoptime();
        unbindService(conn);
    }
}
