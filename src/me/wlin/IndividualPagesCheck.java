package me.wlin;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class IndividualPagesCheck implements Check {
	private JTextArea url;
	
	@Override
	public boolean startDownload(File saveFolder, boolean override, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist){
		if(url.getText().equals("")){
			return false;
		}
		
		Downloader d = new Downloader(saveFolder, override, downloadImages, downloadCSS, downloadJS, blacklist);
		d.download(url.getText().split("\\r?\\n"), this);

		return true;
	}
	
	@Override
	public JComponent settings(){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel urlLabel = new JLabel("Pages:");
		urlLabel.setBorder(new EmptyBorder(0,0,5,0));
		url = new JTextArea();
		
		JScrollPane urlBox = new JScrollPane(url);
		urlBox.setPreferredSize(new Dimension(0,0));
		panel.add(urlLabel,BorderLayout.PAGE_START);
		panel.add(urlBox);		
		return panel;
	}
	
	@Override
	public boolean toDownload(String link, String linkSource) {	
		if(linkSource != null){
			return false;
		}
		
		return true;
	}

	@Override
	public String redirect(String link) {
		return link;
	}
	
	@Override
	public String getName(){
		return "Individual Pages";
	}

}
