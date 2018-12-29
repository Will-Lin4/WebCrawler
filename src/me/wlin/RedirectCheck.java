package me.wlin;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

public class RedirectCheck implements Check {

	private HashMap<String, Integer> links;
	private JSpinner redirectCount;
	private JTextField urlField;
	
	@Override
	public boolean toDownload(String link, String linkSource) {
		link = link.replace("//www.", "//").toLowerCase();
		if(links.containsKey(link)){
			return false;
		}
		
		if(linkSource != null){
			linkSource = linkSource.replace("//www.", "//").toLowerCase();
			if(links.containsKey(linkSource)){
				if(links.get(linkSource) + 1 > (int) redirectCount.getValue()){
					return false;
				}

				links.put(link, links.get(linkSource) + 1);
				return true;
			}
		}
		
		links.put(link, 0);
		return true;
	}

	@Override
	public String redirect(String link) {
		return link;
	}

	@Override
	public JComponent settings() {
		JPanel config = new JPanel(new SpringLayout());
		
		JLabel urlLabel = new JLabel("URL:");
		urlField = new JTextField();
		urlField.setPreferredSize(new Dimension(250,20));
		JLabel redirectsLabel = new JLabel("Redirects:");
		redirectCount = new JSpinner(new SpinnerNumberModel(1,1,99,1));
		((JSpinner.DefaultEditor)redirectCount.getEditor()).getTextField().setColumns(2);

		config.add(urlLabel);
		config.add(urlField);
		
		config.add(redirectsLabel);
		config.add(redirectCount);

		SpringLayout layout = (SpringLayout) config.getLayout();
		layout.putConstraint(SpringLayout.NORTH, urlLabel, 15, SpringLayout.NORTH, config);
		layout.putConstraint(SpringLayout.WEST, urlLabel, 15, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, redirectsLabel, 15, SpringLayout.SOUTH, urlLabel);
		layout.putConstraint(SpringLayout.WEST, redirectsLabel, 15, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, urlField, 15, SpringLayout.NORTH, config);
		layout.putConstraint(SpringLayout.WEST, urlField, 100, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, redirectCount, 15, SpringLayout.SOUTH, urlField);
		layout.putConstraint(SpringLayout.WEST, redirectCount, 100, SpringLayout.WEST, config);
	
		return config;
	}

	@Override
	public boolean startDownload(File saveFolder, boolean override, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist) {
		if(urlField.getText().equals("")){
			return false;
		}
		
		links = new HashMap<>();
		Downloader d = new Downloader(saveFolder, override, downloadImages, downloadCSS, downloadJS, blacklist);
		d.download(urlField.getText(), this);
		return true;
	}
	
	@Override
	public String getName() {
		return "Page with Redirect";
	}

}
