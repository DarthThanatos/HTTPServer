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
	String httpMethod;
	String httpQueryString;
	private StringBuffer responseBuffer = null;

	public myHTTPServer(Socket client, String fileName) {
		connectedClient = client;
		this.fileName = fileName;
		responseBuffer = new StringBuffer();
	}

	protected boolean patternMatching() throws Exception{
		if (httpMethod.equals("GET")) {
			if (httpQueryString.equals("/")) {
				// The default home page
				sendResponse(200, responseBuffer.toString(), false);
				return true;
			} 
			else {
				//This is interpreted as a file name
				String fileName = httpQueryString.replaceFirst("/", "");
				fileName = URLDecoder.decode(fileName);
				if (new File(fileName).isFile()){
					sendResponse(200, fileName, true);
					return true;
				}
				else return false;
			}
		}
		else return false;
	}
	
	public void run() {
		String headerLine = "";
		try {
			//System.out.println( "The Client " +  connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");
			inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String requestString = inFromClient.readLine();
			headerLine = requestString != null ? requestString  : "none";
			
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			httpMethod = tokenizer.nextToken();
			httpQueryString = tokenizer.nextToken();
			
			FileReader fr = new FileReader(new File(fileName));
			BufferedReader br = new BufferedReader(fr);
			String murphy = "", sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
					murphy += sCurrentLine + "\n";
			}
			responseBuffer.append(murphy.replace("\b", " "));
			//System.out.println("The HTTP request string is ....");
			System.out.println(requestString);
			while (inFromClient.ready()){
				// Read the HTTP complete HTTP Query
				//System.out.println(requestString);
				requestString = inFromClient.readLine();
			}
			boolean matched = patternMatching();
			if (!matched){
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
		//System.out.println(header);
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

}