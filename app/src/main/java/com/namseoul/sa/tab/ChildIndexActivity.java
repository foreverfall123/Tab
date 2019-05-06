package com.namseoul.sa.tab;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChildIndexActivity extends AppCompatActivity {

    final static String urlbase = "http://13.125.191.250/";
    HttpURLConnection urlConn = null;
    String ip,id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_index);

        Intent intent = getIntent();
        id = intent.getStringExtra("idstr");

        getIP gp = new getIP();
        gp.execute();

        Intent i = new Intent(ChildIndexActivity.this,ChildService.class);
        i.putExtra("ip",ip);
        startService(i);
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
        }
    }
}
