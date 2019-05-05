package com.namseoul.sa.tab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

    final static String urlbase = "http://13.125.191.250/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_index);

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
                    sb.append("mode").append("=").append("check");
                    break;
                case "save":

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
                mode = "save";
                AlertDialog.Builder ad = new AlertDialog.Builder(ParentIndexActivity.this);
                ad.setTitle("자녀 계정 입력")
                        .setMessage("자녀 계정의 ID를 입력해 주세요");

                final EditText et = new EditText(ParentIndexActivity.this);
                ad.setView(et);

                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sb.setLength(0);
                        sb.append("mode").append("=").append("save").append("&");
                        sb.append("id").append("=").append(et.getText().toString());
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
                doInBackground(mode);
            }else if(mode.equals("check")&&!s.isEmpty()){
                switch (s){
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
                                sb.append("mode").append("=").append("save").append("&");
                                sb.append("id").append("=").append(et.getText().toString());
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
                }
            }
        }
    }


}