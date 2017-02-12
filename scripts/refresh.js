/*var time = new Date().getTime();
$(document.body).bind("mousemove keypress", function(e) {
 time = new Date().getTime();
});

function refresh() {
	if(new Date().getTime() - time >= 5000) {
		window.location.reload(true);
		time = new Date().getTime();
	}
	else 
		setTimeout(refresh, 5000);
}
*/
var time = 50000;
function refresh(){
	window.location.reload(true);
	setTimeout(refresh, time);
}
setTimeout(refresh, time);