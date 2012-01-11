package com.piratebox.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class Connection extends Thread {

	private Socket client;
	private PrintStream out;
	private DataInputStream in;
	private String requestedFile;
	private File rootDir; 

    public Connection (Socket clientSocket)
	{
		client = clientSocket;

        //create input and output streams for conversation with client
		try{
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());
		}
		catch(IOException e) 
		{
			e.printStackTrace();
			try {
				client.close();
			}catch (IOException e2) {};
			
			return;
		}
		
		rootDir = new File(ServerConfiguration.ROOT_DIR);
		if (! rootDir.canRead()) {
			rootDir.mkdir();
		}
		
		this.start();
	}

	public void run()
	{
        String line = null;     //read buffer
        String req = null;      //first line of request
        //OutputStream os;

    	try{
	        //read HTTP request -- the request comes in
	        //on the first line, and is of the form:
	        //      GET <filename> HTTP/1.x
	
	        req = in.readLine();

	        //loop through and discard rest of request
	        line = req; 
	        while (line.length() > 0) {
	        	line = in.readLine();
	        }

	        //parse request -- get filename
	        StringTokenizer st = new StringTokenizer(req);
	        //discard first token ("GET")
	        st.nextToken();
	        requestedFile = st.nextToken();
	        //create File object
	        File f = new File(ServerConfiguration.ROOT_DIR + requestedFile);
	        
	        //read in file
	        
	        if (!f.canRead()) {
	        	sendDefaultPage();
				return;
	        }
	        
	        //send response headers
	        out.println("HTTP/1.0 200 OK");
	        out.println("Content-type: text/html\n\n");
	        
	        //read in file
	        FileInputStream fis = new FileInputStream(f);
	        DataInputStream fdis = new DataInputStream(fis);
	
	        //send file to client
	        line = fdis.readLine();
	        while (line != null && line.length() > 0) {
	        	out.print(line);
                line = fdis.readLine();
	        }
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
    private void sendDefaultPage() {
        //send response headers
        out.println("HTTP/1.0 200 OK");
        out.println("Content-type: text/html\n\n");
        
        // send content
        out.print(generateFile());
        
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String generateFile() {
    	StringBuilder page = new StringBuilder();
    	page.append("<html><head><title>Hello</title></head><body>");
    	page.append("<h1>File list:</h1>");
    	page.append("<ul>");
    	
    	FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
    	        return !f.getName().startsWith(".") && !f.isDirectory();
    	    }
    	};
    	
    	File[] children = rootDir.listFiles(filter);
    	
    	if (children != null) {
	    	for (File file : children) {
	    		page.append("<li><a href='").append(file.getName()).append("'>").append(file.getName()).append("</a></li>");
	    	}
    	}
    	
    	// TODO folder management, retrict access to files that are under rootDir
    	
    	page.append("</ul>");
    	page.append("</body></html>");
    	return page.toString();
    }
}
