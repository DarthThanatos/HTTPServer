function everythingChanged(button){
	console.log(button.value);
	if (button.value=="virgin"){
		document.getElementById("h1").innerHTML = "Death and destruction";
		document.getElementById("im").src = "../images/Mr Anderson.png";
		document.getElementById("im").alt = "Krasu z Mr Andersonem";
		button.value = "second-base";
		document.querySelector("p").textContent = "Yo";
	}else{
		document.getElementById("h1").innerHTML = "Peace and love";
		document.getElementById("im").src = "../images/murphy.png";
		document.getElementById("im").alt = "Murphy";
		button.value = "virgin";
	}
}