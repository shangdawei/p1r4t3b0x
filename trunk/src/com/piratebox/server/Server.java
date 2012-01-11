package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

public class Server extends Thread {
	
	ServerSocket listenSocket;
	ArrayList<Connection> connections = new ArrayList<Connection>(); 
	
	public Server(Context ctx) {
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(ctx, listenSocket.getLocalSocketAddress().toString(), 2).show();

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
			listenSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop();
	}
}
