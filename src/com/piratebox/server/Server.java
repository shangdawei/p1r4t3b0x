package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

public class Server extends Thread {
	
	public static enum ServerState {
		STATE_OFF,
		STATE_WAITING,
		STATE_SENDING
	}
	
	private ServerSocket listenSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>(); 
	private ServerState state;
	private int connectedUsers = 0;
	
	public Server() {
		
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		
		state = ServerState.STATE_OFF;
	}
	
	public void run() {
		try{
			while(true)
			{
				Socket clientSocket = listenSocket.accept();
				removeConnectedUser();
                connections.add(new Connection (clientSocket, this));
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
		
		state = ServerState.STATE_OFF;
	}
	
	public ServerState getServerState() {
		return state;
	}
	
	public void addConnectedUser() {
		connectedUsers++;
		state = ServerState.STATE_SENDING;
	}
	
	public void removeConnectedUser() {
		if (connectedUsers-- <= 0) {
			connectedUsers = 0;
			state = ServerState.STATE_WAITING;
		}
	}
}
