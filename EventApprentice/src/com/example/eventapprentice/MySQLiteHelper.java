package com.example.eventapprentice;

import com.example.eventapprentice.FeedEventContract.FeedEvent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	  private static final String DATABASE_NAME = "Events.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + FeedEvent.TABLE_EVENT + "(" + FeedEvent._ID + " INTEGER PRIMARY KEY,"
	      + FeedEvent.COLUMN_NAME_THEME+"TEXT"+FeedEvent.COLUMN_NAME_DATE+"TEXT"
	      + FeedEvent.COLUMN_NAME_LOCATION+"TEXT"
	      + FeedEvent.COLUMN_NAME_GUESTS+"BLOB" + ")";
	  
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

}
