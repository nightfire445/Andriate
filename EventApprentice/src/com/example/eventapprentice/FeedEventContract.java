package com.example.eventapprentice;

import android.provider.BaseColumns;

public final class FeedEventContract {
	
	public FeedEventContract() {}
	
	public static abstract class FeedEvent implements BaseColumns {
		public static final String TABLE_EVENT = "events";
		public static final String COLUMN_NAME_EVENT_ID = "eventid";
		public static final String COLUMN_NAME_THEME = "name";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_GUESTS = "guests";
	}

}
