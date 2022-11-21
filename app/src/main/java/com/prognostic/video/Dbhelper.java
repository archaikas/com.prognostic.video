package com.prognostic.video;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


/**
 * Created by Nitin on 18-May-17.
 */

public class Dbhelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Video.db";
    private static String DATABASE_PATH = null;
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    static {
        DATABASE_PATH = null;
    }

    public Dbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        DATABASE_PATH = new StringBuilder(String.valueOf(context.getFilesDir().getParent())).append("/databases/").toString();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public ArrayList<VideoModel> getVideos()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM video_master", null);
        ArrayList<VideoModel> recipeList = new ArrayList<>();
        if (c == null || !c.moveToFirst()) {
            c.close();
            db.close();
            return recipeList;
        }
        do {
            VideoModel videoModel = new VideoModel();
            videoModel.id = c.getString(0);
            videoModel.video_path = c.getString(1);
            recipeList.add(videoModel);
        } while (c.moveToNext());
        c.close();
        db.close();
        return recipeList;
    }

    public String link()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM link_master" , null);
        String link = "";
        if(c != null)
        {
            c.moveToNext();
            String id = c.getString(0);
            link = c.getString(1);
            c.close();
            db.close();
            return link;
        }
        return "";
    }
}
