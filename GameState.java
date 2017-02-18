import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameState{
	private final Integer[] values = {0,2,3,4,10,11};
	private final String[] colors = {"pik", "trefl", "karo", "kier"};
	private final String[] figures = {"9","walet","dama","king","10","as"};
	private String[] allCards = null;
	private HashMap<String,Integer> cardValues = null;
	private HashMap<String, Integer[]> playersCards = null;
	private Integer[][] heaps = null;
	private String[] playersNames = null;
	private int highestOffer = 100;
	private HashMap<String, Integer> score = null;
	private String whoseGameTurn = null;
	private String whoseAuctionTurn = null;
	private final Condition myAuctionTurn, isAuctionWon, isBestOfferChanged, isHeapSelected, arePlayersCardsExchanged;
	private final Lock lock;
	private int bestOfferVal;
	private String auctionWinner;
	private boolean auctionEnded  = false;
	private boolean shouldContinue = true;
	private HashMap<String, Boolean> bestOfferChanged;
	private HashMap<String, Boolean> turnsChanged;
	private HashMap<String, Boolean> heapSelected;
	private HashMap<String, Boolean> playersCardsExchanged;
	private String exchange_info;
	private String selectedHeapId;
	private HashMap<String,String> withWho;
	
	private void createPack(){
		allCards = new String[24];
		int i = 0;
		for (String figure : figures){
			for (String color : colors){
				allCards[i] = figure + "_" + color;
				i++;
			}
		}
	}


	private void mapFiguresToValues(){
		cardValues = new HashMap<String, Integer>();
		for(int i = 0; i < figures.length; i++){
			cardValues.put(figures[i],values[i]);
		}
	}
	
	public void closeThreads(){
		lock.lock();
		shouldContinue = false;
		isBestOfferChanged.signalAll();
		isAuctionWon.signalAll();
		myAuctionTurn.signalAll();
		lock.unlock();
		
	}
	
	public String heapSelected(String userName) throws Exception{
		lock.lock();
		while(shouldContinue && !heapSelected.get(userName)) {
			isHeapSelected.await();
		}
		heapSelected.put(userName,false);
		lock.unlock();
		String res = "";
		for (int i : heaps[selectedHeapId.equals("leftHeapPicker") ? 0 : 1]){
			res += allCards[i] + "@";
		}
		System.out.println("heap cards: " + res);
		return res;
	}

	public String selectHeap(String userName, String heapId){
		if(userName.equals(auctionWinner)){
			lock.lock();
			for(String key: heapSelected.keySet()) heapSelected.put(key,true);			
			selectedHeapId = heapId;
			isHeapSelected.signalAll();
			lock.unlock();
			return "ok";
		}
		return "No way";
	}
	
	private void distributeCards(String playerOne, String playerTwo){
		ArrayList<Integer> cardIndecies = new ArrayList<Integer>(24);
		for (int i = 0; i < 24; i++) cardIndecies.add(i,i);
		Collections.shuffle(cardIndecies);
		int base = 20; // index from which cards for heaps start
		heaps = new Integer[2][];
		for (int i = 0; i< 2; i++){
			heaps[i] = new Integer[2];
			for (int j = 0; j<2; j++){
				heaps[i][j] =  cardIndecies.get(base + j);
			}
			base += 2;
		}
		Integer[] playerOneCards = new Integer[10];
		Integer[] playerTwoCards = new Integer[10];
		for(int i = 0; i<10; i++) {
			playerOneCards[i] = cardIndecies.get(i);
			playerTwoCards[i] = cardIndecies.get(i + 10);
		}
		playersCards = new HashMap<String, Integer[]>();
		playersCards.put(playerOne,playerOneCards);
		playersCards.put(playerTwo, playerTwoCards);
	}

	public GameState(String playerOne, String playerTwo){
		createPack();
		mapFiguresToValues();
		distributeCards(playerOne,playerTwo);
		playersNames = new String[2];
		playersNames[0] = playerOne;
		playersNames[1] = playerTwo;
		score = new HashMap<String, Integer>();
		bestOfferChanged = new HashMap<String,Boolean>();
		bestOfferChanged.put(playerOne, true);
		bestOfferChanged.put(playerTwo, true);
		turnsChanged = new HashMap<String, Boolean>();
		turnsChanged.put(playerOne,true);
		turnsChanged.put(playerTwo,true);
		heapSelected = new HashMap<String, Boolean>();
		heapSelected.put(playerOne,false);
		heapSelected.put(playerTwo,false);
		playersCardsExchanged = new HashMap<String, Boolean>();
		playersCardsExchanged.put(playerOne,false);
		playersCardsExchanged.put(playerTwo,false);
		withWho = new HashMap<String,String>();
		withWho.put(playerOne,playerTwo);
		withWho.put(playerTwo,playerOne);
		whoseGameTurn = playerOne;
		whoseAuctionTurn = playerOne;
		lock = new ReentrantLock();
		myAuctionTurn = lock.newCondition();
		isBestOfferChanged = lock.newCondition();
		isAuctionWon = lock.newCondition();
		isHeapSelected = lock.newCondition();
		arePlayersCardsExchanged = lock.newCondition();
		bestOfferVal = 100;
		auctionWinner = playerOne;
	}
	
	public String tellCards(String userName){
		String res = "";
		for (int i : playersCards.get(userName)){
			res += allCards[i] + "@";
		}
		System.out.println("cards: " + res);
		return res;
	}
	
	public String changeHeap(String userName, String descOfChange){
		String[] descParts = descOfChange.split("@");
		int player_hand_index_one = Integer.parseInt(descParts[0]); 
		int player_hand_index_two = Integer.parseInt(descParts[1]);
		int heapIndex = descParts[2].equals("leftHeapPicker") ? 0 : 1;
		int tmp = playersCards.get(userName)[player_hand_index_one];
		playersCards.get(userName)[player_hand_index_one] = heaps[heapIndex][0];
		String heap_card_one = allCards[heaps[heapIndex][0]];
		heaps[heapIndex][0] = tmp;
		tmp = playersCards.get(userName)[player_hand_index_two];
		playersCards.get(userName)[player_hand_index_two] = heaps[heapIndex][1];
		String heap_card_two = allCards[heaps[heapIndex][1]];
		heaps[heapIndex][1] = tmp;
		
		lock.lock();
		for(String key : playersCardsExchanged.keySet()) playersCardsExchanged.put(key,true);
		arePlayersCardsExchanged.signalAll();
		lock.unlock();
		exchange_info = userName + "@" + heap_card_one + "@"  + heap_card_two;
		return "ok";
	}
	
	public String cardsExchanged(String userName) throws Exception{
		lock.lock();
		while(shouldContinue && !playersCardsExchanged.get(userName)){
			arePlayersCardsExchanged.await();
		}
		playersCardsExchanged.put(userName,false);
		lock.unlock();
		return exchange_info;
	}
	
	public String auctionTurn(String userName) throws Exception{
		if(!auctionEnded){
			lock.lock();
			while(shouldContinue && !turnsChanged.get(userName) && !auctionEnded) {
				myAuctionTurn.await();
				System.out.println("awaiting auction turn: " + turnsChanged.get(userName));
			}
			turnsChanged.put(userName,false);
			lock.unlock();
			System.out.println("turn of " + whoseAuctionTurn);
			return whoseAuctionTurn;
		} else return "auctionEnded";
	}

	public String bestOffer(String userName) throws Exception{
		if(!auctionEnded){
			lock.lock();
			while(shouldContinue && !bestOfferChanged.get(userName) && !auctionEnded) {
				isBestOfferChanged.await();
				System.out.println("awaiting best offer: " + bestOfferChanged.get(userName));
			}
			bestOfferChanged.put(userName, false);
			lock.unlock();
			System.out.println("bestOffer " + Integer.toString(bestOfferVal));
			return auctionWinner + " " + Integer.toString(bestOfferVal);
		}
		else return "auctionEnded";
	}
	
	public String offer(String userName, int offerVal) throws Exception{
		if(whoseAuctionTurn.equals(userName)){
			lock.lock();
			if(offerVal == -1){
				auctionEnded = true;
				auctionWinner = withWho.get(userName);
				isBestOfferChanged.signalAll();
				myAuctionTurn.signalAll();
				isAuctionWon.signalAll();
				lock.unlock();
				return "ok";
			}
			else{
				bestOfferVal += offerVal;
				auctionWinner = userName;
				whoseAuctionTurn = withWho.get(userName);
				for(String key: bestOfferChanged.keySet()) bestOfferChanged.put(key,true);
				for(String key: turnsChanged.keySet()) turnsChanged.put(key,true);
				System.out.println("offer from " + auctionWinner + " " + Integer.toString(bestOfferVal));
				isBestOfferChanged.signalAll();
				myAuctionTurn.signalAll();
				lock.unlock();
				return "ok";
			}
		}
		else return "Not your turn";
	}

	public String auctionWon(String userName) throws Exception{
		lock.lock();
		while (shouldContinue && !auctionEnded) {
			isAuctionWon.await();
			System.out.println("Awaiting auction won");
		}
		lock.unlock();
		System.out.println("auctionWon " + auctionWinner + " " + Integer.toString(bestOfferVal));		
		return auctionWinner + " " + Integer.toString(bestOfferVal);
	}


	public void shufflePlayersCards(){
		ArrayList<Integer> cards = new ArrayList<Integer>();
		int j = 0;
		for(Integer [] player_cards : playersCards.values()){
			for (int i = 0; i < player_cards.length; i++)
				cards.add(j+i, player_cards[i]);
			j+=10;
		}
		Collections.shuffle(cards);
		Integer[] playerOneCards = new Integer[10];
		Integer[] playerTwoCards = new Integer[10];
		for(int i = 0; i<10; i++) {
			playerOneCards[i] = cards.get(i);
			playerTwoCards[i] = cards.get(i + 10);
		}
		playersCards.put(playersNames[0], playerOneCards);
		playersCards.put(playersNames[1], playerTwoCards);
	}
}