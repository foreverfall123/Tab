package com.namseoul.sa.tab;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
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
import java.util.Timer;
import java.util.TimerTask;

public class MapServiceActivity extends AppCompatActivity implements OnMapReadyCallback {

    SharedPreferences sp;
    String strContact;
    Type listType;
    List<GPSData> datas;
    Gson gson;
    ArrayList<LatLng> arrayPoints;
    PolylineOptions polylineOptions;

    ParentService parentService = null;
    GoogleMap googleMap;

    TimerTask addTask;

    MarkerOptions markerOptions;
    Marker realmarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        markerOptions = new MarkerOptions();

        gson = new GsonBuilder().create();
        sp = getSharedPreferences("shared",MODE_PRIVATE);
        strContact = sp.getString("gps","");

        listType = new TypeToken<ArrayList<GPSData>>(){}.getType();

        datas = gson.fromJson(strContact,listType);

        addTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(realmarker != null){
                            realmarker.remove();
                        }
                        markerOptions.title("아이의 현재위치")
                                .position(parentService.getchildgps());
                        LatLng ll = markerOptions.getPosition();
                        Log.i("액티비티부분",Double.toString(ll.latitude));
                        Log.i("액티비티부분",Double.toString(ll.longitude));
                        realmarker = googleMap.addMarker(markerOptions);
                    }
                });
            }
        };

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParentService.MyBinder binder = (ParentService.MyBinder)service;
            parentService = binder.getMyService();
            parentService.startgpsset();
            Log.i("바인드서비스","스타트접속");

            Timer timer = new Timer();
            timer.schedule(addTask,0,1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.95,128.25),7));
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

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        parentService.stopgpsset();
        unbindService(conn);
    }
}
