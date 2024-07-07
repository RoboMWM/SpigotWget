package tit79a.spigotwget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WgetCommand implements CommandExecutor {
	
	String prefix = ChatColor.WHITE + "["+ ChatColor.GOLD + "Wget" + ChatColor.WHITE + "] ";

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if(sender.isOp()) {
            if(args.length == 0) {
                sender.sendMessage(prefix + "Usage: /wget <url>");
            } else if (args.length >= 1) {
            	if(isURL(args[0])) {
                	download(args[0], sender);
            	} else {
                    sender.sendMessage(prefix + "This is not a valid url!");
            	}
            }
    	} else {
    		sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command!");
    	}
    	
    	return true;
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    public void download(String urlString, CommandSender sender) {
    	new Thread(new Runnable() {
    	    public void run() {
    			try {
    				URL url = new URL(urlString);
    				
    		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
    		        connection.connect();
    		        
    		        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
    		        	String fileName;
    		            int contentLength = connection.getContentLength();
    		            String disposition = connection.getHeaderField("Content-Disposition");
    		        	
    		            if (disposition != null) {
    		                int index = disposition.indexOf("filename=");
    		                if (index > 0) {
    		                    fileName = disposition.substring(index + 10, disposition.length() - 1);
    		                } else {
    			    	        String urlPath = url.getPath();
    			    	        fileName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
    		                }
    		            } else {
    		    	        String urlPath = url.getPath();
    		    	        fileName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
    		            }
    		            
    		            sender.sendMessage(prefix + "Attempting to download \"" + ChatColor.GREEN + fileName + ChatColor.WHITE + "\" (" + ChatColor.GREEN + humanReadableByteCountBin(contentLength) + ChatColor.WHITE + ") from \"" + ChatColor.GREEN + urlString + ChatColor.WHITE + "\"...");
    		            
    		            InputStream inputStream = connection.getInputStream();

						String fileNameWithoutVersion = fileName.replaceAll("-[0-9]+.*\\.jar$", ".jar");
    		            File file = new File(Main.getInstance().getDataFolder(), fileNameWithoutVersion);
    		            FileOutputStream outputStream = new FileOutputStream(file);
    		 
    		            int bytesRead = -1;
    		            int downloadedSize = 0;
    		            int progress = 0;
    		            byte[] buffer = new byte[4096];
    		            
    		            while ((bytesRead = inputStream.read(buffer)) != -1) {
    		                outputStream.write(buffer, 0, bytesRead);
    		                downloadedSize += bytesRead;
    		                
    		                int currentProgress = (int) ((((double) downloadedSize) / ((double) contentLength)) * 100);
    		                
    		                if(currentProgress != progress) {
    		                	progress = currentProgress;
    		                    sender.sendMessage(prefix + "Download progress: " + ChatColor.GREEN + currentProgress + ChatColor.WHITE + "% (" + ChatColor.GREEN + humanReadableByteCountBin(downloadedSize) + ChatColor.WHITE + ").");
    		                }
    		            }
    		 
    		            outputStream.close();
    		            inputStream.close();
    		            connection.disconnect();
    			        
    		            sender.sendMessage(prefix + "File saved at path: \"" + ChatColor.GREEN + file.getPath() + ChatColor.WHITE + "\" (" + ChatColor.GREEN + humanReadableByteCountBin(contentLength) + ChatColor.WHITE + ").");
    		            
    		        } else {
    		            sender.sendMessage(prefix + "Cannot retrieve data from \"" + ChatColor.GREEN + urlString + ChatColor.WHITE + "\"!");
    		        }
    		        
    			} catch (MalformedURLException e) {
    				sender.sendMessage(prefix + "An error has occured! Please try again.");
    			} catch (IOException e) {
    				sender.sendMessage(prefix + "An error has occured! Please try again.");
    			}
    	    }
    	}).start();
    }
}
