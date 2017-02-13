import java.util.*;

public class GameKeeper{
	private HashMap<String,Pair> pairs;

	public GameKeeper(){
		pairs = new HashMap<String, Pair>();
	}
	
	public boolean addToPair(String gameId, User player){
		if (pairs.containsKey(gameId)) {
			return pairs.get(gameId).addSecondPlayer(player);
		}
		else {
			pairs.put(gameId, new Pair(gameId,player));
			return true;
		}
	}

	public String getOpponentName(String gameId, String userName){
		return pairs.get(gameId).getOpponentName(userName);
	}
	
	public int getNoOfPlayers(String gameId){
			return pairs.get(gameId).getNoOfPlayers();
	}
	
	public void updateHistory(String gameId, String userName, String text){
		pairs.get(gameId).updateHistory(userName, text);
	}
	
	public String getHistory(String gameId, String userName){
		return pairs.get(gameId).getHistory(userName);
	}
	
	class Pair{
		private String gameId;
		private User[] players;
		private HashMap<String,Boolean> msgModified;
		private HashMap<String,String> withWho;
		private String msgHistory = "";
		private int players_added = 0;
		
		public Pair(String gameId, User player){
			players = new User[2];
			this.gameId = gameId;
			msgModified = new HashMap<String,Boolean>();
			withWho = new HashMap<String,String>();
			players[0] = player;
			players_added++;
		}
		
		public void updateHistory(String userName, String text){
			msgHistory += userName + ": " + text.replace("_"," ") + "\n";
			msgModified.put(withWho.get(userName) ,true);
			msgModified.put(userName,true);
		}
		
		public String getHistory(String userName){
			while (!msgModified.get(userName));
			msgModified.put(userName,false);
			return msgHistory;
		}
		
		public boolean addSecondPlayer(User player){
			if (players_added == 1){ //if it is not already set to two 
				if(!player.getName().equals(players[0].getName())){//user cannot play with himself
					players[1] = player;
					msgModified.put(players[0].getName(), false);
					msgModified.put(players[1].getName(), false);
					withWho.put(players[0].getName(),players[1].getName());
					withWho.put(players[1].getName(),players[0].getName());
					players_added++;
					return true;
				}
			}
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