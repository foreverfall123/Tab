package com.namseoul.sa.tab;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChildIndexActivity extends AppCompatActivity {

    final static String urlbase = "http://13.125.227.209/";
    HttpURLConnection urlConn = null;
    String ip,id;

    ChildService childService;

    Button start,stop;

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_index);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        Intent i = getIntent();
        id = i.getStringExtra("idstr");

       final ServiceConnection conn = new ServiceConnection() {
           @Override
           public void onServiceConnected(ComponentName name, IBinder service) {
               ChildService.LocalBinder binder = (ChildService.LocalBinder)service;
               childService = binder.getService();
               childService.setIP(ip);
           }

           @Override
           public void onServiceDisconnected(ComponentName name) {

           }
       };

        getIP gp = new getIP();
        gp.execute();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChildIndexActivity.this,ChildService.class);
                bindService(i,conn, Context.BIND_AUTO_CREATE);

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conn != null){
                    unbindService(conn);
                }
            }
        });



    }



    public class getIP extends AsyncTask<Void, Void, String>{

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
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                if(urlConn != null){
                    urlConn.disconnect();
                }
            }

            return "오류발생";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ip = s;
            Log.i("받은 값",s);

        }
    }
}
