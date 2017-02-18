import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameKeeper{
	private HashMap<String,Pair> pairs;
	private final Lock mutex;

	public GameKeeper(){
		pairs = new HashMap<String, Pair>();
		mutex = new ReentrantLock();
	}
	
	public boolean addToPair(String gameId, User player){
		mutex.lock();
		if (pairs.containsKey(gameId)) {
			boolean res = pairs.get(gameId).addSecondPlayer(player);
			mutex.unlock();
			return res;
		}
		else {
			pairs.put(gameId, new Pair(gameId,player));
			mutex.unlock();
			return true;
		}
	}

	public String getOpponentName(String gameId, String userName){
		return pairs.get(gameId).getOpponentName(userName);
	}
	
	public void waitForFullTable(String gameId){
		pairs.get(gameId).waitForFullTable();
	}
	
	public int getNoOfPlayers(String gameId){
			return pairs.get(gameId).getNoOfPlayers();
	}
	
	public void updateHistory(String gameId, String userName, String text){
		Pair pair = pairs.get(gameId);
		mutex.lock();
		pair.updateHistory(userName, text);
		mutex.unlock();
	}
	
	public String getHistory(String gameId, String userName) throws Exception{
		return pairs.get(gameId).getHistory(userName);
	}
	
	public void closeGame(String gameId, String userName){
		Pair pair = pairs.get(gameId);
		mutex.lock();
		boolean deleteTable = pair.closeGame(userName);
		if (deleteTable) pairs.remove(gameId);
		mutex.unlock();
	}
	
	public String doIWin(String gameId, String userName) throws Exception{
		return pairs.get(gameId).doIWin(userName);
	}
	
	public String tellCards(String gameId, String userName){
		return pairs.get(gameId).tellCards(userName);
	}
	
	public String auctionTurn(String gameId, String userName) throws Exception{
		return pairs.get(gameId).auctionTurn(userName);
	}
	
	public String offer(String gameId, String userName, int offerVal) throws Exception{
		return pairs.get(gameId).offer(userName,offerVal);
	}
	
	public String bestOffer(String gameId, String userName) throws Exception{
		return pairs.get(gameId).bestOffer(userName);
	}
	
	public String auctionWon(String gameId, String userName) throws Exception{
		return pairs.get(gameId).auctionWon(userName);		
	}
			
	public String changeHeap(String gameId, String userName, String heapDesc){
			return pairs.get(gameId).changeHeap(userName, heapDesc);
	}
	
	public String heapSelected(String gameId, String  userName) throws Exception{
		return pairs.get(gameId).heapSelected(userName);
	}
	
	public String selectHeap(String gameId, String userName, String heapId){
		return pairs.get(gameId).selectHeap(userName, heapId);		
	}
	
	public String cardsExchanged(String gameId, String userName) throws Exception{
		return pairs.get(gameId).cardsExchanged(userName);
	}

	public String playerMoveChange(String gameId, String userName) throws Exception{
		return pairs.get(gameId).playerMoveChange(userName);
	}
	
	class Pair{
		private String gameId;
		private User[] players;
		private HashMap<String,Boolean> msgModified;
		private HashMap<String,String> withWho;
		private HashMap<String, String> winMsg;
		private String msgHistory = "";
		private int players_added = 0;
		private final Lock lock; 
		private final Condition fullTable, historyChanged, gameWon; 
		private GameState gs = null;

		public Pair(String gameId, User player){
			players = new User[2];
			this.gameId = gameId;
			msgModified = new HashMap<String,Boolean>();
			withWho = new HashMap<String,String>();
			winMsg = new HashMap<String, String>();
			players[0] = player;
			lock = new ReentrantLock(); 
			fullTable = lock.newCondition();
			historyChanged = lock.newCondition();
			gameWon = lock.newCondition();
			players_added++;
		}
		
		public String playerMoveChange(String userName) throws Exception{
			return gs.playerMoveChange(userName);
		}
		
		public String cardsExchanged(String userName) throws Exception{
			return gs.cardsExchanged(userName);
		}
		
		public String changeHeap(String userName, String descOfChange){
			return gs.changeHeap(userName, descOfChange);
		}
		
		public String selectHeap(String userName, String heapId){
			return gs.selectHeap(userName, heapId);
		}
		
		public String heapSelected(String userName) throws Exception{
			return gs.heapSelected(userName);
		}
		
		public String auctionTurn(String userName) throws Exception{
			return gs.auctionTurn(userName);
		}
		
		public String bestOffer(String userName) throws Exception{
			return gs.bestOffer(userName);
		}
		
		public String offer(String userName, int offerVal) throws Exception{
			return gs.offer(userName, offerVal);
		}
		
		public String auctionWon(String userName) throws Exception{
			return gs.auctionWon(userName);
		}
		
		public String doIWin(String userName) throws Exception{
			lock.lock();
			while(winMsg.get(userName).equals("No")) {
				String res = winMsg.get(userName);
				//System.out.println("\n\n" + res + "\n\n");
				gameWon.await();
			}
			String res = winMsg.get(userName);
			lock.unlock();
			return res;
		}
		
		public boolean closeGame(String userName){
			//System.out.println("IN CLOSE GAME\n\n\n");
			lock.lock();
			if (players_added==1) {
				//System.out.println("\n\n\none player\n\n\n");
				gameWon.signalAll();
				lock.unlock();
				return true;
			}
			//System.out.println("\n\n\ntwo players\n\n\n");
			String opponent = withWho.get(userName);
			winMsg.put(opponent,"You won, because " + userName + " has left the table");
			winMsg.put(userName,"You lost, because you have left the table");
			players_added--;
			gameWon.signalAll();
			gs.closeThreads();
			lock.unlock();
			return false;
		}
		
		public void waitForFullTable(){
			lock.lock();
			try{
				while(players_added!=2) fullTable.await();
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				lock.unlock();
			}
		}
		
		public void updateHistory(String userName, String text){
			lock.lock();
			msgHistory += userName + ": " + text.replace("_"," ") + "\n";
			msgModified.put(withWho.get(userName) ,true);
			msgModified.put(userName,true);
			historyChanged.signalAll();
			lock.unlock();
		}
		
		public String getHistory(String userName) throws Exception{
			lock.lock();
			while (!msgModified.get(userName)) historyChanged.await();
			msgModified.put(userName,false);
			lock.unlock();
			return msgHistory;
		}
		
		public boolean addSecondPlayer(User player){
			lock.lock();
			if (players_added == 1){ //if it is not already set to two 
				if(!player.getName().equals(players[0].getName())){//user cannot play with himself
					players[1] = player;
					msgModified.put(players[0].getName(), false);
					msgModified.put(players[1].getName(), false);
					withWho.put(players[0].getName(),players[1].getName());
					withWho.put(players[1].getName(),players[0].getName());
					winMsg.put(players[0].getName(), "No");
					winMsg.put(players[1].getName(), "No");
					players_added++;
					gs = new GameState(players[0].getName(),players[1].getName());
					fullTable.signalAll();
					lock.unlock();
					return true;
				}
			}
			fullTable.signalAll();
			lock.unlock();
			return false;
		}
		
		public String tellCards(String userName){
			return gs.tellCards(userName);
		} 
		
		public String getOpponentName(String name){
			return withWho.get(name);
		}
		
		public int getNoOfPlayers(){
			return players_added;
		}
	}
}