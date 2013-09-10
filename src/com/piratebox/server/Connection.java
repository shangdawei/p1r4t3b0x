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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.piratebox.utils.ExceptionHandler;

/**
 * This class describes a single connection.
 * It handle the client request and answers it properly.
 * @author Aylatan
 */
public class Connection extends Thread {

    public boolean continueRunning = true;
    
    private final String[] excludeRawFiles = new String[]{"iptables"};
    
    private Context ctx;
	private Socket client;
	private PrintStream out;
	private BufferedReader in;
	private String requestedFile;
	private File rootDir;
	private Server server;
	
	/**
	 * A map between a file extension and its MIME type. 
	 */
	@SuppressWarnings("serial")
	private static Hashtable<String, String> mimeTypes = new Hashtable<String, String>() {
		{
			put("htm", "text/html");
			put("html", "text/html");
            put("js", "text/javascript");
			put("txt", "text/plain");
            put("asc", "text/plain");
			put("gif", "image/gif");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("png", "image/png");
			put("mp3", "audio/mpeg");
			put("m3u", "audio/mpeg-url");
			put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("docx", "application/msword");
			put("ogg", "application/x-ogg");
		}
	};

	/**
	 * Creates a new Connection for the given socket.
	 * @param clientSocket the client socket the connection should handle
	 * @param server the parent server instance that received the client socket
	 */
	public Connection(Socket clientSocket, Server server, Context ctx) {
		client = clientSocket;
		this.server = server;
		this.ctx = ctx;

		//Create input and output streams for conversation with client
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintStream(client.getOutputStream());
		} catch (IOException e) {
            ExceptionHandler.handle(this, e);
			try {
				client.close();
			} catch (IOException e2) {
			}
			
			return;
		}

		//Creates the root directory if it does not exist
		rootDir = new File(ServerConfiguration.getRootDir());
		if (!rootDir.canRead()) {
			rootDir.mkdir();
		}

		this.start();
	}

	/**
	 * Main loop, handle the received request.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		String line = null;
		String req = null;
        File f = null;
		InputStream fis = null;

		//Tell the server that a user is connected
		server.addConnectedUser();

		try {
		    boolean updateStats = false;
		    
			//Read first line of http request
		    //First line looks like :
		    //GET <filename> HTTP/1.x
			req = in.readLine();

			//Loop through and discard rest of request
			line = req;
			if (line == null) {
			    return;
			}

			//Get filename from request
			StringTokenizer st = new StringTokenizer(req);
			//First token is "GET"
			if ("GET".equals(st.nextToken())) {
	            
	            // Finish to read the whole input
	            while (line.length() > 0) {
	                line = in.readLine();
	            }
	            
	            requestedFile = st.nextToken();
	            Log.d("file", requestedFile);
	            
	            String filePath = URLDecoder.decode(ServerConfiguration.getRootDir() + requestedFile,  "UTF-8");
	            f = new File(filePath);

	            fis = getLocalFileStream(extractName(requestedFile));
			}
			
			//If the file is not a local file and does not exist or is a directory, send the default page
			if (fis == null && (f == null || !f.canRead() || f.isDirectory())) {
				sendDefaultPage();
				server.removeConnectedUser();
				return;
			}

            //If the file is not a local file, it is a shared file and its stats should be updated
            if (fis == null) {
                fis = new FileInputStream(f);
                updateStats = true;
            }

			//Else (i.e. the requested file is an existing file) send the file
			//Send basic headers
			PrintWriter pw = new PrintWriter(out);
			pw.print("HTTP/1.0 200 \r\n");
			pw.print("Content-Type: " + getMIMEType(f) + "\r\n");
			pw.print("\r\n");
			pw.flush();
			
			//Read the file and send it
			byte[] buff = new byte[2048];
			while (true) {
				int read = fis.read(buff, 0, 2048);
				if (read <= 0) {
					break;
				}
				out.write(buff, 0, read);
			}
			out.flush();
			out.close();
			fis.close();
			
			//Tell the server to update the statistics for the given file if it is not a local file
			if (updateStats) {			
			    server.addStatForFile(f);
			}
		} catch (IOException e) {
            ExceptionHandler.handle(this, e);
		}
		
		//Tell the server that the connected user is not connected anymore
		server.removeConnectedUser();
	}

	/**
	 * Sends a default page to the client.
	 * @throws IOException when fail to close the client socket
	 */
	private void sendDefaultPage() throws IOException {
		//Send basic headers
		out.println("HTTP/1.0 200 OK");
		out.println("Content-type: text/html\n\n");

		//Send content
		out.print(new GeneratedPage(rootDir));
		out.flush();
		out.close();

		client.close();
	}

	/**
	 * Returns the http string for the MIME type of the file {@code f}.
	 * @param f the file to retrieve the MIME type
	 * @return a string that represents the MIME type of the file {@code f} for http protocol
	 */
	private String getMIMEType(File f) {
		String mime = null;
		int index = f.getPath().lastIndexOf('.');
		String ext = f.getPath().substring(index + 1).toLowerCase(Locale.getDefault());
		
		if (ext != null) {
			mime = mimeTypes.get(ext);
		}
		if (mime == null) {
			mime = "application/octet-stream";
		}

		return mime;
	}
	
	/**
	 * Extract the file name of a web path.
	 * @param webPath a path in the form /<file_name>.<ext>
	 * @return the filename
	 */
	private String extractName(String webPath) {
	    int start = webPath.lastIndexOf('/');
	    if (start == -1) {
	        start = 0;
	    }
	    int end = webPath.lastIndexOf('.');
	    if (end == -1) {
	        end = webPath.length();
	    }
	    return webPath.substring(start+1, end);
	}
	
	/**
	 * Retrieves the {@link InputStream} of the raw file with the provided name.
	 * @param name the name of the file to open
	 * @return An {@link InputStream} or {@code null} if the file was not found
	 */
	private InputStream getLocalFileStream(String name) {
	    if (Arrays.asList(excludeRawFiles).contains(name)) {
	        return null;
	    }
	    
	    Resources resources = ctx.getResources();
	    if (resources == null) {
	        return null;
	    }
	    
	    int resId = resources.getIdentifier(name, "raw", "com.piratebox");
	    if (resId == 0) {
	        return null;
	    }
	    
	    return resources.openRawResource(resId);
	}
}
