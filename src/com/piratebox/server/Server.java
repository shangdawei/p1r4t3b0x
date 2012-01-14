package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	
	ServerSocket listenSocket;
	ArrayList<Connection> connections = new ArrayList<Connection>(); 
	
	public Server() {
		
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.start();
	}
	
	public void run() {
		try{
			while(true)
			{
				Socket clientSocket = listenSocket.accept();
                connections.add(new Connection (clientSocket));
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		try {
			for (Connection conn : connections) {
				conn.destroy();
			}
			listenSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop();
	}
}
