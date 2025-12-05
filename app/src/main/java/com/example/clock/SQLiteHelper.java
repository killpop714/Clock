package com.example.clock;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {



    //Context는 경로 name은 데이터베이스 이름, factory는 커서, 버전은 그저 버전
    public SQLiteHelper (@Nullable Context context){
        super(context, "avocadoteam", null , 1);
    }

    @Override // db 생성 함수
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Members");

        sqLiteDatabase.execSQL(
                "create table avocado (id integer primary key, " +
                        "created datetime," +
                        "updated datetime," +
                        "title text," +
                        "description text," +
                        "creator text," +
                        "portion varchar(10)," +
                        "cookingTime varchar(20)," +
                        "difficulty varchar(20)," +
                        "videoUrl text," +
                        "imageUrl text)");



    }

    @Override // db 업그레이트 즉 패치 함수
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists avocado");
        onCreate(sqLiteDatabase);
    }


    public void onHomeFetch() {
        SQLiteDatabase db = this.getReadableDatabase();


    }

    public void onRecipeInsert(){

    }


    public void onSearch(String Data) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM avocado",null);


        while(cursor.moveToNext()){
            Log.d("첫번째 인수 값", cursor.getString(0));
        }
        cursor.close();
    }
}


