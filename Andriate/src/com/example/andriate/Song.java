package com.example.andriate;

public class Song {
	String title;
	String filePath;
	
	public Song(){}
	
	public Song(String title, String filePath) {
		this.title = title;
		this.filePath = filePath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
}
