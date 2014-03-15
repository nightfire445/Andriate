package com.example.eventapprentice;

import com.example.eventapprentice.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends FragmentActivity {

	private MainFragment mainFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null){
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
		}else{
			mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
		}        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void newEvent(View view) {
		Intent intent = new Intent(this, CreateNewEvent.class);
		startActivity(intent);
	}
	
	public void importFBEvent(View view){
		
		Intent intent = new Intent(this, ImportFBEvent.class);
		startActivity(intent);
	}
	
	public void getExistingEvents(View view) {
		Intent intent = new Intent(this, ListExistingEvents.class);
		startActivity(intent);
	}

}
