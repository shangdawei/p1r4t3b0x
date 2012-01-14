/////////////////////////////////////////////////////////////////////
//TeenyWeb.java -- a brain-dead web server written in Java
//This is a severely limited web server; it can only handle requests for
//HTML documents (no GIFS, no CGIs, etc -- though that could be built
//in later on). The purpose of this is to give you a very simple example
//of how a web server works.
/////////////////////////////////////////////////////////////////////
import java.io.*;
import java.net.*;
import java.util.*;

public class TeenyWeb extends Thread
{
        
	ServerSocket listen_socket;
        String httpRootDir;
	
        public TeenyWeb(String port, String httpRoot)
	{
		try{
                        //set instance variables from constructor args
                        int servPort = Integer.parseInt(port);
                        httpRootDir = httpRoot;

                        //create new ServerSocket
                        listen_socket = new ServerSocket (servPort);
			
		}
		catch(IOException e) {System.err.println(e);}

                //Start running Server thread
		this.start();
	}
	public void run()
	{
		try{
			while(true)
			{
                                //listen for a request. When a request comes in,
                                //accept it, then create a Connection object to
                                //service the request and go back to listening on
                                //the port.

				Socket client_socket = listen_socket.accept();
                                System.out.println("connection request received");
                                Connection c = new Connection (client_socket, httpRootDir);
			}
		}
		catch(IOException e) {System.err.println(e);}
	}

        //simple "main" procedure -- create a TeenyWeb object from cmd-line args
	public static void main(String[] argv)
	{
                if (argv.length < 2)
                        {
                        System.out.println("usage: java TeenyWeb <port> <http root directory>");
                        return;
                        }
                new TeenyWeb(argv[0], argv[1]);
	}
}

//The Connection class -- this is where HTTP requests are serviced

class Connection extends Thread
{
	protected Socket client;
	protected DataInputStream in;
	protected PrintStream out;
        String httpRootDir;
        String requestedFile;

        public Connection (Socket client_socket, String httpRoot)
	{
                //set instance variables from args
                httpRootDir = httpRoot;
		client = client_socket;

                //create input and output streams for conversation with client

		try{
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream (client.getOutputStream());
		}
		catch(IOException e) 
		{
		 	System.err.println(e);
			try {client.close();} 
			catch (IOException e2) {};
			return;
		}
                //start this object's thread -- the rest of the action
                //takes place in the run() method, which is called by
                //the thread.
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
                                while (line.length() > 0)
                                {
                                line = in.readLine();
                                }

                                //parse request -- get filename
                                StringTokenizer st = new StringTokenizer(req);
                                //discard first token ("GET")
                                st.nextToken();
                                requestedFile = st.nextToken();


                                //read in file

                                //create File object
                                File f = new File(httpRootDir + requestedFile);
                                //check to see if file exists
                                if (!f.canRead())
                                        {
                                        sendResponseHeader("text/plain");
                                        sendString("404: not found: " + requestedFile);
                                        return;
                                        }



                                //send response
                                sendResponseHeader("text/html");

                                //read in file
                                FileInputStream fis = new FileInputStream(f);
                                DataInputStream fdis = new DataInputStream(fis);

                                //send file to client
                                line = fdis.readLine();
                                while (line != null && line.length() > 0)
                                {
                                        sendString(line);
                                        line = fdis.readLine();
                                }
                                

		
		}
                catch (IOException e) {System.out.println(e);}
		finally
		{
			try {client.close();}
			catch (IOException e) {};

		}


	}

        //send a HTTP header to the client
        //The first line is a status message from the server to the client.
        //The second line holds the mime type of the document
        void sendResponseHeader(String type)
        {
                out.println("HTTP/1.0 200 OK");
                out.println("Content-type: " +type+ "\n\n");                
        }

        //write a string to the client. 
        void sendString(String str)
        {
                out.print(str);                
        }
	
}
