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

import java.io.File;
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

    private Handler connectedUsersHandler = new Handler();
    private Handler addStatHandler = new Handler();
	
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
		callConnectedUserHandler();
	}
	
	public void removeConnectedUser() {
		connectedUsers--;
		if (connectedUsers < 0) {
			connectedUsers = 0;
		}
		callConnectedUserHandler();
	}
	
	public void addStatForFile(File f) {
	    Message msg = new Message();
	    msg.obj = f;
	    addStatHandler.sendMessage(msg);
	}
    
    public void setConnectedUsersHandler(Handler h) {
        connectedUsersHandler = h;
    }
    
    public void setAddStatHandler(Handler h) {
        addStatHandler = h;
    }
	
	private void callConnectedUserHandler() {
	    Message msg = new Message();
	    msg.obj = connectedUsers;
		connectedUsersHandler.sendMessage(msg);
	}
}
