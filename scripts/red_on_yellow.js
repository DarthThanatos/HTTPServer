function onClick(){
	console.log(this.i);
}

function spawnCards(color, player_cards, width,height){
	var container = document.getElementById("container");
	var amount_of_cards = 10;
	var container_width = $("#container").css("width").replace("px","");
	var gap = (container_width - amount_of_cards * width)/amount_of_cards;
	var vertical_pos = 0;
	if (player_cards == true){
		var container_height = $("#container").css("height").replace("px","");
		vertical_pos = container_height - height;
	}
	console.log(gap);
	for (var i = 0; i < amount_of_cards; i++) {
		var square = document.createElement("div");
		square.style.top = vertical_pos + "px";
		square.style.left = i * width + i*gap + 0.5*gap + "px";
		square.style.position="absolute";
		//square.style.background = color;
		var picture = document.createElement("img");
		picture.src = "../images/card_background.jpg";
		picture.height = height;
		picture.width = width;
		square.appendChild(picture);
		square.i = i;
		square.onclick = onClick;
		container.appendChild(square);
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

function clearContainer(){
	var container =  document.getElementById("container");
	var ids = [];
	for (var i = 0; i < container.children.length; i++){
		ids.push(container.children[i].id);
	}
	for(var i = 0; i < ids.length; i++)
		$("#"+ids[i]).remove();
}

function start(){
	header.innerHTML = "1000";
	spawnCards("green",false,75,150);
	spawnCards("blue",true,75,150);
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
		//console.log(xmlHttp.responseText);
		switch(xmlHttp.responseText){
			case "Waiting for another player":
				clearContainer();
				header.innerHTML = "1000: Waiting for another player";
				xmlHttp = new XMLHttpRequest();
				xmlHttp.onreadystatechange = function() { 
					if(xmlHttp.readyState == 4)
						start();
				}
				xmlHttp.open("CANSTART",request,true);
				xmlHttp.send( null );
				break;
			case "Starting":
				clearContainer();
				start();
				break;
			default:
				info.value = xmlHttp.responseText;
				break;
		}


	}
}

