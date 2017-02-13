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

	public int getNoOfPlayers(String gameId){
			return pairs.get(gameId).getNoOfPlayers();
	}
	
	class Pair{
		private String gameId;
		private User[] players;
		private HashMap<String,String> msgs;
		private int players_added = 0;
		
		public Pair(String gameId, User player){
			players = new User[2];
			this.gameId = gameId;
			msgs = new HashMap<String,String>();
			players[0] = player;
			players_added++;
		}
		
		public boolean addSecondPlayer(User player){
			if (players_added == 1){ //if it is not already set to two 
				if(!player.getName().equals(players[0].getName())){//user cannot play with himself
					players[1] = player;
					players_added++;
					return true;
				}
			}
			return false;
		}
		
		public int getNoOfPlayers(){
			return players_added;
		}
	}
}