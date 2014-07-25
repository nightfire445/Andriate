package com.example.andriate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

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
		Button btn2 = (Button) findViewById(R.id.button2);
		btn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getScannedSongs(v);
				
			}
		});
		Button btn1 = (Button) findViewById(R.id.button1);
		btn1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				processPicture(v);
				
			}
		});
		
	}
	
	public void processPicture(View view) {
		Uri fileUri;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);		
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		
		//Log.d("URI file path", fileUri.getPath());
		Image_Preprocessing preProcessingImage = new Image_Preprocessing();
		
		Bitmap original = BitmapFactory.decodeResource(this.getResources(), R.drawable.sample1);
		//Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
		Mat source = new Mat (original.getWidth(), original.getHeight(), CvType.CV_8UC1);
		
		Utils.bitmapToMat(original, source);
		Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2GRAY);
		//Utils.matToBitmap(source, original);
		Mat binaryMat = preProcessingImage.Convert_to_Binary(source);
		Core.bitwise_not(binaryMat, binaryMat);
		Bitmap binaryBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Config.ARGB_8888);
		Utils.matToBitmap(binaryMat, binaryBitmap);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File fn = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Andriate/Preprocessing"  
			+ File.separator + "IMG" + timeStamp + ".png");
		try {
			FileOutputStream out = new FileOutputStream(fn);
			boolean newPng = binaryBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			if(newPng)
				Log.d("NEW PNG", "TURE");
			else
				Log.e("NEW PNG", "FALSE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Mat edges = new Mat(source.rows(), source.cols(), source.type());
		Mat lines = new Mat();
		Mat drawn_lines = new Mat(source.rows(), source.cols(), source.type());
		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
			Log.d("CAMERA", "Picture taken and saved.");
		else
				Log.d("PICTURE FAIL", "Picture not taken");
	}
	
	private static Uri getOutputMediaFileUri(){
	      return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(){
	    
	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Andriate/Preprocessing");
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    return mediaFile;
	}
	
	
	public void getScannedSongs(View view) {
		MySQLiteHelper mHelper = new MySQLiteHelper(this);
		List<String> songTitles = mHelper.getSongTitles();
		if(songTitles.size()==0) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("No Scanned Music Found!");
			alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		else{
			Intent intent = new Intent(this, ListScannedSongs.class);
			startActivity(intent);
		}
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
