package com.namseoul.sa.tab;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Member;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button login;
    EditText id,pw;
    TextView membership,find;


    static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1000;

    final static String urlbase = "http://13.125.191.250/";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                for(int i = 0; i < permissions.length; i++) {

                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(grantResult == PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "위치 설정", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(this, "위치 취소", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        login = findViewById(R.id.login_btn);
        membership = findViewById(R.id.membership_btn);
        find = findViewById(R.id.test_btn);

        id = findViewById(R.id.loginid);
        pw = findViewById(R.id.loginpw);


        membership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MembershipActivity.class);
                startActivity(i);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginTask lt = new loginTask();
                lt.execute();
            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this,LocationService.class);
                startService(i);
            }
        });
    }

    public class loginTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection urlConn = null;

            sb.append("mode").append("=").append("login").append("&");
            sb.append("userID").append("=").append(id.getText().toString()).append("&");
            sb.append("userPassword").append("=").append(pw.getText().toString());

            try{
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

            return "예외감지";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            loginafter(s);
        }
    }

    public void loginafter(String s){
        Intent i;

        switch (s){
            case "P":
                i = new Intent(MainActivity.this, ParentIndexActivity.class);
                startActivity(i);
                finish();
                break;
            case "C":
                i = new Intent(MainActivity.this,ChildIndexActivity.class);
                startActivity(i);
                finish();
                break;
            default:
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }
}
