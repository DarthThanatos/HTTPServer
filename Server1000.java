import java.net.*;

public class Server1000 extends myHTTPServer{

	private UsersDataBase udb = null;
	private GameKeeper gk = null;
	
	public Server1000(Socket client, String fileName, UsersDataBase udb, GameKeeper gk){
		super(client, fileName);
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
				return;
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
	
	public void sendMsg(String httpQueryString) throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		String msg = httpQueryString.split("%20")[4];
	}
	
	public void waitForChat(String httpQueryString) throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		
	}

	@Override
	public boolean patternMatching(){
		try{
			if(httpMethod.equals("LOG")) {
				logging(httpQueryString);
				return true;
			}
			else if(httpMethod.equals("CANSTART")){
				waitForPlayer(httpQueryString);
				return true;
			}
			return super.patternMatching();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
}