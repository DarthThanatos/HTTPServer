import java.io.*;
import java.net.*;
import java.util.*;

public class ProxyServer extends Thread {

	static final String HTML_START = "";
	static final String HTML_END = "";
	Socket connectedClient = null;
	Socket middleSocket;
	String middleIP = "192.168.0.101";
	int middlePort = 9999;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	static String fileName = "";

	public ProxyServer(Socket client) {
		connectedClient = client;
	}

	public void run() {
		String headerLine = "";
		try{
			middleSocket = new Socket(middleIP,middlePort);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to initialize proxy socket - exception in the constructor");
		}
		try {
			System.out.println( "The Client " +  connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");
			inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());

			String requestString = inFromClient.readLine();
			headerLine = requestString != null ? requestString  : "none";
			System.out.println("The HTTP request string is ....");
			System.out.println("Ready: " + requestString);

			while (inFromClient.ready()){
				// Read the HTTP complete HTTP Query
				requestString = inFromClient.readLine();
			}
			
			StringBuffer responseBuffer = new StringBuffer();
			DataOutputStream outToMiddle = new DataOutputStream(middleSocket.getOutputStream());
		
			outToMiddle.writeBytes(headerLine + "\n");
			InputStream is = middleSocket.getInputStream();
			sendResponse(is,outToClient);
			
			connectedClient.close();
			middleSocket.close();
		}
		catch(NoSuchElementException e){
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public void sendResponse (InputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024] ;
		int bytesRead;

		while ((bytesRead = fin.read(buffer)) != -1 ) {
			out.write(buffer, 0, bytesRead);
		}
	}

	public static void main (String args[]) throws Exception {
		try{
			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			ServerSocket Server = new ServerSocket (port, 10, InetAddress.getByName(ip));
			System.out.println ("TCPServer Waiting for client on port " + args[1]);
			while(true) {
				Socket connected = Server.accept();
				(new ProxyServer(connected)).start();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Usage: java ProxyServer ip port");
		}
	}
}