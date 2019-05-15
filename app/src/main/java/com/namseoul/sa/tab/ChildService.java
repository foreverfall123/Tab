package com.namseoul.sa.tab;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChildService extends Service{

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {
        ChildService getService(){
            return ChildService.this;
        }
    }

    private static final int PORT = 10001;
    String ip;

    Socket socket;
    ServerSocket serverSocket;
    DataInputStream is;
    DataOutputStream os;

    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient = null;

    double latitude, longitude;

    private static final long MIN_DISTACE_UPDATES = 10;
    private static final long MIN_TIME_UPDATE = 1000*1*1;

    boolean isGPSenabled = false;
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        final Context mContext;
        mContext = this;

        locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);

        isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        int a = ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
        }else if(isGPSenabled){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    serverSocket = new ServerSocket(PORT);
                }catch (Exception e){
                    e.printStackTrace();
                }

                try{
                    socket = serverSocket.accept();

                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());

                    setTimegpg settime = new setTimegpg();
                    settime.setDaemon(true);
                    settime.run();

                }catch(Exception e){
                    e.printStackTrace();
                }

                while(true){
                    try{
                        String[] s;
                        String msg,name;
                        double lan,lon;
                        int range;

                        msg = is.readUTF();

                        s = msg.split(" ");

                        realTimegps real = new realTimegps();

                        switch (s[0]){
                            case "realtime":
                                real.run();
                                break;
                            case "realstop":
                                real.threadStop(true);
                                break;
                            case "safezonestart":
                                name = s[1];
                                lan = Double.parseDouble(s[2]);
                                lon = Double.parseDouble(s[3]);
                                range = Integer.parseInt(s[4]);
                                Geofence geofence = new Geofence.Builder()
                                        .setRequestId(name)
                                        .setCircularRegion(lan,lon,range)
                                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                                        .build();

                                addGeofence(geofence);
                                break;
                            case "safeend":
                                mGeofencingClient.removeGeofences(getGeofencePendingIntent());
                                break;
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

    public class realTimegps extends Thread {
        private boolean stop;

        realTimegps(){
            this.stop = false;
        }

        @Override
        public void run() {
            while(!stop){
                StringBuilder sb = new StringBuilder();
                sb.append("getrealgps").append(latitude).append(" ").append(longitude);
                String data = sb.toString();

                try{
                    os.writeUTF(data);
                    os.flush();
                }catch(Exception e){
                    e.printStackTrace();
                }

                try{
                    Thread.sleep(1000);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        public void threadStop(boolean stop){
            this.stop = stop;
        }
    }

    public class setTimegpg extends Thread{
        @Override
        public void run() {
            while(true) {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm");
                String getTime = sdf.format(date);
                StringBuilder sb = new StringBuilder();
                sb.append("gettimegps").append(" ").append(latitude).append(" ").append(longitude).append(" ").append(getTime);
                String data = sb.toString();

                try {
                    os.writeUTF(data);
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000 * 60 * 60);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private GeofencingRequest getGeofencingRequest(Geofence g){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT);

        builder.addGeofence(g);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void addGeofence(Geofence g){
        int permissionState = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionState==PackageManager.PERMISSION_GRANTED){

        }

        mGeofencingClient.addGeofences(getGeofencingRequest(g),getGeofencePendingIntent());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setIP(String ip){

        this.ip = ip;
        Log.i("받은 IP 값",this.ip);
    }

    public void sendwarning(String geofenceinfo, int geofence){
        StringBuilder sb = new StringBuilder();
        sb.append("geofencewarring").append(" ").append(geofenceinfo).append(" ").append(geofence);
        final String data = sb.toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    os.writeUTF(data);
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
