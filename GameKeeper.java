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
		synchronized(pair){
			pair.updateHistory(userName, text);
		}
	}
	
	public String getHistory(String gameId, String userName) throws Exception{
		return pairs.get(gameId).getHistory(userName);
	}
	
	class Pair{
		private String gameId;
		private User[] players;
		private HashMap<String,Boolean> msgModified;
		private HashMap<String,String> withWho;
		private String msgHistory = "";
		private int players_added = 0;
		private final Lock lock; 
		private final Condition fullTable, historyChanged; 

		public Pair(String gameId, User player){
			players = new User[2];
			this.gameId = gameId;
			msgModified = new HashMap<String,Boolean>();
			withWho = new HashMap<String,String>();
			players[0] = player;
			lock = new ReentrantLock(); 
			fullTable = lock.newCondition();
			historyChanged = lock.newCondition();
			
			players_added++;
		}
		
		public void waitForFullTable(){
			lock.lock();
			try{
				while(players_added!=2) fullTable.await();
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				fullTable.signal();
				lock.unlock();
			}
		}
		
		public void updateHistory(String userName, String text){
			lock.lock();
			msgHistory += userName + ": " + text.replace("_"," ") + "\n";
			msgModified.put(withWho.get(userName) ,true);
			msgModified.put(userName,true);
			historyChanged.signal();
			lock.unlock();
		}
		
		public String getHistory(String userName) throws Exception{
			lock.lock();
			while (!msgModified.get(userName)) historyChanged.await();
			msgModified.put(userName,false);
			historyChanged.signal();
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
					players_added++;
					fullTable.signal();
					lock.unlock();
					return true;
				}
			}
			lock.unlock();
			return false;
		}
		
		public String getOpponentName(String name){
			return withWho.get(name);
		}
		
		public int getNoOfPlayers(){
			return players_added;
		}
	}
}