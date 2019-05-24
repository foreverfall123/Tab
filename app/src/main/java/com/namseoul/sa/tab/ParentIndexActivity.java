package com.namseoul.sa.tab;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ParentIndexActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    ActionBar bar;
    private FragmentManager fm;
    private ArrayList<Fragment>fList;

    public StringBuilder sb = new StringBuilder();
    public String mode;

    final static String urlbase = "http://13.125.227.209/";

    public String userID;

    public String ip;

    ParentService parentService;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParentService.MyBinder binder = (ParentService.MyBinder)service;
            parentService = binder.getMyService();
            parentService.setip(ip);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_index);

        Intent i = getIntent();
        userID = i.getStringExtra("idstr");
        Log.i("사용자 ID",userID);

        mode = "check";
        connect con = new connect();
        con.execute(mode);

        mViewPager = (ViewPager)findViewById(R.id.pager);

        fm = getSupportFragmentManager();

        bar = getSupportActionBar();

        bar.setDisplayShowTitleEnabled(true);
        bar.setTitle("타이틀?");

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab tab1 = bar.newTab().setText("기능").setTabListener(tabListener);
        ActionBar.Tab tab2 = bar.newTab().setText("설정").setTabListener(tabListener);

        bar.addTab(tab1);
        bar.addTab(tab2);

        fList = new ArrayList<Fragment>();
        fList.add(ParentFirstFragment.newInstance());
        fList.add(ParentSecondFragment.newInstance());

        mViewPager.setOnPageChangeListener(viewPagerListener);

        CustomFragmentPagerAdapter adapter = new CustomFragmentPagerAdapter(fm, fList);
        mViewPager.setAdapter(adapter);

    }

    ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            bar.setSelectedNavigationItem(position);
        }
    };

    ActionBar.TabListener tabListener = new ActionBar.TabListener(){

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //탭에서 벗어낫을 경우 처리
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //다시 선택되었을 경우 처리
        }
    };

    public class connect extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... s) {
            HttpURLConnection urlConn = null;

            switch(s[0]){
                case "check":
                    sb.setLength(0);
                    sb.append("mode").append("=").append("check").append("&");
                    sb.append("userID").append("=").append(userID);
                    break;
            }
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

            if(mode.equals("check")&&s==null){
                mode = "add";
                AlertDialog.Builder ad = new AlertDialog.Builder(ParentIndexActivity.this);
                ad.setTitle("자녀 계정 입력")
                        .setMessage("자녀 계정의 ID를 입력해 주세요");

                final EditText et = new EditText(ParentIndexActivity.this);
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
            }else if(mode.equals("check")&&!s.isEmpty()){
                ip = s;

            }else if(mode.equals("add")&&s!=null){
                switch (s) {
                    case "":
                        AlertDialog.Builder ad = new AlertDialog.Builder(ParentIndexActivity.this);
                        ad.setTitle("없는 계정입니다.")
                                .setMessage("자녀 계정의 ID를 다시 입력해 주세요");

                        final EditText et = new EditText(ParentIndexActivity.this);
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
            if(s == null) {
                Log.i("받아온값", "널");
            }else{
                Log.i("받아온 값",s);
            }

            if(!(conn == null)){
                Intent bindintent = new Intent(ParentIndexActivity.this,ParentService.class);
                bindService(bindintent,conn, Context.BIND_AUTO_CREATE);
            }
        }
    }
}
