package com.example.eventapprentice;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.eventapprentice.FeedEventContract.FeedEvent;

public class MySQLiteHelper extends SQLiteOpenHelper {

	  static final String DATABASE_NAME = "Events.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + FeedEvent.TABLE_EVENT + "(" + FeedEvent._ID + " INTEGER PRIMARY KEY,"
	      + FeedEvent.COLUMN_NAME_THEME+" TEXT,"+FeedEvent.COLUMN_NAME_DATE+" TEXT,"
	      + FeedEvent.COLUMN_NAME_LOCATION+" TEXT,"
	      + FeedEvent.COLUMN_NAME_GUESTS+" TEXT" + ")";
	  
	  private static final String SQL_DELETE_ENTRIES = 
			  "DROP TABLE IF EXISTS " + FeedEvent.TABLE_EVENT;

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
	  
	  public List<Event> getAllEvent(){
		  List<Event> eventList = new ArrayList<Event>();
		  String selectQuery = "SELECT * FROM "+FeedEvent.TABLE_EVENT;
		  SQLiteDatabase db = this.getWritableDatabase();
		  Cursor cursor = db.rawQuery(selectQuery, null);
		  if(cursor.moveToFirst()) {
			  do {
				  Event event = new Event();
				  event.setTheme(cursor.getString(1));
				  event.setDate(cursor.getString(2));
				  event.setLocation(cursor.getString(3));
				  eventList.add(event);
			  } while (cursor.moveToNext());
		  }
		  return eventList;
	  }

}
