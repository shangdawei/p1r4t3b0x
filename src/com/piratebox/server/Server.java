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

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.piratebox.utils.ExceptionHandler;

/**
 * This class describes the server that will handle requests.
 * @author Aylatan
 */
public class Server extends Thread {

    private Context ctx;
    private ServerSocket listenSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private int connectedUsers = 0;

	//Use handlers as this is the way to give information about drawing in a separate thread
    private Handler connectedUsersHandler;
    private Handler addStatHandler;
	
	/**
	 * Creates a new server.
	 * Initialises the listening socket.
	 */
	public Server(Context ctx) {
	    this.ctx = ctx;
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
			//Set a timeout so that the socket stops listening when the server stops
			listenSocket.setSoTimeout(1000);
		} catch (IOException e) {
            ExceptionHandler.handle(this, e);
		}
	}
	
	/**
	 * Main loop, waits for a socket and create a new {@link Connection} when one is received.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while(listenSocket != null)
		{
			try{
				Socket clientSocket = listenSocket.accept();
                connections.add(new Connection (clientSocket, this, ctx));
			} catch (SocketTimeoutException e) {
			    // Do nothing if the socket has timed out
			} catch(IOException e) {
                ExceptionHandler.handle(this, e);
			}
		}
	}

	/**
	 * Stops the server by breaking the main loop.
	 */
	public void stopRun() {
		for (Connection conn : connections) {
			conn.continueRunning = false;
			conn = null;
		}
		
		try {
			listenSocket.close();
		} catch (IOException e) {
		    ExceptionHandler.handle(this, e);
		}
		listenSocket = null;
	}
	
	/**
	 * Adds a user as connected and call the {@link Handler}.
	 */
	public void addConnectedUser() {
		connectedUsers++;
		callConnectedUserHandler();
	}
	
	/**
	 * Removes a user as connected and call the {@link Handler}.
	 */
	public void removeConnectedUser() {
		connectedUsers--;
		if (connectedUsers < 0) {
			connectedUsers = 0;
		}
		callConnectedUserHandler();
	}
	
	/**
	 * Gives a {@link Message} to the {@link Handler} that manages file statistic updates.
	 * The {@code obj} field of the message contains the file object.
	 * @param f the file which statistic has changed
	 */
	public void addStatForFile(File f) {
	    Message msg = new Message();
	    msg.obj = f;
	    addStatHandler.sendMessage(msg);
	}
    
    /**
     * Sets the {@link Handler} for the connected users.
     * @param h the handler to set
     */
    public void setConnectedUsersHandler(Handler h) {
        connectedUsersHandler = h;
    }

    /**
     * Sets the {@link Handler} for the statistic update.
     * @param h the handler to set
     */
    public void setAddStatHandler(Handler h) {
        addStatHandler = h;
    }

    /**
     * Gives a {@link Message} to the {@link Handler} that manages connected users.
     * The {@code obj} field of the message contains the number of user currently connected.
     */
	private void callConnectedUserHandler() {
	    Message msg = new Message();
	    msg.obj = connectedUsers;
		connectedUsersHandler.sendMessage(msg);
	}
}
