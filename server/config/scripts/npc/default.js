/* 	
Ralph (Old Guy in Boat Quay) 
Function: Useless.
Cody/AAron
*/


var status = 0;
var job;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendOk("Eu sinto falta dos tempos antigos... Lembra quando ainda estavamos comecando, no ano de 2005?");
			cm.dispose();
			return;
		}
	}
}	