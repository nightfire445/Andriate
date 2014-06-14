package com.example.andriate;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.andriate.FeedSongContract.FeedSong;

public class MySQLiteHelper extends SQLiteOpenHelper {

	  static final String DATABASE_NAME = "Songs.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + FeedSong.TABLE_SONG + "(" + FeedSong._ID + " INTEGER PRIMARY KEY,"
	      + FeedSong.COLUMN_NAME_TITLE+" TEXT,"+FeedSong.COLUMN_NAME_PATH+" TEXT"+ ")";
	  
	  private static final String SQL_DELETE_ENTRIES = 
			  "DROP TABLE IF EXISTS " + FeedSong.TABLE_SONG;

	  public MySQLiteHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(MySQLiteHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL(SQL_DELETE_ENTRIES);
	    onCreate(db);
	  }
	  
	  public List<String> getSongTitles(){
		  List<String> songTitles = new ArrayList<String>();		  
		  SQLiteDatabase db = this.getWritableDatabase();
		  String selectQuery = "SELECT * FROM "+FeedSong.TABLE_SONG;
		  Cursor cursor = db.rawQuery(selectQuery, null);
		  if(cursor.moveToFirst()) {
			  do {
				  songTitles.add(cursor.getString(1));
			  } while (cursor.moveToNext());
		  }
		  return songTitles;
	  }
	  
	  public String getPath(String title){
		  String path = null;
		  SQLiteDatabase db = this.getReadableDatabase();
		  Cursor cursor = db.query(FeedSong.TABLE_SONG, new String[]{FeedSong.COLUMN_NAME_PATH}, FeedSong.COLUMN_NAME_TITLE +"=?", 
				  new String[] {title}, "NULL", "NULL", "NULL");
		  if(cursor.moveToFirst())
			  path = cursor.getString(0);
		  else
			  Log.d("ERROR", "No File Found in DB");
		  return path;
	  }

}
