package me.wlin;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JComponent;

public interface Check {
	public boolean startDownload(File saveFolder, boolean override, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist);	
	
	public boolean toDownload(String link, String linkSource);
	public String redirect(String link);
	
	public JComponent settings();
	public String getName();
}
