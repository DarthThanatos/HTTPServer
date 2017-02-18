import java.net.*;

public class Main{
	public static void main (String args[]){
		try{
			UsersDataBase udb = new UsersDataBase();
			GameKeeper gk = new GameKeeper();
			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			String fileName = args[2];
			ServerSocket Server = new ServerSocket (port, 100000, InetAddress.getByName(ip));
			System.out.println ("TCPServer Waiting for client on port " + args[1]);
			while(true) {
				Socket connected = Server.accept();
				(new Server1000(connected, fileName, udb,gk)).start();
				//(new myHTTPServer(connected, fileName)).start();
			}
		}
		catch(Exception e){
			System.out.println("Usage: java myHTTPServer ip port file_path");
		}
	}
}