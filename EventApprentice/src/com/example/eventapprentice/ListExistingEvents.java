package com.example.eventapprentice;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ListExistingEvents extends Activity {
	
    private MySQLiteHelper mHelper;
    private SQLiteDatabase dataBase;

    private ArrayList<String> userId = new ArrayList<String>();
    private ArrayList<String> user_fName = new ArrayList<String>();
    private ArrayList<String> user_lName = new ArrayList<String>();
    private ListView userList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_existing_events);
		// Show the Up button in the action bar.
		setupActionBar();
		mHelper = new MySQLiteHelper(this);
		userList = (ListView) findViewById(R.id.List);
		if(userList==null) Log.i("NULL","NULL");
		displayData();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
    @Override
    protected void onResume() {
    	super.onResume();
        
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_existing_events, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void displayData() {
		 List<Event> allEvents = mHelper.getAllEvent();
		 for(Event event:allEvents) {
			 userId.add(event.getTheme());
			 user_fName.add(event.getLocation());
			 user_lName.add(event.getDate());
		 }
		 
		 DisplayAdapter disadpt = new DisplayAdapter(ListExistingEvents.this,userId, user_fName, user_lName);
	     userList.setAdapter(disadpt);
	    }
}
