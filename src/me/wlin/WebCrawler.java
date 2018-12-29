package me.wlin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.DefaultCaret;

public class WebCrawler {
	private static ArrayList<Check> checks;
	
	public static final int WIDTH = 950;
	public static final int HEIGHT = 700;
	
	public static void main(String[] args) throws Exception {
		checks = new ArrayList<>();
		checks.add(new IndividualPagesCheck());
		checks.add(new RedirectCheck());
		checks.add(new RedditCheck());
		initUI();
	}
	
	private static ArrayList<String> alpha(ArrayList<String> notAlpha){
		ArrayList<String> alpha = new ArrayList<>();
		for(String string : notAlpha){
			if(alpha.isEmpty()){
				alpha.add(string);
			}else{
				for(int i = 0; i < alpha.size(); i++){
					int compare = alpha.get(i).compareTo(string);
					if(compare >= 0){
						alpha.add(i, string);
						break;
					}else if(i == alpha.size() - 1){
						alpha.add(string);
						break;
					}
				}
				
			}
			
		}
		
		return alpha;
	}
	
    private static void initUI(){
        JFrame frame = new JFrame("WebCrawler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH,HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        frame.setContentPane(content);
        
        JTabbedPane tabs = new JTabbedPane();
		tabs.setBorder(null);
        content.add(tabs, BorderLayout.CENTER); 
        
        JPanel download = new JPanel(new GridBagLayout());
        tabs.addTab("Download", download); 
        
        DefaultListModel<String> configuration = new DefaultListModel<>();
        JList<String> downloadOptions = new JList<>(configuration);
        downloadOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   
        
        JPanel options = new JPanel(new CardLayout());      
        downloadOptions.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == 1){
					((CardLayout)(options.getLayout())).show(options, downloadOptions.getSelectedValue());
				}
			}
		});
		
		for(Check check : checks){
			configuration.addElement(check.getName());
			options.add(check.settings(), check.getName());			
		}
		
		downloadOptions.setSelectedIndex(0);
		SpringLayout springLayout = new SpringLayout();
		JPanel settings = new JPanel(springLayout);
		settings.setBorder(new CompoundBorder(new TitledBorder("Settings"), new EmptyBorder(5,5,5,5)));
		
		JButton selectFileButton = new JButton("Select Save Location");
		selectFileButton.setPreferredSize(new Dimension(200, 25));
		
		JLabel directoryLabel = new JLabel();
		directoryLabel.setFont(directoryLabel.getFont().deriveFont(10.0f));
		JFileChooser saveFile = new JFileChooser();
		saveFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		JLabel foundLabel = new JLabel("Found Existing Crawl");
		foundLabel.setVisible(false);
		
		JRadioButton override = new JRadioButton("Override");
		JRadioButton extend = new JRadioButton("Extend");
		ButtonGroup existingAction = new ButtonGroup();
		override.setSelected(true);
		existingAction.add(override);
		existingAction.add(extend);
		override.setVisible(false);
		extend.setVisible(false);
		
		selectFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				if(saveFile.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
					foundLabel.setVisible(false);
					override.setVisible(false);
					extend.setVisible(false);					
					File folder = saveFile.getSelectedFile();
					String folderName = folder.toString();
					if(folder.exists() && folder.isDirectory()){
						directoryLabel.setForeground(Color.GRAY);
						for(File f : folder.listFiles()){
							String fName = f.toString();
							if(f.isFile() && fName.substring(fName.lastIndexOf(File.separator) + 1).equalsIgnoreCase("sites.wcrawl")){
								foundLabel.setVisible(true);
								override.setVisible(true);
								extend.setVisible(true);
								break;
							}
						}
						
					}else{
						directoryLabel.setForeground(Color.RED);
						saveFile.setSelectedFile(null);							
					}
					
					if(folderName.length() <= 30){
						directoryLabel.setText(folderName);
					}else if(folderName.length() <= 60){
						directoryLabel.setText("<html>" + folderName.substring(0, 30) + "...<br>" + folderName.substring(30) + "</html>");
					}else{
						directoryLabel.setText("<html>..." + folderName.substring(folderName.length() - 60, folderName.length() - 30) + "...<br>" + folderName.substring(folderName.length() - 30) + "</html>");
					}						
				}	
			}
		
		});
		
		JLabel optionalDownloadsLabel = new JLabel("Optional Downloads");
		JCheckBox downloadImages = new JCheckBox("Images");
		JCheckBox downloadCSS = new JCheckBox("CSS");
		JCheckBox downloadJS = new JCheckBox("JavaScript");
		
		downloadImages.setSelected(true);
		downloadCSS.setSelected(true);
		downloadJS.setSelected(true);
		
		JLabel blacklistLabel = new JLabel("Blacklisted Terms");
		JTextArea blacklistArea = new JTextArea(5,19);
		JScrollPane blacklist = new JScrollPane(blacklistArea); 
		
		settings.add(selectFileButton);
		settings.add(directoryLabel);
		settings.add(foundLabel);
		settings.add(override);
		settings.add(extend);
		settings.add(optionalDownloadsLabel);
		settings.add(downloadImages);
		settings.add(downloadCSS);
		settings.add(downloadJS);
		settings.add(blacklistLabel);
		settings.add(blacklist);
		
		springLayout.putConstraint(SpringLayout.WEST, selectFileButton, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, directoryLabel, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.NORTH, directoryLabel, 0, SpringLayout.SOUTH, selectFileButton);
		
		springLayout.putConstraint(SpringLayout.NORTH, foundLabel, 30, SpringLayout.SOUTH, selectFileButton);
		springLayout.putConstraint(SpringLayout.NORTH, override, 0, SpringLayout.SOUTH, foundLabel);
		springLayout.putConstraint(SpringLayout.NORTH, extend, 0, SpringLayout.SOUTH, override);
		springLayout.putConstraint(SpringLayout.WEST, foundLabel, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, override, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, extend, 10, SpringLayout.WEST, settings);
		
		springLayout.putConstraint(SpringLayout.WEST, optionalDownloadsLabel, 240, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, downloadImages, 240, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, downloadCSS, 240, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.WEST, downloadJS, 240, SpringLayout.WEST, settings);
		
		springLayout.putConstraint(SpringLayout.NORTH, downloadImages, 20, SpringLayout.NORTH, optionalDownloadsLabel);
		springLayout.putConstraint(SpringLayout.NORTH, downloadCSS, 0, SpringLayout.SOUTH, downloadImages);
		springLayout.putConstraint(SpringLayout.NORTH, downloadJS, 0, SpringLayout.SOUTH, downloadCSS);
		
		springLayout.putConstraint(SpringLayout.WEST, blacklistLabel, 410, SpringLayout.WEST, settings);		
		springLayout.putConstraint(SpringLayout.NORTH, blacklist, 5, SpringLayout.SOUTH, blacklistLabel);
		springLayout.putConstraint(SpringLayout.WEST, blacklist, 410, SpringLayout.WEST, settings);
		
        JPanel downloadType = new JPanel(new BorderLayout());
        downloadType.setBorder(new CompoundBorder(new TitledBorder("Type of Download"), new EmptyBorder(5,5,5,5)));
        downloadOptions.setBorder(new LineBorder(Color.GRAY));
        downloadType.add(downloadOptions);
        
        options.setBorder(new CompoundBorder(new TitledBorder("Configuration"), new EmptyBorder(5,5,5,5)));
        
		JButton downloadButton = new JButton("Download");
		downloadButton.setMaximumSize(new Dimension(0,0));
		
		downloadButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				if(saveFile.getSelectedFile() == null){
					System.out.println("Missing Fields");
					return;
				}
				
				Thread t = new Thread(new Runnable(){
					@Override
					public void run() {
						downloadButton.setEnabled(false);
						if(!checks.get(downloadOptions.getSelectedIndex()).startDownload(saveFile.getSelectedFile(), override.isSelected(), downloadImages.isSelected(), downloadCSS.isSelected(), downloadJS.isSelected(), new ArrayList<>(Arrays.asList(blacklistArea.getText().split("\\r?\\n"))))){
							System.out.println("Missing Fields");
						}
						downloadButton.setEnabled(true);
					}
				
				});
				
				t.start();
				
			}		
		});
		
		JButton add = new JButton(">");
		
		JPanel queue = new JPanel(new BorderLayout());
		DefaultListModel<String> toDownloadList = new DefaultListModel<>();
		JScrollPane toDownload = new JScrollPane(new JList(toDownloadList));
		toDownload.setPreferredSize(new Dimension(0,0));
		toDownload.setBorder(new CompoundBorder(new TitledBorder("Download Queue"), new CompoundBorder(new EmptyBorder(5,5,5,5), new LineBorder(Color.GRAY))));
		queue.add(toDownload);
		toDownloadList.add(0, "<html>asd<br>sidof</html>");
		
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
		
		c.gridwidth = 2;
		c.insets = new Insets(10,10,5,10);
		download.add(settings, c);
		
		c.gridwidth = 1;
        c.gridx = 0;
		c.gridy = 1;
		c.ipady = 40;
        c.insets = new Insets(5,10,5,5);
        download.add(downloadType, c);
		
        c.gridx = 1;
        c.ipadx = 250;
        c.insets = new Insets(5,5,5,10);
        download.add(options, c);		
		
		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 2;
		c.ipadx = 0;
		download.add(queue, c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.ipadx = 0;
		c.ipady = 0;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(0,0,0,0);
		download.add(add, c);
		
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridx = 3;
		c.gridy = 2;
		c.insets = new Insets(5,5,5,10);
		download.add(downloadButton, c);		
		
		/*
		JPanel open = new JPanel(new GridBagLayout());

		JButton openFileButton = new JButton("Open Save Location");
		openFileButton.setPreferredSize(new Dimension(200, 25));
		JLabel openFileLabel = new JLabel("Filed opened");
		
		JTextField linkInput = new JTextField(20);
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		open.add(openFileButton,c);
		
		c.gridy = 1;
		open.add(openFileLabel,c);
		
		c.gridy = 2;
		
		//open.add(openFileLabel);*/
		
		JPanel open = new JPanel();
		open.setLayout(new BoxLayout(open, BoxLayout.Y_AXIS));
		JButton openFileButton = new JButton("Open Save Location");
		openFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JFileChooser openFileChooser = new JFileChooser();
		openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		JLabel openFileLabel = new JLabel(" ");
		openFileLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		openFileLabel.setFont(openFileLabel.getFont().deriveFont(10.0f));

		JPanel urlList = new JPanel(new BorderLayout());
		DefaultListModel<String> urls = new DefaultListModel<>();
		urlList.setBorder(new CompoundBorder(new TitledBorder("URL List"), new EmptyBorder(5,5,5,5)));
		
		JList urlListClick = new JList(urls);
		urlList.add(new JScrollPane(urlListClick));
		urlList.setMaximumSize(new Dimension(WIDTH - 100, HEIGHT- 325));

		urlListClick.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					try {
						Desktop.getDesktop().browse(new File(openFileChooser.getSelectedFile().toString() + File.separator + Downloader.urlFileName((String)urlListClick.getSelectedValue())).toURI());
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent me) {}
			@Override
			public void mouseReleased(MouseEvent me) {}
			@Override
			public void mouseEntered(MouseEvent me) {}
			@Override
			public void mouseExited(MouseEvent me) {}
		
		
		});
		
		openFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(openFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
					File folder = openFileChooser.getSelectedFile();
					String folderName = folder.toString();
					boolean validFolder = false;
					
					if(folder.exists() && folder.isDirectory()){
						directoryLabel.setForeground(Color.GRAY);
						for(File f : folder.listFiles()){
							String fName = f.toString();
							if(fName.length() > 0 && f.isFile() && fName.substring(fName.lastIndexOf(File.separator) + 1).equalsIgnoreCase("sites.wcrawl")){
								validFolder = true;
								break;
							}
						}
					}
					
					if(!validFolder){
						openFileLabel.setForeground(Color.RED);
						openFileChooser.setSelectedFile(null);							
					}else{
						urls.clear();
						try {
							for(String u : Files.readAllLines(Paths.get(folder.toString() + File.separator + "sites.wcrawl"))){
								String filtered = u.substring(u.indexOf("//") + 2).replace("www.", "");
								if(urls.isEmpty()){
									urls.addElement(u);
								}else{
									for(int i = 0; i < urls.size(); i++){
										int compare = urls.get(i).substring(u.indexOf("//") + 2).replace("www.", "").compareTo(filtered);
										if(compare >= 0){
											urls.add(i, u);
											break;
										}else if(i == urls.size() - 1){
											urls.addElement(u);
											break;
										}
									}

								}
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						
					}
					
					if(folderName.length() <= 100){
						openFileLabel.setText(folderName);
					}else{
						openFileLabel.setText("..." + folderName.substring(folderName.length() - 97));
					}						
				}
			}
		});
		
		open.add(Box.createRigidArea(new Dimension(0,10)));
		open.add(openFileButton);
		open.add(openFileLabel);
		open.add(Box.createRigidArea(new Dimension(0,10)));
		open.add(urlList);
		
        tabs.addTab("Open", open);
		
		JTextArea status = new JTextArea();
		status.setEditable(false);
		
		PrintStream out = new PrintStream(new JTextAreaOutputStream(status));
		System.setOut(out);
		System.setErr(out);
		
		DefaultCaret caret = (DefaultCaret) status.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane statusPane = new JScrollPane(status);
		statusPane.setPreferredSize(new Dimension(WIDTH,150));
		
		JPanel console = new JPanel(new BorderLayout());
		console.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new CompoundBorder(new TitledBorder("Console"), new EmptyBorder(5,5,5,5))));
		console.add(statusPane);		
		
		content.add(console,BorderLayout.SOUTH);
        frame.setVisible(true);        
    }
    
}
