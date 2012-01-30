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
			"<h1>Available files:</h1>" +
			"" +
			"<ul>";
	private String footer = "" +
			"</body>" +
			"</html>";
		
	public GeneratedPage(File rootDir) {

    	
    	StringBuilder contentSb = new StringBuilder().append(content);
    	listFilesFromFolder(rootDir, contentSb, "/");
    	contentSb.append("</ul>");
    	content = contentSb.toString();
	}

	private void listFilesFromFolder(File folder, StringBuilder str, String folderPath) {

		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
    	        return !f.getName().startsWith(".");
    	    }
    	};
    	
    	File[] children = folder.listFiles(filter);
    	
    	if (children != null) {
	    	for (File file : children) {
	    		if (file.isFile()) {
	    			str.append("<li><a href='").append(folderPath + file.getName()).append("'>").append(file.getName())
	    				.append("</a> <font size=2>(").append(getReadableFileSize(file)).append(")</font></li>");
	    		} else {
	    			str.append("<li><a class='folderLink' onclick=switchBlockDisplay(this) href='javascript:void(0);'>")
	    				.append(file.getName()).append("</a>");
	    			str.append("<ul class='folder'>");
	    			listFilesFromFolder(file, str, folderPath + file.getName() + "/");
	    			str.append("</ul></li>");
	    		}
	    	}
    	}
	}
	
	private String getReadableFileSize(File f) {
		long len = f.length();
		StringBuilder result = new StringBuilder();
		if (len < 1024) {
			result.append(f.length() + " bytes");
		} else if (len < 1024 * 1024) {
			result.append(Math.round(f.length()*100 / 1024.)/100. + " KB");
		} else {
			result.append(Math.round(f.length()*100 / 1024. / 1024.)/100. + " MB");
		}
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		return header + content + footer;
	}
}
