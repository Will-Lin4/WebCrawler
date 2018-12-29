package me.wlin;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;

public class RedditCheck implements Check{
	
	private JTextField subredditField;
	private JSpinner pageCount;
	private JCheckBox external;
	
	@Override
	public boolean startDownload(File saveFolder, boolean override, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist){
		Downloader d = new Downloader(saveFolder, override, downloadImages, downloadCSS, downloadJS, blacklist);
		d.download("https://www.reddit.com/" + subredditField.getText(), this);	
		return true;
	}
	
	@Override
	public JComponent settings(){
		JPanel config = new JPanel(new SpringLayout());
		
		JLabel subredditLabel = new JLabel("Subreddit:");
		subredditField = new JTextField("r/");
		subredditField.setPreferredSize(new Dimension(150,20));
		JLabel pagesLabel = new JLabel("Pages to Download:");
		pageCount = new JSpinner(new SpinnerNumberModel(1,1,99,1));
		((DefaultEditor)pageCount.getEditor()).getTextField().setColumns(2);
		
		JLabel externalLabel = new JLabel("Download External Links:");
		external = new JCheckBox();
		external.setSelected(true);
		
		config.add(subredditLabel);
		config.add(subredditField);
		
		config.add(pagesLabel);
		config.add(pageCount);
		
		config.add(externalLabel);
		config.add(external);
		
		SpringLayout layout = (SpringLayout) config.getLayout();
		layout.putConstraint(SpringLayout.NORTH, subredditLabel, 15, SpringLayout.NORTH, config);
		layout.putConstraint(SpringLayout.WEST, subredditLabel, 15, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, pagesLabel, 15, SpringLayout.SOUTH, subredditLabel);
		layout.putConstraint(SpringLayout.WEST, pagesLabel, 15, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, subredditField, 15, SpringLayout.NORTH, config);
		layout.putConstraint(SpringLayout.WEST, subredditField, 200, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, pageCount, 15, SpringLayout.SOUTH, subredditField);
		layout.putConstraint(SpringLayout.WEST, pageCount, 200, SpringLayout.WEST, config);
		
		layout.putConstraint(SpringLayout.NORTH, externalLabel, 15, SpringLayout.SOUTH, pagesLabel);
		layout.putConstraint(SpringLayout.WEST, externalLabel, 15, SpringLayout.WEST, config);	
		
		layout.putConstraint(SpringLayout.NORTH, external, 13, SpringLayout.SOUTH, pagesLabel);
		layout.putConstraint(SpringLayout.WEST, external, 200, SpringLayout.WEST, config);		
		return config;
	}
		
	@Override
	public boolean toDownload(String link, String linkSource) {
		if(linkSource == null){
			return true;
		}
		
		link = link.toLowerCase();
		linkSource = linkSource.toLowerCase();
		
		if((int)pageCount.getValue() > 1 && link.contains("reddit.com/") && link.contains("count=")){
			if(link.contains("after=")){
				for(int i = 1; i < (int)pageCount.getValue(); i++){
					if(link.contains("count=" + i * 25)){
						return true;
					}
				}
			}else if(link.contains("before=")){
				return true;
			}
		}
		
		// Coming from reddit.com
		if(linkSource.contains("reddit.com")){
			// Coming from comment
			if(linkSource.contains("/comments/")){
				return false;
			
			// Going to other parts of reddit outside of comments
			}else if(link.contains("reddit.com") && !link.contains("/comments/")){
				return false;
				
			// Going to Reddit App links
			}else if(link.contains("itunes.apple.com/us/app/reddit-the-official-app/id1064216828?mt=8") || link.contains("play.google.com/store/apps/details?id=com.reddit.frontpage") || link.contains("reddit.zendesk.com")){
				return false;
				
			// Going anywhere else
			}else if(linkSource.contains("/r/") && link.contains("reddit.com") && link.contains("/r/")){
				String strippedSource = linkSource;
				if(strippedSource.contains("?")){
					strippedSource = strippedSource.substring(0,linkSource.lastIndexOf("?"));
				}
				
				String stippedSource = strippedSource.replace("/", "").replace("www.", "");
				String strippedLink = link.replace("/", "").replace("www.", "");
				return strippedLink.toLowerCase().contains(stippedSource.toLowerCase());
				
			}else if(external.isSelected()){
				return true;
			}
		}
		
		// Accept visual Files
		if(link.contains("?")){
			link = link.substring(0,link.lastIndexOf("?"));
		}
		
		if(link.contains(".")){			
			String fileType = link.substring(link.lastIndexOf("."));
			if(fileType.contains(".css") || fileType.contains(".js") || fileType.contains(".png") || fileType.contains(".ico") || fileType.contains(".jpg") || fileType.contains(".srv") || fileType.contains(".gif")){
				return true;
			}	
		}
		
		return false;
	}

	@Override
	public String redirect(String link) {
		
		if(link.contains("?utm_content")){
			return link.substring(0,link.indexOf("?utm_content"));
		}
		
		if(link.contains("//out.reddit.com") && link.contains("?url=") && link.contains("&token=")){
			return link.substring(link.indexOf("?url=") + 5, link.indexOf("&token=")).replace("%3A", ":").replace("%2F", "/").replace("%3B", ";").replace("%3F", "?").replace("%40", "@").replace("%26", "&").replace("%3D", "=");
		}
		
		return link;
	}
	
	@Override
	public String getName(){
		return "Reddit";
	}
	
}
