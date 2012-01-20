package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import android.util.Log;

import com.piratebox.utils.Callback;

public class Server extends Thread {

	private ServerSocket listenSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private int connectedUsers = 0;
	
	private ArrayList<Callback> listeners = new ArrayList<Callback>();
	
	public Server() {
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
			listenSocket.setSoTimeout(1000);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}
	
	public void run() {
		while(listenSocket != null)
		{
			try{
				Socket clientSocket = listenSocket.accept();
                connections.add(new Connection (clientSocket, this));
			} catch(IOException e) {
				if (!e.getClass().equals(SocketTimeoutException.class)) {
					Log.e(this.getClass().getName(), e.toString());
				}
			}
		}
	}

	public void stopRun() {
		for (Connection conn : connections) {
			conn.stop();
			conn = null;
		}
		
		try {
			listenSocket.close();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		listenSocket = null;
	}
	
	public void addConnectedUser() {
		connectedUsers++;
		callListeners();
	}
	
	public void removeConnectedUser() {
		connectedUsers--;
		if (connectedUsers < 0) {
			connectedUsers = 0;
		}
		callListeners();
	}
	
	public boolean addConnectedUsersListener(Callback c) {
		return listeners.add(c);
	}
	
	public boolean removeConnectedUsersListener(Callback c) {
		return listeners.remove(c);
	}
	
	private void callListeners() {
		for (Callback c : listeners) {
			c.call(connectedUsers);
		}
	}
}
