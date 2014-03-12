package com.example.eventapprentice;


public class Event {
	String theme;
	String date;
	String location;
	

	public Event(){}
	
	public Event(String theme, String date, String location) {
		this.theme = theme;
		this.date = date;
		this.location = location;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
