// contract turn your_score opponent_score atut 
// renew
// atut
// contract
// winning condition

function change_chat(event, input){
	input_elem = document.getElementById("chat_input");
	var chatInput = document.getElementById("chat_input").value;
	//var request = localStorage.getItem("request");
	var request = $("#chat_input").data("request");
	console.log(chatInput + " " + String.fromCharCode(event.which));
	if(event.keyCode == 13) {
		console.log("yep, enter");
		if(chatInput.value != "\n"){
			var sendMsg = new XMLHttpRequest();
			sendMsg.open( "SENDMSG", request + " " + chatInput.replace(/ /g,"_"), false ); // false for synchronous request
			sendMsg.send( null );
			input_elem.value = "";
		}
	}
}

function updateChatWindow(request, responseText){
	var chat_window = document.getElementById("chat_window");
	chat_window.value = responseText;
	var incomingMsgs = new XMLHttpRequest();
	incomingMsgs.onreadystatechange = function() { 
		if(incomingMsgs.readyState == 4)
			updateChatWindow(request, incomingMsgs.responseText);
	}
	incomingMsgs.open("GETMSG",request,true);
	incomingMsgs.send( null );
}

function onClick(){
	var idParts = this.id.split("_");
	if(idParts[0] == "my"){
		if($("#container").data("phase") == "exchanging"){
			var picked = $("#container").data("picked_to_exchange");
			if(picked.indexOf(this.id) == -1 ){
				if(picked.length != 2){
					console.log("adding " + this.id);
					picked.push(this.id);
					this.style.top = parseInt(this.style.top.replace("px","")) - 30;
				}
			}
			else{ 
				console.log("deleting " + this.id);
				picked.splice(picked.indexOf(this.id),1);
				this.style.top = parseInt(this.style.top.replace("px","")) + 30;
			}
			//$("#container").data("picked_to_exchange",picked);
			console.log($("#container").data("picked_to_exchange"));
		}
		if($("#container").data("phase") == "play"){
			var playerName = $("#container").data("playerName");
			var gameTurn = $("#container").data("gameTurn");
			console.log(gameTurn);
			if(playerName == gameTurn){
				var i = $("#" + this.id).data("i");
				var request = $("#container").data("request");
				var moveChangeRq = new XMLHttpRequest();
				moveChangeRq.open("CHANGEMOVE", request + " " + i, false);
				moveChangeRq.send(null);
				var container_height = parseInt($("#container").css("height").replace("px",""));
				var card_height = parseInt($(".animate").css("height").replace("px",""));
				this.style.top = container_height - 2*card_height - 20;
				this.style.left = "45%";
				$("#container").data("gameTurn","opponent");
				$("#container").data("my_card", this.id);
				if($("#container").data("lastInRound") == playerName ) {
					setTimeout(endRound, 3000);
					$("#container").data("phase", "endRound");
				}
				document.getElementById("gameTurnInfo").value = "opponent's turn";
			}
		}
	}
}

function spawnChat(request){
	var chat_window = document.createElement("TEXTAREA");
	chat_window.readOnly = true;
	chat_window.id = "chat_window";
	var chat_input = document.createElement("TEXTAREA");
	chat_input.id = "chat_input";
	var chat_room = document.getElementById("chatroom");
	chat_room.appendChild(chat_window);
	chat_room.appendChild(chat_input);
	var incomingMsgs = new XMLHttpRequest();
	incomingMsgs.onreadystatechange = function() { 
		if(incomingMsgs.readyState == 4)
			updateChatWindow(request,incomingMsgs.responseText);
	}
	incomingMsgs.open("GETMSG",request,true);
	incomingMsgs.send( null );
	//localStorage.setItem('request', request);
	$("#chat_input").data("request",request);
	$("#chat_input").keypress(change_chat);
}

function spawnHeap(id){
	var horizontal_pos = 0;
	var gap = 50;
	var width = $(".animate").css("width").replace("px", "");
	var height = parseInt($(".animate").css("height").replace("px", ""));
	if (id == 0) {//left heap
		
	}
	else {
		var container_width = $("#container").css("width").replace("px","");
		horizontal_pos = container_width - width;
	}
	var container_height = parseInt($("#container").css("height").replace("px",""));
	heap_cards_vert_pos = [];
	heap_cards_vert_pos.push(gap + height);
	heap_cards_vert_pos.push(container_height - gap - 2*height);
	for (var i = 0; i < 2; i++){
		var picture = document.createElement("img");
		picture.src = "../images/card_background.jpg";
		picture.width = width;
		picture.height = height;
		picture.id = "heap_card_" + id + "_" + i;
		picture.style.top = heap_cards_vert_pos[i] + "px";
		picture.style.left = horizontal_pos + "px";
		picture.style.position = "absolute";
		var container = document.getElementById("container");
		container.appendChild(picture);
	}
}

function endRound(){
	console.log("endRound");
	$("#" + $("#container").data("my_card")).remove();
	$("#" + $("#container").data("enemy_card")).remove();
	var request = $("#container").data("request");
	var endRoundRq = new XMLHttpRequest();
	endRoundRq.open("ENDROUND", request, false);
	endRoundRq.send(null);
	var msgParts = endRoundRq.responseText.split("@"); //whose_turn@last_In_round@myscore@enemy_score
	$("#container").data("gameTurn", msgParts[0]);
	$("#container").data("lastInRound", msgParts[1]);
	$("#container").data("score", msgParts[2]);
	document.getElementById("gameInfo").value = "Your score: " + msgParts[2];
	document.getElementById("enemyScoreInfo").value = "Enemy score: " + msgParts[3];
	document.getElementById("gameTurnInfo").value = msgParts[0] + "' turn";
	$("#container").data("phase", "play");
	$("#container").data("enemyScore",msgParts[3]);
	$("#container").data("myScore",msgParts[2]);
	var rounds = parseInt($("#container").data("rounds"));
	rounds++;
	if(rounds == 10){
		$("#container").data("rounds", 0);
		clearContainer(request);
		start($("#container").data("startMsg"),request);
	}
	else $("#container").data("rounds", rounds);
}

function enemyMove(request, responseText){
	var msgParts = responseText.split("@");
	var playerName = $("#container").data("playerName");
	$("#container").data("gameTurn", playerName);
	document.getElementById("gameTurnInfo").value = "Your move";
	var enemyCard = document.getElementById("enemy_" + msgParts[0]);
	console.log("enemy_" + msgParts[0] + msgParts);
	var card_height = parseInt($(".animate").css("height").replace("px", ""));
	enemyCard.style.left = "45%";
	enemyCard.style.top = card_height + 20;
	enemyCard.src = "../images/" + msgParts[1] + ".png";
	$("#container").data("enemy_card", enemyCard.id);
	if(parseInt($("#container").data("rounds")) != 10)
		sendAsynchHTTPQuery(request, enemyMove, "PLAYERMOVECHANGE");
	if($("#container").data("lastInRound") != playerName) {
		setTimeout(endRound, 3000);
		$("#container").data("phase", "endRound");
	}
}

function cards_exchanged(request, responseText){
	console.log("cards_exchanged " + responseText);
	var picked = $("#container").data("picked_to_exchange");
	var new_values = responseText.split("@"); //userName@card1@card2
	var playerName = request.replace("/","").replace("..","").split(" ")[0];
	var container_height = $("#container").css("height").replace("px","");
	var card_height = $(".animate").css("height").replace("px", "");
	if(new_values[0] == playerName){
		for(var i = 0; i < 2; i++){
			var exchange_card = document.getElementById(picked[i]);
			exchange_card.src = "../images/" + new_values[i+1] + ".png";
			exchange_card.style.top = container_height - card_height;
		}
		for (var i = 0; i<2; i++)
			picked.splice(picked.indexOf(exchange_card.id),1); //remove name of card from picked
		$("#exchange_cards_button").remove();
	}
	var id = $("#container").data("selectedHeapId") == "leftHeapPicker" ? 0 : 1;
	for (var i = 0; i < 2; i++){ //cover heap cards
		document.getElementById("heap_card_" + id + "_" + i).src = "../images/card_background.jpg";
	}
	$("#auctionInfo").remove();
	
	var gameInfo = document.createElement("INPUT");
	gameInfo.id = "gameInfo";
	gameInfo.readOnly = true;
	gameInfo.style.left = "15%";
	gameInfo.style.position = "absolute";
	gameInfo.style.top = "45%";	
	gameInfo.value = "Your score: " + $("#container").data("myScore");
	
	var gameTurnInfo = document.createElement("INPUT");
	gameTurnInfo.id = "gameTurnInfo";
	gameTurnInfo.readOnly = true;
	gameTurnInfo.style.left = "25%";
	gameTurnInfo.style.position = "absolute";
	gameTurnInfo.style.top = "45%";	
	gameTurnInfo.value = $("#container").data("gameTurn") + "'s turn";

	var enemyScoreInfo = document.createElement("INPUT");
	enemyScoreInfo.id = "enemyScoreInfo";
	enemyScoreInfo.readOnly = true;
	enemyScoreInfo.style.left = "35%";
	enemyScoreInfo.style.position = "absolute";
	enemyScoreInfo.style.top = "45%";
	enemyScoreInfo.value = "Enemy score: " + $("#container").data("enemyScore");
	
	var contractInfo = document.createElement("INPUT");
	contractInfo.id = "contractInfo";
	contractInfo.readOnly = true;
	contractInfo.style.left = "45%";
	contractInfo.style.position = "absolute";
	contractInfo.style.top = "45%";
	contractInfo.value = "contract: " + $("#container").data("contract");
	
	var atutInfo = document.createElement("INPUT");
	atutInfo.id = "atutInfo";
	atutInfo.readOnly = true;
	atutInfo.style.left = "55%";
	atutInfo.style.position = "absolute";
	atutInfo.style.top = "45%";
	atutInfo.value = "atut color: none";
	
	document.getElementById("container").appendChild(gameInfo);
	document.getElementById("container").appendChild(gameTurnInfo);
	document.getElementById("container").appendChild(enemyScoreInfo);
	document.getElementById("container").appendChild(contractInfo);
	document.getElementById("container").appendChild(atutInfo);
	sendAsynchHTTPQuery(request, enemyMove, "PLAYERMOVECHANGE");
	$("#container").data("phase","play");
	$("#container").data("rounds",0);
}

function exchange_cards(){
	console.log("exchange button clicked");
	var request = $("#container").data("request");
	var picked = $("#container").data("picked_to_exchange");
	if (picked.length == 2){
		var descOfChange = "";
		for (var i = 0; i < 2; i++){
			console.log(picked[i]);
			descOfChange += picked[i].split("_")[1] + "@"; //my_i
			console.log(descOfChange);
		}
		var exch_req = new XMLHttpRequest();
		exch_req.open( "CHANGEHEAP", request + " " + descOfChange, false ); // false for synchronous request
		exch_req.send( null );		
	}
}

function showHeap(request, responseText){
	var card_background = responseText.split("@");
	var container = document.getElementById("container");
	var id = $("#container").data("selectedHeapId") == "leftHeapPicker" ? 0 : 1;
	for (var i = 0; i < 2; i++){
		document.getElementById("heap_card_" + id + "_" + i).src = "../images/" + card_background[i] + ".png";
	}
	var playerName = request.replace("/","").replace("..","").split(" ")[0];
	var auctionWinner = $("#container").data("auctionWinner");
	var auctionInfo = document.getElementById("auctionInfo");
	if(playerName == auctionWinner){
		auctionInfo.value = "Pick cards to exchange";
		var exchange_cards_button = document.createElement("BUTTON");
		exchange_cards_button.id = "exchange_cards_button";
		exchange_cards_button.onclick = exchange_cards;
		exchange_cards_button.style.position = "absolute";
		exchange_cards_button.style.left = "45%";
		exchange_cards_button.style.top = "45%";
		exchange_cards_button.innerHTML = "exchange cards";
		container.appendChild(exchange_cards_button)
		$("#container").data("phase","exchanging");
		$("#container").data("picked_to_exchange",[]);
	}
	else{
		auctionInfo.value = auctionInfo.value + "; " + auctionWinner + " is picking cards to exchange";
	}
	sendAsynchHTTPQuery(request,cards_exchanged,"CARDSEXCHANGED");
	auctionInfo.setAttribute("size", auctionInfo.value.length);
}

function heapPickerClicked(){
	var request = $("#container").data("request") + " " + this.id; 	
	$("#container").data("selectedHeapId", this.id);
	sendAsynchHTTPQuery(request, function(_req,_response){}, "SELECTHEAP");
	document.getElementById("leftHeapPicker").disabled = true;
	document.getElementById("rightHeapPicker").disabled = true;
}

function spawnHeapButtons(){	
	var container = document.getElementById("container");	
	var leftHeapPicker = document.createElement("BUTTON");
	var rightHeapPicker = document.createElement("BUTTON");
	leftHeapPicker.disabled = true;
	leftHeapPicker.id = "leftHeapPicker";
	leftHeapPicker.onclick = heapPickerClicked;
	leftHeapPicker.innerHTML = "left heap";
	rightHeapPicker.innerHTML = "right heap";
	leftHeapPicker.style.position = "absolute";
	rightHeapPicker.style.position = "absolute";	
	leftHeapPicker.style.top = "49%";
	rightHeapPicker.style.top = "48%";
	rightHeapPicker.style.right = 0;
	leftHeapPicker.style.left = 0;
	rightHeapPicker.disabled = true;
	rightHeapPicker.id = "rightHeapPicker";
	rightHeapPicker.onclick = heapPickerClicked;
	container.appendChild(leftHeapPicker);
	container.appendChild(rightHeapPicker);
}

function offer(){
	var request = $("#container").data("request");		
	offerQuestion = new XMLHttpRequest();
	offerQuestion.open("OFFER",request + " " + this.value,false);
	offerQuestion.send( null );	
}

function bestOfferFun(request, responseText){
	var auctionEnded = $("#container").data("auctionEnded");
	if(auctionEnded=="false") {
		//$("#bestOffer").attr("value",responseText);
		console.log("best offer");
		var msgParts = responseText.split(" ");
		var bestOfferIn = document.getElementById("bestOffer");
		bestOfferIn.value = "Best offer: " + msgParts[1] + " coming from " + msgParts[0] ;
		bestOfferIn.setAttribute("size", bestOfferIn.value.length);
		console.log("bestOffer flag: " + auctionEnded);
		sendAsynchHTTPQuery(request,bestOfferFun,"BESTOFFER");
	}
}

function auctionWon(request, responseText){
	$("#container").data("auctionEnded","true");
	$("#pass").remove();
	$("#plusTen").remove();
	$("#bestOffer").remove();
	var msgParts = responseText.split(" ");
	var playerName = request.replace("/","").replace("..","").split(" ")[0];
	$("#container").data("gameTurn", msgParts[0]);
	$("#container").data("playerName",playerName);
	var auctionInfo = document.getElementById("auctionInfo");
	console.log("won" + playerName);
	$("#container").data("auctionWinner",msgParts[0]);
	if(msgParts[0] == playerName){
		$("#container").data("lastInRound","opponent");
		auctionInfo.value = "Select heap";
		document.getElementById("leftHeapPicker").disabled = false;
		document.getElementById("rightHeapPicker").disabled = false;
	}
	else{
		$("#container").data("lastInRound",playerName);
		auctionInfo.value = msgParts[0] + " has won the auction with score " + msgParts[1];
	}
	$("#container").data("contract",msgParts[1]);
	sendAsynchHTTPQuery(request,showHeap,"HEAPSELECTED");
	auctionInfo.setAttribute("size", auctionInfo.value.length);
}

function auctionTurn(request, responseText){
	var auctionEnded = $("#container").data("auctionEnded");	
	if(auctionEnded=="false"){
		//$("#auctionInfo").attr("value",responseText + " 's turn");
		document.getElementById("auctionInfo").value = responseText + "'s turn";
		console.log("auctionturn flag : " + auctionEnded);
		sendAsynchHTTPQuery(request,auctionTurn,"AUCTIONTURN");	
	}
}

function sendAsynchHTTPQuery(request, f, type){	
	var question = new XMLHttpRequest();
	question.onreadystatechange = function() { 
		if(question.readyState == 4)
			f(request,question.responseText);
	}
	question.open(type,request,true);
	question.send( null );
}

function spawnAuction(request){
	var container = document.getElementById("container");
	$("#container").data("auctionEnded","false");
	
	var bestOffer = document.createElement("INPUT");
	var plus10Button = document.createElement("BUTTON");
	var passButton = document.createElement("BUTTON");
	var auctionInfo = document.createElement("INPUT");
	
	auctionInfo.readOnly = true;
	auctionInfo.style.left = "25%";
	auctionInfo.style.position = "absolute";
	auctionInfo.style.top = "45%";
	
	bestOffer.readOnly = true;
	plus10Button.innerHTML = "+ 10";
	passButton.innerHTML = "pass";
	bestOffer.value = "Best offer: 100";
	auctionInfo.value = "Your turn";
	auctionInfo.id = "auctionInfo";
	bestOffer.id = "bestOffer";
	plus10Button.id = "plusTen";
	passButton.id = "pass";
	
	bestOffer.style.position = "absolute";
	plus10Button.style.position = "absolute";
	passButton.style.position = "absolute";
	
	bestOffer.style.left = "45%";
	plus10Button.style.left = "45%";
	passButton.style.left = "50%";
	
	bestOffer.style.top = "45%";
	plus10Button.style.top = "50%";
	passButton.style.top = "50%";
	
	container.appendChild(bestOffer);
	container.appendChild(plus10Button);
	container.appendChild(passButton);
	container.appendChild(auctionInfo);
	
	var resetRq = new XMLHttpRequest();
	resetRq.open( "RESETAUCTION", request, false ); // false for synchronous request
	resetRq.send( null );
	
	sendAsynchHTTPQuery(request, bestOfferFun, "BESTOFFER");
	sendAsynchHTTPQuery(request, auctionWon, "AUCTIONWON");
	sendAsynchHTTPQuery(request, auctionTurn, "AUCTIONTURN");
	
	$("#container").data("request", request);
	passButton.value = -1;
	plus10Button.value = 10;
	
	plus10Button.onclick = offer;
	passButton.onclick = offer;
}


function spawnCards(player_cards,request){
	var container = document.getElementById("container");
	var amount_of_cards = 10;
	var container_width = $("#container").css("width").replace("px","");
	var width = $(".animate").css("width").replace("px", "");
	var height = $(".animate").css("height").replace("px", "");
	var gap = (container_width - amount_of_cards * width)/amount_of_cards;
	var vertical_pos = 0;
	var cards = [];
	if (player_cards == true){
		var container_height = $("#container").css("height").replace("px","");
		var cardsRequest = new XMLHttpRequest();
		cardsRequest.open( "GETCARDS", request, false ); // false for synchronous request
		cardsRequest.send( null );
		cards = cardsRequest.responseText.split("@");
		for (var i = 0; i<amount_of_cards; i++) cards[i] = "../images/" + cards[i] + ".png";
		vertical_pos = container_height - height;
	}
	else{
		for (var i = 0; i < amount_of_cards; i ++) cards.push( "../images/card_background.jpg");
	}
	console.log(gap);
	for (var i = 0; i < amount_of_cards; i++) {
		var picture = document.createElement("img");
		picture.src = cards[i];
		picture.height = height;
		picture.width = width;
		picture.id = player_cards ? "my_" + i : "enemy_" + i;
		picture.onclick = onClick;
		picture.style.top = vertical_pos + "px";
		picture.style.left = i * width + i*gap + 0.5*gap + "px";
		picture.style.position="absolute";
		container.appendChild(picture);
		$("#my_" + i).data("card",cards[i].match(/[/]images[/](\w+_\w+)[\.]/)[1]);
		$("#my_" + i).data("i",i);
	}
}

function adjust(){
	var selector = document.getElementById("type of game");
	var gameId = document.getElementById("game_id");
	var selectedItem = selector.options[selector.selectedIndex].text;
	if(selectedItem == "NewGame"){
		gameId.placeholder = "New Game Identifier";
	}else{ //existing game selected
		gameId.placeholder = "Existing Game Identifier";
	}
}

function clearContainer(request){
	var container =  document.getElementById("container");
	var ids = [];
	for (var i = 0; i < container.children.length; i++){
		ids.push(container.children[i].id);
	}
	for(var i = 0; i < ids.length; i++)
		$("#"+ids[i]).remove();
}

function informAboutWinning(request, msg){
	console.log(msg);
	$("#header").html(msg + ". To start another game, reload the page.");
}

function start(msg,request){
	xmlHttp = new XMLHttpRequest();
	xmlHttp.open( "WITHWHO", request, false ); // false for synchronous request
	xmlHttp.send( null );
	header.innerHTML = msg + "; playing with " + xmlHttp.responseText;	
	spawnCards(false,request);
	spawnCards(true,request);
	spawnHeap(0);
	spawnHeap(1);
	spawnAuction(request);
	spawnHeapButtons();
}

function login(){
	var container =  document.getElementById("container");
	console.log(container);
	var username = document.getElementById("name").value;
	var password = document.getElementById("password").value;
	var selector = document.getElementById("type of game");
	var gameMode = selector.options[selector.selectedIndex].text;
	var gameId = document.getElementById("game_id").value;
	var info = document.getElementById("info");
	var header = document.getElementById("header");
	if (username=="") info.value = "Type your Username!";
	else if (password=="") info.value = "Type your password!";
	else if(gameId == "") info.value = "Enter a game identifier!";
	else{
		var request = "../" + username + " " + password + " " + gameMode + " " + gameId;
		var xmlHttp = new XMLHttpRequest();
		console.log("texts: "  +  request);
		xmlHttp.open( "LOG", request, false ); // false for synchronous request
		xmlHttp.send( null );
		var startMsg = "1000: Player: " + username + " at table: " +   gameId + "; game mode: " + gameMode;
		$("#container").data("startMsg", startMsg);
		//console.log(xmlHttp.responseText);
		switch(xmlHttp.responseText){
			case "Waiting for another player":
				clearContainer(request);
				header.innerHTML = "1000: " + username + " is waiting for another player at table: " + gameId + "; game mode: " + gameMode;
				xmlHttp = new XMLHttpRequest();
				xmlHttp.onreadystatechange = function() { 
					if(xmlHttp.readyState == 4){
						start(startMsg,request);
						spawnChat(request);
						sendAsynchHTTPQuery(request, informAboutWinning,"DOIWIN");
					}
				}
				xmlHttp.open("CANSTART",request,true);
				xmlHttp.send( null );
				break;
			case "Starting":
				clearContainer(request);
				start(startMsg,request);
				spawnChat(request);
				sendAsynchHTTPQuery(request, informAboutWinning,"DOIWIN");
				break;
			default:
				info.value = xmlHttp.responseText;
				break;
		}
	}
	info.setAttribute("size", info.value.length);
	$("#container").data("enemyScore",0);
	$("#container").data("myScore",0);	
	$(window).on("beforeunload", function() {
		var closeRequest = new XMLHttpRequest();
		closeRequest.open( "CLOSEGAME", request, false ); // false for synchronous request
		closeRequest.send( null );
	});
}

