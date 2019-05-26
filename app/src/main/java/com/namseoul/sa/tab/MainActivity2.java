package com.namseoul.sa.tab;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private Button btn1,btn2;
    private Gson gson;
    private SharedPreferences sp;
    private List<Contact> datas;
    private String strContact;

    ParentService parentService;

    Context mcontext;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ParentService.MyBinder binder = (ParentService.MyBinder)service;
            parentService = binder.getMyService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        btn1=(Button)findViewById(R.id.SendSMS);
        btn2=(Button)findViewById(R.id.LinkCopy);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sp = getSharedPreferences("shared",MODE_PRIVATE);
                strContact = sp.getString("contact","");
                gson = new Gson();
                if(TextUtils.isEmpty(strContact))
                {
                    Toast.makeText(getApplicationContext(),"저장된 연락처가 없습니다.",Toast.LENGTH_LONG);
                    return;
                }
                else {
                    datas = gson.fromJson(strContact, new TypeToken<List<Contact>>() {}.getType());
                    try {
                        //전송
                        final LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        SmsManager smsManager = SmsManager.getDefault();
                        String sms = "http://maps.google.com/?q="+location.getLongitude()+","+location.getLatitude();
                        for(Contact data : datas) {
                            smsManager.sendTextMessage(data.getpnum(), null, data.getname()+" "+sms, null, null);
                        }
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_LONG).show();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch(Exception e){
                        Toast.makeText(getApplicationContext(), "전송에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }


            }
        });

        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent i = new Intent(mcontext,ParentService.class);
                bindService(i,conn,Context.BIND_AUTO_CREATE);

                LatLng ll = parentService.getchildgps();
                unbindService(conn);

                String imsi2 = "http://maps.google.com/?q="+ll.latitude+","+ll.longitude;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData.newPlainText("label",imsi2);

                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "주소가 클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
