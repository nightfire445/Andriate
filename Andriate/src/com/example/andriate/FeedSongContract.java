package com.example.andriate;

import android.provider.BaseColumns;

public class FeedSongContract {
	
	public FeedSongContract() {}
	
	public static abstract class FeedSong implements BaseColumns {
		public static final String TABLE_SONG = "songs";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_PATH = "path";
	}

}
