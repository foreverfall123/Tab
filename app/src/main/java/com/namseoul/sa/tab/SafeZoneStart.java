package com.namseoul.sa.tab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SafeZoneStart extends AppCompatActivity {

    private static final String TAG = "TestDataBase";
    private DbOpenHelper mDbOpenHelper;
    private Cursor mCursor;
    private InfoClass mInfoClass;
    private ArrayList<InfoClass> mInfoArr;
    private CustomAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safezonestart_activity);

        setLayout();

        mDbOpenHelper = new DbOpenHelper(this);
        try{
            mDbOpenHelper.open();
        }catch(SQLException e){
            e.printStackTrace();
        }

        Intent intent = getIntent();

        if(intent != null){
            mDbOpenHelper.insertColumn(
                    intent.getStringExtra("name"),
                    intent.getDoubleExtra("latitude",0),
                    intent.getDoubleExtra("longitude",0),
                    intent.getIntExtra("range",0)
            );

        }

        mInfoArr = new ArrayList<InfoClass>();

        doWhileCursorToArray();

        mAdapter = new CustomAdapter(this, mInfoArr);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

                new AlertDialog.Builder(SafeZoneStart.this)
                        .setTitle("안전지대 삭제 확인")
                        .setMessage(mInfoArr.get(position).getName() + " 안전지대를 정말 삭제 하시겠습니까?")
                        .setIcon(R.drawable.ic_launcher_background)
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                InfoClass test = mInfoArr.get(position);
                                boolean result = mDbOpenHelper.deleteColumn(test.get_id());

                                if(result){
                                    mInfoArr.remove(position);
                                    mAdapter.setArrayList(mInfoArr);
                                    mAdapter.notifyDataSetChanged();
                                }else{
                                    Toast.makeText(SafeZoneStart.this, "INDEX Chack Plz", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(SafeZoneStart.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

                return false;
            }
        });



    }

    private void doWhileCursorToArray(){
        mCursor = null;
        mCursor = mDbOpenHelper.getAllColumn();

        while (mCursor.moveToNext()){
            mInfoClass = new InfoClass(
                    mCursor.getInt(mCursor.getColumnIndex("_id")),
                    mCursor.getString(mCursor.getColumnIndex("name")),
                    mCursor.getDouble(mCursor.getColumnIndex("latitude")),
                    mCursor.getDouble(mCursor.getColumnIndex("longitude")),
                    mCursor.getInt(mCursor.getColumnIndex("range"))
            );

            mInfoArr.add(mInfoClass);
        }

        mCursor.close();
    }

    public void btnAdd(View v){
        mDbOpenHelper.insertColumn(
                mEditTexts[Contents.NAME].getText().toString().trim(),
                Double.parseDouble(mEditTexts[Contents.LATITUDE].getText().toString().trim()),
                Double.parseDouble(mEditTexts[Contents.LONGITUDE].getText().toString().trim()),
                Integer.parseInt(mEditTexts[Contents.RANGE].getText().toString().trim())
        );

        mInfoArr.clear();

        doWhileCursorToArray();

        mAdapter.setArrayList(mInfoArr);
        mAdapter.notifyDataSetChanged();
        mCursor.close();
    }

    // 레이아웃 셋팅

    private EditText[] mEditTexts;
    private ListView mListView;

    private void setLayout(){
        mEditTexts = new EditText[]{
                (EditText)findViewById(R.id.etName),
                (EditText)findViewById(R.id.etLatitude),
                (EditText)findViewById(R.id.etLongitude),
                (EditText)findViewById(R.id.etRange)
        };

        mListView = (ListView)findViewById(R.id.list);

    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    public void mapadd(View v){
        Intent i = new Intent(this,MapActivity.class);
        startActivity(i);
        finish();
    }

    public void clearbtn(View v){

    }
}
