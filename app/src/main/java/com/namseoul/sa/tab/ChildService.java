package com.namseoul.sa.tab;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChildService extends Service{

    private static final int PORT = 10001;
    String ip;

    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    double latitude, longitude;

    private static final long MIN_DISTACE_UPDATES = 10;
    private static final long MIN_TIME_UPDATE = 1000*1*1;

    boolean isGPSenabled = false;
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ip = intent.getStringExtra("ip");

        final Context mContext;
        mContext = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket = new Socket(InetAddress.getByName(ip),PORT);

                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());

                    locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);

                    isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    int a = ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);

                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
                    }else if(isGPSenabled){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                while(true){
                    try{
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                        String getTime = sdf.format(date);
                        StringBuilder sb = new StringBuilder();
                        sb.append("latitude").append("=").append(latitude).append("&");
                        sb.append("longitude").append("=").append(longitude).append("&");
                        sb.append("time").append("=").append(getTime);
                        String data = sb.toString();

                        os.writeUTF(data);
                        os.flush();

                        Thread.sleep(1000*60*60);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        return startId;
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
