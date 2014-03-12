package com.example.eventapprentice;

import com.example.eventapprentice.FeedEventContract.FeedEvent;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class ImportGuestList extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_guest_list);
		// Show the Up button in the action bar.
		setupActionBar();
		NewEventDBEntry();
		
	}
	
	private void NewEventDBEntry() {
		Intent intent = getIntent();
		String theme = intent.getStringExtra(CreateNewEvent.THEME);
		String date = intent.getStringExtra(CreateNewEvent.DATE);
		String location = intent.getStringExtra(CreateNewEvent.LOCATION);
		Event newEvent = new Event(theme, date, location);
		MySQLiteHelper DbHelper = new MySQLiteHelper(this);
		SQLiteDatabase db = DbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FeedEvent.COLUMN_NAME_THEME, newEvent.getTheme());
		values.put(FeedEvent.COLUMN_NAME_DATE, newEvent.getDate());
		values.putNull(FeedEvent.COLUMN_NAME_GUESTS);
		values.put(FeedEvent.COLUMN_NAME_LOCATION, newEvent.getLocation());
		db.insert(FeedEvent.TABLE_EVENT, null, values);
		db.close();
		
		
	}


	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.import_guest_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
