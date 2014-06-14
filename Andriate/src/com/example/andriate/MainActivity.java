package com.example.andriate;

import java.io.File;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Log.d("Directory Create", "FALSE");
		} else {
			File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+"Andriate");
			directory.mkdirs();
	        Log.i("Directory Create", "TRUE");
		}
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getScannedSongs(v);
				
			}
		});
		
	}
	
	public void getScannedSongs(View view) {
		Intent intent = new Intent(this, ListScannedSongs.class);
		startActivity(intent);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
