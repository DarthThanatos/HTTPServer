import java.net.*;

public class Server1000 extends myHTTPServer{

	private UsersDataBase udb = null;
	private GameKeeper gk = null;
	
	public Server1000(Socket client, String fileName, UsersDataBase udb, GameKeeper gk){
		super(client, fileName);
		this.udb = udb;
		this.gk = gk;
	}
	
	public void logging() throws Exception{
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
	
	public void withWho() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		sendResponse(200, gk.getOpponentName(gameId,userName),false);
	}
	
	public void waitForPlayer() throws Exception{
		String gameId = httpQueryString.split("%20")[3];
		gk.waitForFullTable(gameId);
		sendResponse(200,"Starting",false);
	}
	
	public void sendMsg() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		String msg = httpQueryString.split("%20")[4];
		gk.updateHistory(gameId,userName,msg);
		sendResponse(200,"Ok",false);
	}

	public void getMsg() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		sendResponse(200, gk.getHistory(gameId,userName),false);
	}

	public void closeGame() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		gk.closeGame(gameId,userName);
		sendResponse(200,"Ok",false);
	}

	public void doIWin() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		sendResponse(200,gk.doIWin(gameId,userName),false);
	}

	public void getCards() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];
		sendResponse(200,gk.tellCards(gameId,userName),false);
	}

	public void offer() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];		
		int offer = Integer.parseInt(httpQueryString.split("%20")[4]);
		sendResponse(200, gk.offer(gameId, userName, offer), false);
	}

	public void auctionTurn() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];		
		sendResponse(200, gk.auctionTurn(gameId, userName), false);
	}

	public void bestOffer() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];		
		sendResponse(200, gk.bestOffer(gameId,userName), false);
	}
	
	public void auctionWon() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];			
		sendResponse(200, gk.auctionWon(gameId,userName), false);
	}
	
	public void heapSelected() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];			
		sendResponse(200, gk.heapSelected(gameId,userName), false);		
	}
	
	public void selectHeap() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];			
		String heapId = httpQueryString.split("%20")[4];
		sendResponse(200, gk.selectHeap(gameId,userName, heapId), false);		
	}

	public void changeHeap() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];			
		String heapDesc = httpQueryString.split("%20")[4];
		sendResponse(200, gk.changeHeap(gameId,userName, heapDesc), false);		
	}	
	
	public void cardsExchanged() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];	
		sendResponse(200, gk.cardsExchanged(gameId,userName), false);		
	}	

	public void playerMoveChange() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];	
		sendResponse(200, gk.playerMoveChange(gameId,userName), false);		
	}	

	public void changeMove() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];	
		String moveDesc = httpQueryString.split("%20")[4];	
		sendResponse(200, gk.changeMove(gameId,userName,moveDesc), false);		
	}	

	public void endRound() throws Exception{
		String userName = httpQueryString.replaceFirst("/", "").split("%20")[0];
		String gameId = httpQueryString.split("%20")[3];	
		sendResponse(200, gk.endRound(gameId,userName), false);		
	}	
	
	@Override
	public boolean patternMatching(){
		try{
			if(httpMethod.equals("LOG")) {
				logging();
				return true;
			}
			else if(httpMethod.equals("CANSTART")){
				waitForPlayer();
				return true;
			}
			else if(httpMethod.equals("WITHWHO")){
				withWho();
				return true;
			}
			else if(httpMethod.equals("SENDMSG")){
				sendMsg();
				return true;
			}
			else if(httpMethod.equals("GETMSG")){
				getMsg();
				return true;
			}
			else if(httpMethod.equals("CLOSEGAME")){
				closeGame();
				return true;
			}
			else if(httpMethod.equals("DOIWIN")){
				doIWin();
				return true;
			}
			else if(httpMethod.equals("GETCARDS")){
				getCards();
				return true;
			}
			else if(httpMethod.equals("OFFER")){
				offer();
				return true;
			}		
			else if(httpMethod.equals("AUCTIONTURN")){
				auctionTurn();
				return true;
			}				
			else if(httpMethod.equals("BESTOFFER")){
				bestOffer();
				return true;
			}	
			else if(httpMethod.equals("AUCTIONWON")){
				auctionWon();
				return true;
			}
			else if(httpMethod.equals("HEAPSELECTED")){
				heapSelected();
				return true;
			}			
			else if(httpMethod.equals("SELECTHEAP")){
				selectHeap();
				return true;
			}			
			else if(httpMethod.equals("CHANGEHEAP")){
				changeHeap();
				return true;
			}	
			else if(httpMethod.equals("CARDSEXCHANGED")){
				cardsExchanged();
				return true;
			}	
			else if(httpMethod.equals("PLAYERMOVECHANGE")){
				playerMoveChange();
				return true;
			}	
			else if(httpMethod.equals("CHANGEMOVE")){
				changeMove();
				return true;
			}	
			else if(httpMethod.equals("ENDROUND")){
				endRound();
				return true;
			}
			return super.patternMatching();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
}