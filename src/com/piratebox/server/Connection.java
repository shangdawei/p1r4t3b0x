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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;

import android.util.Log;

/**
 * This class describes a single connection.
 * It handle the client request and answers it properly.
 * @author Aylatan
 */
public class Connection extends Thread {

	private Socket client;
	private PrintStream out;
	private DataInputStream in;
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
	public Connection(Socket clientSocket, Server server) {
		client = clientSocket;
		this.server = server;

		//Create input and output streams for conversation with client
		try {
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
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

		//Tell the server that a user is connected
		server.addConnectedUser();

		try {
			//Read first line of http request
		    //First line looks like :
		    //GET <filename> HTTP/1.x
			req = in.readLine();

			//Loop through and discard rest of request
			line = req;
			while (line.length() > 0) {
				line = in.readLine();
			}

			//Get filename from request
			StringTokenizer st = new StringTokenizer(req);
			//First token is "GET"
			st.nextToken();
			requestedFile = st.nextToken();
			
			String filePath = URLDecoder.decode(ServerConfiguration.getRootDir()
					+ requestedFile);
			File f = new File(filePath);
			
			//If the file does not exist or is a directory, send the default page
			if (!f.canRead() || f.isDirectory()) {
				sendDefaultPage();
				server.removeConnectedUser();
				return;
			}

			//Else (ie the requested file is an existing file) send the file
			//Send basic headers
			PrintWriter pw = new PrintWriter(out);
			pw.print("HTTP/1.0 200 \r\n");
			pw.print("Content-Type: " + getMIMEType(f) + "\r\n");
			pw.print("\r\n");
			pw.flush();

			//Read the file and send it
			FileInputStream fis = new FileInputStream(f);
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
			
			//Tell the server to update the statistics for the given file
			server.addStatForFile(f);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		
		//Tell the server that the connected user is not connected anymore
		server.removeConnectedUser();
	}

	/**
	 * Sends a default page to the client
	 */
	private void sendDefaultPage() {
		//Send basic headers
		out.println("HTTP/1.0 200 OK");
		out.println("Content-type: text/html\n\n");

		//Send content
		out.print(new GeneratedPage(rootDir));

		try {
			client.close();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}

	/**
	 * Returns the http string for the MIME type of the file <code>f</code>.
	 * @param f the file to retrieve the MIME type
	 * @return a string that represents the MIME type of the file <code>f</code> for http protocol
	 */
	private String getMIMEType(File f) {
		String mime = null;
		int index = f.getPath().lastIndexOf('.');
		String ext = f.getPath().substring(index + 1).toLowerCase();
		
		if (ext != null) {
			mime = mimeTypes.get(ext);
		}
		if (mime == null) {
			mime = "application/octet-stream";
		}

		return mime;
	}
}
