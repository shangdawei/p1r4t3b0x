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

public class Connection extends Thread {

	private Socket client;
	private PrintStream out;
	private DataInputStream in;
	private String requestedFile;
	private File rootDir;
	private Server server;
	
	@SuppressWarnings("serial")
	private static Hashtable<String, String> mimeTypes = new Hashtable<String, String>() {
		{
			put("htm", "text/html");
			put("html", "text/html");
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
			put("ogg", "application/x-ogg");
			put("zip", "application/octet-stream");
			put("exe", "application/octet-stream");
			put("class", "application/octet-stream");
		}
	};

	public Connection(Socket clientSocket, Server server) {
		client = clientSocket;
		this.server = server;

		// create input and output streams for conversation with client
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

		rootDir = new File(ServerConfiguration.getRootDir());
		if (!rootDir.canRead()) {
			rootDir.mkdir();
		}

		this.start();
	}

	public void run() {
		String line = null; // read buffer
		String req = null; // first line of request
		// OutputStream os;
		server.addConnectedUser();

		try {
			
			// read HTTP request -- the request comes in
			// on the first line, and is of the form:
			// GET <filename> HTTP/1.x
			req = in.readLine();

			// loop through and discard rest of request
			line = req;
			while (line.length() > 0) {
				line = in.readLine();
			}

			// parse request -- get filename
			StringTokenizer st = new StringTokenizer(req);
			// discard first token ("GET")
			st.nextToken();
			requestedFile = st.nextToken();
			// create File object
			String filePath = URLDecoder.decode(ServerConfiguration.getRootDir()
					+ requestedFile);
			File f = new File(filePath);

			if (!f.canRead() || f.isDirectory()) {
				sendDefaultPage();
				server.removeConnectedUser();
				return;
			}

			PrintWriter pw = new PrintWriter(out);
			pw.print("HTTP/1.0 200 \r\n");
			pw.print("Content-Type: " + getMIMEType(f) + "\r\n");
			pw.print("\r\n");
			pw.flush();

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
			
			server.addStatForFile(f);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		
		server.removeConnectedUser();
	}

	private void sendDefaultPage() {
		// send response headers
		out.println("HTTP/1.0 200 OK");
		out.println("Content-type: text/html\n\n");

		// send content
		out.print(new GeneratedPage(rootDir));

		try {
			client.close();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}

	private String getMIMEType(File f) {
		String mime = null;
		int index = -1;
		String ext = null;
		try {
			index = f.getCanonicalPath().lastIndexOf('.');
			ext = f.getCanonicalPath().substring(index + 1).toLowerCase();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}

		if (ext != null) {
			mime = mimeTypes.get(ext);
		}
		if (mime == null) {
			mime = "application/octet-stream";
		}

		return mime;
	}
}
