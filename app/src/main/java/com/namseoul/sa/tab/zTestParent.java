package com.namseoul.sa.tab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class zTestParent extends AppCompatActivity {

    static final int PORT = 10005;

    Socket socket;
    DataInputStream is;
    DataOutputStream os;
    String msg="";
    String ip = null;

    public StringBuilder sb = new StringBuilder();
    public String mode;

    final static String urlbase = "http://13.125.227.209/";

    public String userID;

    Button btn;
    TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_test_parent);

        btn = findViewById(R.id.pbtn);
        tv = findViewById(R.id.ptv);

        Intent i = getIntent();
        userID = i.getStringExtra("idstr");
        Log.i("사용자 ID",userID);

        mode = "check";

        connect con = new connect();
        con.execute(mode);
    }

    public class connect extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... s) {
            HttpURLConnection urlConn = null;

            switch (s[0]) {
                case "check":
                    sb.setLength(0);
                    sb.append("mode").append("=").append("check").append("&");
                    sb.append("userID").append("=").append(userID);
                    break;
            }
            try {
                URL url = new URL(urlbase + "login.php");
                urlConn = (HttpURLConnection) url.openConnection();

                String strParam = sb.toString();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Accep", "application/json");
                urlConn.setRequestProperty("Context_type", "application/x-www-form-urlencoded; charset=UTF-8");

                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);

                OutputStream os = urlConn.getOutputStream();
                os.write(strParam.getBytes("UTF-8"));
                os.flush();
                os.close();

                if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "네트워크 연결실패";
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

                return reader.readLine();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            }

            return "예외감지";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (mode.equals("check") && s == null) {
                mode = "add";
                AlertDialog.Builder ad = new AlertDialog.Builder(zTestParent.this);
                ad.setTitle("자녀 계정 입력")
                        .setMessage("자녀 계정의 ID를 입력해 주세요");

                final EditText et = new EditText(zTestParent.this);
                ad.setView(et);

                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sb.setLength(0);
                        sb.append("mode").append("=").append("add").append("&");
                        sb.append("sid").append("=").append(et.getText().toString()).append("&");
                        sb.append("mid").append("=").append(userID);
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                ad.show();
                connect ct = new connect();
                ct.execute(mode);
            } else if (mode.equals("check") && !s.isEmpty()) {
                ip = s;

            } else if (mode.equals("add") && s != null) {
                switch (s) {
                    case "":
                        AlertDialog.Builder ad = new AlertDialog.Builder(zTestParent.this);
                        ad.setTitle("없는 계정입니다.")
                                .setMessage("자녀 계정의 ID를 다시 입력해 주세요");

                        final EditText et = new EditText(zTestParent.this);
                        ad.setView(et);

                        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sb.setLength(0);
                                sb.append("mode").append("=").append("add").append("&");
                                sb.append("sid").append("=").append(et.getText().toString()).append("&");
                                sb.append("mid").append("=").append(userID);
                                connect ct = new connect();
                                ct.execute(mode);
                            }
                        });
                        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        });
                        ad.show();
                        break;
                    default:
                        ip = s;
                        break;
                }
            }
            if (s == null) {
                Log.i("받아온값", "널");
            } else {
                Log.i("받아온 값", s);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try{
                        Log.i("아이피",ip);
                        Log.i("포트",Integer.toString(PORT));
                        socket = new Socket(InetAddress.getByName(ip),PORT);
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                        Log.i("네트워크설정","연결완료");
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i("설정 오류","소켓 및 스트림 설정 오류 발생");
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                try{
                                    os.flush();
                                    os.writeUTF("test");
                                    Thread.sleep(1000);
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
