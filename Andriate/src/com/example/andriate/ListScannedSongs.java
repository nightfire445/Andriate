package com.example.andriate;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ListScannedSongs extends Activity{

	    private MySQLiteHelper mHelper;

	    ArrayList<String> songTitleList;
        public void onCreate(Bundle saveInstanceState)
        {
                super.onCreate(saveInstanceState);
                setContentView(R.layout.activity_list_scanned_songs);
                setupActionBar();
                mHelper = new MySQLiteHelper(this);
                ListView songTitlesList = (ListView) findViewById(R.id.listViewSongs);
                songTitleList = (ArrayList<String>) mHelper.getSongTitles();
                ArrayAdapter<String> arrayAdapter = 
                		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,songTitleList);
                songTitlesList.setAdapter(arrayAdapter);
                
                songTitlesList.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View v,
							int position, long arg3) {
						MediaPlayer player;
						String selectedSong = songTitleList.get(position);
						player = MediaPlayer.create(ListScannedSongs.this, Uri.parse(mHelper.getPath(selectedSong)));
						player.start();
					}
                	
                });
                
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
			getMenuInflater().inflate(R.menu.list_scanned_songs, menu);
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
