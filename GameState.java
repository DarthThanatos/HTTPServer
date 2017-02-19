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
	private String whoseAuctionTurn = null;
	private final Condition myAuctionTurn, isAuctionWon;
	private final Condition isBestOfferChanged, isHeapSelected;
	private final Condition arePlayersCardsExchanged;
	private final Condition isplayerMoveChanged;
	private final Lock lock;
	private int bestOfferVal;
	private String auctionWinner;
	private boolean auctionEnded  = false;
	private boolean shouldContinue = true;
	private HashMap<String, Boolean> bestOfferChanged;
	private HashMap<String, Boolean> turnsChanged;
	private HashMap<String, Boolean> heapSelected;
	private HashMap<String, Boolean> playersCardsExchanged;
	private HashMap<String, Boolean> playersMoveChanged;
	private String exchange_info;
	private String selectedHeapId;
	private HashMap<String,String> withWho;
	private String gameTurn;
	private HashMap<String, String> playerMove;
	private String lastInRound;
	
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
		lock = new ReentrantLock();
		playersNames = new String[2];
		playersNames[0] = playerOne;
		playersNames[1] = playerTwo;
		score = new HashMap<String, Integer>();
		
		bestOfferChanged = new HashMap<String,Boolean>();
		bestOfferChanged.put(playerOne, true);
		bestOfferChanged.put(playerTwo, true);
		isBestOfferChanged = lock.newCondition();
		
		turnsChanged = new HashMap<String, Boolean>();
		turnsChanged.put(playerOne,true);
		turnsChanged.put(playerTwo,true);
		myAuctionTurn = lock.newCondition();
		
		heapSelected = new HashMap<String, Boolean>();
		heapSelected.put(playerOne,false);
		heapSelected.put(playerTwo,false);
		isHeapSelected = lock.newCondition();
		
		playersCardsExchanged = new HashMap<String, Boolean>();
		playersCardsExchanged.put(playerOne,false);
		playersCardsExchanged.put(playerTwo,false);
		arePlayersCardsExchanged = lock.newCondition();
		
		playersMoveChanged = new HashMap<String,Boolean>();
		playersMoveChanged.put(playerOne, false);
		playersMoveChanged.put(playerTwo, false);
		playerMove = new HashMap<String,String>();
		isplayerMoveChanged = lock.newCondition();
				
		withWho = new HashMap<String,String>();
		withWho.put(playerOne,playerTwo);
		withWho.put(playerTwo,playerOne);
		
		whoseAuctionTurn = playerOne;
		isAuctionWon = lock.newCondition();
		bestOfferVal = 100;
		auctionWinner = playerOne;
		
		score = new HashMap<String, Integer>();
		score.put(playerOne,0);
		score.put(playerTwo,0);
	}
	
	public String resetAuction(String userName){
		if(userName.equals(auctionWinner)){
			String playerOne = playersNames[0];
			String playerTwo = playersNames[1];
			bestOfferChanged.put(playerOne, true);
			bestOfferChanged.put(playerTwo, true);		
			turnsChanged.put(playerOne,true);
			turnsChanged.put(playerTwo,true);
			bestOfferVal = 100;
			auctionWinner = playerOne;
			whoseAuctionTurn = playerOne;
			auctionEnded = false;
		}
		return "ok";
	}
	
	public String endRound(String userName){
		return bestCardUser + "@" + withWho.get(bestCardUser) + "@" + Integer.toString(score.get(userName)) + "@" + Integer.toString(score.get(withWho.get(userName)));
	}
	
	public String playerMoveChange(String userName) throws Exception{
		lock.lock();
		while(shouldContinue && !playersMoveChanged.get(userName)) isplayerMoveChanged.await();
		playersMoveChanged.put(userName, false);
		lock.unlock();
		return playerMove.get(userName);
	} 
	
	private int movesCounter = 0; //counter to check if both players picked cards to "lewa"
	private String winningColor;
	private int recentScore = 0; //round score, reseted at the start of each round
	private String bestCardUser;
	private String atutColor = "none";
	private String initialCard;
	
	public String changeMove(String userName, String moveDesc){
		lock.lock();
		int opponentCardIndex = playersCards.get(userName)[Integer.parseInt(moveDesc)];
		moveDesc += "@" + allCards[opponentCardIndex];
		playersMoveChanged.put(withWho.get(userName), true); 
		playerMove.put(withWho.get(userName), moveDesc);
		String card = allCards[opponentCardIndex]; 
		if(movesCounter == 0){
			recentScore = cardValues.get(allCards[opponentCardIndex].split("_")[0]);
			initialCard = card;
			movesCounter = 1;
			winningColor = card.split("_")[1];
			bestCardUser = userName;
		}
		else{
			recentScore += cardValues.get(allCards[opponentCardIndex].split("_")[0]);
			movesCounter = 0;
			if(card.split("_")[1].equals(winningColor)){
				if(cardValues.get(card.split("_")[0]) > cardValues.get(initialCard.split("_")[0])){
					bestCardUser = userName;
					System.out.println(card + " is bigger than " + initialCard);
				}
				else System.out.println(initialCard + " is bigger than " + card);
			}
			int bestPlayerScore = score.get(bestCardUser);
			score.put(bestCardUser, recentScore + bestPlayerScore);
		}
		isplayerMoveChanged.signalAll();
		lock.unlock();
		return "ok";
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
		int heapIndex = selectedHeapId.equals("leftHeapPicker") ? 0 : 1;
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
		exchange_info = userName + "@" + heap_card_one + "@"  + heap_card_two;
		arePlayersCardsExchanged.signalAll();
		lock.unlock();
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
				gameTurn = userName;
				lastInRound = withWho.get(userName);
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


	public String shuffleCards(String userName){
		if(userName.equals(playersNames[0])){
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
		return "ok";
	}
}