package com.example.jevil.fixer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "saveDB";
    static final String TABLE_SAVE = "save";
    static final String KEY_ID = "_id";
    static final String KEY_CURRENCY = "currency";
    static final String KEY_DATE = "date";
    static final String KEY_VALUE = "value";

    // конструктор БД
    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //вызывается при создании базы данных
        db.execSQL("create table " + TABLE_SAVE + "("
                + KEY_ID + " integer primary key,"
                + KEY_CURRENCY + " text,"
                + KEY_DATE + " text,"
                + KEY_VALUE + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //вызывается при изменении версии базы данных
        db.execSQL("drop table if exists " + TABLE_SAVE); //удаляем текущую таблицу
        onCreate(db); //создаем новую заново
    }
}
