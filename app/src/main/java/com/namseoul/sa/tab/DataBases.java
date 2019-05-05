package com.namseoul.sa.tab;

import android.provider.BaseColumns;

public class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String LAT = "latitude";
        public static final String LON =  "longitude";
        public static final String RANGE = "range";
        public static final String _TABLENAME = "safezone";
        public static final String _CREATE = "create table " + _TABLENAME + "("
                + _ID + " integer primary key autoincrement, "
                + NAME + " text not null , "
                + LAT + " double not null , "
                + LON + " double not null , "
                + RANGE + " integer not null );";
    }
}
