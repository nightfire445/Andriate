package com.example.eventapprentice;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.eventapprentice.FeedEventContract.FeedEvent;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ImportFBEvent extends ListActivity{
	
	private List<String> mEventNames;
	private List<String> mEventIDs;
	private Session mFBSession;
	private ProgressDialog pDialog;
	
	//json node names
	private static final String TAG_ID = "id";
	private static final String TAG_EVENT_NAME = "name";
	private static final String TAG_DATA = "data";
	private static final String TAG_GUEST_NAME = "name";
	private static final String TAG_INVITED = "invited";
	private static final String TAG_DATE_TIME = "start_time";
	private static final String TAG_PLACE = "location";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mEventNames = new ArrayList<String>();
        mEventIDs = new ArrayList<String>();
		mFBSession = Session.getActiveSession();
		if(mFBSession == null){
			//add Error Handling here, probably new login stuff
			//for now:
			mEventNames.add("Something Went Wrong");
			mEventIDs.add("1");
		}
		
		//maybe not necessary, request executes quickly
		pDialog = new ProgressDialog(ImportFBEvent.this);
		pDialog.setMessage("Please Wait...");
		pDialog.setCancelable(false);
		pDialog.show();

		new Request(
			    mFBSession,
			    "/me/events",
			    null,
			    HttpMethod.GET,
			    new Request.Callback() {
			        public void onCompleted(Response response) {
			        	if(response.getGraphObject() == null){
			        		Log.d("TAG", "No Response for /me/events");
			        		return;
			        	}
			        	try{
				        	JSONObject jsonObj = response.getGraphObject().getInnerJSONObject();
				        	JSONArray events = jsonObj.getJSONArray(TAG_DATA);
				        	for(int i = 0; i < events.length(); i++){
				        		JSONObject currEvent = events.getJSONObject(i);
				        		String id = currEvent.getString(TAG_ID);
				        		String name = currEvent.getString(TAG_EVENT_NAME);
				        		mEventNames.add(name);
				        		mEventIDs.add(id);
				        		
				        	}

			        	} catch(JSONException e){
			        		e.printStackTrace();
			        	}
						if(pDialog.isShowing()){
							pDialog.dismiss();
						}
						ListAdapter adapter = new ArrayAdapter<String>(ImportFBEvent.this, R.layout.list_item, mEventNames);
						setListAdapter(adapter);

			        }
			    }
			).executeAsync();



	    ListView lv = getListView();

	    // listening to single list item on click
	    lv.setOnItemClickListener(new OnItemClickListener() {
	      public void onItemClick(AdapterView<?> parent, View view,
	          int position, long id) {

	          // selected item 
	          final String eventName = ((TextView) view).getText().toString();
	          int eventNum = mEventNames.indexOf(eventName);
	          //deal with on click
	          String eventID = mEventIDs.get(eventNum);
	          String requestString = "/" + eventID  + "?fields=invited, start_time, location";
	          pDialog = new ProgressDialog(ImportFBEvent.this);
	          pDialog.setMessage("Importing Event...");
	          pDialog.setCancelable(false);
	          pDialog.show();
	          //get info for specific event
	          //TO DO: After event is imported, go to an activity to display the event
	          new Request(
	        		  mFBSession,
	        		  requestString,
	        		  null,
	        		  HttpMethod.GET,
	        		  new Request.Callback() {
	        			  public void onCompleted(Response response) {
	        				  if(response.getGraphObject() == null){
	        					  Log.d("TAG", "No Response for " + response.getRequest().toString());
	        					  pDialog.dismiss();
	        					  return;
	        				  }

	        				  try{
	        					  JSONObject jsonObj = response.getGraphObject().getInnerJSONObject();
	        					  Log.d("TAG", jsonObj.toString());
	        					  String start_time = jsonObj.getString(TAG_DATE_TIME);
	        					  //TO DO: parse string, format YYYY-MM-DDTHH:MM:SS
	        					  
	        					  String location = jsonObj.getString(TAG_PLACE);
	        					  JSONObject invObj = jsonObj.getJSONObject(TAG_INVITED);
	        					  JSONArray invited = invObj.getJSONArray(TAG_DATA);
	        					  List<String> guests = new ArrayList<String>();
	        					  for(int i = 0; i < invited.length(); i++){
	        						  JSONObject guestJSON = invited.getJSONObject(i);
	        						  guests.add(guestJSON.getString(TAG_GUEST_NAME));
	        					  }
	        					  MySQLiteHelper DbHelper = new MySQLiteHelper(ImportFBEvent.this);
	        					  SQLiteDatabase db = DbHelper.getWritableDatabase();
	        					  ContentValues values = new ContentValues();
	        					  values.put(FeedEvent.COLUMN_NAME_THEME, eventName);
	        					  values.put(FeedEvent.COLUMN_NAME_DATE, start_time);
	        					  //TO DO: figure out how to get guest list into database
	        					  values.putNull(FeedEvent.COLUMN_NAME_GUESTS);
	        					  values.put(FeedEvent.COLUMN_NAME_LOCATION, location);
	        					  db.insert(FeedEvent.TABLE_EVENT, null, values);
	        					  db.close();


	  							} catch(JSONException e){

	  								e.printStackTrace();
	  							}
	  							if(pDialog.isShowing()){
	  								pDialog.dismiss();
	  							}
	  							
	  						}
	  					}
	  				).executeAsync();

	      }
	    });
	    
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



}
