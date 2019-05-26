package com.namseoul.sa.tab;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class zTestChild extends AppCompatActivity {

    final static String urlbase = "http://13.125.227.209/";
    HttpURLConnection urlConn = null;
    private static final int PORT = 10005;
    String ip,id;

    Button btn;
    TextView tv;

    StringBuilder sb;
    String[] s;
    String msg;

    Socket socket;
    ServerSocket serverSocket;
    DataInputStream is;
    DataOutputStream os;

    double latitude, longitude;

    int setTime = 0;

    private static final long MIN_DISTACE_UPDATES = 10;
    private static final long MIN_TIME_UPDATE = 1000*1*1;

    boolean isGPSenabled = false;
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    int geofenceTransition;
    String triggeringGeofencesIdsString;

    boolean realtimeswitch = false;
    boolean geofenceEnter = false;
    boolean geofenceExit = false;

    String check;

    LocationListener gpsLocationListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_test_child);

        btn = findViewById(R.id.cbtn);
        tv = findViewById(R.id.ctv);

        Intent i = getIntent();
        id = i.getStringExtra("idstr");

        sb = new StringBuilder();

        getIP gp = new getIP();
        gp.execute();

        final Context mContext;
        mContext = this;

        gpsLocationListener = new LocationListener() {
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

        sb.append("첫 위도 : ").append(latitude).append("경도 : ").append(longitude).append("&");
        tv.setText(sb.toString());
    }





    public class getIP extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try{
                StringBuilder sb = new StringBuilder();

                sb.append("mode").append("=").append("getpip").append("&");
                sb.append("id").append("=").append(id);

                URL url = new URL(urlbase + "login.php");
                urlConn = (HttpURLConnection) url.openConnection();

                String strParam = sb.toString();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Accep","application/json");
                urlConn.setRequestProperty("Context_type", "application/x-www-form-urlencoded; charset=UTF-8");

                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);

                OutputStream os = urlConn.getOutputStream();
                os.write(strParam.getBytes("UTF-8"));
                os.flush();
                os.close();

                if(urlConn.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return "네트워크 연결실패";
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),"UTF-8"));

                return reader.readLine();


            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e) {
                e.printStackTrace();
            }

            return "오류발생";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ip = s;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        serverSocket = new ServerSocket(PORT);
                        Log.i("중간","소캣생성");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    try{
                        socket = serverSocket.accept();
                        Log.i("중간","연결");

                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                        Log.i("중간","셋팅 완료");

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    Log.i("중간","스레드 실행1 직전");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                try{
                                    os.flush();
                                    os.writeUTF(Double.toString(latitude));
                                    Log.i("전송","데이터 전송");
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                try{
                                    msg = is.readUTF();
                                    sb.append(msg).append(" ");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv.setText(sb.toString());
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                }
            }).start();
        }
    }
}
