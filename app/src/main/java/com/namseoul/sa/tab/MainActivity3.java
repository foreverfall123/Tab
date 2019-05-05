package com.namseoul.sa.tab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {

    private Button btn5;
    private ArrayList Data = new ArrayList();
    private ListView listView;
    private Gson gson;
    private SharedPreferences sp;
    private String strContact;
    private EditText name,number;
    private List<Contact> datas;
    private int i;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        btn5 = (Button)findViewById(R.id.Addbutton);

        listView = (ListView)findViewById(R.id.ListView);

        sp = getSharedPreferences("shared", MODE_PRIVATE);
        strContact = sp.getString("contact", "");
        gson = new Gson();
        if(TextUtils.isEmpty(strContact))
        {
            datas = new ArrayList<Contact>();
        }
        else {
            datas = gson.fromJson(strContact, new TypeToken<List<Contact>>() {}.getType());
        }
        for(Contact data3 : datas){
            Data.add(data3.getname());
        }
        ArrayAdapter<String> Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Data);

        btn5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.fixinfo,null);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity3.this);

                alert.setTitle("지인 등록");
                alert.setView(layout);

                name = (EditText)layout.findViewById(R.id.name);
                number = (EditText)layout.findViewById(R.id.number);

                gson = new GsonBuilder().create();
                alert.setPositiveButton("취소", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                });
                alert.setNegativeButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        contact = new Contact();
                        contact.setname(name.getText().toString().trim());
                        contact.setpnum(number.getText().toString().trim());
                        datas.add(contact);
                        String json = gson.toJson(datas, new TypeToken<List<Contact>>() {}.getType());
                        SharedPreferences.Editor editor = sp.edit();

                        editor.putString("contact", json);
                        editor.commit();
                        finish();
                        startActivity(new Intent(MainActivity3.this, MainActivity3.class));
                        Toast.makeText(getApplicationContext(),"저장 성공!!",Toast.LENGTH_LONG);
                    }
                });
                alert.show();

            }
        });

        listView.setAdapter(Adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                i = position;
                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.fixinfo,null);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity3.this);

                alert.setTitle("정보 수정");
                alert.setView(layout);

                name = (EditText)layout.findViewById(R.id.name);
                number = (EditText)layout.findViewById(R.id.number);
                name.setText(datas.get(i).getname());
                number.setText(datas.get(i).getpnum());
                //number.setText(datas.get(i).getpnum());
                alert.setPositiveButton("취소", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                });

                alert.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        SharedPreferences.Editor editor = sp.edit();

                        for(int j = i;j<datas.size();j++){
                            if(j==datas.size()-1){
                                datas.remove(datas.get(j));
                            }else {
                                datas.get(j).setname(datas.get(j + 1).getname());
                                datas.get(j).setpnum(datas.get(j + 1).getpnum());
                            }
                        }
                        String json = gson.toJson(datas, new TypeToken<List<Contact>>(){}.getType());
                        editor.putString("contact",json);
                        editor.commit();
                        finish();
                        startActivity(new Intent(MainActivity3.this, MainActivity3.class));
                    }
                });
                alert.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        datas.get(i).setname(name.getText().toString().trim());
                        datas.get(i).setpnum(number.getText().toString().trim());
                        String json = gson.toJson(datas, new TypeToken<List<Contact>>() {}.getType());

                        SharedPreferences.Editor editor = sp.edit();

                        editor.putString("contact", json);
                        editor.commit();
                        finish();
                        startActivity(new Intent(MainActivity3.this, MainActivity3.class));
                    }
                });
                alert.show();
            }
        });

    }
}
