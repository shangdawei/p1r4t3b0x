package com.piratebox.server;

import java.io.DataInputStream;
import java.io.File;
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
		
		rootDir = new File(ServerConfiguration.rootDir);
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
	        File f = new File(ServerConfiguration.rootDir + requestedFile);
	        
	        //read in file
	        
	        if (!f.canRead() || f.isDirectory()) {
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
        out.print(new GeneratedPage(rootDir));
        
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
