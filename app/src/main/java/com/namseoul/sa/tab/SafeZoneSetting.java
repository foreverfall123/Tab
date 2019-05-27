package com.namseoul.sa.tab;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class SafeZoneSetting extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private ToggleButton[] toarray = new ToggleButton[10];
    private TextView[] tvarray = new TextView[10];
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private InfoClass mInfoClass;
    private ArrayList<InfoClass> mInfoArr;
    private int zoneCount;
    private ArrayList<Geofence> geolist;

    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient = null;

    private List<String> list = new ArrayList();

    ServiceConnection conn;
    ParentService parentService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safezonesetting_activity);

        toarray[0] = findViewById(R.id.toarray1);
        toarray[1] = findViewById(R.id.toarray2);
        toarray[2] = findViewById(R.id.toarray3);
        toarray[3] = findViewById(R.id.toarray4);
        toarray[4] = findViewById(R.id.toarray5);
        toarray[5] = findViewById(R.id.toarray6);
        toarray[6] = findViewById(R.id.toarray7);
        toarray[7] = findViewById(R.id.toarray8);
        toarray[8] = findViewById(R.id.toarray9);
        toarray[9] = findViewById(R.id.toarray10);

        tvarray[0] = findViewById(R.id.tvarray1);
        tvarray[1] = findViewById(R.id.tvarray2);
        tvarray[2] = findViewById(R.id.tvarray3);
        tvarray[3] = findViewById(R.id.tvarray4);
        tvarray[4] = findViewById(R.id.tvarray5);
        tvarray[5] = findViewById(R.id.tvarray6);
        tvarray[6] = findViewById(R.id.tvarray7);
        tvarray[7] = findViewById(R.id.tvarray8);
        tvarray[8] = findViewById(R.id.tvarray9);
        tvarray[9] = findViewById(R.id.tvarray10);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ParentService.MyBinder mbinder = (ParentService.MyBinder)service;
                parentService = mbinder.getMyService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(SafeZoneSetting.this,ParentService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        for(int i = 0; i < 10; i++){
            toarray[i].setVisibility(View.INVISIBLE);
            tvarray[i].setVisibility(View.INVISIBLE);
        }

        mDbOpenHelper = new DbOpenHelper(this);
        try{
            mDbOpenHelper.open();
        }catch(SQLException e){
            e.printStackTrace();
        }

        mInfoArr = new ArrayList<>();

        doWhileCursorToArray();

        if(zoneCount > 10)
            zoneCount = 10;

        for(int i = 0; i < zoneCount; i++){
            tvarray[i].setText(mInfoArr.get(i).getName());
            tvarray[i].setVisibility(View.VISIBLE);
            toarray[i].setVisibility(View.VISIBLE);
            toarray[i].setTag(i);
            toarray[i].setOnCheckedChangeListener(this);
        }
    }

    private void doWhileCursorToArray(){
        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumn();

        zoneCount = 0;

        while (mCursor.moveToNext()){
            mInfoClass = new InfoClass(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getDouble(mCursor.getColumnIndex("latitude")),
                    mCursor.getDouble(mCursor.getColumnIndex("longitude")),
                    mCursor.getInt(mCursor.getColumnIndex("range"))
            );

            mInfoArr.add(mInfoClass);
            zoneCount += 1;
        }

        mCursor.close();
    }

    @Override
    public void onCheckedChanged(CompoundButton cb, boolean b) {

        int index = (Integer)cb.getTag();

        if(b == true){

            list.add(Integer.toString(index));

            parentService.startgeoset(mInfoArr.get(index).getName(),mInfoArr.get(index).getLatitude(),mInfoArr.get(index).getLongitude(),mInfoArr.get(index).getRange());

            tvarray[index].setText(mInfoArr.get(index).getName() + "\n실행중");

        }else{

            list.remove(Integer.toString(index));

            parentService.stopgeoset();

            tvarray[index].setText(mInfoArr.get(index).getName());
        }
    }
}
