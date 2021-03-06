package com.namseoul.sa.tab;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChildService extends Service{

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {
        ChildService getService(){
            return ChildService.this;
        }
    }

    private static final int PORT = 10001;
    String ip;
    StringBuilder sb;
    String[] s;
    String msg;

    Socket socket;
    ServerSocket serverSocket;
    DataInputStream is;
    DataOutputStream os;

    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient = null;

    double latitude, longitude;

    int setTime = 0;

    private static final long MIN_DISTACE_UPDATES = 10;
    private static final long MIN_TIME_UPDATE = 1000*1*1;

    boolean isGPSenabled = false;
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    static int geofenceTransition;
    static String triggeringGeofencesIdsString;

    boolean realtimeswitch = false;
    static boolean geofenceEnter = false;
    static boolean geofenceExit = false;

    String check;

    boolean isStream;
    boolean osStream;

    TimerTask addTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        final Context mContext;
        mContext = this;

        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
        sb = new StringBuilder();
        isStream = true;
        osStream = true;
        realtimeswitch = false;
        geofenceEnter = false;
        geofenceExit = false;
        isGPSenabled = false;
        isNetworkEnabled = false;

        locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);

        isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        int a = ContextCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
        }else if(isGPSenabled){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATE,MIN_DISTACE_UPDATES,gpsLocationListener);
        }
        latitude = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
        longitude = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();

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

                }catch(Exception e){
                    e.printStackTrace();
                }

                addTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            os.flush();
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm");
                            String getTime = sdf.format(date);
                            sb.setLength(0);
                            sb.append("gettimegps").append(" ").append(latitude).append(" ").append(longitude).append(" ").append(getTime);
                            os.writeUTF(sb.toString());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };

                Timer timer = new Timer();
                timer.schedule(addTask,0,1000*60*30);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(osStream) {
                            try {
                                if (realtimeswitch) {
                                    os.flush();
                                    sb.setLength(0);
                                    sb.append("realtime").append(" ").append(latitude).append(" ").append(longitude);
                                    check = sb.toString();
                                    os.writeUTF(check);
                                }
                                if(geofenceEnter){
                                    Log.i("서비스","지오팬스진입");
                                    sb.setLength(0);
                                    sb.append("geofenceenter").append(" ").append(geofenceTransition).append(" ").append(triggeringGeofencesIdsString);
                                    os.writeUTF(sb.toString());
                                    os.flush();
                                    geofenceEnter = false;
                                }
                                if(geofenceExit){
                                    sb.setLength(0);
                                    sb.append("geofenceexit").append(" ").append(geofenceTransition).append(" ").append(triggeringGeofencesIdsString);
                                    os.writeUTF(sb.toString());
                                    os.flush();
                                    geofenceExit = false;
                                }
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(isStream){
                            try{
                                msg = is.readUTF();
                                s = msg.split(" ");

                                switch(s[0]){
                                    case "realstart":
                                        realtimeswitch = true;
                                        break;
                                    case "realstop":
                                        realtimeswitch = false;
                                        break;
                                    case "geostart":
                                        Log.i("서비스","지오팬스 시작");
                                        Log.i("서비스",msg);
                                        setGeofence(s[1],Double.parseDouble(s[2]),Double.parseDouble(s[3]),Integer.parseInt(s[4]));
                                        break;
                                    case "geostop":
                                        mGeofencingClient.removeGeofences(getGeofencePendingIntent());
                                        break;
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
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
        Intent intent = new Intent(this, ChildService.GeofenceBroadcastReceiver.class);

        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void addGeofence(Geofence g){
        int permissionState = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionState==PackageManager.PERMISSION_GRANTED){

        }

        mGeofencingClient.addGeofences(getGeofencingRequest(g),getGeofencePendingIntent());
    }
    private void setGeofence(String name, double lan, double lon, int range){
        Geofence geofence = new Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion(lan,lon,range)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        addGeofence(geofence);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            isStream = false;
            osStream = false;
            is.close();
            os.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setIP(String ip){

        this.ip = ip;
        Log.i("받은 IP 값",this.ip);
    }

    public static class GeofenceBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if(geofencingEvent.hasError()){
                String errorMaggase = GeofenceErrorMessages.getErrorString(context,geofencingEvent.getErrorCode());
                Log.e("error",errorMaggase);
                return;
            }

            Log.i("방송","방송받음");

            geofenceTransition = geofencingEvent.getGeofenceTransition();
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofences);
                geofenceEnter = true;
            }else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofences);
                geofenceExit = true;
            }
        }
    }
}
