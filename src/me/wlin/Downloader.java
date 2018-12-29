package me.wlin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Downloader {
	
	private ArrayList<String> downloaded = new ArrayList<>();
	private ArrayList<String> blacklist;
	private ArrayList<String> failed;
	private boolean downloadImages;
	private boolean downloadCSS;
	private boolean downloadJS;
	
	private File saveFolder;
	private File downloadedData;
	private FileWriter writer;
	
	public Downloader(File saveFolder, boolean override, boolean downloadImages, boolean downloadCSS, boolean downloadJS, ArrayList<String> blacklist){
		this.downloadImages = downloadImages;
		this.downloadCSS = downloadCSS;
		this.downloadJS = downloadJS;
		this.blacklist = blacklist;
		
		failed = new ArrayList<>();
		failed.add("data:");
		
		this.saveFolder = saveFolder;

		downloadedData = new File(saveFolder.toString() + File.separator + "sites.wcrawl");
		try{
			if(!downloadedData.exists()){
				downloadedData.createNewFile();
			}
			if(!override){
				BufferedReader reader = new BufferedReader(new FileReader(downloadedData));
				String line;
				while((line = reader.readLine()) != null){
					downloaded.add(line.replace("//www.", "//").toLowerCase());
				}
				writer = new FileWriter(downloadedData, true);
			}else{
				writer = new FileWriter(downloadedData, false);
			}			
			
		}catch (IOException e){
			e.printStackTrace();
		}

	
	}
	
	public void download(String urlString, Check check){
		download(urlString, null, "href", check);
		try {
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Done");
	}
	
	public void download(String[] urlStrings, Check check){
		for(String urlString : urlStrings){
			download(urlString, null, "href", check);
		}
		try {
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Done");
	}	
	
	private String download(String urlString, String source, String sourceTag, Check check) {
		try {
			urlString = check.redirect(urlString);
			if(downloaded.contains(urlString.replace("//www.", "//").toLowerCase())){
				return urlString;
			}
			
			if(failed.contains(urlString.replace("//www.", "//").toLowerCase())){
				return null;
			}
			
			if(blacklist != null && blacklist.size() > 0){
				for(String black : blacklist){
					if(black.length() > 0 && urlString.toLowerCase().contains(black.toLowerCase())){
						return null;
					}
				}
			}
			
			String temp = urlString;
			if(temp.contains("?")){
				temp = urlString.substring(0,urlString.lastIndexOf("?"));
			}
			
			boolean skipCheck = false;
			
			if(temp.contains(".")){		
				String fileType = temp.substring(temp.lastIndexOf("."));
				if(fileType.equalsIgnoreCase(".css")){
					if(downloadCSS){
						skipCheck = true;
					}else{
						return null;
					}
				}else if(fileType.equalsIgnoreCase(".js")){
					if(downloadJS){
						skipCheck = true;
					}else{
						return null;
					}
				}else if((fileType.equalsIgnoreCase(".png") || fileType.contains(".png") || fileType.contains(".ico") || fileType.contains(".jpg") || fileType.contains(".srv") || fileType.contains(".gif"))){
					if(sourceTag.equalsIgnoreCase("src") || sourceTag.equalsIgnoreCase("url")){
						if(downloadImages){
							skipCheck = true;
						}else{
							return null;
						}
					}

				}
			}
			
			if(!skipCheck && !check.toDownload(urlString, source)){
				return null;
			}
			
			String fileName = urlFileName(urlString);
			File file = new File(saveFolder.toString() + File.separator + fileName);
			
			URL url = new URL(urlString);
			URLConnection yc = url.openConnection();
			yc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
			InputStream in = yc.getInputStream();
			Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			in.close();
			downloaded.add(urlString.replace("//www.", "//").toLowerCase());
			
			System.out.println("Downloaded: " + urlString);
			
			String fileType = Files.probeContentType(file.toPath());
			if (fileType.contains("htm")) {		
				ArrayList<String> lines = new ArrayList<>();
				boolean commenting = false;	
				boolean css = false;
				boolean js = false;
				
				boolean tag = false;
				
				char hangingQuote = 0;
				String hangingLink = "";
				
				for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
					if(!hangingLink.equals("")){
						line = hangingLink + line;
					}
					
					for(int i = 0; i < line.length(); i++){
						if(hangingQuote != 0){
							if((i = line.indexOf(hangingQuote)) == -1){
								break;
							}else{
								hangingQuote = 0;
							}
						}else if(commenting){
							if(css){
								if((i = line.indexOf("*/", i)) != -1){
									commenting = false;
									i += 1;
								}else{
									break;
								}
							}else if((i = line.indexOf("-->", i)) != -1){
								commenting = false;
								i += 2;
							}else{
								break;
							}
						}else if(js && !tag){
							if((i = line.indexOf("</script>", i)) == -1){
								break;
							}else{
								js = false;
								i += 8;
							}
							
						}else if(tag || css){
							if(line.charAt(i) == '\''){
								if((i = line.indexOf('\'', i+1)) == -1){
									hangingQuote = '\'';
									break;
								}
							}else if(line.charAt(i) == '"'){
								if((i = line.indexOf('"', i+1)) == -1){
									hangingQuote = '"';
									break;
								}
							}else if(css){
								if(i + 1 < line.length() && line.substring(i, i+2).equals("/*")){
									commenting = true;
									i++;
								}else if(i + 7 < line.length() && line.substring(i, i + 8).equals("</style>")){
									css = false;
									i += 7;
								}else if(i + 2 < line.length() && line.substring(i, i + 3).equals("url")){
									int start = line.indexOf("(", i+3);
									int end = line.indexOf(")",start+1);
									
									String relativeLink = line.substring(start + 1,end).trim();
									if(relativeLink.charAt(0) == relativeLink.charAt(relativeLink.length() -1) && (relativeLink.charAt(0) == '\'' || relativeLink.charAt(0) == '"')){
										relativeLink = relativeLink.substring(1,relativeLink.length()-1);										
									}
									
									String link = getFullUrl(urlString, relativeLink);
									if((link = download(link,urlString,"url",check)) != null){
										String downloadedName = urlFileName(link);
										line = line.substring(0,start+1) + downloadedName + line.substring(end);
										i = start + 1 + downloadedName.length();
									}else{
										i = start + 1 + relativeLink.length();
									}								
								}
							}else if(tag){
								if(line.charAt(i) == '>'){
									tag = false;
								}else if((i + 3 < line.length() && line.substring(i, i+4).equalsIgnoreCase("href")) || (i + 2 < line.length() && line.substring(i, i+3).equalsIgnoreCase("src")) || (i + 2 < line.length() && line.substring(i, i+3).equalsIgnoreCase("url"))){
									int nextDouble = line.indexOf('"',i+4);
									int nextSingle = line.indexOf('\'',i+4);
									int start = 0;
									if(nextDouble == -1){
										if(nextSingle == -1){
											hangingLink = line.substring(i);
											line = line.substring(0, i);
											break;											
										}else{
											start = nextSingle;
										}
									}else if(nextSingle != -1){
										start = nextDouble < nextSingle ? nextDouble : nextSingle;
									}else{
										start = nextDouble;											
									}

									int end = line.indexOf(line.charAt(start), start + 1);
									if(end != -1){
										String link = getFullUrl(urlString, line.substring(start + 1, end));
										String tagg = (i + 3 < line.length() && line.substring(i, i+4).equalsIgnoreCase("href")) ? "href" : line.substring(i, i+3);
										if((link = download(link, urlString, tagg, check)) != null){
											String downloadedName = urlFileName(link);
											line = line.substring(0,start+1) + downloadedName + line.substring(end);
											i = start + 1 + downloadedName.length();
										}else{
											i = end;
										}
												

										hangingLink = "";
									}else{
										hangingLink = line.substring(i);
										line = line.substring(0, i);
										break;
									}

								}
							}
						}else{
							if(line.charAt(i) == '<'){
								if(i + 3 < line.length() && line.substring(i+1, i+4).equals("!--")){
									commenting = true;
									i += 3;
								}else if(i + 6 < line.length() && line.substring(i+1, i+7).equalsIgnoreCase("script")){
									js = true;
									tag = true;
									i += 6;
								}else if(i + 5 < line.length() && line.substring(i+1, i+6).equalsIgnoreCase("style")){
									css = true;
									i += 5;
								}else if(i < line.length() && ((line.charAt(i + 1) >= 65 && line.charAt(i + 1) <= 90) || (line.charAt(i + 1) >= 97 && line.charAt(i + 1) <= 122))){
									tag = true;
								}
							}
						}
												
					}
					
					lines.add(line);
				}
				
				writer.append(urlString + "\n");
				Files.write(file.toPath(), lines, StandardOpenOption.WRITE);		
			}else{
				if(urlString.contains(".")){			
					if(urlString.contains("?")){
						fileType = urlString.substring(0,urlString.lastIndexOf("?"));
						if(urlString.contains(".")){
							fileType = fileType.substring(fileType.lastIndexOf("."));
							if(!fileType.contains(".css")){
								return urlString;
							}	
						}
					}else{
						fileType = urlString.substring(urlString.lastIndexOf("."));
						if(!fileType.contains(".css")){
							return urlString;
						}
					}
				}
				ArrayList<String> lines = new ArrayList<>();
				boolean commenting = false;									
				for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
					for(int i = 0; i < line.length(); i++){
						if(commenting){
							if(i + 1 < line.length() && line.substring(i,i+2).equals("*/")){
								commenting = false;
							}
						}else{
							if(i + 1 < line.length() && line.substring(i,i+2).equals("/*")){
								commenting = true;
							}else if(line.charAt(i) == '\''){
								if((i = line.indexOf('\'', i+1)) == -1){
									break;
								}
							}else if(line.charAt(i) == '"'){
								if((i = line.indexOf('"', i+1)) == -1){
									break;
								}
							}else if(i + 3 < line.length() && line.substring(i, i+3).equalsIgnoreCase("url")){
								int start = line.indexOf("(", i+3);
								if(start == -1){
									break;
								}
								
								int end = line.indexOf(")",start+1);
								if(end == -1){
									break;
								}
								
								String relativeLink = line.substring(start + 1,end).trim();
								if(relativeLink.length() > 0 && relativeLink.charAt(0) == relativeLink.charAt(relativeLink.length() -1) && (relativeLink.charAt(0) == '\'' || relativeLink.charAt(0) == '"')){
									relativeLink = relativeLink.substring(1,relativeLink.length()-1);
								}
								
								String link = getFullUrl(urlString, relativeLink);
								if((link = download(link, urlString, "url", check)) != null){
									String downloadedName = urlFileName(link);
									line = line.substring(0,start+1) + downloadedName + line.substring(end);
									i = start + 1 + downloadedName.length();
								}else{
									i = end;
								}							
							}
								
							
						}
					}
					lines.add(line);
				}
				Files.write(file.toPath(), lines, StandardOpenOption.WRITE);
			}	
			return urlString;
		} catch (Exception e){
			blacklist.add(urlString.replace("//www.", "//").toLowerCase());
			System.out.println("Failed to download: " + urlString);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getFullUrl(String base, String url) {
		url = url.replace("&amp;","&");
		if(url.length() > 4 && url.substring(0, 5).equalsIgnoreCase("data:")){
			return "data:";
		}else if (url.contains("https://") || url.contains("http://")) {
			return url;
		} else if (url.length() >= 2 && url.substring(0, 2).equals("//")) {
			return base.contains("https://") ? "https:" + url : "http:" + url;
		} else if (url.length() >= 1 && url.charAt(0) == '/') {
			String type = "";
			if (base.length() >= 8 && base.substring(0, 8).equals("https://")) {
				type = "https://";
				base = base.substring(8, base.length());
			} else if (base.length() >= 7 && base.substring(0, 7).equals("http://")) {
				type = "http://";
				base = base.substring(7, base.length());
			}
			while (base.contains("/")) {
				base = base.substring(0, base.lastIndexOf('/'));
			}
			return type + base + url;

		} else {
			boolean onRoot = false;
			while ((url.length() >= 2 && url.substring(0, 2).equals("./")) || (url.length() >= 3 && url.substring(0, 3).equals("../"))) {
				if (url.substring(0, 2).equals("./")) {
					url = url.substring(2);
				} else if (url.substring(0, 3).equals("../")) {
					if(!onRoot){
						if(base.substring(0, base.lastIndexOf("/")).contains("//")){
							base = base.substring(0, base.lastIndexOf("/"));
						}else{
							base = base + "/";
							onRoot = true;
						}
					}
					
					url = url.substring(3);
				}
				if(!onRoot && base.substring(0, base.lastIndexOf("/")).contains("//")){
					base = base.substring(0, base.lastIndexOf("/") + 1);
				}
			}

			if(!base.substring(0,base.lastIndexOf('/')).contains("//")){
				base = base + "/";
			}
			return base.substring(0, base.lastIndexOf('/') + 1) + url;
		}
	}

	public static String urlFileName(String url){			
		for(int i = 0; i < url.length(); i++){
			switch (url.charAt(i)) {
				case '/':
					url = url.substring(0, i) + "_5F" + url.substring(i+1,url.length());
					break;
				case '?':
					url = url.substring(0, i) + "_3F" + url.substring(i+1,url.length());
					break;
				case ';':
					url = url.substring(0, i) + "_3B" + url.substring(i+1,url.length());
					break;
				case '&':
					url = url.substring(0, i) + "_26" + url.substring(i+1,url.length());
					break;
				case ':':
					url = url.substring(0, i) + "_3A" + url.substring(i+1,url.length());
					break;
				case '=':
					url = url.substring(0, i) + "_3D" + url.substring(i+1,url.length());
					break;
				case '@':
					url = url.substring(0, i) + "_40" + url.substring(i+1,url.length());
					break;
				default:
					break;
			}
		}
		url = url.toLowerCase();
		return "_" + (url.length() > 254 ? url.substring(0,254) : url);
	}
}
