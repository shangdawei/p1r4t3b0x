package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Server extends Thread {

	private ServerSocket listenSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private int connectedUsers = 0;
	
	private ArrayList<Handler> listeners = new ArrayList<Handler>();
	
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
	
	public boolean addConnectedUsersListener(Handler h) {
		return listeners.add(h);
	}
	
	public boolean removeConnectedUsersListener(Handler h) {
		return listeners.remove(h);
	}
	
	private void callListeners() {
		for (Handler h : listeners) {
		    Message msg = new Message();
		    msg.obj = connectedUsers;
			h.sendMessage(msg);
		}
	}
}
