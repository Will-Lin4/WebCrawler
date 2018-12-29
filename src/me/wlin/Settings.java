package me.wlin;

import java.io.File;
import java.util.ArrayList;

public class Settings {

	private Check check;
	private File save;
	
	private boolean downloadImages;
	private boolean downloadCSS;
	private boolean downloadJS;
	
	private ArrayList<String> blacklist;
	
	public Settings(File save, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist, Check check){
		this.save = save;
		this.downloadImages = downloadImages;
		this.downloadCSS = downloadCSS;
		this.downloadJS = downloadJS;
		this.blacklist = blacklist;
		this.check = check;
	}
	
}
