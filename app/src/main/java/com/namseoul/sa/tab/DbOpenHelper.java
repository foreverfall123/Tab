package com.namseoul.sa.tab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper{

    private static final String DATABASE_NAME = "safe.db";
    private static final int DATABASE_VERSION = 1;
    public  static SQLiteDatabase mDB;
    private DataBaseHelper mDBHelper;
    private Context mCtx;

    private class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context,name,factory,version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DataBases.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            onCreate(sqLiteDatabase);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DataBaseHelper(mCtx,DATABASE_NAME, null,DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDB.close();
    }

    public long insertColumn(String name, double latitude, double longitude, int range){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.LAT, latitude);
        values.put(DataBases.CreateDB.LON, longitude);
        values.put(DataBases.CreateDB.RANGE, range);
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
    }

    public boolean updateColumn(long id, String name, double latitude, double longitude, int range){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.LAT, latitude);
        values.put(DataBases.CreateDB.LON, longitude);
        values.put(DataBases.CreateDB.RANGE, range);
        return mDB.update(DataBases.CreateDB._TABLENAME,values,"_id="+id,null) > 0;
    }

    //입력한 ID 값을 가진 DB를 지우는 메소드
    public boolean deleteColumn(long number){
        Log.i("삭제 실행","인자 : " + number);
        return mDB.delete(DataBases.CreateDB._TABLENAME,"_id ="+number,null) > 0;
    }

    public Cursor getAllColumn(){
        return mDB.query(DataBases.CreateDB._TABLENAME,null,null,null,null,null,null);
    }

    //ID로 칼럼 얻어오기
    public Cursor getColumn(long id){
        Cursor c = mDB.query(DataBases.CreateDB._TABLENAME,null,"_id="+id,null,null,null,null);
        if( c != null && c.getCount() != 0){
            c.moveToFirst();
        }
        return c;
    }

    //이름으로 검색하기
    public Cursor getMatchName(String name){
        Cursor c = mDB.rawQuery("Select * from safezone where name" + "" + name + "",null);
        return c;
    }
}
