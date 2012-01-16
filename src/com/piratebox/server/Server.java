package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class Server extends Thread {
	
	ServerSocket listenSocket;
	ArrayList<Connection> connections = new ArrayList<Connection>(); 
	
	public Server() {
		
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
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
			Log.e(this.getClass().getName(), e.toString());
		}
	}
	
	public void shutdown() {
		try {
			for (Connection conn : connections) {
				conn.destroy();
			}
			listenSocket.close();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		stop();
	}
}
