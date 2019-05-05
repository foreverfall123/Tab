package com.namseoul.sa.tab;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
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

    private BackPressCloseHanlder backPressCloseHanlder;

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

        for(int i = 0; i < 10; i++){
            toarray[i].setVisibility(View.INVISIBLE);
            tvarray[i].setVisibility(View.INVISIBLE);
        }

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        backPressCloseHanlder = new BackPressCloseHanlder(this);

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

            setGeofence();

            tvarray[index].setText(mInfoArr.get(index).getName() + "\n실행중");

        }else{

            list.remove(Integer.toString(index));

            mGeofencingClient.removeGeofences(getGeofencePendingIntent());

            setGeofence();

            tvarray[index].setText(mInfoArr.get(index).getName());
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT);

        builder.addGeofences(geolist);

        return builder.build();
    }

    private void setGeofence(){
        if(list.size() == 0){
            return;
        }

        geolist = new ArrayList<>();

        for(String s : list){
            int i = Integer.parseInt(s);
            geolist.add(new Geofence.Builder()
            .setRequestId(mInfoArr.get(i).getName())
            .setCircularRegion(mInfoArr.get(i).getLatitude(),mInfoArr.get(i).getLongitude(),mInfoArr.get(i).getRange())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
            .build());
        }

        addGeofence();
    }

    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void addGeofence(){
        int permissionState = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionState==PackageManager.PERMISSION_GRANTED){

        }

        mGeofencingClient.addGeofences(getGeofencingRequest(),getGeofencePendingIntent());
        Log.i("지오팬스 실행 완료","컴플리트");
    }

    public void onBackPressed(){
        backPressCloseHanlder.onBackPressed();
    }

    public class BackPressCloseHanlder{
        private long backKeyClickTime = 0;
        private Activity activity;

        public BackPressCloseHanlder(Activity activity){
            this.activity = activity;
        }

        public void onBackPressed(){
            if(System.currentTimeMillis() > backKeyClickTime + 2000){
                backKeyClickTime = System.currentTimeMillis();
                showToast();
            }
            if(System.currentTimeMillis() <= backKeyClickTime + 2000){
                mGeofencingClient.removeGeofences(getGeofencePendingIntent());
                activity.finish();
            }
        }
        public void showToast(){
            Toast.makeText(activity, "뒤로 가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
