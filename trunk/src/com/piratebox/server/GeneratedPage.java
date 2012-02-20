/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox.server;

import java.io.File;
import java.io.FileFilter;

/**
 * This class describes a generated web page for a specific directory.
 * @author Aylatan
 */
public class GeneratedPage {
	
	private String header = "" +
			"<html>" +
			"<head>" +
			"<title>P1R4T3B0X Home</title>" +
			"<script>" +
			"	function switchBlockDisplay(a) {" +
			"		content = a.parentNode.getElementsByClassName('folder')[0];" +
			"		content.style.display = content.style.display == 'block' ? 'none' : 'block'" +
			"	}" +
			"</script>" +
			"" +
			"<style>" +
			"	.folderLink {" +
			"		font-weight: bold;" +
			"	}" +
			"" +
			"	.folder {" +
			"		display: none;" +
			"	}" +
			"</style>" +
			"</head>";
	private String content = "" +
			"<body>" +
	        "<img src='piratebox.png' alt='PirateBox' />" +
			"<h1>Available files:</h1>" +
			"" +
			"<ul>";
	private String footer = "" +
			"</body>" +
			"</html>";
		
	/**
	 * Generates a web page that shows a list of all the file and folders in the given folder.
	 * File names are link to the file.
	 * Folder names are link that allow the user to view the files and folder inside it.
	 * @param rootDir the root directory which content is to be shown
	 */
	public GeneratedPage(File rootDir) {
    	StringBuilder contentSb = new StringBuilder().append(content);
    	listFilesFromFolder(rootDir, contentSb, "/");
    	contentSb.append("</ul>");
    	content = contentSb.toString();
	}

	/**
	 * Recursive function that generates a list of links of files and folders of the given folder.
	 * @param folder the folder which content is to be listed
	 * @param str the {@link StringBuilder} where the content is added
	 * @param folderPath the path to the folder, relative to the root directory
	 */
	private void listFilesFromFolder(File folder, StringBuilder str, String folderPath) {

	    //Remove hidden files from list
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
    	        return !f.getName().startsWith(".");
    	    }
    	};
    	
    	File[] children = folder.listFiles(filter);
    	
    	if (children != null) {
	    	for (File file : children) {
	    	    //If this is a file, add a list entry and a link to the file
	    		if (file.isFile()) {
	    			str.append("<li><a href='").append(folderPath + file.getName()).append("'>").append(file.getName())
	    				.append("</a> <font size=2>(").append(getReadableFileSize(file)).append(")</font></li>");
	    		} else {
	    		    //Else add a folder link and list the content of this folder
	    			str.append("<li><a class='folderLink' onclick=switchBlockDisplay(this) href='javascript:void(0);'>")
	    				.append(file.getName()).append("</a>");
	    			str.append("<ul class='folder'>");
	    			listFilesFromFolder(file, str, folderPath + file.getName() + "/");
	    			str.append("</ul></li>");
	    		}
	    	}
    	}
	}
	
	/**
	 * Generates a string that represents the size of the file {@code f}.
	 * @param f the file which size is to be calculated
	 * @return a human readable string that represents the size of {@code f}
	 */
	private String getReadableFileSize(File f) {
		long len = f.length();
		StringBuilder result = new StringBuilder();
		//If the size is more than 1 KB, show the value with 2 decimals
		if (len < 1024) {
			result.append(f.length() + " bytes");
		} else if (len < 1024 * 1024) {
			result.append(Math.round(f.length()*100 / 1024.)/100. + " KB");
		} else {
			result.append(Math.round(f.length()*100 / 1024. / 1024.)/100. + " MB");
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the content of the web page.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return header + content + footer;
	}
}
