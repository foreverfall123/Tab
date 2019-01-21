package com.namseoul.sa.tab;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    Button login,membership,find;
    EditText id,pw;

    final static String urlbase = "http://13.125.191.250/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.login_btn);
        membership = findViewById(R.id.membership_btn);
        find = findViewById(R.id.find_id_btn);

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

                //Socket socket = new Socket(urlbase,80);
                //sb.append("userIP").append("=").append(socket.getLocalAddress().getHostAddress());

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
        Intent i = null;

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
