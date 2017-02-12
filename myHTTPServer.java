import java.io.*;
import java.net.*;
import java.util.*;

public class myHTTPServer extends Thread {

	
	static final String HTML_START = "";
	static final String HTML_END = "";
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	static String fileName = "";
	UsersDataBase udb = null;
	GameKeeper gk = null;

	public myHTTPServer(Socket client, UsersDataBase udb, GameKeeper gk) {
		connectedClient = client;
		this.udb = udb;
		this.gk = gk;
	}

	public void logging(String httpQueryString) throws Exception{
		System.out.println("Logging request: " + httpQueryString);
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String password = httpQueryString.split("%20")[1];
		String newGame = httpQueryString.split("%20")[2];
		String gameId = httpQueryString.split("%20")[3];
		if(!udb.addUserToDB(userName,password)) {
			boolean correctLogin = udb.passwordCorrect(userName,password);
			if(!correctLogin) {
				sendResponse(200, "Incorrect password",false);
			}
		}
		boolean canJoin = gk.addToPair(gameId,udb.getUser(userName));
		if (canJoin){
			if (gk.getNoOfPlayers(gameId) == 2)
				sendResponse(200,"Starting",false);
			else 
				sendResponse(200,"Waiting for another player",false);
		}
		else {
			sendResponse(200,"There is no room for ya at this table",false);
		}
	}
	
	public void waitForPlayer(String httpQueryString) throws Exception{
		String gameId = httpQueryString.split("%20")[3];
		while(gk.getNoOfPlayers(gameId)!=2);
		sendResponse(200,"Starting",false);
	}
	
	public void run() {
		String headerLine = "";
		try {
			System.out.println( "The Client " +  connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");
			inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String requestString = inFromClient.readLine();
			headerLine = requestString != null ? requestString  : "none";
			
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			StringBuffer responseBuffer = new StringBuffer();
			FileReader fr = new FileReader(new File(fileName));
			BufferedReader br = new BufferedReader(fr);
			String murphy = "", sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
					murphy += sCurrentLine + "\n";
			}
			responseBuffer.append(murphy.replace("\b", " "));
			System.out.println("The HTTP request string is ....");
			System.out.println("Ready: " + requestString);
			while (inFromClient.ready()){
				// Read the HTTP complete HTTP Query
				System.out.println(requestString);
				requestString = inFromClient.readLine();
			}
			if (httpMethod.equals("GET")) {
				if (httpQueryString.equals("/")) {
					// The default home page
					sendResponse(200, responseBuffer.toString(), false);
				} 
				else {
					//This is interpreted as a file name
					String fileName = httpQueryString.replaceFirst("/", "");
					fileName = URLDecoder.decode(fileName);
					if (new File(fileName).isFile()){
						sendResponse(200, fileName, true);
					}
					else {
						sendResponse(404, "<b>The Requested resource " + httpQueryString + " not found ....</b>" , false);
					}
				}
			}
			else if(httpMethod.equals("LOG")) {
				logging(httpQueryString);
			}
			else if(httpMethod.equals("CANSTART")){
				waitForPlayer(httpQueryString);
			}
			else {
				System.out.println("The Requested resource " + httpQueryString + " not found ");
				sendResponse(404, "<b>The Requested resource " + httpQueryString + " not found </b>", false);
			}
		}
		catch(NoSuchElementException e){
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer\r\n";
		String contentLengthLine = null;
		String fileName = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		if (statusCode == 200) statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		if (isFile) {
			System.out.println(isFile);
			fileName = responseString;
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";
			if(fileName.endsWith(".css")){
				contentTypeLine = "Content-Type: text/css\r\n";
			}
		}
		else {
			responseString = myHTTPServer.HTML_START + responseString + myHTTPServer.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		}
		String header = statusLine + serverdetails + contentTypeLine + contentLengthLine + "Connection: close\r\n\r\n"; 
		System.out.println(header);
		if (isFile) sendFile(fin, outToClient);
		else outToClient.writeBytes(responseString);
		outToClient.close();
	}

	public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024] ;
		int bytesRead;
		while ((bytesRead = fin.read(buffer)) != -1 ) {
			out.write(buffer, 0, bytesRead);
		}
		fin.close();
	}

	public static void main (String args[]) throws Exception {
		try{
			UsersDataBase udb = new UsersDataBase();
			GameKeeper gk = new GameKeeper();
			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			fileName = args[2];
			ServerSocket Server = new ServerSocket (port, 10, InetAddress.getByName(ip));
			System.out.println ("TCPServer Waiting for client on port " + args[1]);
			while(true) {
				Socket connected = Server.accept();
				(new myHTTPServer(connected,udb,gk)).start();
			}
		}
		catch(Exception e){
			System.out.println("Usage: java myHTTPServer ip port file_path");
		}
	}
}