package com.example.andriate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.example.andriate.FeedSongContract.FeedSong;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity{
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	static {
		if (!OpenCVLoader.initDebug())
			Log.d("OPENCV", "OPEN CV NOT LOADED!");
		else
			Log.i("OPENCV LIB", "LOADED OPENCV LIBRARY");
	}

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
		
		Bitmap original = BitmapFactory.decodeResource(this.getResources(), R.drawable.sample6);
		//Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
		Mat source = new Mat (original.getWidth(), original.getHeight(), CvType.CV_8UC1);
		
		Utils.bitmapToMat(original, source);
		Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2GRAY);
		//Utils.matToBitmap(source, original);
		Mat binaryMat = Image_Preprocessing.Convert_to_Binary(source);
		Core.bitwise_not(binaryMat, binaryMat);
		
		//Utils.matToBitmap(binaryMat, binaryBitmap);
		Mat edges = new Mat(source.rows(), source.cols(), source.type());
		Mat lines = new Mat();
		Mat drawn_lines = new Mat(source.rows(), source.cols(), source.type());
		Imgproc.Canny(binaryMat, edges, 50, 200);
		
		int threshold = source.width() / 10;
		//int threshold = 100;
		Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
		
		Image_Preprocessing.Draw_Lines(source, lines);
		double skew_angle = Image_Preprocessing.Calculate_Skew(lines);					
		Mat warp_dst = Image_Preprocessing.Deskew(binaryMat, skew_angle);
		Mat warp_dst2 = warp_dst.clone();
		
		
		Imgproc.Canny(warp_dst, edges, 50, 200);
		Mat edges2 = edges.clone();
		Mat lines2 = new Mat();
		Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
		Imgproc.HoughLines(edges2, lines2, 1, Math.PI/180, 100);
		Image_Preprocessing.Draw_Lines(drawn_lines, lines);
		
		
		List<Double> horizontal_rhos = new ArrayList<Double>();	
		Image_Preprocessing.Find_Horizontal_Rhos(lines, horizontal_rhos);
		double range = source.rows() * .04;
		System.out.println("Range = " + range);
		List<Double> line_positions = new ArrayList<Double>();
		Image_Preprocessing.Find_Line_Positions(warp_dst, horizontal_rhos, range, line_positions);
		
		
		List<Double[]> vertical_rhos = new ArrayList<Double[]>();
		Image_Preprocessing.Find_Vertical_Rhos(lines2, vertical_rhos);
		List<Double[]> line_positions2 = new ArrayList<Double[]>();
		Image_Preprocessing.Find_Vertical_Lines(warp_dst, vertical_rhos, range, line_positions2);
				
		double space_between_lines = 0;
		for (int i = 0; i < line_positions.size()-1; i++) {
			//System.out.println(line_positions.get(i));
			space_between_lines += (line_positions.get(i+1)-line_positions.get(i));
		}
		
		space_between_lines /= line_positions.size()-1;
		//System.out.println(space_between_lines);

		List<RotatedRect> note_heads = Image_Preprocessing.Find_Note_Heads(warp_dst2, range);
		//Mat contour_dst = new Mat(source.rows(), source.cols(), source.type());
		
		Image_Preprocessing.Find_Notes(note_heads, line_positions2, range);
		System.out.println("After Find_Notes");
		
		for (int i = 0; i < note_heads.size(); i++) {
			Image_Preprocessing.Find_Pitch(line_positions, note_heads.get(i), space_between_lines);
		}
		
		Image_Preprocessing.Remove_Lines(warp_dst, line_positions, line_positions2, range);
		
		for (int i = 0; i < note_heads.size(); i++) {
			//Core.ellipse(source, note_heads.get(i), new Scalar(255, 0, 0), 2);
			Core.ellipse(warp_dst, note_heads.get(i), new Scalar(255, 0, 0), -1);
		}

		Mat notes_only = new Mat(warp_dst.rows(), warp_dst.cols(), warp_dst.type());
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(warp_dst, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
		double area = notes_only.size().area();
		
		for (int i = 0; i < contours.size(); i++) {
			if (contours.get(i).size().area() < area*.00015) continue;
			//System.out.println(area*.00015);
			Imgproc.drawContours(notes_only, contours, i, new Scalar(255, 0, 0));
			//System.out.println(contours.get(i).size().area());
		}
		
		Bitmap drawn_lines_bitmap = Bitmap.createBitmap(notes_only.width(), notes_only.height(), Config.ARGB_8888);
		Utils.matToBitmap(notes_only, drawn_lines_bitmap);
		createFileFromBitmap(drawn_lines_bitmap);
		createMidiFile();
	}
	
	private void createMidiFile() {
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack = new MidiTrack();

        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

        Tempo t = new Tempo();
        t.setBpm(228);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        for(int i = 0; i < 80; i++)
        {
            int channel = 0, pitch = 1 + i, velocity = 100;
            noteTrack.insertNote(channel, pitch + 2, velocity, i * 480, 120);
        }


        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Andriate"  
    			+ File.separator + "MIDI" + timeStamp + ".mid");
        try
        {
            midi.writeToFile(output);
            MySQLiteHelper DbHelper = new MySQLiteHelper(this);
            SQLiteDatabase db = DbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(FeedSong.COLUMN_NAME_TITLE, timeStamp);
            values.put(FeedSong.COLUMN_NAME_PATH, output.getPath());
            db.insert(FeedSong.TABLE_SONG, null, values);
            db.close();
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }
	
	public void createFileFromBitmap(Bitmap bitmap) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File fn = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Andriate/Preprocessing"  
			+ File.separator + "IMG" + timeStamp + ".png");
		try {
			FileOutputStream out = new FileOutputStream(fn);
			boolean newPng = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			if(newPng)
				Log.d("NEW PNG", "TRUE");
			else
				Log.e("NEW PNG", "FALSE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
