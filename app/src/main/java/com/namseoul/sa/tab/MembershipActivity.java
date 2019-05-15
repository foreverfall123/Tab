package com.namseoul.sa.tab;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Created by forev on 2018-11-19.
 */

public class MembershipActivity extends AppCompatActivity {

    EditText eid,epw,ename;
    RadioButton rbp,rbc;
    Button signbtn;
    String stat;

    HttpURLConnection urlConn = null;
    StringBuilder sb = new StringBuilder();

    final static String urlbase = "http://13.125.227.209/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.membership);

        eid = findViewById(R.id.signid);
        epw = findViewById(R.id.signpw);
        ename = findViewById(R.id.signname);

        rbp = findViewById(R.id.rbp);
        rbc = findViewById(R.id.rbc);

        signbtn = findViewById(R.id.signupbtn);

        signbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checksignup()){
                    SignupTask st = new SignupTask();
                    st.execute();

                }
            }
        });
    }

    public boolean checksignup(){

        //아이디 점검
        //영어 숫자 최대 20자
        if(!Pattern.matches("^[a-zA-Z0-9].{4,20}$",eid.getText().toString())){
            Toast.makeText(this, "아이디는 영문,숫자로 이루어진 20글자 미만입니다", Toast.LENGTH_SHORT).show();
            return false;
        }

        //비밀번호 점검
        //최소 8에서 최대 20자
        if(!Pattern.matches("^(?=.*?[A-Za-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,20}$",epw.getText().toString())){
            Toast.makeText(this, "비밀번호는 적어도 하나 이상의 숫자 영어 특수기호를 포함해야 합니다", Toast.LENGTH_SHORT).show();
            return false;
        }

        //이름점검
        //단순 한글만 10자
        if(!Pattern.matches("^[가-힣].{1,10}$",ename.getText().toString())){
            Toast.makeText(this, "이름은 한글로만 이루어진 10자이하의 문자입니다", Toast.LENGTH_SHORT).show();
            return false;
        }

        //관계점검
        //체크 되었나만 판단.
        if(rbp.isChecked()){
            stat = "P";
        }else if(rbc.isChecked()){
            stat = "C";
        }else{
            Toast.makeText(this, "적절한 관계에 체크해주시기 바랍니다", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public class SignupTask extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {

            sb.setLength(0);
            sb.append("mode").append("=").append("save").append("&");
            sb.append("userID").append("=").append(eid.getText().toString()).append("&");
            sb.append("userPassword").append("=").append(epw.getText().toString()).append("&");
            sb.append("userStatus").append("=").append(stat);

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

                    Log.i("test",urlConn.getErrorStream().toString());

                    return urlConn.getErrorStream().toString();
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

            switch (s){
                case "already":
                    Toast.makeText(MembershipActivity.this, "이미 존재하는 아이디 입니다", Toast.LENGTH_SHORT).show();
                    break;
                case "ok":
                    Toast.makeText(MembershipActivity.this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MembershipActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                    break;
                default:
                    Toast.makeText(MembershipActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
